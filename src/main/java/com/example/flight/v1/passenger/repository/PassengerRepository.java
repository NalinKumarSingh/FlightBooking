package com.example.flight.v1.passenger.repository;

import com.example.flight.v1.passenger.entity.Passenger;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
  List<Passenger> findByBookingId(Long bookingId);
  @Transactional
  void deleteByBookingId(Long bookingId);
}
