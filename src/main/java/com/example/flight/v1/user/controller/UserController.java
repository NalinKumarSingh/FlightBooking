package com.example.flight.v1.user.controller;

import com.example.flight.v1.airline.exceptions.AirlineNotFound;
import com.example.flight.v1.user.enums.Role;
import com.example.flight.v1.user.exception.UserNotFound;
import com.example.flight.v1.user.model.LoginRequest;
import com.example.flight.v1.user.model.UserRequest;
import com.example.flight.v1.user.model.UserResponse;
import com.example.flight.v1.user.service.UserService;
import com.example.flight.v1.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody UserRequest request) {
    try {
      UserResponse user =  userService.createUser(request);
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("OTP sent successfully", true, user));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (AirlineNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
    try {
      UserResponse user = userService.getUser(id);
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("User Details fetched successfully", true, user));
    } catch (UserNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<UserResponse>>> getAll(@RequestParam(required = false) Boolean isVerified, @RequestParam(required = false) Role role) {
    List<UserResponse> users = userService.getAllUsers(isVerified, role);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Users fetched successfully", true, users));
  }

  @GetMapping("/verify-otp")
  public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
    try {
      userService.verifyOtp(email, otp);
      return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("OTP verified successfully", true, null));
    } catch (UserNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  @PostMapping("/verify-staff")
  public ResponseEntity<ApiResponse<String>> verifyStaffByAdmin(@RequestHeader("Authorization") String authHeader, @RequestParam("staffEmail") String staffEmail) {
    try {
      String token = authHeader.substring(7); // remove "Bearer "
      System.out.println(staffEmail);
      System.out.println(token);
      userService.verifyStaffByAdminFromToken(token, staffEmail);
      return ResponseEntity.ok(new ApiResponse<>("Airline staff verified by admin successfully", true, null));
    } catch (UserNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(e.getMessage(), false, null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest loginRequest) {
    try {
      UserResponse response = userService.login(loginRequest);
      return ResponseEntity.ok(new ApiResponse<>("Login successful", true, response));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(e.getMessage(), false, null));
    }catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(e.getMessage(), false, null));
    }
  }
}
