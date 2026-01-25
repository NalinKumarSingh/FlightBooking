package com.example.flight.v1.location.controller;

import com.example.flight.v1.location.model.LocationRequest;
import com.example.flight.v1.location.model.LocationResponse;
import com.example.flight.v1.location.service.LocationService;
import com.example.flight.v1.utils.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LocationController {

  private final LocationService locationService;

  @PostMapping("/add")
  public ResponseEntity<ApiResponse<LocationResponse>> addLocation(
      @RequestHeader("Authorization") String authHeader,
      @RequestBody LocationRequest request
  ) {
    try {
      LocationResponse response = locationService.addLocationWithAuth(authHeader, request);
      return ResponseEntity.ok(new ApiResponse<>("Location added successfully", true, response));

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


  @GetMapping
  public ResponseEntity<ApiResponse<List<LocationResponse>>> getAllLocations() {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("locations fetched successfully", true, locationService.getAllLocations()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LocationResponse>> getLocation(@PathVariable Long id) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Location fetched successfully", true, locationService.getLocation(id)));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }
}
