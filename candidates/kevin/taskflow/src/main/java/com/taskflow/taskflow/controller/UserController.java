package com.taskflow.taskflow.controller;

import com.taskflow.taskflow.domain.User;
import com.taskflow.taskflow.dto.CreateUserRequest;
import com.taskflow.taskflow.repositories.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    public User createUser(){
        return null;
    }

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    public  UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/api/users")
    public User createUser(@RequestBody @Valid CreateUserRequest request){
        log.info("Creating user: {}", request);
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());

        return userRepository.save(user);
    }

}
