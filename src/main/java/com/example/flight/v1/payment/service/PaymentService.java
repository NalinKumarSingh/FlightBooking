package com.example.flight.v1.payment.service;

import com.example.flight.v1.payment.model.PaymentRequest;
import com.example.flight.v1.payment.model.PaymentResponse;

public interface PaymentService {
  PaymentResponse makePayment(PaymentRequest request);
  PaymentResponse getPaymentById(Long id);
}
