package com.example.flight.v1.airline.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AirlineResponse {
  private Long id;
  private String name;
  private String cancellationPolicy;
  private String baggagePolicy;
  private LocalDateTime createdAt;
}
