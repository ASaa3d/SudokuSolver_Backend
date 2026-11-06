package com.example.SudokuSolver.service;

import com.example.SudokuSolver.dto.UserResponseDto;

import java.util.List;

public interface FriendService {
    void sendRequest(Long userId, Long friendId);
    void acceptRequest(Long userId, Long friendId);
    void refuseRequest(Long userId, Long friendId);
    void cancelRequest(Long userId, Long friendId);
    void removeFriend(Long userId, Long friendId);
    void block(Long userId, Long blockedId);
    void unblock(Long userId, Long blockedId);
    List<UserResponseDto> getFriends(Long userId);
    List<UserResponseDto> getIncomingRequests(Long userId);
    List<UserResponseDto> getOutgoingRequests(Long userId);
    List<UserResponseDto> getBlocked(Long userId);
}
