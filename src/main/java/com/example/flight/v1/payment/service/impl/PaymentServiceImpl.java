package com.example.flight.v1.payment.service.impl;

import com.example.flight.v1.booking.entity.Booking;
import com.example.flight.v1.booking.enums.BookingStatus;
import com.example.flight.v1.booking.exception.BookingNotFound;
import com.example.flight.v1.booking.repository.BookingRepository;
import com.example.flight.v1.flight.repository.FlightRepository;
import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import com.example.flight.v1.location.repository.LocationRepository;
import com.example.flight.v1.passenger.entity.Passenger;
import com.example.flight.v1.passenger.repository.PassengerRepository;
import com.example.flight.v1.payment.entity.Payment;
import com.example.flight.v1.payment.enums.PaymentStatus;
import com.example.flight.v1.payment.enums.PaymentMethod;
import com.example.flight.v1.payment.exception.PaymentNotFound;
import com.example.flight.v1.payment.model.PaymentRequest;
import com.example.flight.v1.payment.model.PaymentResponse;
import com.example.flight.v1.payment.repository.PaymentRepository;
import com.example.flight.v1.payment.service.PaymentService;
import com.example.flight.v1.ticket.entity.Ticket;
import com.example.flight.v1.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final BookingRepository bookingRepository;
  private final PassengerRepository passengerRepository;
  private final FlightRepository flightRepository;
  private final TicketRepository ticketRepository;
  private final FlightScheduleRepository flightScheduleRepository;
  private final LocationRepository locationRepository;

  @Override
  public PaymentResponse makePayment(PaymentRequest request) {
    // Validate booking
    Booking booking = bookingRepository.findById(request.getBookingId())
        .orElseThrow(() -> new BookingNotFound("Booking not found"));

    // Save payment
    Payment payment = new Payment();
    payment.setBookingId(request.getBookingId());
    payment.setAmount(request.getAmount());
    payment.setTransactionId(request.getTransactionId());
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setStatus(PaymentStatus.SUCCESS); // For simplicity
    payment.setPaymentTime(LocalDateTime.now());

    paymentRepository.save(payment);

    // Updating booking status to CONFIRMED
    booking.setStatus(BookingStatus.CONFIRMED);
    bookingRepository.save(booking);

    //Generating Ticket
    List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
    FlightSchedule schedule = flightScheduleRepository.findById(booking.getScheduleId()).orElseThrow(()->new RuntimeException("Schedule Not Found"));

    for(int i = 0; i<passengers.size(); i++){
      Passenger p = passengers.get(i);
      Ticket ticket = new Ticket();
      ticket.setPassengerId(p.getId());
      ticket.setUserId(booking.getUserId());
      ticket.setCreatedOn(LocalDateTime.now());
      ticket.setFlightId(schedule.getFlightId());
      ticket.setScheduleId(schedule.getId());
      ticketRepository.save(ticket);
    }

    // Building response
    PaymentResponse response = new PaymentResponse();
    response.setPaymentId(payment.getId());
    response.setBookingId(payment.getBookingId());
    response.setAmount(payment.getAmount());
    response.setTransactionId(payment.getTransactionId());
    response.setPaymentMethod(payment.getPaymentMethod());
    response.setStatus(payment.getStatus());
    response.setPaymentTime(payment.getPaymentTime());

    return response;
  }

  @Override
  public PaymentResponse getPaymentById(Long id) {
    Payment payment = paymentRepository.findById(id)
        .orElseThrow(() -> new PaymentNotFound("Payment not found"));

    PaymentResponse response = new PaymentResponse();
    response.setPaymentId(payment.getId());
    response.setBookingId(payment.getBookingId());
    response.setAmount(payment.getAmount());
    response.setTransactionId(payment.getTransactionId());
    response.setPaymentMethod(payment.getPaymentMethod());
    response.setStatus(payment.getStatus());
    response.setPaymentTime(payment.getPaymentTime());

    return response;
  }
}
