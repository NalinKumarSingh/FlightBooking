package com.example.flight.v1.flightschedule.exception;

public class ScheduleNotFound extends RuntimeException {
  public ScheduleNotFound(String message) {
    super(message);
  }
}
