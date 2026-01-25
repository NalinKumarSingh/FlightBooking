package com.example.flight.v1.booking.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingCancelResponse {
  private Long bookingId;
  private String status;
  private String message;
  private BigDecimal refundAmount;
  private LocalDateTime cancelledAt;
}
