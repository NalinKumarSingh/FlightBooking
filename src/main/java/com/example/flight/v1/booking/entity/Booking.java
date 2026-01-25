package com.example.flight.v1.booking.entity;

import com.example.flight.v1.booking.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "booking")
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "USER_ID", nullable = false)
  private Long userId;

  @Column(name = "SCHEDULE_ID", nullable = false)
  private Long scheduleId;

  @Column(name = "SEAT_IDS")
  private String seatIds; // e.g., "101,102,103"

  @Column(name = "TOTAL_PRICE")
  private BigDecimal totalPrice;

  @Column(name = "PRICE_LOCKED")
  private BigDecimal priceLocked;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS")
  private BookingStatus status;

  @Column(name = "BOOKED_AT")
  private LocalDateTime bookedAt;

  @Column(name = "EXPIRES_AT")
  private LocalDateTime expiresAt;

  @Version
  private Integer version;
}
