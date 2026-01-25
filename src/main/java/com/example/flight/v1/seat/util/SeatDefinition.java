package com.example.flight.v1.seat.util;


import com.example.flight.v1.seat.enums.SeatClass;
import com.example.flight.v1.seat.enums.SeatType;

import java.math.BigDecimal;

public record SeatDefinition(
    String seatNumber,
    SeatClass seatClass,
    SeatType seatType,
    BigDecimal basePrice,
    BigDecimal premiumFee
) {}

