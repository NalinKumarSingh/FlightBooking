package com.example.flight.v1.user.model;

import com.example.flight.v1.user.enums.Role;
import lombok.Data;

@Data
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private Role role;
  private Boolean isVerified;
  private Boolean adminVerification;
  private Long airlineId;
  private String token;
}

