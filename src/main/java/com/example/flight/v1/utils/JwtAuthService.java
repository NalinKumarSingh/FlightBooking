package com.example.flight.v1.utils;

import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthService {

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  public User authenticate(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    String email = jwtUtil.extractEmail(token);

    if (!jwtUtil.isTokenValid(token, email)) {
      throw new UnauthorizedException("Invalid or expired token");
    }

    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
  }

  public User authenticateWithRole(String authHeader, Role requiredRole) {
    User user = authenticate(authHeader);
    if (!user.getRole().equals(requiredRole)) {
      throw new UnauthorizedException("Access denied for role: " + requiredRole);
    }
    return user;
  }
}
