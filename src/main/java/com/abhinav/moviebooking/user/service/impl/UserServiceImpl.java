package com.abhinav.moviebooking.user.service.impl;

import com.abhinav.moviebooking.security.token.service.RefreshTokenService;
import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.dto.response.RoleResponseDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.entity.Role;
import com.abhinav.moviebooking.user.entity.User;
import com.abhinav.moviebooking.user.exception.RoleNotFoundException;
import com.abhinav.moviebooking.user.exception.RoleValidationException;
import com.abhinav.moviebooking.user.exception.UserAlreadyExistsException;
import com.abhinav.moviebooking.user.exception.UserNotFoundException;
import com.abhinav.moviebooking.user.repository.RoleRepository;
import com.abhinav.moviebooking.user.repository.UserRepository;
import com.abhinav.moviebooking.user.service.UserService;
import com.abhinav.moviebooking.util.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO createUser(CreateUserRequestDTO userRequestDTO) {

        // Check username uniqueness
        if (userRepository.findByUsername(userRequestDTO.getUsername()).isPresent())
            throw new UserAlreadyExistsException("Username already exists");

        // Check email uniqueness
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent())
            throw new UserAlreadyExistsException("email already exists");

        // Create user entity
        User userToBeSaved = new User();
        userToBeSaved.setUsername(userRequestDTO.getUsername());
        userToBeSaved.setEmail(userRequestDTO.getEmail());
        userToBeSaved.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        // Assign default role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("USER"));

        userToBeSaved.getRoles().add(userRole);

        // save
        User savedUser = userRepository.save(userToBeSaved);

        return mapToUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToUserResponseDTO(user);
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable)
                .map(this::mapToUserResponseDTO);

    }

    @Override
    public void assignRoleToUser(Long userId, String roles) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String[] roleNames = roles.split(",");

        if (roleNames.length == 0 || roles.trim().isEmpty())
            throw new RoleValidationException(
                    ErrorCode.INVALID_ROLE_FORMAT,
                    "At least one role must be provided"
            );

        // get current roles
        Set<String> currentRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        for (String roleName : roleNames) {
            String normalizedRoleName = roleName.trim().toUpperCase();

            // Skip empty strings
            if (normalizedRoleName.isEmpty()) {
                continue;
            }

            // Check for duplicates
            if (currentRoleNames.contains(normalizedRoleName)) {
                throw new RoleValidationException(
                        ErrorCode.DUPLICATE_ROLE,
                        "User already has role: " + normalizedRoleName
                );
            }

            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new RoleNotFoundException(normalizedRoleName));

            user.getRoles().add(role);
            currentRoleNames.add(normalizedRoleName);
        }

    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setActive(false);
        user.setDeactivatedAt(Instant.now());
        userRepository.save(user);

        // Revoke all refresh tokens for this user
        refreshTokenService.revokeAllTokensForUser(user.getEmail());
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new RoleResponseDTO(role.getId(), role.getName()))
                .toList();

    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }


    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setUsername(user.getUsername());

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        userResponseDTO.setRoles(roles);
        return userResponseDTO;
    }


}
