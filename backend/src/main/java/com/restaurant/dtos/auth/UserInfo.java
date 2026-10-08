package com.restaurant.dtos.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
}
