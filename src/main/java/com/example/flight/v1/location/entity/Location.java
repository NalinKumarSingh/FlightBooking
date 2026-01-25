package com.example.flight.v1.location.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "location")
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "CODE", nullable = false, unique = true)
  private String code; // e.g., "DEL", "LKO"

  @Column(name = "NAME", nullable = false)
  private String name; // Full airport/station name

  @Column(name = "CITY", nullable = false)
  private String city;

  @Column(name = "COUNTRY", nullable = false)
  private String country;
}
