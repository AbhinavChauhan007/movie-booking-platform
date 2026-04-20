package com.abhinav.moviebooking.user.service;

import com.abhinav.moviebooking.user.dto.request.CreateUserRequestDTO;
import com.abhinav.moviebooking.user.dto.response.RoleResponseDTO;
import com.abhinav.moviebooking.user.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequestDTO userRequestDTO);

    UserResponseDTO getUserById(Long id);

    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    void assignRoleToUser(Long userId, String roles);

    void deleteUser(Long id);

    List<RoleResponseDTO> getAllRoles();

    Set<String> getUserRoles(Long userId);
}
