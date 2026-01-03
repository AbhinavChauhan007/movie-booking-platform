package com.abhinav.moviebooking.user.service.impl;

import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.entity.Role;
import com.abhinav.moviebooking.user.entity.User;
import com.abhinav.moviebooking.user.exception.RoleNotFoundException;
import com.abhinav.moviebooking.user.exception.UserAlreadyExistsException;
import com.abhinav.moviebooking.user.exception.UserNotFoundException;
import com.abhinav.moviebooking.user.repository.RoleRepository;
import com.abhinav.moviebooking.user.repository.UserRepository;
import com.abhinav.moviebooking.user.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER"));

        userToBeSaved.getRoles().add(userRole);

        // save
        User savedUser = userRepository.save(userToBeSaved);

        return mapToUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return null;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        user.getRoles().add(role);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(user);
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
