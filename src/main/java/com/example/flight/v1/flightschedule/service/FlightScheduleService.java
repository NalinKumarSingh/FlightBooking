package com.example.flight.v1.flightschedule.service;

import com.example.flight.v1.flightschedule.model.FlightScheduleRequest;
import com.example.flight.v1.flightschedule.model.FlightScheduleResponse;
import com.example.flight.v1.flightschedule.model.FlightScheduleResponse1;

import java.time.LocalDate;
import java.util.List;

public interface FlightScheduleService {
  FlightScheduleResponse createSchedule(FlightScheduleRequest request, String authHeader);
//  List<FlightScheduleResponse> getAllSchedules();
  FlightScheduleResponse getScheduleById(Long id);
  public List<FlightScheduleResponse1> searchFlights(Long from, Long to, LocalDate date, String sortBy, Long airlineId);
}
