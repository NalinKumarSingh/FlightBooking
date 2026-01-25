package com.example.flight.v1.payment.entity;

import com.example.flight.v1.payment.enums.PaymentMethod;
import com.example.flight.v1.payment.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "BOOKING_ID", nullable = false)
  private Long bookingId;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS")
  private PaymentStatus status; // SUCCESS, FAILED, PENDING

  @Column(name = "AMOUNT")
  private BigDecimal amount;

  @Column(name = "TRANSACTION_ID")
  private String transactionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "PAYMENT_METHOD")
  private PaymentMethod paymentMethod;

  @Column(name = "PAYMENT_TIME")
  private LocalDateTime paymentTime;
}
