package com.example.flight.v1.flight.service.impl;

import com.example.flight.v1.flight.entity.Flight;
import com.example.flight.v1.flight.model.FlightRequest;
import com.example.flight.v1.flight.model.FlightResponse;
import com.example.flight.v1.flight.repository.FlightRepository;
import com.example.flight.v1.flight.service.FlightService;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

  private final FlightRepository flightRepository;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  @Override
  public FlightResponse createFlightWithAuth(String authHeader, FlightRequest request) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    String role = jwtUtil.extractRole(token);
    Long userId = jwtUtil.extractUserId(token);

    if (!"AIRLINE_STAFF".equals(role)) {
      throw new RuntimeException("Only airline staff can add flights.");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getAirlineId().equals(request.getAirlineId())) {
      throw new RuntimeException("You can only add flights for your assigned airline.");
    }

    List<Flight> flightList = flightRepository.findByAirlineId(request.getAirlineId());

    boolean isflightExists = false;
    for(Flight opt : flightList){
      if(opt.getFlightCode().equals(request.getFlightCode())){
        isflightExists= true;
      }
    }

    if(isflightExists){
      throw  new RuntimeException("The flight already exists");
    }

    Flight flight = new Flight();
    flight.setFlightCode(request.getFlightCode());
    flight.setAirlineId(request.getAirlineId());
    flight.setDepartureId(request.getDepartureId());
    flight.setArrivalId(request.getArrivalId());
    flight.setAircraftType(request.getAircraftType());
    flight.setCreatedAt(LocalDateTime.now());

    Flight saved = flightRepository.save(flight);
    return mapToResponse(saved);
  }

  @Override
  public FlightResponse getFlight(Long id) {
    Flight flight = flightRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Flight not found"));
    return mapToResponse(flight);
  }

  @Override
  public List<FlightResponse> getAllFlights(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    String role = jwtUtil.extractRole(token);
    Long userId = jwtUtil.extractUserId(token);
    Optional<User> optional = userRepository.findByEmail(jwtUtil.extractEmail(token));
    if(optional.isEmpty()){
      throw new RuntimeException("User doesn't exists");
    }
    User user = optional.get();
    if (!"AIRLINE_STAFF".equals(role)) {
      throw new RuntimeException("Only airline staff can see all the flights");
    }
    return flightRepository.findByAirlineId(user.getAirlineId()).stream()
        .map(this::mapToResponse)
        .toList();
  }

  private FlightResponse mapToResponse(Flight flight) {
    FlightResponse res = new FlightResponse();
    res.setId(flight.getId());
    res.setFlightCode(flight.getFlightCode());
    res.setAirlineId(flight.getAirlineId());
    res.setDepartureId(flight.getDepartureId());
    res.setArrivalId(flight.getArrivalId());
    res.setAircraftType(flight.getAircraftType());
    res.setCreatedAt(flight.getCreatedAt());
    return res;
  }
}
