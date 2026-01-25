package com.example.flight.v1.scheduler;

import com.example.flight.v1.booking.entity.Booking;
import com.example.flight.v1.booking.enums.BookingStatus;
import com.example.flight.v1.booking.repository.BookingRepository;
import com.example.flight.v1.passenger.repository.PassengerRepository;
import com.example.flight.v1.seat.entity.Seat;
import com.example.flight.v1.seat.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatLockExpiryScheduler {

  private final BookingRepository bookingRepository;
  private final SeatRepository seatRepository;
  private final PassengerRepository passengerRepository;

  @Scheduled(fixedRate = 300000) // every 5 mins
  @Transactional
  public void expireLockedSeats() {
    List<Booking> expiredBookings = bookingRepository
        .findByStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());

    for (Booking booking : expiredBookings) {
      log.info("Expiring booking: {}", booking.getId());

      // Step 1: Free seats
      List<Long> seatIds = Arrays.stream(booking.getSeatIds().split(","))
          .map(Long::parseLong).toList();

      List<Seat> seats = seatRepository.findAllById(seatIds);
      for (Seat seat : seats) {
        seat.setIsAvailable(true);
        seat.setLockedUntil(null);
      }
      seatRepository.saveAll(seats);

      // Step 2: Remove passengers
      passengerRepository.deleteByBookingId(booking.getId());

      // Step 3: Cancel booking
      booking.setStatus(BookingStatus.CANCELLED);
      bookingRepository.save(booking);

      log.info("Booking {} cancelled and seats released", booking.getId());
    }
  }
}
