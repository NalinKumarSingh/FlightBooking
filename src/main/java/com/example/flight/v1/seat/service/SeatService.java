package com.example.flight.v1.seat.service;


import com.example.flight.v1.seat.entity.Seat;

import java.util.List;

public interface SeatService {
  void generateSeatsForSchedule(Long scheduleId);
  List<Seat> getAllSeatsBySchedule(Long scheduleId);
  List<Seat> getAvailableSeatsBySchedule(Long scheduleId);
  Seat getSeatById(Long id);
}
