package com.team9.cinema.service;

import com.team9.cinema.model.User;
import com.team9.cinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationTokenService {

    @Autowired
    private UserRepository userRepository;

    public void createVerificationToken(User user, String token) {
        user.setVerificationToken(token);
        userRepository.save(user);
    }

    public String validateVerificationToken(String token) {
        // Retrieve the user associated with the token
        User user = userRepository.findByVerificationToken(token).orElse(null);

        if (user == null) {
            return "invalid";  // Token not found
        }

        // Successful verification so enable user
        user.setEnabled(true);

        // clear token for security purposes
        user.setVerificationToken(null);

        // save user to database
        userRepository.save(user);

        return "valid";  // Token successfully validated
    }

}
