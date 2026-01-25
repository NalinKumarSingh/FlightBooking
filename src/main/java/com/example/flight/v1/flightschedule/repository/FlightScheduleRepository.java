package com.example.flight.v1.flightschedule.repository;

import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long> {
  @Query("""
SELECT s FROM FlightSchedule s
JOIN Flight f ON f.id = s.flightId
WHERE f.departureId = :departureId
  AND f.arrivalId = :arrivalId
  AND DATE(s.departureDateTime) = DATE(:departureDate)
  AND s.status = 'SCHEDULED'
""")
  List<FlightSchedule> searchFlights(
      @Param("departureId") Long departureId,
      @Param("arrivalId") Long arrivalId,
      @Param("departureDate") LocalDate departureDate
  );
  List<FlightSchedule> findByStatusAndArrivalDateTimeBefore(FlightStatus status, LocalDateTime before);
}
