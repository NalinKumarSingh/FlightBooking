package com.example.flight.v1.payment.model;

import com.example.flight.v1.payment.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
  private Long bookingId;
  private BigDecimal amount;
  private String transactionId;
  private PaymentMethod paymentMethod;
}