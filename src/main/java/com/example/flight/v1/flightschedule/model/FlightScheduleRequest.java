package com.example.flight.v1.flightschedule.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightScheduleRequest {
  private Long userId; // role check
  private Long airlineId;
  private Long flightId;
  private LocalDateTime departureDateTime;
  private LocalDateTime arrivalDateTime;
}
