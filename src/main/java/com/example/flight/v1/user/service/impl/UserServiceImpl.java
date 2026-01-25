package com.example.flight.v1.user.service.impl;

import com.example.flight.v1.airline.exceptions.AirlineNotFound;
import com.example.flight.v1.airline.repository.AirlineRepository;
import com.example.flight.v1.user.entity.User;
import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.user.model.LoginRequest;
import com.example.flight.v1.user.model.UserRequest;
import com.example.flight.v1.user.model.UserResponse;
import com.example.flight.v1.user.repository.UserRepository;
import com.example.flight.v1.user.service.UserService;
import com.example.flight.v1.utils.JwtUtil;
import com.example.flight.v1.utils.MailService;
import com.example.flight.v1.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final MailService mailService;
  private final OtpUtil otpUtil;
  private final AirlineRepository airlineRepository;
  private final JwtUtil jwtUtil;

  @Override
  public UserResponse createUser(UserRequest request) {
    if (
        request.getEmail() == null || request.getPassword() == null ||
            request.getName() == null || request.getRole() == null ||
            request.getEmail().isEmpty() || request.getPassword().isEmpty() || request.getName().isEmpty()
    ) {
      throw new IllegalArgumentException("All fields are required");
    }

    Role role;
    try {
      role = Role.valueOf(request.getRole().toString().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role provided");
    }

    Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());

    User user;
    if (existingUserOpt.isPresent()) {
      user = existingUserOpt.get();

      if (Boolean.TRUE.equals(user.getIsVerified())) {
        throw new IllegalArgumentException("Email already registered and verified");
      }

      // Update unverified user
      user.setName(request.getName());
      user.setPassword(request.getPassword());
      user.setRole(role);
//      user.setUpdatedAt(LocalDateTime.now());

    } else {
      user = new User();
      user.setEmail(request.getEmail());
      user.setName(request.getName());
      user.setPassword(request.getPassword());
      user.setRole(role);
      user.setIsVerified(false);
      user.setCreatedAt(LocalDateTime.now());
    }

    // Handle airline staff (applies in both new & existing user scenarios)
    handleAirlineStaffLogic(user, role, request.getAirlineId());

    User savedUser = userRepository.save(user);

    if (Boolean.FALSE.equals(savedUser.getIsVerified())) {
      String otp = otpUtil.generateOtp(savedUser.getEmail());
      mailService.sendMail(
          savedUser.getEmail(),
          "Verify Your Account",
          "<p>Your OTP is <b>" + otp + "</b>. It is valid for 5 minutes.</p>"
      );
    }

    return mapToResponse(savedUser);
  }

  private void handleAirlineStaffLogic(User user, Role role, Long airlineId) {
    if (role == Role.AIRLINE_STAFF) {
      if (airlineId == null) {
        throw new IllegalArgumentException("Airline ID must be provided for airline staff.");
      }

      if (!airlineRepository.existsById(airlineId)) {
        throw new AirlineNotFound("Airline not found with ID: " + airlineId);
      }

      user.setAirlineId(airlineId);
      user.setAdminVerification(false);

    } else {
      user.setAirlineId(null);
      user.setAdminVerification(null);
    }
  }

  @Override
  public UserResponse getUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFound("User not found"));
    return mapToResponse(user);
  }

  @Override
  public List<UserResponse> getAllUsers(Boolean isVerified, Role role) { // admin verification for flightstaff
    return userRepository.findAll().stream()
        .filter(user -> isVerified == null || user.getIsVerified().equals(isVerified))
        .filter(user -> role == null || user.getRole().equals(role))
        .map(this::mapToResponse)
        .toList();
  }

  private UserResponse mapToResponse(User user) {
    UserResponse res = new UserResponse();
    res.setId(user.getId());
    res.setName(user.getName());
    res.setEmail(user.getEmail());
    res.setRole(user.getRole());
    res.setIsVerified(user.getIsVerified());
    res.setAdminVerification(user.getAdminVerification());
    res.setAirlineId(user.getAirlineId());
    return res;
  }

  @Override
  public boolean verifyOtp(String email, String otp) {
    boolean isValid = otpUtil.verifyOtp(email, otp);

    if (isValid) {
      User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

      user.setIsVerified(true);
      userRepository.save(user);

      return true;
    } else {
      throw new IllegalArgumentException("Invalid or expired OTP.");
    }
  }

  @Override
  public void verifyStaffByAdminFromToken(String token, String staffEmail) {
    Long adminId = jwtUtil.extractUserId(token);
    String role = jwtUtil.extractRole(token);

    if (!"ADMIN".equals(role)) {
      throw new IllegalArgumentException("Only admins can verify airline staff.");
    }

    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new UserNotFound("Admin not found with ID: " + adminId));

    User staff = userRepository.findByEmail(staffEmail)
        .orElseThrow(() -> new UserNotFound("User (staff) not found with email: " + staffEmail));

    if (staff.getRole() != Role.AIRLINE_STAFF) {
      throw new IllegalArgumentException("User is not an airline staff.");
    }

    staff.setAdminVerification(true);
    userRepository.save(staff);
  }

  @Override
  public UserResponse login(LoginRequest loginRequest) {
    String email = loginRequest.getEmail();
    String password = loginRequest.getPassword();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getPassword().equals(password)) {
      throw new RuntimeException("Invalid credentials");
    }

    String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setRole(user.getRole());
    response.setIsVerified(user.getIsVerified());
    response.setAdminVerification(user.getAdminVerification());
    response.setAirlineId(user.getAirlineId());
    response.setToken(token);

    return response;
  }

}
