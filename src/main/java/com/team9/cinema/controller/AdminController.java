package com.team9.cinema.controller;

import com.team9.cinema.model.User;
import com.team9.cinema.summary.BookingSummary;
import com.team9.cinema.service.BookingService;
import com.team9.cinema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    // redirect to movie list
    @GetMapping("/manage-movies")
    public String manageMovies() {
        return "redirect:/movies?adminPortal=true";
    }

    // display admin dashboard
    @GetMapping
    public String adminDashboard(Model model) {
        // Add current admin information to the model
        model.addAttribute("admin", userService.getCurrentUser());
        return "admin/admin"; // Renders admin.html
    }

    // manage users
    @GetMapping("/manage-users")
    public String manageUsers(Model model) {
        // fetch all users from service and add to model
        model.addAttribute("users", userService.getAllUsers());
        return "admin/manage-users"; // render html page
    }

    // edit users
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/edit-user"; // render page
    }

    // update users
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.updateUser(user.getId(), user); // pass id and user object
            model.addAttribute("message", "User updated successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update user: " + e.getMessage());
        }
        return "admin/edit-user";
    }

    // delete user
    @DeleteMapping("/users/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return ResponseEntity.ok().build(); // success
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // manage sales
    @GetMapping("/manage-sales")
    public String manageSales(Model model) {
        // get all bookings
        List<BookingSummary> bookings = bookingService.getAllBookingSummaries();

        // calculate total sales
        BigDecimal totalSales = bookings.stream()
                .map(BookingSummary::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // format total sales as currency
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedTotalSales = currencyFormatter.format(totalSales);

        // add data to model
        model.addAttribute("bookings", bookings); // booking summary
        model.addAttribute("totalSales", formattedTotalSales); // total sales formatted

        return "admin/manage-sales"; // render page
    }
}