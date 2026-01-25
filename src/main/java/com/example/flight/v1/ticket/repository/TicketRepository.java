package com.example.flight.v1.ticket.repository;

import com.example.flight.v1.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
  List<Ticket> findByUserId(Long userId);
  List<Ticket> findByPassengerId(Long passengerId);
  List<Ticket> findByScheduleId(Long scheduleId);
}
