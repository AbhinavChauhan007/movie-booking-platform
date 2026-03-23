package com.abhinav.moviebooking.user.controller;


import com.abhinav.moviebooking.user.dto.response.RoleResponseDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.exception.RoleNotFoundException;
import com.abhinav.moviebooking.user.exception.UserAlreadyExistsException;
import com.abhinav.moviebooking.user.exception.UserNotFoundException;
import com.abhinav.moviebooking.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ADMIN ONLY
    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get all users (Admin only)",
            description = "Retrieve a list of all registered users"
    )
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity
                .ok()
                .body(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("getUserById/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get user by ID (Admin only)",
            description = "Retrieve detailed information about a specific user"
    )
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) throws UserNotFoundException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserById(userId));
    }

    // PUBLIC (usually signup is via AuthController)
    @PostMapping("/createUser")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with email, password, and name"
    )
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserRequestDTO createUserRequestDTO) throws UserAlreadyExistsException, RoleNotFoundException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(createUserRequestDTO));

    }

    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Delete user (Admin only)",
            description = "Permanently delete a user account"
    )
    public void deleteUser(@PathVariable Long userId) throws UserNotFoundException {
        userService.deleteUser(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("assignRole/{userId}/roles/{roleName}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Assign role to user (Admin only)",
            description = "Assign a role (ADMIN/USER) to a specific user"
    )
    public void assignRole(@PathVariable Long userId, @PathVariable String roleName) throws UserNotFoundException, RoleNotFoundException {
        userService.assignRoleToUser(userId, roleName);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get all roles (Admin only)",
            description = "Retrieve a list of all available roles in the system"
    )
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get user's roles",
            description = "Retrieve all roles assigned to a specific user. Users can view their own roles, admins can view any user's roles"
    )
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }
}
