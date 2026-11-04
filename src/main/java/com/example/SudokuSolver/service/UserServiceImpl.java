package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.BadGatewayException;
import com.example.SudokuSolver.config.ExceptionHandling.BadRequestException;
import com.example.SudokuSolver.config.ExceptionHandling.ConflictException;
import com.example.SudokuSolver.config.ExceptionHandling.NotFoundException;
import com.example.SudokuSolver.config.ExceptionHandling.UnauthorizedException;
import com.example.SudokuSolver.dto.LogInRequestDto;
import com.example.SudokuSolver.dto.SignUpRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.mapper.UserMapper;
import com.example.SudokuSolver.model.*;
import com.example.SudokuSolver.repository.FriendshipRepository;
import com.example.SudokuSolver.repository.UserRepository;
import com.example.SudokuSolver.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository usrRepo;
    private final UserMapper usrMapper;
    private final PasswordEncoder passwordEncoder;
    private final FriendshipRepository friendsRepo;
    private final VerificationTokenRepository verifyRepo;
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;
    private final SecureRandom secureRandom;


    @Override
    public Long getLoginToken(LogInRequestDto loginReq) {
        User currUser = usrRepo.findByEmailOrPhone(loginReq.emailOrPhone()).orElseThrow(
                () -> new UnauthorizedException("Invalid email, phone, or password.")
        );

        if (!passwordEncoder.matches(loginReq.password(), currUser.getPassword())) {
            throw new UnauthorizedException("Invalid email, phone, or password.");
        }
        return currUser.getId();
    }

    @Override
    public UserResponseDto getUser(String usrIDStr) {
        Long usrID = Long.valueOf(usrIDStr);
        User currUser = usrRepo.findById(usrID).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrID)
        );
        return usrMapper.toResponseDto(currUser);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(SignUpRequestDto userReq) {
        // 1. Utilize modern Optional stream-like syntax & handle specific conflicts
        if (usrRepo.findByEmail(userReq.email()).isPresent()) {
            throw new ConflictException("Email is already registered: " + userReq.email());
        }

        userReq.phone().ifPresent(phone -> {
            if (usrRepo.findByPhone(phone).isPresent()) {
                throw new ConflictException("Phone number is already registered: " + phone);
            }
        });

        User newUser = usrMapper.toEntity(userReq);
        newUser.setPassword(passwordEncoder.encode(userReq.password()));

        User savedUser = usrRepo.save(newUser);
        return usrMapper.toResponseDto(savedUser);
    }


    @Override
    @Transactional
    public void updateProfilePic(Long usrId, String imgUrl) {
        // 1. Fetch the user or immediately fail fast with a targeted exception
        User currUser = usrRepo.findById(usrId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + usrId));

        currUser.setImgUrl(imgUrl);
    }

    @Override
    @Transactional
    public void solvePuzzle(Long usrId, Long puzzleId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        currUser.solvePuzzle(puzzleId);

    }

    @Override
    @Transactional
    public void sendFriendRequest(Long usrId, Long friendId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(friendId).orElseThrow(
                () -> new NotFoundException("Friend not found with ID: " + friendId)
        );
        currUser.sendFriendRequest(friendUser);

    }

    @Override
    @Transactional
    public void changeUserPassword(Long usrId, String oldPassword, String newPassword) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );

        if (!passwordEncoder.matches(oldPassword, currUser.getPassword())) {
            throw new BadRequestException("Old password is incorrect.");
        }

        currUser.setPassword(passwordEncoder.encode(newPassword));
    }

    @Override
    @Transactional
    public void verifyAccount(Long usrId, String code) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        VerificationToken token = verifyRepo.findTopByUserAndTypeAndUsedAtIsNullOrderByExpiresAtDesc(currUser,TokenType.ACCOUNT_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("No verification code was found."));
        if(Instant.now().isAfter(token.getExpiresAt()))
        {
            throw new BadRequestException("Verification code has expired.");
        }
        else
        {
            verifyRepo.deleteByUserAndType(currUser,TokenType.ACCOUNT_VERIFICATION);
            currUser.setAccountVerified(true);


        }

    }

    @Override
    public Long forgetPassword(String emailOrPhone) {
        User currUser = usrRepo.findByEmail(emailOrPhone)
                .or(() -> usrRepo.findByPhone(emailOrPhone))
                .orElseThrow(() -> new NotFoundException("User not found: " + emailOrPhone));
        String code = String.valueOf(secureRandom.nextInt(100000,999999));
        if (currUser.getEmail() !=  null) {
            SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
            msg.setTo(currUser.getEmail());
            msg.setText(
                    "Dear " + currUser.getUserName()
                            + "Your Verification Code is "
                            + code);
            try {
                this.mailSender.send(msg);
            }
            catch (MailException ex) {
                throw new BadGatewayException("Unable to send the verification email.", ex);
            }finally {
                VerificationToken newToken = new VerificationToken(
                currUser,
                code,
                TokenType.PASSWORD_RESET,
                Instant.now().plus(15, ChronoUnit.MINUTES));
                verifyRepo.save(newToken);
            }
            return currUser.getId();
        }
        else {
            throw new BadRequestException("Password reset is unavailable for accounts without an email address.");
        }
    }

    @Override
    @Transactional
    public void changeForgottenPassword(Long usrId, String newPassword) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        currUser.setPassword(passwordEncoder.encode(newPassword));
    }

    @Override
    public List<UserResponseDto> getFriendsList(Long usrId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        List<User> usersList = friendsRepo.findFriends(currUser);
        return usersList.stream()
                .map(usrMapper::toResponseDto)
                .toList();

    }


    @Override
    public List<UserResponseDto> getRequests(Long usrId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        List<User> usersList = friendsRepo.findFriendRequests(currUser);
        return usersList.stream()
                .map(usrMapper::toResponseDto)
                .toList();

    }

    @Override
    public List<UserResponseDto> getBlocked(Long usrId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        List<User> usersList = friendsRepo.findBlockedUsers(currUser);
        return usersList.stream()
                .map(usrMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long usrId, Long friendId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(friendId).orElseThrow(
                () -> new NotFoundException("Friend not found with ID: " + friendId)
        );
        Friendship friendship = friendsRepo.findBetween(currUser,friendUser).orElseThrow(
                () -> new NotFoundException("No friendship was found.")
        );
        friendship.setStatus(FriendshipStatus.ACCEPTED);
    }

    @Override
    public void refuseFriendRequest(Long usrId, Long friendId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(friendId).orElseThrow(
                () -> new NotFoundException("Friend not found with ID: " + friendId)
        );
        Friendship friendship = friendsRepo.findBetween(currUser,friendUser).orElseThrow(
                () -> new NotFoundException("No friendship was found.")
        );
        friendsRepo.delete(friendship);

    }

    @Override
    @Transactional
    public void blockUser(Long usrId, Long blockedId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(blockedId).orElseThrow(
                () -> new NotFoundException("Friend not found with ID: " + blockedId)
        );
        Friendship friendship = friendsRepo.findBetween(currUser,friendUser).orElseThrow(
                () -> new NotFoundException("No friendship was found.")
        );
        friendship.setStatus(FriendshipStatus.BLOCKED);
    }
}
