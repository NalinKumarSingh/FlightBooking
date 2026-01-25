package com.example.flight.v1.user.model;

import com.example.flight.v1.user.enums.Role;
import lombok.Data;

@Data
public class UserRequest {
  private String name;
  private String email;
  private String password;
  private Role role;
  private Long airlineId;
}

