package com.abhinav.moviebooking.user.service;

import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.dto.response.RoleResponseDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import com.abhinav.moviebooking.user.exception.RoleNotFoundException;
import com.abhinav.moviebooking.user.exception.UserAlreadyExistsException;
import com.abhinav.moviebooking.user.exception.UserNotFoundException;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequestDTO userRequestDTO) throws UserAlreadyExistsException, RoleNotFoundException;

    UserResponseDTO getUserById(Long id) throws UserNotFoundException;

    List<UserResponseDTO> getAllUsers();

    void assignRoleToUser(Long userId, String roleName) throws UserNotFoundException, RoleNotFoundException;

    void deleteUser(Long id) throws UserNotFoundException;

    List<RoleResponseDTO> getAllRoles();

    Set<String> getUserRoles(Long userId) throws UserNotFoundException;
}
