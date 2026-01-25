package com.example.flight.v1.flight.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightResponse {
  private Long id;
  private String flightCode;
  private Long airlineId;
  private Long departureId;
  private Long arrivalId;
  private String aircraftType;
  private LocalDateTime createdAt;
}
