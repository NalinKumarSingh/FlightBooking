package com.example.flight.v1.flight.repository;

import com.example.flight.v1.flight.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
  List<Flight> findByAirlineId(Long airlineId);
}

