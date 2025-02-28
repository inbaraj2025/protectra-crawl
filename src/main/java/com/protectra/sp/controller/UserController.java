package com.protectra.sp.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.protectra.sp.entity.User;

@RestController
public class UserController {

    @GetMapping("/users")
    public List<User> getAllUsers() {
        // Dummy data for demonstration
        User user1 = new User("1", "John Doe", "johndoe@example.com");
        User user2 = new User("2", "Jane Smith", "janesmith@example.com");
        User user3 = new User("3", "Alice Johnson", "alicejohnson@example.com");

        // Returning the list of dummy users
        return Arrays.asList(user1, user2, user3);
    }
}
