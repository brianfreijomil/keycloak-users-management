package com.api.rest.controller;

import com.api.rest.http.UserCreateRequest;
import com.api.rest.http.UserResponse;
import com.api.rest.service.IKeycloakService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api-test")
public class TestController {

    private final IKeycloakService keycloakService;

    @GetMapping("")
    @PreAuthorize("hasRole('admin_client_role')")
    public List<UserResponse> getAllUsers(@Valid String id_current_user) {
        return this.keycloakService.findAllUsers(id_current_user);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('admin_client_role')")
    public UserResponse getUserByUsername(@PathVariable String username) {
        return this.keycloakService.findUserByUsername(username);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('admin_client_role')")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateRequest user) {
        return this.keycloakService.create(user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('admin_client_role')")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UserCreateRequest user, @PathVariable String userId) {
        return this.keycloakService.update(userId,user);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('admin_client_role')")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        return this.keycloakService.delete(userId);
    }

    /* first tests getting 403 , 401*/

    @GetMapping("/hello")
    @PreAuthorize("hasRole('admin_client_role')")
    public String firstTest() {
        return "hello user";
    }

    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('user_client_role') or hasRole('admin_client_role')")
    public String secondTest() {
        return "hello user 2";
    }


}

