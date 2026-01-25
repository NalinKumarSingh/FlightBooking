package com.example.flight.v1.ticket.model;

import com.example.flight.v1.passenger.entity.Passenger;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TicketResponse {

  private Long id;

  private Long userId;

  private Long passengerId;

  private String name;

  private String seatNumber;

  private Integer age;

  private String gender;

  private Long flightId;

  private Long scheduleId;

  private LocalDateTime departureTime;

  private LocalDateTime arrivalTime;

  private String departureAirport;

  private String arrivalAirport;

  private LocalDate travelDate;

  private LocalDateTime createdOn;
}
