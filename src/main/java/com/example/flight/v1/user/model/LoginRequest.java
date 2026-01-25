package com.example.flight.v1.user.model;

import lombok.Data;

@Data
public class LoginRequest {
  private String email;
  private String password;
}
