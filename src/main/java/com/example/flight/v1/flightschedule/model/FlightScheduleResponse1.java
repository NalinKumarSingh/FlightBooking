package com.example.flight.v1.flightschedule.model;

import com.example.flight.v1.flightschedule.enums.FlightStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
@Data
public class FlightScheduleResponse1 {
  private Long scheduleId;
  private Long flightId;
  private LocalDateTime departureDateTime;
  private LocalDateTime arrivalDateTime;
  private Duration duration;
  private FlightStatus status;
  private String flightCode;
  private String airlineName;
  private String fromCity;
  private String toCity;
  private BigDecimal basePrice;

}
