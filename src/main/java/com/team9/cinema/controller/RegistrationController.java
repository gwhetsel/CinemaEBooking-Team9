package com.team9.cinema.controller;

import com.team9.cinema.dtos.UserRegistrationDto;
import com.team9.cinema.event.OnRegistrationCompleteEvent;
import com.team9.cinema.model.User;
import com.team9.cinema.service.UserService;
import com.team9.cinema.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenService tokenService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#_\\-])[A-Za-z\\d@$!%*?&#_\\-]{8,}$";

    // display registration
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "registration";
    }

    // register a new account
    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto userDto, Model model) {
        // trim the inputs
        userDto.setEmail(userDto.getEmail().trim());
        userDto.setFirstName(userDto.getFirstName().trim());
        userDto.setLastName(userDto.getLastName().trim());
        userDto.setPhoneNumber(userDto.getPhoneNumber().trim());
        if (userDto.getPassword() != null) {
            userDto.setPassword(userDto.getPassword().trim());
        }

        // check if email exists
        if (userService.emailExists(userDto.getEmail())) {
            model.addAttribute("error", "An account with this email already exists.");
            model.addAttribute("user", userDto);
            return "registration";
        }

        // validate pass
        if (!isValidPassword(userDto.getPassword())) {
            model.addAttribute("error", "Password must be at least 8 characters long and include one uppercase letter, one lowercase letter, one number, and one special character (@, $, !, %, *, ?, &, #, _, -).");
            model.addAttribute("user", userDto);
            return "registration";
        }

        try {
            // register new user
            User registeredUser = userService.registerNewUserAccount(userDto);

            // email verification
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registeredUser, baseUrl));
            model.addAttribute("message", "Registration successful! A verification token has been sent to your email.");
            return "verify-email";
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            model.addAttribute("error", "Registration failed. Please try again later.");
            model.addAttribute("user", userDto);
            return "registration";
        }
    }

    // verification page
    @GetMapping("/verify-email")
    public String showTokenVerificationPage() {
        return "verify-email";
    }

    // verify token and enable account
    @PostMapping("/verify-token")
    public String verifyToken(@RequestParam("token") String token, Model model) {
        String result = tokenService.validateVerificationToken(token);

        if ("valid".equals(result)) {
            model.addAttribute("message", "Verification successful!");
            return "verified";
        } else {
            model.addAttribute("error", "Verification failed. Invalid or expired token.");
            return "verify-email";
        }
    }

    // validate pass format
    private boolean isValidPassword(String password) {
        return Pattern.matches(PASSWORD_REGEX, password);
    }
}