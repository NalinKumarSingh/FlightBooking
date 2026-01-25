package com.example.flight.v1.airline.controller;

import com.example.flight.v1.airline.exceptions.AirlineNotFound;
import com.example.flight.v1.airline.model.AirlineRequest;
import com.example.flight.v1.airline.model.AirlineResponse;
import com.example.flight.v1.airline.service.AirlineService;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airlines")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AirlineController {

  private final AirlineService airlineService;

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<AirlineResponse>> createAirline(@RequestHeader("Authorization") String authHeader, @RequestBody AirlineRequest request) {
    try {
      String token = authHeader.replace("Bearer ", "");
      AirlineResponse response = airlineService.createAirline(token, request);
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Airline created successfully", true, response));
    } catch (UnauthorizedException e) {
      return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (UserNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  // GET: Get all airlines
  @GetMapping
  public ResponseEntity<ApiResponse<List<AirlineResponse>>> getAllAirlines() {
    try {
      List<AirlineResponse> list = airlineService.getAllAirlines();
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Airline list successfully", true, list));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  // GET: Get airline by ID
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<AirlineResponse>> getAirlineById(@PathVariable Long id) {
    try {
      AirlineResponse response = airlineService.getAirline(id);
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Airline fetched successfully", true, response));
    } catch (AirlineNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }
}
