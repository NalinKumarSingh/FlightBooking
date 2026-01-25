package com.example.flight.v1.payment.controller;

import com.example.flight.v1.booking.exception.BookingNotFound;
import com.example.flight.v1.payment.exception.PaymentNotFound;
import com.example.flight.v1.payment.model.PaymentRequest;
import com.example.flight.v1.payment.model.PaymentResponse;
import com.example.flight.v1.payment.service.PaymentService;
import com.example.flight.v1.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

  private final PaymentService paymentService;

  //Make a payment
  @PostMapping("/make")
  public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(@RequestBody PaymentRequest request) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Payment made successfully", true,  paymentService.makePayment(request)));
    } catch (BookingNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), true, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), true, null));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Payment fetched successfully", true, paymentService.getPaymentById(id)));
    } catch (PaymentNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), true, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), true, null));
    }
  }
}
