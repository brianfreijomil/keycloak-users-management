package com.api.rest.service;

import com.api.rest.http.UserCreateRequest;
import com.api.rest.http.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IKeycloakService {

    List<UserResponse> findAllUsers(String idCurrentUser);
    UserResponse findUserByUsername(String username);
    ResponseEntity<?> create(UserCreateRequest user);
    ResponseEntity<?> delete(String userId);
    ResponseEntity<?> update(String userId, UserCreateRequest user);
}
