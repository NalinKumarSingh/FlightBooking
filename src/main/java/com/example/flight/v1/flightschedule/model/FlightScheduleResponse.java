package com.example.flight.v1.flightschedule.model;

import com.example.flight.v1.flightschedule.enums.FlightStatus;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class FlightScheduleResponse {
  private Long id;
  private Long flightId;
  private LocalDateTime departureDateTime;
  private LocalDateTime arrivalDateTime;
  private Duration duration;
  private FlightStatus status;
  private LocalDateTime createdAt;
}
