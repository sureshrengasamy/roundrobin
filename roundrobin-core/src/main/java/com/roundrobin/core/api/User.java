package com.roundrobin.core.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
  @JsonProperty("user_id")
  private String userId;

  private String username;

  private List<String> roles = new ArrayList<>();

  private boolean verified;

}
