package com.example.flight.v1.utils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpUtil {

  private final ConcurrentHashMap<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
  private final int OTP_VALID_DURATION_MIN = 5;

  public String generateOtp(String key) {
    String otp = String.valueOf(new Random().nextInt(900000) + 100000);
    otpStore.put(key, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_VALID_DURATION_MIN)));
    return otp;
  }

  public boolean verifyOtp(String key, String enteredOtp) {
    OtpEntry entry = otpStore.get(key);
    if (entry == null) return false;
    if (LocalDateTime.now().isAfter(entry.getExpiresAt())) return false;
    return entry.getOtp().equals(enteredOtp);
  }

  @Getter
  private static class OtpEntry {
    private final String otp;
    private final LocalDateTime expiresAt;

    public OtpEntry(String otp, LocalDateTime expiresAt) {
      this.otp = otp;
      this.expiresAt = expiresAt;
    }
  }
}
