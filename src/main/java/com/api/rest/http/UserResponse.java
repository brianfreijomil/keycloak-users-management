package com.api.rest.http;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String lastName;
    private String firstName;
    private List<String> roles;
    private Boolean enabled;
}
