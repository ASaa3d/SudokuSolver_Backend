package com.example.SudokuSolver.model;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="user_name", nullable = false)
    private String userName;
    @Column(name="email", nullable = true)
    private String email;
    @Column(name="phone", nullable = true)
    private String phone;
    private String password;
    @CreatedDate
    private LocalDateTime creationDate;
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

    private Set<UserPuzzle> puzzles = new HashSet<>();

    public void solvePuzzle(Long puzzleId) {
        this.puzzles.add(new UserPuzzle(puzzleId, java.time.LocalDateTime.now()));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }



    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }



    public User(){}


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public Set<Friendship> getFriendships() {
        return friendships;
    }

    public void setFriendships(Set<Friendship> friendships) {
        this.friendships = friendships;
    }

    public Set<UserPuzzle> getPuzzles() {
        return puzzles;
    }

    public void setPuzzles(Set<UserPuzzle> puzzles) {
        this.puzzles = puzzles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
