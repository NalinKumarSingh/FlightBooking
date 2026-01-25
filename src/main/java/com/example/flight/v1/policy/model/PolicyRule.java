package com.example.flight.v1.policy.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolicyRule {
  private int refundPercent;
  private int beforeHours;
}
