package com.example.flight.v1.flightschedule.controller;


import com.example.flight.v1.flight.FlightNotFound;
import com.example.flight.v1.flightschedule.exception.ScheduleNotFound;
import com.example.flight.v1.flightschedule.model.FlightScheduleRequest;
import com.example.flight.v1.flightschedule.model.FlightScheduleResponse;
import com.example.flight.v1.flightschedule.model.FlightScheduleResponse1;
import com.example.flight.v1.flightschedule.service.FlightScheduleService;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FlightScheduleController {

  private final FlightScheduleService scheduleService;

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<FlightScheduleResponse>> createSchedule(
      @RequestBody FlightScheduleRequest request,
      @RequestHeader("Authorization") String authHeader
  ) {
    try {
      FlightScheduleResponse response = scheduleService.createSchedule(request, authHeader);
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Created Schedule", true, response));
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (UserNotFound | FlightNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  // Get a schedule by ID
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<FlightScheduleResponse>> getScheduleById(@PathVariable Long id) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Schedules get fetched", true, scheduleService.getScheduleById(id)));
    } catch (ScheduleNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  //search
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<FlightScheduleResponse1>>> searchFlights(
      @RequestParam Long from,
      @RequestParam Long to,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String sortBy,   // price , duration
      @RequestParam(required = false) Long airlineId   // airline filter
  ) {
    try {
      List<FlightScheduleResponse1> result = scheduleService.searchFlights(from, to, date, sortBy, airlineId);
      return ResponseEntity.status(HttpStatus.OK)
          .body(new ApiResponse<>("Flights fetched successfully", true, result));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }
}
