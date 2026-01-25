package com.example.flight.v1.user.entity;

import com.example.flight.v1.user.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "user_flight_test")
public class User {
  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "EMAIL")
  private String email;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "ROLE")
  @Enumerated(EnumType.STRING)
  private Role role; // USER, ADMIN, AIRLINE_STAFF

  @Column(name = "VERIFICATION")
  private Boolean isVerified = false;

  @Column(name = "ADMIN_VERIFICATION")
  private Boolean adminVerification;

  @Column(name = "CREATED_AT")
  private LocalDateTime createdAt;

  @Column(name = "AIRLINE_ID")
  private Long airlineId;


}
