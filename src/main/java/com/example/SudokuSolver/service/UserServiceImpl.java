package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.BadRequestException;
import com.example.SudokuSolver.config.ExceptionHandling.NotFoundException;
import com.example.SudokuSolver.dto.UpdateProfileRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.mapper.UserMapper;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto getUser(Long userId) {
        return userMapper.toResponseDto(findUser(userId));
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(Long userId, UpdateProfileRequestDto request) {
        User user = findUser(userId);
        if (request.userName() != null) {
            user.setUserName(request.userName());
        }
        if (request.imgUrl() != null) {
            user.setImgUrl(request.imgUrl());
        }
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + userId)
        );
    }
}
