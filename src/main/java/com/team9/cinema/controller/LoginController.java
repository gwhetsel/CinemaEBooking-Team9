package com.team9.cinema.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    // show login form
    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "suspended", required = false) String suspendedError,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }

        if (suspendedError != null) {
            model.addAttribute("suspendedError", "Your account has been suspended. Please contact teamninecinema@gmail.com for further details.");
        }

        return "login"; // render page
    }
}
