package com.example.flight.v1.airline.model;

import lombok.Data;

@Data
public class AirlineRequest {
  private String name;
  private String cancellationPolicy;
  private String baggagePolicy;
}
