package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.UserRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.model.User;

import java.util.Optional;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userReq);
    void updateProfilePic(Long usrId,String imgUrl);
    void solvePuzzle(Long usrId,Long puzzleId);
    void sendFriendRequest(Long usrId,Long friendId);
    void acceptFriendRequest(Long usrId,Long friendId);
    void refuseFriendRequest(Long usrId,Long friendId);
    void blockUser(Long usrId,Long blockedId);

}
