package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.UserRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.mapper.UserMapper;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserRepository usrRepo;
    private final UserMapper usrMapper;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository usrRepo, UserMapper usrMapper, PasswordEncoder passEncoder) {
        this.usrRepo = usrRepo;
        this.usrMapper = usrMapper;
        this.passwordEncoder = passEncoder;
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userReq) {
        // 1. Utilize modern Optional stream-like syntax & handle specific conflicts
        userReq.email().ifPresent(email -> {
            if (usrRepo.findByEmail(email).isPresent()) {
                throw new RuntimeException("Email is already registered.");
            }
        });

        userReq.phone().ifPresent(phone -> {
            if (usrRepo.findByPhone(phone).isPresent()) {
                throw new RuntimeException("Phone number is already registered.");
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
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + usrId));

        currUser.setImgUrl(imgUrl);
    }

    @Override
    @Transactional
    public void solvePuzzle(Long usrId, Long puzzleId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new RuntimeException("User not found with ID: " + usrId)
        );
        currUser.solvePuzzle(puzzleId);

    }

    @Override
    @Transactional
    public void sendFriendRequest(Long usrId, Long friendId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new RuntimeException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(friendId).orElseThrow(
                () -> new RuntimeException("Friend not found with ID: " + usrId)
        );
        currUser.sendFriendRequest(friendUser);

    }
    @Override
    @Transactional
    public void acceptFriendRequest(Long usrId, Long friendId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new RuntimeException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(friendId).orElseThrow(
                () -> new RuntimeException("Friend not found with ID: " + usrId)
        );
        currUser.sendFriendRequest(friendUser);
    }

    @Override
    public void refuseFriendRequest(Long usrId, Long friendId) {

    }

    @Override
    @Transactional
    public void blockUser(Long usrId, Long blockedId) {
        User currUser = usrRepo.findById(usrId).orElseThrow(
                () -> new RuntimeException("User not found with ID: " + usrId)
        );
        User friendUser = usrRepo.findById(blockedId).orElseThrow(
                () -> new RuntimeException("Friend not found with ID: " + usrId)
        );
        currUser.sendFriendRequest(friendUser);

    }
}
