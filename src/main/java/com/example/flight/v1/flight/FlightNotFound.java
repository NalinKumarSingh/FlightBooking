package com.example.flight.v1.flight;

public class FlightNotFound extends RuntimeException {
  public FlightNotFound(String message) {
    super(message);
  }
}
