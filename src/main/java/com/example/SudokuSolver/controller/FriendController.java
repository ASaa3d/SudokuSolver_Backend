package com.example.SudokuSolver.controller;

import com.example.SudokuSolver.dto.UserResponseDto;
import com.example.SudokuSolver.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getFriends(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.removeFriend(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<UserResponseDto>> getIncomingRequests(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(friendService.getIncomingRequests(userId));
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<UserResponseDto>> getOutgoingRequests(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(friendService.getOutgoingRequests(userId));
    }

    @PostMapping("/requests/{id}")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.sendRequest(userId, id);
        return ResponseEntity.created(URI.create("/api/friends/requests/" + id)).build();
    }

    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.acceptRequest(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{id}/refuse")
    public ResponseEntity<Void> refuseRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.refuseRequest(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> cancelRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.cancelRequest(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<UserResponseDto>> getBlocked(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(friendService.getBlocked(userId));
    }

    @PostMapping("/blocked/{id}")
    public ResponseEntity<Void> block(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.block(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/blocked/{id}")
    public ResponseEntity<Void> unblock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        friendService.unblock(userId, id);
        return ResponseEntity.noContent().build();
    }
}
