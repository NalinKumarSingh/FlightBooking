package com.example.flight.v1.booking.repository;

import com.example.flight.v1.booking.entity.Booking;
import com.example.flight.v1.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime time);
  List<Booking> findByUserId(Long userId);
}
