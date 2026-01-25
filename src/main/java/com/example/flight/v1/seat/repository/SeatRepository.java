package com.example.flight.v1.seat.repository;

import com.example.flight.v1.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
  List<Seat> findByScheduleIdAndIsAvailableTrue(Long scheduleId);
  List<Seat> findByLockedUntilBefore(LocalDateTime time);
  List<Seat> findByScheduleId(Long scheduleId);
}
