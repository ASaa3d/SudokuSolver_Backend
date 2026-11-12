package com.example.SudokuSolver.mapper;

import com.example.SudokuSolver.dto.SignUpRequestDto;
import com.example.SudokuSolver.dto.SignUpResponseDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Request DTO -> User Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "imgUrl", ignore = true)
    @Mapping(target = "streak", ignore = true)
    @Mapping(target = "totalScore", ignore = true)
    @Mapping(target = "friendships", ignore = true)
    @Mapping(target = "puzzles", ignore = true)
    @Mapping(target = "accountVerified", ignore = true)
    User toEntity(SignUpRequestDto requestDto);

    // User Entity -> Response DTO
    UserResponseDto toResponseDto(User entity);
    SignUpResponseDto toSignUpResponseDto(User entity);
}
