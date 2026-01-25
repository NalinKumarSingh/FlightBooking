package com.example.flight.v1.scheduler;

import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.enums.FlightStatus;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleStatusUpdater {

  private final FlightScheduleRepository flightScheduleRepository;

  @Scheduled(fixedRate = 300000) // 5 min
  public void updateExpiredSchedules() {
    LocalDateTime now = LocalDateTime.now();
    List<FlightSchedule> schedules = flightScheduleRepository.findByStatusAndArrivalDateTimeBefore(
        FlightStatus.SCHEDULED, now
    );

    for (FlightSchedule schedule : schedules) {
      schedule.setStatus(FlightStatus.COMPLETED);
      log.info("Schedule {} is COMPLETED", schedule.getId());
    }

    flightScheduleRepository.saveAll(schedules);
  }
}