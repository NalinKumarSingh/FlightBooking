package com.example.flight.v1.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private String intent;  // FLIGHT_SEARCH, BOOKING_STATUS, FAQ, GENERAL
    private List<FlightSuggestion> flights;  // Populated when searching flights

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightSuggestion {
        private Long scheduleId;
        private String flightNumber;
        private String airline;
        private String origin;
        private String destination;
        private String departureTime;
        private String arrivalTime;
        private String price;
        private Integer availableSeats;
    }
}
