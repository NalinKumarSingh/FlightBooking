package com.example.flight.v1.airline.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "airline")
public class Airline {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "CANCELLATION_POLICY")
  private String cancellationPolicy;

  @Column(name = "BAGGAGE_POLICY")
  private String baggagePolicy;

  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt;
}
