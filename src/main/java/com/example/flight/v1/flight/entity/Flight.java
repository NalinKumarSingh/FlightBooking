package com.example.flight.v1.flight.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "flight")
public class Flight {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "FLIGHT_CODE", nullable = false)
  private String flightCode;

  @Column(name = "AIRLINE_ID", nullable = false)
  private Long airlineId;

  @Column(name = "DEPARTURE_ID", nullable = false)
  private Long departureId;

  @Column(name = "ARRIVAL_ID", nullable = false)
  private Long arrivalId;

  @Column(name = "AIRCRAFT_TYPE")
  private String aircraftType;

  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt;
}
