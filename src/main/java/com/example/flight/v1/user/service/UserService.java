package com.example.flight.v1.user.service;

import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.model.LoginRequest;
import com.example.flight.v1.user.model.UserRequest;
import com.example.flight.v1.user.model.UserResponse;

import java.util.List;

public interface UserService {
  UserResponse createUser(UserRequest request);
  UserResponse getUser(Long id);
  List<UserResponse> getAllUsers(Boolean isVerified, Role role);
  boolean verifyOtp(String email, String otp);
  void verifyStaffByAdminFromToken(String token, String staffEmail);
  UserResponse login(LoginRequest loginRequest);
}
