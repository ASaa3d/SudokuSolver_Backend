package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.dto.GetGridRequestDto;
import com.example.SudokuSolver.dto.GetGridResponseDto;
import com.example.SudokuSolver.service.SudokuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/sudoku")
public class SudokuController {
    private final SudokuService sudokuService;

    @GetMapping("/grid")
    ResponseEntity<GetGridResponseDto> getGrid(@RequestBody @Valid GetGridRequestDto request)
    {
        GetGridResponseDto response = new GetGridResponseDto(sudokuService.getGrid(request.imageUrl()));
        return  ResponseEntity.ok(response);
    }

}
