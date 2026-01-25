package com.example.flight.v1.booking.exception;

public class BookingNotFound extends RuntimeException {
  public BookingNotFound(String message) {
    super(message);
  }
}
