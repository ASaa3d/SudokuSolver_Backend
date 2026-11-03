package com.example.SudokuSolver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name="puzzle")
public class Puzzle {
    @Id
    private String id;
    private int size;
    private String grid;
    private LocalDateTime creationDate;
}
