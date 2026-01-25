package com.example.flight.v1.ticket.service;

import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.passenger.entity.Passenger;
import com.example.flight.v1.ticket.entity.Ticket;
import com.example.flight.v1.ticket.model.TicketResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public interface TicketService {
  public List<TicketResponse> getTicketsByUserId(Long userId);
  public List<TicketResponse>getTicketsByUserIdFuture(Long userId);
  public List<TicketResponse> getTicketsByUserIdPast(Long userId);
}
