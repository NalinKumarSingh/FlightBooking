package com.example.flight.v1.payment.exception;

public class PaymentNotFound extends RuntimeException {
  public PaymentNotFound(String message) {
    super(message);
  }
}
