package com.example.flight.v1.airline.service;

import com.example.flight.v1.airline.model.AirlineRequest;
import com.example.flight.v1.airline.model.AirlineResponse;

import java.util.List;

public interface AirlineService {
  AirlineResponse createAirline(String token, AirlineRequest request);
  AirlineResponse getAirline(Long id);
  List<AirlineResponse> getAllAirlines();
}
