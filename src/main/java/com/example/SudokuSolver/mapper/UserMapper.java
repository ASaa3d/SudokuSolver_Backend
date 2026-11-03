package com.example.SudokuSolver.mapper;

import com.example.SudokuSolver.dto.UserRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Request DTO -> User Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "imgUrl", ignore = true)
    @Mapping(target = "streak", ignore = true)
    @Mapping(target = "totalScore", ignore = true)
    @Mapping(target = "friendships", ignore = true)
    @Mapping(target = "puzzles", ignore = true)
    User toEntity(UserRequestDto requestDto);

    // User Entity -> Response DTO
    UserResponseDto toResponseDto(User entity);
}
