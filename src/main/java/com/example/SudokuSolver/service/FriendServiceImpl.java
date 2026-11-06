package com.example.SudokuSolver.service;

import com.example.SudokuSolver.config.ExceptionHandling.BadRequestException;
import com.example.SudokuSolver.config.ExceptionHandling.ConflictException;
import com.example.SudokuSolver.config.ExceptionHandling.NotFoundException;
import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.mapper.UserMapper;
import com.example.SudokuSolver.model.Friendship;
import com.example.SudokuSolver.model.FriendshipStatus;
import com.example.SudokuSolver.model.User;
import com.example.SudokuSolver.repository.FriendshipRepository;
import com.example.SudokuSolver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void sendRequest(Long userId, Long friendId) {
        User user = findUser(userId);
        User friend = findUser(friendId);
        requireDifferentUsers(user, friend);
        friendshipRepository.findBetween(user, friend).ifPresent(existing -> {
            if (existing.getStatus() == FriendshipStatus.BLOCKED) {
                throw new ConflictException("A blocked user cannot receive a friend request.");
            }
            throw new ConflictException("A friendship or friend request already exists.");
        });
        user.sendFriendRequest(friend);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void acceptRequest(Long userId, Long friendId) {
        Friendship request = findRequest(userId, friendId, true);
        request.setStatus(FriendshipStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public void refuseRequest(Long userId, Long friendId) {
        friendshipRepository.delete(findRequest(userId, friendId, true));
    }

    @Override
    @Transactional
    public void cancelRequest(Long userId, Long friendId) {
        friendshipRepository.delete(findRequest(userId, friendId, false));
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        User user = findUser(userId);
        User friend = findUser(friendId);
        Friendship friendship = friendshipRepository.findBetween(user, friend)
                .filter(existing -> existing.getStatus() == FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new NotFoundException("Friendship not found."));
        friendshipRepository.delete(friendship);
    }

    @Override
    @Transactional
    public void block(Long userId, Long blockedId) {
        User user = findUser(userId);
        User blockedUser = findUser(blockedId);
        requireDifferentUsers(user, blockedUser);

        Friendship friendship = friendshipRepository.findBetween(user, blockedUser).orElseGet(Friendship::new);
        friendship.setUser(user);
        friendship.setFriend(blockedUser);
        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public void unblock(Long userId, Long blockedId) {
        User user = findUser(userId);
        User blockedUser = findUser(blockedId);
        Friendship friendship = friendshipRepository.findBetween(user, blockedUser)
                .filter(existing -> existing.getStatus() == FriendshipStatus.BLOCKED)
                .filter(existing -> existing.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Blocked user not found."));
        friendshipRepository.delete(friendship);
    }

    @Override
    @Transactional
    public List<UserResponseDto> getFriends(Long userId) {
        return friendshipRepository.findFriends(findUser(userId))
                .stream().map(userMapper::toResponseDto).toList();
    }

    @Override
    @Transactional
    public List<UserResponseDto> getIncomingRequests(Long userId) {
        return friendshipRepository.findFriendRequests(findUser(userId))
                .stream().map(userMapper::toResponseDto).toList();
    }

    @Override
    @Transactional
    public List<UserResponseDto> getOutgoingRequests(Long userId) {
        return findUser(userId).getFriendships().stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .map(Friendship::getFriend)
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public List<UserResponseDto> getBlocked(Long userId) {
        return friendshipRepository.findBlockedUsers(findUser(userId))
                .stream().map(userMapper::toResponseDto).toList();
    }

    private Friendship findRequest(Long userId, Long friendId, boolean incoming) {
        User user = findUser(userId);
        User friend = findUser(friendId);
        requireDifferentUsers(user, friend);
        return friendshipRepository.findBetween(user, friend)
                .filter(request -> request.getStatus() == FriendshipStatus.PENDING)
                .filter(request -> incoming
                        ? request.getUser().getId().equals(friendId) && request.getFriend().getId().equals(userId)
                        : request.getUser().getId().equals(userId) && request.getFriend().getId().equals(friendId))
                .orElseThrow(() -> new NotFoundException("Friend request not found."));
    }

    private void requireDifferentUsers(User user, User other) {
        if (user.getId().equals(other.getId())) {
            throw new BadRequestException("Users cannot perform this action on themselves.");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + userId)
        );
    }
}
