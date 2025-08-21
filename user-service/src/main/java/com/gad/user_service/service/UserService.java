package com.gad.user_service.service;

import org.springframework.stereotype.Service;

import com.gad.user_service.model.User;
import com.gad.user_service.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }
}
