package com.example.flight.v1.seat.service.impl;

import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.exception.ScheduleNotFound;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import com.example.flight.v1.seat.entity.Seat;
import com.example.flight.v1.seat.repository.SeatRepository;
import com.example.flight.v1.seat.service.SeatService;
import com.example.flight.v1.seat.util.AircraftConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

  private final SeatRepository seatRepository;
  private final FlightScheduleRepository scheduleRepository;

  @Override
  public void generateSeatsForSchedule(Long scheduleId) {
    FlightSchedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new ScheduleNotFound("Schedule not found"));

    List<Seat> seats = AircraftConstants.getSeatLayout().stream().map(def -> {
      Seat seat = new Seat();
      seat.setSeatNumber(def.seatNumber());
      seat.setSeatClass(def.seatClass());
      seat.setSeatType(def.seatType());
      seat.setBasePrice(def.basePrice());
      seat.setPremiumFee(def.premiumFee());
      seat.setScheduleId(schedule.getId());
      seat.setIsAvailable(true);
      return seat;
    }).toList();

    seatRepository.saveAll(seats);
  }

  @Override
  public List<Seat> getAllSeatsBySchedule(Long scheduleId) {
    return seatRepository.findByScheduleId(scheduleId);
  }

  @Override
  public List<Seat> getAvailableSeatsBySchedule(Long scheduleId) {
    return seatRepository.findByScheduleIdAndIsAvailableTrue(scheduleId);
  }

  @Override
  public Seat getSeatById(Long id) {
    return seatRepository.findById(id).orElseThrow(() -> new RuntimeException("Seat not found"));
  }
}
