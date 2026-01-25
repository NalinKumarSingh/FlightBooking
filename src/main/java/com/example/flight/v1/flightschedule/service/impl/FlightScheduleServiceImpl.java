package com.example.flight.v1.flightschedule.service.impl;

import com.example.flight.v1.airline.entity.Airline;
import com.example.flight.v1.airline.repository.AirlineRepository;
import com.example.flight.v1.flight.FlightNotFound;
import com.example.flight.v1.flight.entity.Flight;
import com.example.flight.v1.flight.repository.FlightRepository;
import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.enums.FlightStatus;
import com.example.flight.v1.flightschedule.exception.ScheduleNotFound;
import com.example.flight.v1.flightschedule.model.FlightScheduleRequest;
import com.example.flight.v1.flightschedule.model.FlightScheduleResponse;
import com.example.flight.v1.flightschedule.model.FlightScheduleResponse1;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import com.example.flight.v1.flightschedule.service.FlightScheduleService;
import com.example.flight.v1.location.entity.Location;
import com.example.flight.v1.location.repository.LocationRepository;
import com.example.flight.v1.seat.entity.Seat;
import com.example.flight.v1.seat.enums.SeatClass;
import com.example.flight.v1.seat.enums.SeatType;
import com.example.flight.v1.seat.repository.SeatRepository;
import com.example.flight.v1.seat.service.SeatService;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.exception.UnauthorizedException;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightScheduleServiceImpl implements FlightScheduleService {

  private final FlightScheduleRepository scheduleRepo;
  private final FlightRepository flightRepository;
  private final UserRepository userRepository; // NEW
  private final SeatService seatService;
  private final LocationRepository locationRepository;
  private final AirlineRepository airlineRepository;
  private final JwtUtil  jwtUtil;
  private final SeatRepository seatRepository;

  @Override
  public FlightScheduleResponse createSchedule(FlightScheduleRequest request, String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Authorization header is missing or invalid");
    }

    String token = authHeader.substring(7);
    Long userId = jwtUtil.extractUserId(token);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFound("User not found"));

    if (user.getRole() != Role.AIRLINE_STAFF) {
      throw new UnauthorizedException("Only airline staff can create flight schedules.");
    }

    if (user.getAirlineId() == null || !user.getAirlineId().equals(request.getAirlineId())) {
      throw new UnauthorizedException("Only airline staff of that airline can create flight schedules.");
    }

    //  Correct validation for flight existence
    List<Flight> flights = flightRepository.findByAirlineId(request.getAirlineId());
    boolean flightExists = flights.stream()
        .anyMatch(flight -> flight.getId().equals(request.getFlightId()));

    if (!flightExists) {
      throw new FlightNotFound("Flight not found for the given airline.");
    }

    //  Proceed to create the schedule
    FlightSchedule schedule = new FlightSchedule();
    schedule.setFlightId(request.getFlightId());
    schedule.setDepartureDateTime(request.getDepartureDateTime());
    schedule.setArrivalDateTime(request.getArrivalDateTime());
    schedule.setDuration(Duration.between(request.getDepartureDateTime(), request.getArrivalDateTime()));
    schedule.setStatus(FlightStatus.SCHEDULED);
    schedule.setCreatedAt(LocalDateTime.now());

    FlightSchedule saved = scheduleRepo.save(schedule);

    seatService.generateSeatsForSchedule(saved.getId());

    return mapToResponse(saved);
  }

  @Override
  public FlightScheduleResponse getScheduleById(Long id) {
    FlightSchedule s = scheduleRepo.findById(id)
        .orElseThrow(() -> new ScheduleNotFound("Schedule not found"));
    return mapToResponse(s);
  }

  @Override
  public List<FlightScheduleResponse1> searchFlights(Long from, Long to, LocalDate date, String sortBy, Long airlineId) {
    List<FlightSchedule> schedules = scheduleRepo.searchFlights(from, to, date);
    List<FlightScheduleResponse1> results = new ArrayList<>();
    for(FlightSchedule schedule : schedules){
      Flight flight = flightRepository.findById(schedule.getFlightId()).orElse(null);
      if(flight == null){
        continue;
      }

      if (airlineId != null && !airlineId.equals(flight.getAirlineId())) {
        continue;
      }

      //For airline name
      Airline airline = airlineRepository.findById(flight.getAirlineId()).orElse(null);
      //Get Seats
      List<Seat> seats = seatRepository.findByScheduleId(schedule.getId());
      BigDecimal basePrice = seats.stream().filter(seat->seat.getSeatClass()== SeatClass.ECONOMY)
          .map(Seat::getBasePrice)
          .min(BigDecimal::compareTo)
          .orElse(BigDecimal.ZERO);

      //Creating Response
      FlightScheduleResponse1 dto = mapToEnhancedResponse(schedule,basePrice);
      results.add(dto);
    }
    results.sort(getComparator(sortBy));
    return results;
  }

  private Comparator<FlightScheduleResponse1> getComparator(String sortBy){
    if("price".equalsIgnoreCase(sortBy)){
      return Comparator.comparing(FlightScheduleResponse1::getBasePrice);
    }else if("duration".equalsIgnoreCase(sortBy)){
      return Comparator.comparing(FlightScheduleResponse1::getDuration);
    }
    return Comparator.comparing(FlightScheduleResponse1::getScheduleId);
  }

  private FlightScheduleResponse1 mapToEnhancedResponse(FlightSchedule s, BigDecimal basePrice) {
    FlightScheduleResponse1 res = new FlightScheduleResponse1();
    res.setFlightId(s.getFlightId());
    res.setDepartureDateTime(s.getDepartureDateTime());
    res.setArrivalDateTime(s.getArrivalDateTime());
    res.setDuration(s.getDuration());
    res.setStatus(s.getStatus());
    res.setScheduleId(s.getId());

    // Fetch flight
    Flight flight = flightRepository.findById(s.getFlightId())
        .orElseThrow(() -> new RuntimeException("Flight not found"));

    // Fetch airline
    String airlineName = airlineRepository.findById(flight.getAirlineId())
        .map(Airline::getName)
        .orElse("Unknown Airline");

    // Fetch locations
    String fromCity = locationRepository.findById(flight.getDepartureId())
        .map(Location::getCity)
        .orElse("Unknown");
    String toCity = locationRepository.findById(flight.getArrivalId())
        .map(Location::getCity)
        .orElse("Unknown");

    res.setFlightCode(flight.getFlightCode());
    res.setAirlineName(airlineName);
    res.setFromCity(fromCity);
    res.setToCity(toCity);
    res.setBasePrice(basePrice);
    return res;
  }


  private FlightScheduleResponse mapToResponse(FlightSchedule s) {
    FlightScheduleResponse res = new FlightScheduleResponse();
    res.setId(s.getId());
    res.setFlightId(s.getFlightId());
    res.setDepartureDateTime(s.getDepartureDateTime());
    res.setArrivalDateTime(s.getArrivalDateTime());
    res.setDuration(s.getDuration());
    res.setStatus(s.getStatus());
    res.setCreatedAt(s.getCreatedAt());
    return res;
  }
}
