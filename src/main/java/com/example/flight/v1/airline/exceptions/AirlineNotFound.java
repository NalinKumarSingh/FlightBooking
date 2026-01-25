package com.example.flight.v1.airline.exceptions;

public class AirlineNotFound extends RuntimeException {
  public AirlineNotFound(String message) {
    super(message);
  }
}
