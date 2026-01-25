package com.example.flight.v1.location.service.impl;

import com.example.flight.v1.location.entity.Location;
import com.example.flight.v1.location.model.LocationRequest;
import com.example.flight.v1.location.model.LocationResponse;
import com.example.flight.v1.location.repository.LocationRepository;
import com.example.flight.v1.location.service.LocationService;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  @Override
  public LocationResponse addLocationWithAuth(String authHeader, LocationRequest request) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    String role = jwtUtil.extractRole(token);
    Long userId = jwtUtil.extractUserId(token);

    if (!"AIRLINE_STAFF".equals(role)) {
      throw new RuntimeException("Only airline staff can add locations.");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    locationRepository.findByCode(request.getCode().toUpperCase())
        .ifPresent(existing -> {
          throw new RuntimeException("Location with code already exists: " + request.getCode());
        });

    Location location = new Location();
    location.setCode(request.getCode().toUpperCase());
    location.setName(request.getName());
    location.setCity(request.getCity());
    location.setCountry(request.getCountry());

    Location saved = locationRepository.save(location);
    return mapToResponse(saved);
  }

  @Override
  public List<LocationResponse> getAllLocations() {
    return locationRepository.findAll().stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public LocationResponse getLocation(Long id) {
    Location loc = locationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Location not found"));
    return mapToResponse(loc);
  }

  private LocationResponse mapToResponse(Location location) {
    LocationResponse res = new LocationResponse();
    res.setId(location.getId());
    res.setCode(location.getCode());
    res.setName(location.getName());
    res.setCity(location.getCity());
    res.setCountry(location.getCountry());
    return res;
  }
}