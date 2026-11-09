package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.BadGatewayException;
import com.example.SudokuSolver.config.ExceptionHandling.BadRequestException;
import com.example.SudokuSolver.config.ExceptionHandling.ConflictException;
import com.example.SudokuSolver.config.ExceptionHandling.UnauthorizedException;
import com.example.SudokuSolver.config.jwt.JwtTokenService;
import com.example.SudokuSolver.dto.*;
import com.example.SudokuSolver.mapper.UserMapper;
import com.example.SudokuSolver.model.TokenType;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.model.VerificationToken;
import com.example.SudokuSolver.repository.UserRepository;
import com.example.SudokuSolver.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    private static final long TOKEN_TTL_MINUTES = 10;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;
    private final SecureRandom secureRandom;
    private final JwtTokenService jwtTokenService;

    @Override
    public TokenResponseDto login(LogInRequestDto request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new UnauthorizedException("Invalid email or password.")
        );
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        return new TokenResponseDto(jwtTokenService.generateAccessToken(user.getId()));
    }

    @Override
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email is already registered: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);
        String token = createToken(savedUser, TokenType.ACCOUNT_VERIFICATION);
        sendToken(savedUser, token, "Verify your account");
        return userMapper.toSignUpResponseDto(savedUser);
    }


    @Override
    @Transactional
    public void verifyAccount(String email, String code) {
        User currUser = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException("Email not Found")
        );
        VerificationToken verificationToken = findValidAccountVerificationToken(currUser.getId(), code);
        verificationToken.getUser().setAccountVerified(true);
        verificationTokenRepository.deleteByUserAndType(verificationToken.getUser(), TokenType.ACCOUNT_VERIFICATION);
    }

    @Override
    @Transactional
    public void sendVerificationToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found."));
        if (user.isAccountVerified()) {
            throw new BadRequestException("The account is already verified.");
        }

        String code = createToken(user, TokenType.ACCOUNT_VERIFICATION);
        sendToken(user, code, "Verify your account");
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        sendPasswordChangeToken(email);
    }

    @Override
    @Transactional
    public void resendPasswordChange(String email) {
        sendPasswordChangeToken(email);
    }

    private void sendPasswordChangeToken(String email) {
        User matchingUser = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException("Email not Found")
        );
        String token = createToken(matchingUser, TokenType.PASSWORD_RESET);
        try {
            sendToken(matchingUser, token, "Reset your password");
        } catch (BadGatewayException exception) {
            verificationTokenRepository.deleteByUserAndType(matchingUser, TokenType.PASSWORD_RESET);
            LOGGER.log(Level.WARNING, "Unable to deliver password reset email.", exception);
        }
    }

    @Override
    @Transactional
    public void validatePasswordResetCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Token is invalid or has expired."));
        findValidPasswordResetToken(user.getId(), code);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Token is invalid or has expired."));
        user.setPassword(passwordEncoder.encode(newPassword));
        verificationTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);
    }

    private String createToken(User user, TokenType type) {
        String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
        verificationTokenRepository.deleteByUserAndType(user, type);
        verificationTokenRepository.save(new VerificationToken(
                user,
                code,
                type,
                Instant.now().plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES)
        ));
        return code;
    }

    private void sendToken(User user, String token, String subject) {
        SimpleMailMessage message = new SimpleMailMessage(templateMessage);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText("Hello " + user.getUserName() + ",\n\nYour OTP is: " + token + ",\n\nIt will be valid for " + TOKEN_TTL_MINUTES + " minutes");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new BadGatewayException("Unable to send the verification email.", exception);
        }
    }

    private VerificationToken findValidAccountVerificationToken(Long userId, String code) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByUser_IdAndCodeAndType(userId, code, TokenType.ACCOUNT_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("The token is invalid or has expired."));
        return validateExpiration(verificationToken);
    }

    private void findValidPasswordResetToken(Long userId, String code) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByUser_IdAndCodeAndType(userId, code, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("Token is invalid or has expired."));
        validateExpiration(verificationToken);
    }

    private VerificationToken validateExpiration(VerificationToken verificationToken) {
        if (!verificationToken.getExpiresAt().isAfter(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException("Token is invalid or has expired.");
        }
        return verificationToken;
    }
}
