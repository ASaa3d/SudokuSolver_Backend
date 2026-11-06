package com.example.SudokuSolver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_puzzle")
@Getter
@Setter
@NoArgsConstructor
public class UserPuzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Connects back to the User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "puzzle_id")
    private Long puzzleId;

    private LocalDateTime solvedAt;

    public UserPuzzle(User user, Long puzzleId, LocalDateTime solvedAt) {
        this.user = user;
        this.puzzleId = puzzleId;
        this.solvedAt = solvedAt;
    }
}
