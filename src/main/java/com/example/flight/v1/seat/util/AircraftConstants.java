package com.example.flight.v1.seat.util;


import com.example.flight.v1.seat.enums.SeatClass;
import com.example.flight.v1.seat.enums.SeatType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AircraftConstants {

//  public static List<SeatDefinition> getSeatLayout() {
//    List<SeatDefinition> layout = new ArrayList<>();
//
//    // Business Class (1–5)
//    for (int row = 1; row <= 5; row++) {
//      for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
//        layout.add(new SeatDefinition(
//            row + "" + col,
//            SeatClass.BUSINESS,
//            (row <= 3) ? SeatType.EXTRA_LEGROOM : SeatType.REGULAR,
//            new BigDecimal("300.00"),
//            (row <= 3) ? new BigDecimal("50.00") : BigDecimal.ZERO
//        ));
//      }
//    }
//
//    // Economy Class (6–27)
//    for (int row = 6; row <= 27; row++) {
//      for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
//        layout.add(new SeatDefinition(
//            row + "" + col,
//            SeatClass.ECONOMY,
//            SeatType.REGULAR,
//            new BigDecimal("100.00"),
//            BigDecimal.ZERO
//        ));
//      }
//    }
//
//    return layout;
//  }
public static List<SeatDefinition> getSeatLayout() {
  List<SeatDefinition> layout = new ArrayList<>();
  Random random = new Random();

  // Generate prices for this specific call
  BigDecimal businessPrice = BigDecimal.valueOf(300 + random.nextInt(401));
  BigDecimal economyPrice = BigDecimal.valueOf(100 + random.nextInt(201));
  BigDecimal extraLegroomFee = BigDecimal.valueOf(50 + random.nextInt(101));

  // Business Class (1–5)
  for (int row = 1; row <= 5; row++) {
    for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
      SeatType type = (row <= 3) ? SeatType.EXTRA_LEGROOM : SeatType.REGULAR;
      layout.add(new SeatDefinition(
          row + "" + col,
          SeatClass.BUSINESS,
          type,
          businessPrice,
          (type == SeatType.EXTRA_LEGROOM) ? extraLegroomFee : BigDecimal.ZERO
      ));
    }
  }

  // Economy Class (6–27)
  for (int row = 6; row <= 27; row++) {
    for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
      layout.add(new SeatDefinition(
          row + "" + col,
          SeatClass.ECONOMY,
          SeatType.REGULAR,
          economyPrice,
          BigDecimal.ZERO
      ));
    }
  }

  return layout;
}
}
