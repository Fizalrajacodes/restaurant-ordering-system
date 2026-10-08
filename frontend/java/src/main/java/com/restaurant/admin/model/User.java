package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
}