package com.example.flight.v1.booking.controller;

import com.example.flight.v1.booking.model.BookingCancelResponse;
import com.example.flight.v1.booking.model.BookingRequest;
import com.example.flight.v1.booking.model.BookingResponse;
import com.example.flight.v1.booking.service.BookingService;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.utils.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BookingController {

  private final BookingService bookingService;

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
      @RequestHeader("Authorization") String authHeader,
      @RequestBody BookingRequest request
  ) {
    try {
      BookingResponse response = bookingService.createBookingWithJwt(authHeader, request);
      return ResponseEntity.ok(new ApiResponse<>("Booking created", true, response));
    } catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Token expired", false, null));
    } catch (JwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Invalid token", false, null));
    } catch (UserNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("Unauthorized", false, null));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>("Internal server error", false, null));
    }
  }

  @GetMapping("/my-bookings")
  public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
      @RequestHeader(value = "Authorization", required = false) String authHeader
  ) {
    try {
      List<BookingResponse> response = bookingService.getBookingsByUser(authHeader);
      return ResponseEntity.ok(new ApiResponse<>("Bookings fetched successfully", true, response));
    } catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Token expired", false, null));
    } catch (JwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Invalid token", false, null));
    } catch (UserNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Unauthorized", false, null));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>("Internal server error", false, null));
    }
  }

  @PostMapping("/cancel/{id}")
  public ResponseEntity<ApiResponse<BookingCancelResponse>> cancelBooking(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = false) String authHeader
  ) {
    try {
      BookingCancelResponse response = bookingService.cancelBooking(id, authHeader);
      return ResponseEntity.ok(new ApiResponse<>("Booking cancelled successfully", true, response));
    } catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Token expired", false, null));
    } catch (JwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>("Invalid token", false, null));
    } catch (UnauthorizedException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>("Internal server error", false, null));
    }
  }
}
