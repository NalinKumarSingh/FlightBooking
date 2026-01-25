package com.example.flight.v1.seat.entity;

import com.example.flight.v1.seat.enums.SeatClass;
import com.example.flight.v1.seat.enums.SeatType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "seat")
public class Seat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "SEAT_NUMBER", nullable = false)
  private String seatNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEAT_CLASS", nullable = false)
  private SeatClass seatClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEAT_TYPE", nullable = false)
  private SeatType seatType;

  @Column(name = "BASE_PRICE", nullable = false)
  private BigDecimal basePrice;

  @Column(name = "PREMIUM_FEE")
  private BigDecimal premiumFee;

  @Column(name = "IS_AVAILABLE")
  private Boolean isAvailable = true;

  @Column(name = "SCHEDULE_ID")
  private Long scheduleId;

  @Column(name = "LOCKED_UNTIL")
  private java.time.LocalDateTime lockedUntil;
}
