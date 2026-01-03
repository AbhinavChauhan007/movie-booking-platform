package com.abhinav.moviebooking.user.controller;


import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ADMIN ONLY
    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity
                .ok()
                .body(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("getUserById/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserById(userId));
    }

    // PUBLIC (usually signup is via AuthController)
    @PostMapping("/createUser")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserRequestDTO createUserRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(createUserRequestDTO));

    }

    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("assignRole/{userId}/roles/{roleName}")
    public void assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        userService.assignRoleToUser(userId, roleName);
    }
}
