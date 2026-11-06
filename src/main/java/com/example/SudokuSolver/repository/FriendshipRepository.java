package com.example.SudokuSolver.repository;

import com.example.SudokuSolver.model.Friendship;
import com.example.SudokuSolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {


    // Pending requests received by a user
    @Query("""
    SELECT f.user FROM Friendship f
    WHERE f.friend = :friend AND f.status = 'PENDING'
    """)
    List<User> findFriendRequests(@Param("friend") User friend);

    // Blocked requests received by a user
    @Query("""
    SELECT f.friend FROM Friendship f
    WHERE f.user = :user AND f.status = 'BLOCKED'
    """)
    List<User> findBlockedUsers(@Param("user") User user);

    // Check whether a friendship exists in either direction
    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.user = :a AND f.friend = :b)
           OR (f.user = :b AND f.friend = :a)
        """)
    Optional<Friendship> findBetween(@Param("a") User a, @Param("b") User b);

    // All accepted friends of a user, in either direction
    @Query("""
    SELECT CASE WHEN f.user = :user THEN f.friend ELSE f.user END
    FROM Friendship f
    WHERE (f.user = :user OR f.friend = :user)
      AND f.status = 'ACCEPTED'
    """)
    List<User> findFriends(@Param("user") User user);
}