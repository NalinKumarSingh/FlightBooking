package com.example.flight.v1.chatbot.service;

import com.example.flight.v1.chatbot.model.ChatRequest;
import com.example.flight.v1.chatbot.model.ChatResponse;

public interface ChatService {
    ChatResponse processMessage(String authHeader, ChatRequest request);
}
