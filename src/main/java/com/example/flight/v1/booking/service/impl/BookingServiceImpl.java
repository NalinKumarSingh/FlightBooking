package com.example.flight.v1.booking.service.impl;

import com.example.flight.v1.airline.repository.AirlineRepository;
import com.example.flight.v1.booking.entity.Booking;
import com.example.flight.v1.booking.enums.BookingStatus;
import com.example.flight.v1.booking.model.BookingCancelResponse;
import com.example.flight.v1.booking.model.BookingRequest;
import com.example.flight.v1.booking.model.BookingResponse;
import com.example.flight.v1.booking.repository.BookingRepository;
import com.example.flight.v1.booking.service.BookingService;
import com.example.flight.v1.flight.repository.FlightRepository;
import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.enums.FlightStatus;
import com.example.flight.v1.flightschedule.exception.ScheduleNotFound;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import com.example.flight.v1.passenger.entity.Passenger;
import com.example.flight.v1.passenger.repository.PassengerRepository;
import com.example.flight.v1.policy.util.CancellationPolicyParser;
import com.example.flight.v1.seat.entity.Seat;
import com.example.flight.v1.seat.repository.SeatRepository;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;
  private final FlightScheduleRepository scheduleRepository;
  private final SeatRepository seatRepository;
  private final PassengerRepository passengerRepository;
  private final FlightRepository flightRepository;
  private final AirlineRepository airlineRepository;
  private final CancellationPolicyParser cancellationPolicyParser;
  private final JwtUtil jwtUtil;
  private final RedissonClient redissonClient;



  @Override
  public BookingResponse createBookingWithJwt(String authHeader, BookingRequest request) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    Long userId = jwtUtil.extractUserId(token);

    // Validate user
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFound("User not found"));

    // Validate schedule
    FlightSchedule schedule = scheduleRepository.findById(request.getScheduleId())
        .orElseThrow(() -> new ScheduleNotFound("Schedule not found"));

    if (schedule.getStatus() != FlightStatus.SCHEDULED) {
      throw new RuntimeException("Flight Departed");
    }

    List<Long> seatIds = request.getSeatIds().stream().sorted().toList();
    List<RLock> locks = new ArrayList<>();

    try {
      //Acquire Redis Locks
      for (Long seatId : seatIds) {
        RLock lock = redissonClient.getLock("lock:seat:" + seatId);
        boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS); // wait 5s, hold for 10s
        if (!acquired) {
          throw new RuntimeException("Could not acquire lock for seat " + seatId);
        }
        locks.add(lock);
      }

      //Fetching seats after locking
      List<Seat> seats = seatRepository.findAllById(seatIds);
      if (seats.size() != seatIds.size()) {
        throw new RuntimeException("One or more seats not found");
      }

      for (Seat seat : seats) {
        if (!seat.getIsAvailable() ||
            (seat.getLockedUntil() != null && seat.getLockedUntil().isAfter(LocalDateTime.now()))) {
          throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
        }
      }

      //Calculating total price
      BigDecimal totalPrice = seats.stream()
          .map(seat -> seat.getBasePrice().add(seat.getPremiumFee() != null ? seat.getPremiumFee() : BigDecimal.ZERO))
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      //Locking seats in DB
      for (Seat seat : seats) {
        seat.setIsAvailable(false);
        seat.setLockedUntil(LocalDateTime.now().plusMinutes(10));
      }
      seatRepository.saveAll(seats);

      //Saving booking
      Booking booking = new Booking();
      booking.setUserId(userId);
      booking.setScheduleId(schedule.getId());
      booking.setSeatIds(request.getSeatIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
      booking.setTotalPrice(totalPrice);
      booking.setPriceLocked(totalPrice);
      booking.setStatus(BookingStatus.PENDING);
      booking.setBookedAt(LocalDateTime.now());
      booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));

      Booking saved = bookingRepository.save(booking);

      //Saving passengers
      if (request.getPassengers() != null && request.getPassengers().size() == seats.size()) {
        List<Passenger> passengerEntities = new ArrayList<>();
        for (int i = 0; i < request.getPassengers().size(); i++) {
          BookingRequest.PassengerInfo info = request.getPassengers().get(i);
          Seat seat = seats.get(i);

          Passenger p = new Passenger();
          p.setBookingId(saved.getId());
          p.setName(info.getName());
          p.setAge(info.getAge());
          p.setGender(info.getGender());
          p.setSeatNumber(seat.getSeatNumber());

          passengerEntities.add(p);
        }
        passengerRepository.saveAll(passengerEntities);
      }

      //Preparing response
      BookingResponse response = new BookingResponse();
      response.setBookingId(saved.getId());
      response.setStatus(saved.getStatus());
      response.setBookedAt(saved.getBookedAt());
      response.setExpiresAt(saved.getExpiresAt());
      response.setTotalPrice(saved.getTotalPrice());
      response.setSeatNumbers(seats.stream().map(Seat::getSeatNumber).toList());

      if (request.getPassengers() != null) {
        List<BookingResponse.PassengerDTO> passengerDTOS = new ArrayList<>();
        for (int i = 0; i < request.getPassengers().size(); i++) {
          BookingRequest.PassengerInfo info = request.getPassengers().get(i);
          BookingResponse.PassengerDTO dto = new BookingResponse.PassengerDTO();
          dto.setName(info.getName());
          dto.setAge(info.getAge());
          dto.setGender(info.getGender());
          dto.setSeatNumber(seats.get(i).getSeatNumber());
          passengerDTOS.add(dto);
        }
        response.setPassengers(passengerDTOS);
      }

      return response;

    } catch (InterruptedException e) {
      throw new RuntimeException("Thread interrupted during seat lock", e);
    } finally {
      // releasing locks
      for (RLock lock : locks) {
        if (lock.isHeldByCurrentThread()) {
          lock.unlock();
        }
      }
    }
  }


  @Override
  public List<BookingResponse> getBookingsByUser(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);

    Long userId = jwtUtil.extractUserId(token);

    // Validate user existence
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFound("User not found"));

    List<Booking> bookings = bookingRepository.findByUserId(userId);

    return bookings.stream().map(booking -> {
      BookingResponse response = new BookingResponse();
      response.setBookingId(booking.getId());
      response.setStatus(booking.getStatus());
      response.setBookedAt(booking.getBookedAt());
      response.setExpiresAt(booking.getExpiresAt());
      response.setTotalPrice(booking.getTotalPrice());

      List<String> seatNumbers = Arrays.asList(booking.getSeatIds().split(","));
      response.setSeatNumbers(seatNumbers);

      List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
      List<BookingResponse.PassengerDTO> passengerDTOs = passengers.stream().map(p -> {
        BookingResponse.PassengerDTO dto = new BookingResponse.PassengerDTO();
        dto.setName(p.getName());
        dto.setAge(p.getAge());
        dto.setGender(p.getGender());
        dto.setSeatNumber(p.getSeatNumber());
        return dto;
      }).toList();

      response.setPassengers(passengerDTOs);
      return response;
    }).toList();
  }

  //some mistake in amount calculation
  @Transactional
  @Override
  public BookingCancelResponse cancelBooking(Long id, String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header");
    }

    String token = authHeader.substring(7);
    Long userId = jwtUtil.extractUserId(token);

    Booking booking = bookingRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Booking not found"));

    if (!booking.getUserId().equals(userId)) {
      throw new UnauthorizedException("You are not authorized to cancel this booking");
    }

    if (booking.getStatus() == BookingStatus.CANCELLED) {
      throw new RuntimeException("Booking already cancelled");
    }

    if(booking.getStatus()==BookingStatus.PENDING){
      throw new RuntimeException("Booking pending");
    }

    FlightSchedule schedule = scheduleRepository.findById(booking.getScheduleId())
        .orElseThrow(() -> new RuntimeException("Schedule not found"));

    Long flightId = schedule.getFlightId();
    Long airlineId = flightRepository.findById(flightId)
        .orElseThrow(() -> new RuntimeException("Flight not found"))
        .getAirlineId();

    String cancellationPolicy = airlineRepository.findById(airlineId)
        .orElseThrow(() -> new RuntimeException("Airline not found"))
        .getCancellationPolicy();

    long hoursBeforeFlight = Duration.between(LocalDateTime.now(), schedule.getDepartureDateTime()).toHours();

    int refundPercent = CancellationPolicyParser.getRefundPercentage(cancellationPolicy, hoursBeforeFlight);

    BigDecimal refundAmount = booking.getTotalPrice()
        .multiply(BigDecimal.valueOf(refundPercent))
        .divide(BigDecimal.valueOf(100));

    booking.setStatus(BookingStatus.CANCELLED);
    bookingRepository.save(booking);

    List<Long> seatIds = Arrays.stream(booking.getSeatIds().split(","))
        .map(Long::parseLong).toList();

    List<Seat> seats = seatRepository.findAllById(seatIds);
    for (Seat seat : seats) {
      seat.setIsAvailable(true);
      seat.setLockedUntil(null);
    }
    seatRepository.saveAll(seats);

    BookingCancelResponse response = new BookingCancelResponse();
    response.setBookingId(booking.getId());
    response.setStatus("CANCELLED");
    response.setRefundAmount(refundAmount);
    response.setCancelledAt(LocalDateTime.now());
    response.setMessage("Booking cancelled successfully with " + refundPercent + "% refund.");

    return response;
  }

}
