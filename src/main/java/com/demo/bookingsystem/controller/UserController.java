package com.demo.bookingsystem.controller;

import com.demo.bookingsystem.domain.dto.UserRequest;
import com.demo.bookingsystem.domain.entity.User;
import com.demo.bookingsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "APIs for managing users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Add a new user", description = "Creates a new user account.")
    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody UserRequest userRequest) {
        User createdUser = userService.addUser(userRequest);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Update user details", description = "Updates an existing user’s details.")
    @PutMapping("/update/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody UserRequest userRequest) {
        User updatedUser = userService.updateUser(userId, userRequest);

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}
