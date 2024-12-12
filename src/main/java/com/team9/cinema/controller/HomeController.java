package com.team9.cinema.controller;

import com.team9.cinema.dtos.UserRegistrationDto;
import com.team9.cinema.model.User;
import com.team9.cinema.service.EmailService;
import com.team9.cinema.model.Booking;
import com.team9.cinema.model.SearchQuery;
import com.team9.cinema.service.MovieService;
import com.team9.cinema.service.UserService;
import com.team9.cinema.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // display edit profile screen
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model) {
        User currentUser = userService.getCurrentUserForDisplay();
        model.addAttribute("user", currentUser);
        return "user/edit-profile";
    }

    // correctly update profile
    @PostMapping("/edit-profile")
    public String updateProfile(@ModelAttribute("user") UserRegistrationDto userDto, Model model) {
        User currentUser = userService.getCurrentUser();

        // validate pass
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#_\\-])[A-Za-z\\d@$!%*?&#_\\-]{8,}$";

        if (userDto.getNewPassword() != null && !userDto.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(userDto.getCurrentPassword(), currentUser.getPassword())) {
                model.addAttribute("error", "Current password is incorrect.");
                return showEditProfileForm(model);
            }
            if (!userDto.getNewPassword().matches(passwordRegex)) {
                model.addAttribute("error", "New password must meet the requirements: at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.");
                return showEditProfileForm(model);
            }
            currentUser.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
        }

        // update user info
        currentUser.setFirstName(userDto.getFirstName());
        currentUser.setLastName(userDto.getLastName());
        currentUser.setPhoneNumber(userDto.getPhoneNumber());
        currentUser.setPromotions(userDto.isPromotions());

        // save user
        userService.save(currentUser);

        // send email for update
        String subject = "Profile Updated Successfully";
        String body = String.format("Dear %s %s,\n\nYour profile information has been updated successfully.\n\nBest Regards,\nCinema Team",
                currentUser.getFirstName(), currentUser.getLastName());
        emailService.sendEmail(currentUser.getEmail(), subject, body);

        model.addAttribute("message", "Profile updated successfully.");
        return showEditProfileForm(model);
    }

    // display order history in navbar button
    @GetMapping("/history")
    public String showHistory(Model model) {
        // retrieve user
        User currentUser = userService.getCurrentUser();

        // fetch history
        List<Booking> bookings = bookingService.getBookingHistory(currentUser);

        // add history to model
        model.addAttribute("bookings", bookings);

        return "booking/history";
    }

    // display home page
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("searchQuery", new SearchQuery());
        model.addAttribute("currentlyRunning", movieService.getCurrentlyRunningMovies());
        model.addAttribute("comingSoon", movieService.getComingSoonMovies());
        return "home";
    }
}