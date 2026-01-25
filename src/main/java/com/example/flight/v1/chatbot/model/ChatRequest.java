package com.example.flight.v1.chatbot.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private Long bookingId;  // Optional: for booking-specific queries
}
