package com.example.flight.v1.chatbot.service.impl;

import com.example.flight.v1.airline.entity.Airline;
import com.example.flight.v1.airline.repository.AirlineRepository;
import com.example.flight.v1.booking.entity.Booking;
import com.example.flight.v1.booking.repository.BookingRepository;
import com.example.flight.v1.chatbot.model.ChatRequest;
import com.example.flight.v1.chatbot.model.ChatResponse;
import com.example.flight.v1.chatbot.service.ChatService;
import com.example.flight.v1.flight.entity.Flight;
import com.example.flight.v1.flight.repository.FlightRepository;
import com.example.flight.v1.flightschedule.entity.FlightSchedule;
import com.example.flight.v1.flightschedule.enums.FlightStatus;
import com.example.flight.v1.flightschedule.repository.FlightScheduleRepository;
import com.example.flight.v1.location.entity.Location;
import com.example.flight.v1.location.repository.LocationRepository;
import com.example.flight.v1.passenger.entity.Passenger;
import com.example.flight.v1.passenger.repository.PassengerRepository;
import com.example.flight.v1.seat.entity.Seat;
import com.example.flight.v1.seat.repository.SeatRepository;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final LocationRepository locationRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Override
    public ChatResponse processMessage(String authHeader, ChatRequest request) {
        String userMessage = request.getMessage().toLowerCase();
        Long userId = extractUserId(authHeader);

        // Detect intent and process accordingly
        String intent = detectIntent(userMessage);

        return switch (intent) {
            case "FLIGHT_SEARCH" -> handleFlightSearch(userMessage);
            case "BOOKING_STATUS" -> handleBookingStatus(userId, request.getBookingId(), userMessage);
            case "MY_BOOKINGS" -> handleMyBookings(userId);
            case "CANCEL_INFO" -> handleCancellationInfo();
            case "FAQ" -> handleFAQ(userMessage);
            default -> handleGeneralQuery(userMessage, userId);
        };
    }

    private String detectIntent(String message) {
        // Flight search patterns
        if (message.contains("flight") && (message.contains("from") || message.contains("to") ||
                message.contains("search") || message.contains("find") || message.contains("available"))) {
            return "FLIGHT_SEARCH";
        }

        // Booking status patterns
        if (message.contains("booking") && (message.contains("status") || message.contains("check") ||
                message.contains("where") || message.contains("track"))) {
            return "BOOKING_STATUS";
        }

        // My bookings
        if (message.contains("my booking") || message.contains("my flight") ||
                (message.contains("show") && message.contains("booking"))) {
            return "MY_BOOKINGS";
        }

        // Cancellation info
        if (message.contains("cancel") || message.contains("refund")) {
            return "CANCEL_INFO";
        }

        // FAQ patterns
        if (message.contains("how") || message.contains("what") || message.contains("policy") ||
                message.contains("baggage") || message.contains("luggage") || message.contains("help")) {
            return "FAQ";
        }

        return "GENERAL";
    }

    private ChatResponse handleFlightSearch(String message) {
        // Extract origin and destination from message
        List<Location> allLocations = locationRepository.findAll();
        String origin = null;
        String destination = null;

        // Try to find "from X to Y" pattern
        Pattern pattern = Pattern.compile("from\\s+(\\w+)\\s+to\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String fromCity = matcher.group(1).toLowerCase();
            String toCity = matcher.group(2).toLowerCase();

            for (Location loc : allLocations) {
                if (loc.getCity().toLowerCase().contains(fromCity) ||
                        loc.getCode().toLowerCase().equals(fromCity)) {
                    origin = loc.getCity();
                }
                if (loc.getCity().toLowerCase().contains(toCity) ||
                        loc.getCode().toLowerCase().equals(toCity)) {
                    destination = loc.getCity();
                }
            }
        }

        // Find matching flights
        List<FlightSchedule> schedules;
        if (origin != null && destination != null) {
            final String finalOrigin = origin;
            final String finalDestination = destination;
            schedules = flightScheduleRepository.findAll().stream()
                    .filter(s -> s.getStatus() == FlightStatus.SCHEDULED)
                    .filter(s -> {
                        Flight flight = flightRepository.findById(s.getFlightId()).orElse(null);
                        if (flight == null) return false;
                        Location orig = locationRepository.findById(flight.getDepartureId()).orElse(null);
                        Location dest = locationRepository.findById(flight.getArrivalId()).orElse(null);
                        return orig != null && dest != null &&
                                orig.getCity().equalsIgnoreCase(finalOrigin) &&
                                dest.getCity().equalsIgnoreCase(finalDestination);
                    })
                    .filter(s -> s.getDepartureDateTime().isAfter(LocalDateTime.now()))
                    .limit(5)
                    .toList();
        } else {
            // Return upcoming flights
            schedules = flightScheduleRepository.findAll().stream()
                    .filter(s -> s.getStatus() == FlightStatus.SCHEDULED)
                    .filter(s -> s.getDepartureDateTime().isAfter(LocalDateTime.now()))
                    .limit(5)
                    .toList();
        }

        List<ChatResponse.FlightSuggestion> suggestions = schedules.stream()
                .map(this::toFlightSuggestion)
                .filter(Objects::nonNull)
                .toList();

        String reply;
        if (suggestions.isEmpty()) {
            reply = "I couldn't find any flights matching your criteria. Try searching with different cities or dates.";
        } else if (origin != null && destination != null) {
            reply = String.format("I found %d flight(s) from %s to %s:", suggestions.size(), origin, destination);
        } else {
            reply = "Here are some upcoming flights. Please specify 'from [city] to [city]' for better results:";
        }

        return ChatResponse.builder()
                .reply(reply)
                .intent("FLIGHT_SEARCH")
                .flights(suggestions)
                .build();
    }

    private ChatResponse.FlightSuggestion toFlightSuggestion(FlightSchedule schedule) {
        try {
            Flight flight = flightRepository.findById(schedule.getFlightId()).orElse(null);
            if (flight == null) return null;

            Airline airline = airlineRepository.findById(flight.getAirlineId()).orElse(null);
            Location origin = locationRepository.findById(flight.getDepartureId()).orElse(null);
            Location dest = locationRepository.findById(flight.getArrivalId()).orElse(null);

            if (airline == null || origin == null || dest == null) return null;

            // Count available seats
            List<Seat> seats = seatRepository.findByScheduleId(schedule.getId());
            int availableSeats = (int) seats.stream().filter(Seat::getIsAvailable).count();

            // Get minimum price
            BigDecimal minPrice = seats.stream()
                    .filter(Seat::getIsAvailable)
                    .map(Seat::getBasePrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

            return ChatResponse.FlightSuggestion.builder()
                    .scheduleId(schedule.getId())
                    .flightNumber(flight.getFlightCode())
                    .airline(airline.getName())
                    .origin(origin.getCity() + " (" + origin.getCode() + ")")
                    .destination(dest.getCity() + " (" + dest.getCode() + ")")
                    .departureTime(schedule.getDepartureDateTime().format(formatter))
                    .arrivalTime(schedule.getArrivalDateTime().format(formatter))
                    .price("₹" + minPrice.toString())
                    .availableSeats(availableSeats)
                    .build();
        } catch (Exception e) {
            log.error("Error converting schedule to suggestion", e);
            return null;
        }
    }

    private ChatResponse handleBookingStatus(Long userId, Long bookingId, String message) {
        if (bookingId != null) {
            Optional<Booking> booking = bookingRepository.findById(bookingId);
            if (booking.isPresent() && booking.get().getUserId().equals(userId)) {
                return buildBookingStatusResponse(booking.get());
            }
        }

        // Try to extract booking ID from message
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                Long extractedId = Long.parseLong(matcher.group(1));
                Optional<Booking> booking = bookingRepository.findById(extractedId);
                if (booking.isPresent() && booking.get().getUserId().equals(userId)) {
                    return buildBookingStatusResponse(booking.get());
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return ChatResponse.builder()
                .reply("Please provide your booking ID to check the status. You can say 'Check booking 123' or use the bookingId field.")
                .intent("BOOKING_STATUS")
                .build();
    }

    private ChatResponse buildBookingStatusResponse(Booking booking) {
        List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
        FlightSchedule schedule = flightScheduleRepository.findById(booking.getScheduleId()).orElse(null);

        StringBuilder reply = new StringBuilder();
        reply.append("<div class='booking-detail'>");
        reply.append("<h4>📋 Booking #").append(booking.getId()).append("</h4>");
        reply.append("<p><strong>Status:</strong> <span class='status-").append(booking.getStatus().name().toLowerCase()).append("'>").append(booking.getStatus()).append("</span></p>");
        reply.append("<p><strong>Total Price:</strong> ₹").append(booking.getTotalPrice()).append("</p>");
        reply.append("<p><strong>Booked At:</strong> ").append(booking.getBookedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");

        if (booking.getStatus().name().equals("PENDING") && booking.getExpiresAt() != null) {
            reply.append("<p><strong>Expires At:</strong> ").append(booking.getExpiresAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");
            reply.append("<p class='warning'>⚠️ Please complete payment before expiry!</p>");
        }

        if (schedule != null) {
            Flight flight = flightRepository.findById(schedule.getFlightId()).orElse(null);
            if (flight != null) {
                reply.append("<p><strong>Flight:</strong> ").append(flight.getFlightCode()).append("</p>");
                reply.append("<p><strong>Departure:</strong> ").append(schedule.getDepartureDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");
            }
        }

        if (!passengers.isEmpty()) {
            reply.append("<p><strong>Passengers:</strong></p><ul>");
            for (Passenger p : passengers) {
                reply.append("<li>").append(p.getName()).append(" (Seat: ").append(p.getSeatNumber()).append(")</li>");
            }
            reply.append("</ul>");
        }
        reply.append("</div>");

        return ChatResponse.builder()
                .reply(reply.toString())
                .intent("BOOKING_STATUS")
                .build();
    }

    private ChatResponse handleMyBookings(Long userId) {
        if (userId == null) {
            return ChatResponse.builder()
                    .reply("Please log in to view your bookings.")
                    .intent("MY_BOOKINGS")
                    .build();
        }

        List<Booking> bookings = bookingRepository.findByUserId(userId);
        if (bookings.isEmpty()) {
            return ChatResponse.builder()
                    .reply("You don't have any bookings yet. Would you like to search for flights?")
                    .intent("MY_BOOKINGS")
                    .build();
        }

        StringBuilder reply = new StringBuilder();
        reply.append("<div class='bookings-list'>");
        reply.append("<h4>📋 Your Bookings</h4>");

        for (Booking booking : bookings.stream().limit(5).toList()) {
            reply.append("<div class='booking-card'>");
            reply.append("<div class='booking-header'><strong>Booking #").append(booking.getId()).append("</strong></div>");
            reply.append("<div class='booking-info'>");
            reply.append("<span class='status-").append(booking.getStatus().name().toLowerCase()).append("'>").append(booking.getStatus()).append("</span>");
            reply.append(" &bull; ₹").append(booking.getTotalPrice());
            reply.append("</div>");

            FlightSchedule schedule = flightScheduleRepository.findById(booking.getScheduleId()).orElse(null);
            if (schedule != null) {
                Flight flight = flightRepository.findById(schedule.getFlightId()).orElse(null);
                if (flight != null) {
                    reply.append("<div class='booking-flight'>✈️ ").append(flight.getFlightCode());
                    reply.append(" &bull; ").append(schedule.getDepartureDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("</div>");
                }
            }
            reply.append("</div>");
        }

        if (bookings.size() > 5) {
            reply.append("<p class='more-info'>...and ").append(bookings.size() - 5).append(" more bookings.</p>");
        }
        reply.append("</div>");

        return ChatResponse.builder()
                .reply(reply.toString())
                .intent("MY_BOOKINGS")
                .build();
    }

    private ChatResponse handleCancellationInfo() {
        String reply = """
                <div class='info-card'>
                <h4>❌ Cancellation Policy</h4>
                <p>Our cancellation policy varies by airline. Generally:</p>
                <ul>
                <li><strong>24+ hours before:</strong> Up to 75-100% refund</li>
                <li><strong>12-24 hours before:</strong> Up to 50% refund</li>
                <li><strong>6-12 hours before:</strong> Up to 25% refund</li>
                <li><strong>Less than 6 hours:</strong> Usually non-refundable</li>
                </ul>
                <p><strong>To cancel a booking:</strong></p>
                <ol>
                <li>Go to 'My Bookings'</li>
                <li>Select the booking you want to cancel</li>
                <li>Click 'Cancel Booking'</li>
                <li>Refund will be processed based on the airline's policy</li>
                </ol>
                <p>Need help cancelling a specific booking? Provide your booking ID!</p>
                </div>
                """;

        return ChatResponse.builder()
                .reply(reply)
                .intent("CANCEL_INFO")
                .build();
    }

    private ChatResponse handleFAQ(String message) {
        String reply;

        if (message.contains("baggage") || message.contains("luggage")) {
            reply = """
                    <div class='info-card'>
                    <h4>💼 Baggage Information</h4>
                    <ul>
                    <li><strong>Cabin Baggage:</strong> 7kg (one piece)</li>
                    <li><strong>Check-in Baggage:</strong> Varies by fare type (15-30kg)</li>
                    <li><strong>Extra Baggage:</strong> Can be purchased during booking</li>
                    </ul>
                    <p><em>Note: Baggage allowance may vary by airline. Check your booking details.</em></p>
                    </div>
                    """;
        } else if (message.contains("payment")) {
            reply = """
                    <div class='info-card'>
                    <h4>💳 Payment Information</h4>
                    <p>We accept:</p>
                    <ul>
                    <li>Credit/Debit Cards (Visa, Mastercard)</li>
                    <li>UPI payments</li>
                    <li>Net Banking</li>
                    </ul>
                    <p><em>⏱️ Payment must be completed within 10 minutes of booking to secure your seats.</em></p>
                    </div>
                    """;
        } else if (message.contains("check-in") || message.contains("checkin")) {
            reply = """
                    <div class='info-card'>
                    <h4>✈️ Check-in Information</h4>
                    <ul>
                    <li><strong>Web Check-in:</strong> Available 48-2 hours before departure</li>
                    <li><strong>Airport Check-in:</strong> Counter opens 3 hours before departure</li>
                    <li><strong>Documents Required:</strong> Valid ID, Booking confirmation</li>
                    </ul>
                    <p><em>💡 We recommend web check-in to save time at the airport!</em></p>
                    </div>
                    """;
        } else {
            reply = """
                    <div class='info-card'>
                    <h4>👋 How can I help you?</h4>
                    <p>I can assist you with:</p>
                    <ul>
                    <li>🔍 <strong>Flight Search:</strong> "Find flights from Delhi to Mumbai"</li>
                    <li>📋 <strong>Booking Status:</strong> "Check booking 123"</li>
                    <li>📝 <strong>My Bookings:</strong> "Show my bookings"</li>
                    <li>❌ <strong>Cancellation:</strong> "How to cancel?"</li>
                    <li>💼 <strong>Baggage Info:</strong> "What's the baggage limit?"</li>
                    <li>💳 <strong>Payment Help:</strong> "Payment options"</li>
                    </ul>
                    <p>Just type your question!</p>
                    </div>
                    """;
        }

        return ChatResponse.builder()
                .reply(reply)
                .intent("FAQ")
                .build();
    }

    private ChatResponse handleGeneralQuery(String message, Long userId) {
        // If Gemini API key is configured, use AI for general queries
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            return callGeminiAPI(message, userId);
        }

        // Fallback response
        return ChatResponse.builder()
                .reply("""
                        <div class='info-card'>
                        <h4>✈️ Flight Booking Assistant</h4>
                        <p>Here's what I can help with:</p>
                        <ul>
                        <li>🔍 <strong>Search Flights:</strong> "Find flights from Delhi to Mumbai"</li>
                        <li>📋 <strong>Check Booking:</strong> "Check booking status 123"</li>
                        <li>📝 <strong>My Bookings:</strong> "Show my bookings"</li>
                        <li>❌ <strong>Cancellation:</strong> "How do I cancel?"</li>
                        <li>❓ <strong>FAQs:</strong> "Baggage policy", "Payment options"</li>
                        </ul>
                        <p>What would you like to do?</p>
                        </div>
                        """)
                .intent("GENERAL")
                .build();
    }

    private ChatResponse callGeminiAPI(String userMessage, Long userId) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Build context about the user
            String userContext = "";
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    userContext = "The user's name is " + user.getName() + ". ";
                    List<Booking> recentBookings = bookingRepository.findByUserId(userId);
                    if (!recentBookings.isEmpty()) {
                        userContext += "They have " + recentBookings.size() + " booking(s). ";
                    }
                }
            }

            String systemPrompt = """
                    You are a helpful flight booking assistant for SkyBook Airlines. Be concise and friendly.
                    %s
                    Answer questions about flights, bookings, and travel. If asked about specific booking details,
                    ask for the booking ID. Keep responses under 200 words.
                    """.formatted(userContext);

            // Gemini API request body
            String requestBody = """
                    {
                        "contents": [{
                            "parts": [{
                                "text": "%s\\n\\nUser: %s"
                            }]
                        }],
                        "generationConfig": {
                            "temperature": 0.7,
                            "maxOutputTokens": 500
                        }
                    }
                    """.formatted(
                    systemPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
                    userMessage.replace("\"", "\\\"")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            String url = GEMINI_API_URL + "?key=" + geminiApiKey;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String aiReply = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();

                return ChatResponse.builder()
                        .reply(aiReply)
                        .intent("GENERAL")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
        }

        // Fallback
        return handleFAQ("help");
    }

    private Long extractUserId(String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtUtil.extractUserId(token);
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from token", e);
        }
        return null;
    }
}
