package com.example.flight.v1.booking.model;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
  private Long userId;
  private Long scheduleId;
  private List<Long> seatIds;

  private List<PassengerInfo> passengers;

  @Data
  public static class PassengerInfo {
    private String name;
    private Integer age;
    private String gender;
  }
}
