package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.LogInRequestDto;
import com.example.SudokuSolver.dto.SignUpRequestDto;
import com.example.SudokuSolver.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    Long getLoginToken(LogInRequestDto loginReq);
    UserResponseDto getUser(String usrIDStr);
    UserResponseDto createUser(SignUpRequestDto userReq);
    void updateProfilePic(Long usrId,String imgUrl);
    void solvePuzzle(Long usrId,Long puzzleId);
    void sendFriendRequest(Long usrId,Long friendId);
    void changeUserPassword(Long usrId, String oldPassword, String newPassword);
    void verifyAccount(Long usrId, String code);

    Long forgetPassword(String emailOrPhone);
    void changeForgottenPassword(Long usrId, String newPassword);

    List<UserResponseDto> getFriendsList(Long usrId);
    List<UserResponseDto> getRequests(Long usrId);
    List<UserResponseDto> getBlocked(Long usrId);

    void acceptFriendRequest(Long usrId,Long friendId);
    void refuseFriendRequest(Long usrId,Long friendId);
    void blockUser(Long usrId,Long blockedId);
}
