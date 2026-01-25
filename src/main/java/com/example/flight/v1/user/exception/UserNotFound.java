package com.example.flight.v1.user.exception;

public class UserNotFound extends RuntimeException {
  public UserNotFound(String message) {
    super(message);
  }
}
