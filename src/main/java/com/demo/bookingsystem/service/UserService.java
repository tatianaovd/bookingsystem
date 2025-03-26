package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.dto.UserRequest;
import com.demo.bookingsystem.domain.entity.User;
import com.demo.bookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User addUser(UserRequest userRequest) {
        User user = User.builder()
                .username(userRequest.getUsername())
                .surname(userRequest.getSurname())
                .build();
        return userRepository.save(user);
    }

    public User updateUser(Long userId, UserRequest userRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setUsername(userRequest.getUsername());
        existingUser.setSurname(userRequest.getSurname());

        return userRepository.save(existingUser);
    }
}
