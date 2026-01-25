package com.example.flight.v1.location.model;

import lombok.Data;

@Data
public class LocationResponse {
  private Long id;
  private String code;
  private String name;
  private String city;
  private String country;
}