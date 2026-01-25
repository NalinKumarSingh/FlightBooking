package com.example.flight.v1.ticket.service.impl;

import com.example.flight.v1.booking.entity.Booking;
import com.example.flight.v1.booking.enums.BookingStatus;
import com.example.flight.v1.booking.repository.BookingRepository;
import com.example.flight.v1.flight.repository.FlightRepository;
import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import com.example.flight.v1.location.repository.LocationRepository;
import com.example.flight.v1.passenger.entity.Passenger;
import com.example.flight.v1.passenger.repository.PassengerRepository;
import com.example.flight.v1.payment.repository.PaymentRepository;
import com.example.flight.v1.ticket.model.TicketResponse;
import com.example.flight.v1.ticket.repository.TicketRepository;
import com.example.flight.v1.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

  private final PaymentRepository paymentRepository;
  private final BookingRepository bookingRepository;
  private final PassengerRepository passengerRepository;
  private final FlightRepository flightRepository;
  private final TicketRepository ticketRepository;
  private final FlightScheduleRepository flightScheduleRepository;
  private final LocationRepository locationRepository;

  //Get All Tickets Booked By A User

  @Override
  public List<TicketResponse> getTicketsByUserId(Long userId) {
    List<TicketResponse> tickets = new ArrayList<>();

    List<Booking> bookings = bookingRepository.findByUserId(userId).stream()
        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
        .toList();

    for (Booking booking : bookings) {
      List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
      List<String> seatNumbers = Arrays.asList(booking.getSeatIds().split(","));
      FlightSchedule schedule = flightScheduleRepository.findById(booking.getScheduleId())
          .orElseThrow(() -> new RuntimeException("Schedule Not Found"));
      Long flightId = schedule.getFlightId();

      String source = flightRepository.findById(flightId)
          .flatMap(flight -> locationRepository.findById(flight.getDepartureId()))
          .map(loc -> loc.getCity()).orElse("Unknown");

      String destination = flightRepository.findById(flightId)
          .flatMap(flight -> locationRepository.findById(flight.getArrivalId()))
          .map(loc -> loc.getCity()).orElse("Unknown");

      for (int i = 0; i < passengers.size(); i++) {
        Passenger p = passengers.get(i);

        String seatNo = (i < seatNumbers.size()) ? seatNumbers.get(i) : "NA";

        TicketResponse ticket = new TicketResponse();
        ticket.setPassengerId(p.getId());
        ticket.setUserId(userId);
        ticket.setSeatNumber(seatNo);
        ticket.setName(p.getName());
        ticket.setAge(p.getAge());
        ticket.setGender(p.getGender());
        ticket.setDepartureTime(schedule.getDepartureDateTime());
        ticket.setArrivalTime(schedule.getArrivalDateTime());
        ticket.setTravelDate(schedule.getDepartureDateTime().toLocalDate());
        ticket.setArrivalAirport(destination);
        ticket.setDepartureAirport(source);
        ticket.setCreatedOn(booking.getBookedAt());
        ticket.setFlightId(schedule.getFlightId());
        ticket.setScheduleId(schedule.getId());

        tickets.add(ticket);
      }
    }

    return tickets;
  }

  //Get All Upcoming Tickets For A User
  @Override
  public List<TicketResponse> getTicketsByUserIdFuture(Long userId) {
    List<TicketResponse> tickets = new ArrayList<>();

    List<Booking> bookings = bookingRepository.findByUserId(userId).stream()
        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
        .toList();

    LocalDate today = LocalDate.now();

    for (Booking booking : bookings) {
      FlightSchedule schedule = flightScheduleRepository.findById(booking.getScheduleId())
          .orElseThrow(() -> new RuntimeException("Schedule Not Found"));

      // Only proceed if travel date is today or in future
      if (!schedule.getDepartureDateTime().toLocalDate().isBefore(today)) {

        List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
        List<String> seatNumbers = Arrays.asList(booking.getSeatIds().split(","));
        Long flightId = schedule.getFlightId();

        String source = flightRepository.findById(flightId)
            .flatMap(flight -> locationRepository.findById(flight.getDepartureId()))
            .map(loc -> loc.getCity()).orElse("Unknown");

        String destination = flightRepository.findById(flightId)
            .flatMap(flight -> locationRepository.findById(flight.getArrivalId()))
            .map(loc -> loc.getCity()).orElse("Unknown");

        for (int i = 0; i < passengers.size(); i++) {
          Passenger p = passengers.get(i);
          String seatNo = (i < seatNumbers.size()) ? seatNumbers.get(i) : "NA";

          TicketResponse ticket = new TicketResponse();
          ticket.setPassengerId(p.getId());
          ticket.setUserId(userId);
          ticket.setSeatNumber(seatNo);
          ticket.setName(p.getName());
          ticket.setAge(p.getAge());
          ticket.setGender(p.getGender());
          ticket.setDepartureTime(schedule.getDepartureDateTime());
          ticket.setArrivalTime(schedule.getArrivalDateTime());
          ticket.setTravelDate(schedule.getDepartureDateTime().toLocalDate());
          ticket.setArrivalAirport(destination);
          ticket.setDepartureAirport(source);
          ticket.setCreatedOn(booking.getBookedAt());
          ticket.setFlightId(schedule.getFlightId());
          ticket.setScheduleId(schedule.getId());

          tickets.add(ticket);
        }
      }
    }

    return tickets;
  }
  @Override
  public List<TicketResponse> getTicketsByUserIdPast(Long userId){
    List<TicketResponse> tickets = new ArrayList<>();

    List<Booking> bookings = bookingRepository.findByUserId(userId).stream()
        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
        .toList();

    LocalDate today = LocalDate.now();

    for (Booking booking : bookings) {
      FlightSchedule schedule = flightScheduleRepository.findById(booking.getScheduleId())
          .orElseThrow(() -> new RuntimeException("Schedule Not Found"));

      // Only proceed if travel date is in past
      if (!schedule.getDepartureDateTime().toLocalDate().isAfter(today)) {

        List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
        List<String> seatNumbers = Arrays.asList(booking.getSeatIds().split(","));
        Long flightId = schedule.getFlightId();

        String source = flightRepository.findById(flightId)
            .flatMap(flight -> locationRepository.findById(flight.getDepartureId()))
            .map(loc -> loc.getCity()).orElse("Unknown");

        String destination = flightRepository.findById(flightId)
            .flatMap(flight -> locationRepository.findById(flight.getArrivalId()))
            .map(loc -> loc.getCity()).orElse("Unknown");

        for (int i = 0; i < passengers.size(); i++) {
          Passenger p = passengers.get(i);
          String seatNo = (i < seatNumbers.size()) ? seatNumbers.get(i) : "NA";

          TicketResponse ticket = new TicketResponse();
          ticket.setPassengerId(p.getId());
          ticket.setUserId(userId);
          ticket.setSeatNumber(seatNo);
          ticket.setName(p.getName());
          ticket.setAge(p.getAge());
          ticket.setGender(p.getGender());
          ticket.setDepartureTime(schedule.getDepartureDateTime());
          ticket.setArrivalTime(schedule.getArrivalDateTime());
          ticket.setTravelDate(schedule.getDepartureDateTime().toLocalDate());
          ticket.setArrivalAirport(destination);
          ticket.setDepartureAirport(source);
          ticket.setCreatedOn(booking.getBookedAt());
          ticket.setFlightId(schedule.getFlightId());
          ticket.setScheduleId(schedule.getId());

          tickets.add(ticket);
        }
      }
    }
    return tickets;
  }
}
