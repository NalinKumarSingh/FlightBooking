package com.example.flight.v1.passenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "passenger")
@Data
public class Passenger {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "BOOKING_ID", nullable = false)
  private Long bookingId;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "AGE", nullable = false)
  private Integer age;

  @Column(name = "GENDER", nullable = false)
  private String gender;

  @Column(name = "SEAT_NUMBER", nullable = false)
  private String seatNumber; // for reference
}
