package com.example.flight.v1.ticket.entity;

import com.example.flight.v1.passenger.entity.Passenger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="ticket_new")
@Data
public class Ticket {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "USER_ID")
  private Long userId;

  @Column(name = "PASSENGER_ID")
  private Long passengerId;

  @Column(name = "FLIGHT_ID")
  private Long flightId;

  @Column(name = "SCHEDULE_ID")
  private Long scheduleId;

  @Column(name = "CREATED_ON")
  private LocalDateTime createdOn;

}
