package com.example.flight.v1.booking.service;

import com.example.flight.v1.booking.model.BookingCancelResponse;
import com.example.flight.v1.booking.model.BookingRequest;
import com.example.flight.v1.booking.model.BookingResponse;

import java.util.List;

public interface BookingService {
  BookingResponse createBookingWithJwt(String authHeader, BookingRequest request);
  List<BookingResponse> getBookingsByUser(String authHeader);
  BookingCancelResponse cancelBooking(Long id, String authHeader);
}
