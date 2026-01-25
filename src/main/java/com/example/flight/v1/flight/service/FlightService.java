package com.example.flight.v1.flight.service;

import com.example.flight.v1.flight.model.FlightRequest;
import com.example.flight.v1.flight.model.FlightResponse;

import java.util.List;

public interface FlightService {
  FlightResponse createFlightWithAuth(String authHeader, FlightRequest request);
  FlightResponse getFlight(Long id);
  List<FlightResponse> getAllFlights(String authHeader);
}
