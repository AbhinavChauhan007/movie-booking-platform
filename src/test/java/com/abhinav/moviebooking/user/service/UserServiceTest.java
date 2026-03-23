package com.abhinav.moviebooking.user.service;

import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.entity.Role;
import com.abhinav.moviebooking.user.entity.User;
import com.abhinav.moviebooking.user.exception.RoleNotFoundException;
import com.abhinav.moviebooking.user.exception.UserAlreadyExistsException;
import com.abhinav.moviebooking.user.exception.UserNotFoundException;
import com.abhinav.moviebooking.user.repository.RoleRepository;
import com.abhinav.moviebooking.user.repository.UserRepository;
import com.abhinav.moviebooking.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should create user successfully with default USER role")
    void createUser_shouldCreateUserWithUserRole() throws UserAlreadyExistsException, RoleNotFoundException {
        // Given
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO();
        requestDTO.setUsername("johndoe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setPassword("password123");

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Mock save to capture and return the user being saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserResponseDTO result = userService.createUser(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("johndoe", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_USER"));
        verify(userRepository).findByUsername("johndoe");
        verify(userRepository).findByEmail("john@example.com");
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username exists")
    void createUser_shouldThrowException_whenUsernameExists() {
        // Given
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO();
        requestDTO.setUsername("existinguser");
        requestDTO.setEmail("new@example.com");
        requestDTO.setPassword("password123");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(requestDTO)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository).findByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email exists")
    void createUser_shouldThrowException_whenEmailExists() {
        // Given
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO();
        requestDTO.setUsername("newuser");
        requestDTO.setEmail("existing@example.com");
        requestDTO.setPassword("password123");

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(requestDTO)
        );

        assertTrue(exception.getMessage().contains("email already exists"));
        verify(userRepository).findByUsername("newuser");
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw RoleNotFoundException when ROLE_USER not found")
    void createUser_shouldThrowException_whenRoleNotFound() {
        // Given
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO();
        requestDTO.setUsername("johndoe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setPassword("password123");

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // When & Then
        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> userService.createUser(requestDTO)
        );

        assertTrue(exception.getMessage().contains("ROLE_USER"));
        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_shouldReturnUser() throws UserNotFoundException {
        // Given
        Long userId = 1L;
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setRoles(new HashSet<>(Set.of(userRole)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserResponseDTO result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("johndoe", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_USER"));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by ID")
    void getUserById_shouldThrowException_whenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should get all users successfully")
    void getAllUsers_shouldReturnListOfUsers() {
        // Given
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setRoles(new HashSet<>(Set.of(userRole)));

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRoles(new HashSet<>(Set.of(userRole)));

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // When
        List<UserResponseDTO> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should assign role to user successfully")
    void assignRoleToUser_shouldAddRoleToUser() throws UserNotFoundException, RoleNotFoundException {
        // Given
        Long userId = 1L;
        String roleName = "ROLE_ADMIN";

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");
        user.setRoles(new HashSet<>(Set.of(userRole)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(adminRole));

        // When
        userService.assignRoleToUser(userId, roleName);

        // Then
        assertTrue(user.getRoles().contains(adminRole));
        verify(userRepository).findById(userId);
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when assigning role to non-existent user")
    void assignRoleToUser_shouldThrowException_whenUserNotFound() {
        // Given
        Long userId = 999L;
        String roleName = "ROLE_ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UserNotFoundException.class,
                () -> userService.assignRoleToUser(userId, roleName)
        );

        verify(userRepository).findById(userId);
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    @DisplayName("Should throw RoleNotFoundException when assigning non-existent role")
    void assignRoleToUser_shouldThrowException_whenRoleNotFound() {
        // Given
        Long userId = 1L;
        String roleName = "ROLE_NONEXISTENT";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                RoleNotFoundException.class,
                () -> userService.assignRoleToUser(userId, roleName)
        );

        verify(userRepository).findById(userId);
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_shouldDeleteUser() throws UserNotFoundException {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("johndoe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void deleteUser_shouldThrowException_whenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserResponseDTO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }
}
