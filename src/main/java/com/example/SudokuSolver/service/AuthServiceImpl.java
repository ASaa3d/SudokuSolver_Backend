package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.BadGatewayException;
import com.example.SudokuSolver.config.ExceptionHandling.BadRequestException;
import com.example.SudokuSolver.config.ExceptionHandling.ConflictException;
import com.example.SudokuSolver.config.ExceptionHandling.UnauthorizedException;
import com.example.SudokuSolver.config.jwt.JwtTokenService;
import com.example.SudokuSolver.dto.LogInRequestDto;
import com.example.SudokuSolver.dto.SignUpRequestDto;
import com.example.SudokuSolver.dto.TokenResponseDto;
import com.example.SudokuSolver.dto.UserResponseDto;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    private static final long TOKEN_TTL_MINUTES = 15;

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
        User user = userRepository.findByEmailOrPhone(request.emailOrPhone()).orElseThrow(
                () -> new UnauthorizedException("Invalid email, phone, or password.")
        );
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email, phone, or password.");
        }
        return new TokenResponseDto(jwtTokenService.generateAccessToken(user.getId()));
    }

    @Override
    @Transactional
    public UserResponseDto signUp(SignUpRequestDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email is already registered: " + request.email());
        }
        Optional<String> requestedPhone = Optional.ofNullable(request.phone()).orElseGet(Optional::empty);
        requestedPhone.ifPresent(phone -> {
            if (userRepository.findByPhone(phone).isPresent()) {
                throw new ConflictException("Phone number is already registered: " + phone);
            }
        });

        User user = userMapper.toEntity(request);
        user.setEmail(request.email());
        user.setPhone(requestedPhone.orElse(null));
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);
        String token = createToken(savedUser, TokenType.ACCOUNT_VERIFICATION);
        sendToken(savedUser, token, "Verify your account");
        return userMapper.toSignUpResponseDto(savedUser);
    }


    @Override
    @Transactional
    public void verifyAccount(Long userId, String code) {
        VerificationToken verificationToken = findValidAccountVerificationToken(userId, code);
        verificationToken.getUser().setAccountVerified(true);
        verificationTokenRepository.deleteByUserAndType(verificationToken.getUser(), TokenType.ACCOUNT_VERIFICATION);
    }

    @Override
    @Transactional
    public void sendVerificationToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));
        if (user.isAccountVerified()) {
            throw new BadRequestException("The account is already verified.");
        }

        String code = createToken(user, TokenType.ACCOUNT_VERIFICATION);
        sendToken(user, code, "Verify your account");
    }

    @Override
    @Transactional
    public void forgotPassword(String emailOrPhone) {
        Optional<User> matchingUser = userRepository.findByEmail(emailOrPhone)
                .or(() -> userRepository.findByPhone(emailOrPhone));
        if (matchingUser.isEmpty() || matchingUser.get().getEmail() == null) {
            return;
        }

        User user = matchingUser.get();
        String token = createToken(user, TokenType.PASSWORD_RESET);
        try {
            sendToken(user, token, "Reset your password");
        } catch (BadGatewayException exception) {
            verificationTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);
            LOGGER.log(Level.WARNING, "Unable to deliver password reset email.", exception);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String emailOrPhone, String newPassword, String code) {
        User user = userRepository.findByEmail(emailOrPhone)
                .orElseThrow(() -> new BadRequestException("The token is invalid or has expired."));
        VerificationToken resetToken = findValidPasswordResetToken(user.getId(), code);
        resetToken.getUser().setPassword(passwordEncoder.encode(newPassword));
        verificationTokenRepository.deleteByUserAndType(resetToken.getUser(), TokenType.PASSWORD_RESET);
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
        message.setText("Hello " + user.getUserName() + ",\n\nYour token is: " + token);
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

    private VerificationToken findValidPasswordResetToken(Long userId, String code) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByUser_IdAndCodeAndType(userId, code, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("The token is invalid or has expired."));
        return validateExpiration(verificationToken);
    }

    private VerificationToken validateExpiration(VerificationToken verificationToken) {
        if (!verificationToken.getExpiresAt().isAfter(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException("The token is invalid or has expired.");
        }
        return verificationToken;
    }
}
