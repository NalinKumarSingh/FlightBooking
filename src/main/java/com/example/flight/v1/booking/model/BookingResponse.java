package com.example.flight.v1.booking.model;

import com.example.flight.v1.booking.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
  private Long bookingId;
  private BookingStatus status;
  private BigDecimal totalPrice;
  private LocalDateTime bookedAt;
  private LocalDateTime expiresAt;
  private List<String> seatNumbers;
  private List<PassengerDTO> passengers;

  @Data
  public static class PassengerDTO {
    private String name;
    private Integer age;
    private String gender;
    private String seatNumber;
  }
}
