package com.example.flight.v1.flight.controller;

import com.example.flight.v1.flight.model.FlightRequest;
import com.example.flight.v1.flight.model.FlightResponse;
import com.example.flight.v1.flight.service.FlightService;
import com.example.flight.v1.utils.ApiResponse;
import com.example.flight.v1.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FlightController {

  private final FlightService flightService;
  private final JwtUtil jwtUtil;

  @PostMapping("/add")
  public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
      @RequestHeader("Authorization") String authHeader,
      @RequestBody FlightRequest request
  ) {
    try {
      FlightResponse response = flightService.createFlightWithAuth(authHeader, request);
      return ResponseEntity.ok(new ApiResponse<>("Flight created successfully", true, response));

    } catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Token expired", false, null));
    } catch (JwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Invalid token", false, null));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>("Internal server error", false, null));
    }
  }

  // Get all flights
  @GetMapping
  public ResponseEntity<ApiResponse<List<FlightResponse>>>getAllFlights(@RequestHeader("Authorization") String authHeader) {
//    return ResponseEntity.ok(flightService.getAllFlights());
    try{
      List<FlightResponse> responseList = flightService.getAllFlights(authHeader);
      return ResponseEntity.ok(new ApiResponse<>("Flight list fetching successful", true,responseList));
    }catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Token expired", false, null));
    } catch (JwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Invalid token", false, null));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>("Internal server error", false, null));
    }
  }

  // Get single flight
  @GetMapping("/{id}")
  public ResponseEntity<FlightResponse> getFlight(@PathVariable Long id) {
    return ResponseEntity.ok(flightService.getFlight(id));
  }
}
