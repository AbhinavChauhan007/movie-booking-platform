package com.abhinav.moviebooking.user.controller;


import com.abhinav.moviebooking.common.dto.ApiResponse;
import com.abhinav.moviebooking.user.dto.request.AssignRoleRequest;
import com.abhinav.moviebooking.user.dto.response.RoleResponseDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId,asc") String[] sort
    ) {
        // Parse sort parameters
        Sort.Order order = sort.length > 1
                ? new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0])
                : new Sort.Order(Sort.Direction.ASC, sort[0]);

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity
                .ok(
                        ApiResponse.success("Users retrieved successfully", userService.getAllUsers(pageable))
                );

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUserById/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get user by ID (Admin only)",
            description = "Retrieve detailed information about a specific user"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        return ResponseEntity
                .ok(
                        ApiResponse.success("User retrieved successfully", userService.getUserById(userId))
                );

    }

    @DeleteMapping("/deleteUser/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Delete user (Admin only)",
            description = "Permanently delete a user account"
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User with ID " + userId + " deleted successfully")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/assignRole/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Assign role to user (Admin only)",
            description = "Assign a role (ADMIN/USER) to a specific user"
    )
    public ResponseEntity<ApiResponse<Void>> assignRole(@PathVariable Long userId, @Valid @RequestBody AssignRoleRequest roleRequest) {
        userService.assignRoleToUser(userId, roleRequest.getRoleName());
        return
                ResponseEntity.ok(
                        ApiResponse.success("Roles assigned successfully to user")
                );
    }

    @GetMapping("/getAllRoles")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get all roles (Admin only)",
            description = "Retrieve a list of all available roles in the system"
    )
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getAllRoles() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roles retrieved successfully", userService.getAllRoles()
                )
        );
    }

    @GetMapping("/getUserRoles/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get user's roles",
            description = "Retrieve all roles assigned to a specific user. Users can view their own roles, admins can view any user's roles"
    )
    public ResponseEntity<ApiResponse<Set<String>>> getUserRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User roles retrieved successfully", userService.getUserRoles(userId))
        );
    }
}
