package com.example.flight.v1.flight.model;

import lombok.Data;

@Data
public class FlightRequest {
  private String flightCode;
  private Long airlineId;
  private Long departureId;
  private Long arrivalId;
  private String aircraftType;
}

