package com.example.flight.v1.location.service;

import com.example.flight.v1.location.model.LocationRequest;
import com.example.flight.v1.location.model.LocationResponse;

import java.util.List;

public interface LocationService {
  LocationResponse addLocationWithAuth(String authHeader, LocationRequest request);
  List<LocationResponse> getAllLocations();
  LocationResponse getLocation(Long id);
}
