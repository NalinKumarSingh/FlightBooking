package com.example.flight.v1.ticket.controller;

import com.example.flight.v1.ticket.model.TicketResponse;
import com.example.flight.v1.ticket.service.impl.TicketServiceImpl;
import com.example.flight.v1.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TicketController {
  private final JwtUtil jwtUtil;
  private final TicketServiceImpl ticketService;
  @GetMapping("/tickets")
  public ResponseEntity<List<TicketResponse>> getAllUserTickets(@RequestHeader("Authorization") String authHeader) {
    Long userId = jwtUtil.extractUserId(authHeader.substring(7));
    List<TicketResponse> tickets = ticketService.getTicketsByUserId(userId);
    return ResponseEntity.ok(tickets);
  }
  @GetMapping("/tickets/future")
  public ResponseEntity<List<TicketResponse>> getAllUserFutureTickets(@RequestHeader("Authorization") String authHeader){
    Long userId = jwtUtil.extractUserId(authHeader.substring(7));
    List<TicketResponse> tickets = ticketService.getTicketsByUserIdFuture(userId);
    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/tickets/past")
  public ResponseEntity<List<TicketResponse>> getAllUserPastTickets(@RequestHeader("Authorization") String authHeader){
    Long userId = jwtUtil.extractUserId(authHeader.substring(7));
    List<TicketResponse> tickets = ticketService.getTicketsByUserIdPast(userId);
    return ResponseEntity.ok(tickets);
  }
}
