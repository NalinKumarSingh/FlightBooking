package com.example.flight.v1.chatbot.controller;

import com.example.flight.v1.chatbot.model.ChatRequest;
import com.example.flight.v1.chatbot.model.ChatResponse;
import com.example.flight.v1.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ChatRequest request) {

        ChatResponse response = chatService.processMessage(authHeader, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot is running!");
    }
}
