package com.example.flight.v1.airline.repository;

import com.example.flight.v1.airline.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
  Boolean existsByNameIgnoreCase(String name);
}
