package com.example.flight.v1.seat.controller;

import com.example.flight.v1.flightschedule.exception.ScheduleNotFound;
import com.example.flight.v1.seat.entity.Seat;
import com.example.flight.v1.seat.service.SeatService;
import com.example.flight.v1.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SeatController {

  private final SeatService seatService;

  @GetMapping("/all")
  public ResponseEntity<ApiResponse<List<Seat>>> getAllSeatsBySchedule(@RequestParam Long scheduleId) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Seats fetched successfully", true, seatService.getAllSeatsBySchedule(scheduleId)));
    } catch (ScheduleNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), true, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  //  Get only available seats for a schedule
  @GetMapping("/available")
  public ResponseEntity<ApiResponse<List<Seat>>> getAvailableSeatsBySchedule(@RequestParam Long scheduleId) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Seats fetched according to schedule", true, seatService.getAvailableSeatsBySchedule(scheduleId)));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  //  Get details of a specific seat by ID
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Seat>> getSeatById(@PathVariable Long id) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Seat Fetched by ID", true, seatService.getSeatById(id)));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }
}
