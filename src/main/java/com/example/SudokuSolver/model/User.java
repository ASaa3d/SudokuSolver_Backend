package com.example.SudokuSolver.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="user_name", nullable = false)
    private String userName;
    @Column(name="email", nullable = false,unique = true)
    private String email;
    @Column(name = "account_verified", nullable = false)
    private boolean accountVerified = false;
    private String password;
    @CreatedDate
    private LocalDateTime creationDate;
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    @LastModifiedDate
    private LocalDateTime updateDate;
    @Column(name="img_url", nullable = true)
    private String imgUrl;
    private int streak = 0;
    @Column(name="total_score")
    private int totalScore = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Friendship> friendships = new HashSet<>();


    public void sendFriendRequest(User targetFriend) {
        Friendship friendship = new Friendship();
        friendship.setUser(this);
        friendship.setFriend(targetFriend);
        friendship.setStatus(FriendshipStatus.PENDING);
        this.friendships.add(friendship);
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserPuzzle> puzzles = new HashSet<>();

    public void setPassword(String password) {
        this.password = password;
        if (id != null) {
            this.passwordChangedAt = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        }
    }

    public void solvePuzzle(Long puzzleId) {
        this.puzzles.add(new UserPuzzle(this, puzzleId, LocalDateTime.now()));
        this.streak = this.streak+1;
        this.totalScore = this.totalScore+1;
    }
}
