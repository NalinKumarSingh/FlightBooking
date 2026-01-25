package com.example.flight.v1.airline.service.impl;

import com.example.flight.v1.airline.entity.Airline;
import com.example.flight.v1.airline.exceptions.AirlineNotFound;
import com.example.flight.v1.airline.model.AirlineRequest;
import com.example.flight.v1.airline.model.AirlineResponse;
import com.example.flight.v1.airline.repository.AirlineRepository;
import com.example.flight.v1.airline.service.AirlineService;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AirlineServiceImpl implements AirlineService {

  private final AirlineRepository airlineRepository;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  @Override
  public AirlineResponse createAirline(String token, AirlineRequest request) {
    // Extract user info from token
    Long userId = jwtUtil.extractUserId(token);
    String role = jwtUtil.extractRole(token);

    if (!"ADMIN".equals(role)) {
      throw new UnauthorizedException("Only ADMIN users can create airlines.");
    }

    // Validate user existence
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFound("User not found"));

    // Check if airline with the same name already exists
    if (airlineRepository.existsByNameIgnoreCase(request.getName())) {
      throw new IllegalArgumentException("An airline with the same name already exists.");
    }

    // Create and save new airline
    Airline airline = new Airline();
    airline.setName(request.getName());
    airline.setCancellationPolicy(request.getCancellationPolicy());
    airline.setBaggagePolicy(request.getBaggagePolicy());
    airline.setCreatedAt(LocalDateTime.now());

    Airline saved = airlineRepository.save(airline);
    return mapToResponse(saved);
  }

  @Override
  public AirlineResponse getAirline(Long id) {
    Airline airline = airlineRepository.findById(id)
        .orElseThrow(() -> new AirlineNotFound("Airline not found"));
    return mapToResponse(airline);
  }

  @Override
  public List<AirlineResponse> getAllAirlines() {
    return airlineRepository.findAll().stream()
        .map(this::mapToResponse)
        .toList();
  }

  private AirlineResponse mapToResponse(Airline airline) {
    AirlineResponse res = new AirlineResponse();
    res.setId(airline.getId());
    res.setName(airline.getName());
    res.setCancellationPolicy(airline.getCancellationPolicy());
    res.setBaggagePolicy(airline.getBaggagePolicy());
    res.setCreatedAt(airline.getCreatedAt());
    return res;
  }
}

