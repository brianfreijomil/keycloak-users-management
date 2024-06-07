package com.api.rest.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateRequest {

    @NotNull(message = "ID cannot be null")
    private Long id;

    @NotBlank(message = "username cannot be empty")
    private String username;

    @NotBlank(message = "email cannot be empty")
    private String email;

    @NotBlank(message = "surname cannot be empty")
    private String lastName;

    @NotBlank(message = "name cannot be empty")
    private String firstName;

    @NotBlank(message = "password cannot be empty")
    private String password;

    private Set<String> roles;

    @NotNull(message = "enabled cannot be null")
    private Boolean enabled;

}
