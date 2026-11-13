package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.BadGatewayException;
import com.example.SudokuSolver.config.ExceptionHandling.BadRequestException;
import com.example.SudokuSolver.config.ExceptionHandling.ConflictException;
import com.example.SudokuSolver.config.ExceptionHandling.UnauthorizedException;
import com.example.SudokuSolver.config.FrontendProperties;
import com.example.SudokuSolver.config.jwt.JwtTokenService;
import com.example.SudokuSolver.dto.*;
import com.example.SudokuSolver.mapper.UserMapper;
import com.example.SudokuSolver.model.TokenType;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.model.VerificationToken;
import com.example.SudokuSolver.repository.UserRepository;
import com.example.SudokuSolver.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final FrontendProperties frontendProperties;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JavaMailSender mailSender;
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
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);
        String token = createToken(savedUser, TokenType.ACCOUNT_VERIFICATION);
        sendToken(savedUser, token, TokenType.ACCOUNT_VERIFICATION);
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
        sendToken(user, code, TokenType.ACCOUNT_VERIFICATION);
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
            sendToken(matchingUser, token, TokenType.PASSWORD_RESET);
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
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Token is invalid or has expired."));
        findValidPasswordResetToken(user.getId(), code);
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

    private void sendToken(User user, String token, TokenType type) {
        String subject = type == TokenType.ACCOUNT_VERIFICATION
                ? "Verify your Sudoku account"
                : "Reset your Sudoku password";
        String title = type == TokenType.ACCOUNT_VERIFICATION
                ? "Welcome to Sudoku Solver"
                : "Reset your password";
        String description = type == TokenType.ACCOUNT_VERIFICATION
                ? "Your Sudoku Solver account is ready. Verify your email address to get started."
                : "We received a request to reset the password for your Sudoku Solver account.";
        String buttonText = type == TokenType.ACCOUNT_VERIFICATION
                ? "Verify my account"
                : "Reset my password";
        String frontendPath = type == TokenType.ACCOUNT_VERIFICATION
                ? frontendProperties.verifyPasswordPath()
                : frontendProperties.resetPasswordPath();
        String link = UriComponentsBuilder.fromUriString(frontendProperties.baseUrl())
                .path(frontendPath)
                .queryParam("email", user.getEmail())
                .queryParam("code", token)
                .build()
                .encode()
                .toUriString();

        String safeName = HtmlUtils.htmlEscape(user.getUserName());
        String safeLink = HtmlUtils.htmlEscape(link);
        String plainText = "Hello " + user.getUserName() + ",\n\n" + description
                + "\n\nYour code is: " + token
                + "\n\nContinue here: " + link
                + "\n\nThis link and code expire in " + TOKEN_TTL_MINUTES + " minutes.";
        String fromAddress = templateMessage.getFrom();
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Email sender address is not configured.");
        }
        String html = """
                <!doctype html>
                <html lang="en">
                <body style="margin:0;padding:32px 12px;background-color:#f3f5f7;font-family:Arial,Helvetica,sans-serif;color:#243746;">
                  <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;">
                    <tr>
                      <td style="padding:28px 36px;background:#2f4a5a;text-align:center;">
                        <img src="cid:sudoku-logo" width="72" height="72" alt="Sudoku Solver" style="display:block;width:72px;height:72px;margin:0 auto 12px;border:0;border-radius:16px;">
                        <div style="font-size:14px;font-weight:bold;letter-spacing:1px;color:#f8f5ee;">SUDOKU SOLVER</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:36px 36px 28px;">
                        <p style="margin:0 0 10px;font-size:15px;color:#536773;">Hello %s,</p>
                        <h1 style="margin:0 0 16px;font-size:26px;line-height:1.25;color:#243746;">%s</h1>
                        <p style="margin:0 0 26px;font-size:15px;line-height:1.65;color:#536773;">%s</p>
                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin:0 auto 24px;">
                          <tr>
                            <td style="border-radius:8px;background:#2f4a5a;text-align:center;">
                              <a href="%s" style="display:inline-block;padding:14px 26px;border-radius:8px;color:#ffffff;text-decoration:none;font-size:15px;font-weight:bold;">%s</a>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:0 0 8px;text-align:center;font-size:13px;color:#71818a;">Or enter this code manually:</p>
                        <p style="margin:0;text-align:center;font-size:24px;font-weight:bold;letter-spacing:6px;color:#2f4a5a;">%s</p>
                        <p style="margin:24px 0 0;font-size:13px;line-height:1.6;color:#71818a;">This link and code expire in %d minutes. If you didn’t request this, you can safely ignore this email.</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:18px 36px;background:#f8f5ee;text-align:center;font-size:12px;color:#71818a;">Sudoku Solver · A little clarity, one square at a time.</td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, title, description, safeLink, buttonText, token, TOKEN_TTL_MINUTES);

        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(plainText, html);
            helper.addInline("sudoku-logo", new ClassPathResource("images/logo.png"), "image/png");
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new BadGatewayException("Unable to send the account email.", exception);
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
