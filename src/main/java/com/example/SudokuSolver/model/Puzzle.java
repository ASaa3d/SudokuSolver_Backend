package com.example.SudokuSolver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="puzzle")
@Getter
@Setter
@NoArgsConstructor
public class Puzzle {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int size;
    private String grid;
    private LocalDateTime creationDate;
}
