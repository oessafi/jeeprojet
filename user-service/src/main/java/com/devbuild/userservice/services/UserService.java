package com.devbuild.userservice.services;

import com.devbuild.userservice.dto.*;
import com.devbuild.userservice.enums.UserRole;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(String id);
    UserDTO createUser(CreateUserRequest request);
    UserDTO updateUser(String id, UpdateUserRequest request);
    void deleteUser(String id);
    List<UserDTO> getUsersByRole(UserRole role);
    UserDTO updateUserStatus(String id, UpdateStatusRequest request);
    UserDTO getUserByEmail(String email);
    UserProfileDTO getUserProfile(String id);

    UserDTO getByEmail(String name);
}
