package com.example.flight.v1.payment.model;

import com.example.flight.v1.payment.enums.PaymentStatus;
import com.example.flight.v1.payment.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
  private Long paymentId;
  private Long bookingId;
  private BigDecimal amount;
  private String transactionId;
  private PaymentMethod paymentMethod;
  private PaymentStatus status;
  private LocalDateTime paymentTime;
}
