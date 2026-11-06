package com.example.SudokuSolver.repository;

import com.example.SudokuSolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    @Query("""
            SELECT u from User u
            WHERE u.email = :email_or_phone OR u.phone = :email_or_phone
            """)
    Optional<User> findByEmailOrPhone(@Param("email_or_phone") String emailOrPhone);
}
