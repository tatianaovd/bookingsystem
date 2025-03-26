package com.demo.bookingsystem.service;

import com.demo.bookingsystem.domain.dto.UserRequest;
import com.demo.bookingsystem.domain.entity.User;
import com.demo.bookingsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testAddUser() {
        UserRequest userRequest = new UserRequest("testUsername", "testSurname");
        User userToSave = User.builder().username("testUsername").surname("testSurname").build();
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        User createdUser = userService.addUser(userRequest);

        assertNotNull(createdUser);
        assertEquals("testUsername", createdUser.getUsername());
        assertEquals("testSurname", createdUser.getSurname());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserExists() {
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("newUsername", "newSurname");
        User existingUser = User.builder().id(userId).username("oldUsername").surname("oldSurname").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User updatedUser = userService.updateUser(userId, userRequest);

        assertNotNull(updatedUser);
        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("newSurname", updatedUser.getSurname());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("newUsername", "newSurname");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(userId, userRequest));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }
}
