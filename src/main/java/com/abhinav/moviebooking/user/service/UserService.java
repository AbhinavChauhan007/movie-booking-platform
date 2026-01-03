package com.abhinav.moviebooking.user.service;

import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequestDTO userRequestDTO);

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getAllUsers();

    void assignRoleToUser(Long userId, String roleName);

    void deleteUser(Long id);
}
