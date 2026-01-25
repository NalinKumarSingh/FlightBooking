package com.example.flight.v1.policy.util;

import com.example.flight.v1.policy.model.PolicyRule;
import jakarta.persistence.Column;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

@Component
public class CancellationPolicyParser {

  private static final Pattern PATTERN = Pattern.compile("REFUND_(\\d+)_BEFORE=(\\d+)h");

  public static List<PolicyRule> parse(String policyText) {
    List<PolicyRule> rules = new ArrayList<>();
    if (policyText == null || policyText.isBlank()) return rules;

    String[] lines = policyText.split("\\n");

    for (String line : lines) {
      line = line.trim();
      Matcher matcher = PATTERN.matcher(line);
      if (matcher.matches()) {
        int refund = Integer.parseInt(matcher.group(1));
        int hours = Integer.parseInt(matcher.group(2));
        rules.add(new PolicyRule(refund, hours));
      } else if (line.startsWith("NO_REFUND_AFTER")) {
        String[] parts = line.split("=");
        if (parts.length == 2 && parts[1].endsWith("h")) {
          int hours = Integer.parseInt(parts[1].replace("h", ""));
          rules.add(new PolicyRule(0, hours));
        }
      }
    }

    rules.sort((a, b) -> Integer.compare(b.getBeforeHours(), a.getBeforeHours()));
    return rules;
  }

  public static int getRefundPercentage(String policyText, long hoursBeforeFlight) {
    List<PolicyRule> rules = parse(policyText);
    for (PolicyRule rule : rules) {
      if (hoursBeforeFlight >= rule.getBeforeHours()) {
        return rule.getRefundPercent();
      }
    }
    return 0;
  }
}
