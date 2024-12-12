package com.team9.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.team9.cinema.service.UserService;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    // GET to display reset password page
    @GetMapping("/resetpassword")
    public String showResetPasswordForm() {
        return "resetpassword";
    }

    // POST to handle reset password token
    @PostMapping("/resetpassword")
    public String handleResetPassword(@RequestParam("email") String email, Model model) {
        boolean tokenSent = userService.sendResetPasswordToken(email);

        if (tokenSent) {
            model.addAttribute("message", "A reset password token has been sent to your email.");
            return "verify-resetpassword";  // redirect to token verification
        } else {
            model.addAttribute("error", "Email not found.");
            return "resetpassword";  // stay on reset password page
        }
    }

    // GET to display the token verification form
    @GetMapping("/verify-resetpassword")
    public String showVerifyResetPasswordForm() {
        return "verify-resetpassword";
    }

    // show page that password is verified
    @GetMapping("/verified-resetpassword")
    public String showVerifiedResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);  // pass token to view
        return "verified-resetpassword";  // return form
    }

    // POST to verify token and redirect to success
    @PostMapping("/verify-resetpassword")
    public String verifyResetPassword(@RequestParam("token") String token, Model model) {
        boolean isValid = userService.verifyResetPasswordToken(token);

        if (isValid) {
            System.out.println("Token is valid, redirecting to verified-resetpassword");
            return "redirect:/verified-resetpassword?token=" + token;  // redirect to next endpoint
        } else {
            System.out.println("Token is invalid");
            model.addAttribute("error", "Invalid token.");
            return "verify-resetpassword";  // stay on same page
        }
    }

    // correctly update the password
    @PostMapping("/verified-resetpassword")
    public String updatePassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // reset password conditions
        final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#_\\-])[A-Za-z\\d@$!%*?&#_\\-]{8,}$";

        // validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);  // pass token back to view
            return "verified-resetpassword";  // show page again
        }

        // password conditions
        if (!newPassword.matches(PASSWORD_REGEX)) {
            model.addAttribute("error", "Password must be at least 8 characters long and include one uppercase letter, one lowercase letter, one number, and one special character (@, $, !, %, *, ?, &, #, _, -).");
            model.addAttribute("token", token);  // pass token back
            return "verified-resetpassword";  // show page again
        }

        boolean isUpdated = userService.resetPassword(token, newPassword, confirmPassword);

        if (isUpdated) {
            model.addAttribute("message", "Password updated successfully.");
            return "redirect:/login";  // password reset success, go back to login
        } else {
            model.addAttribute("error", "Invalid token or issue updating the password.");
            return "verified-resetpassword";  // error, stay on same page
        }
    }
}