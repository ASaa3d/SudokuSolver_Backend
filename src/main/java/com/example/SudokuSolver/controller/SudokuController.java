package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.dto.GetGridRequestDto;
import com.example.SudokuSolver.dto.GetGridResponseDto;
import com.example.SudokuSolver.service.SudokuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/sudoku")
public class SudokuController {
    private final SudokuService sudokuService;

    @PostMapping("/grid")
    ResponseEntity<GetGridResponseDto> getGrid(@RequestBody @Valid GetGridRequestDto request)
    {
        GetGridResponseDto response = new GetGridResponseDto(sudokuService.getGrid(request.imageUrl()));
        return  ResponseEntity.ok(response);
    }

}
