package com.example.flight.v1.flightschedule.entity;

import com.example.flight.v1.flightschedule.enums.FlightStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_schedule")
@Data
public class FlightSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "FLIGHT_ID")
  private Long flightId;

  @Column(name = "DEPARTURE_DATE_TIME")
  private LocalDateTime departureDateTime;

  @Column(name = "ARRIVAL_DATE_TIME")
  private LocalDateTime arrivalDateTime;

  @Column(name = "DURATION")
  private Duration duration;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS")
  private FlightStatus status = FlightStatus.SCHEDULED;

  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt;
}

