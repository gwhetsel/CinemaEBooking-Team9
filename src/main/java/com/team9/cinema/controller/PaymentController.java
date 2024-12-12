package com.team9.cinema.controller;

import com.team9.cinema.model.PaymentMethod;
import com.team9.cinema.model.User;
import com.team9.cinema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {

    @Autowired
    private UserService userService;

    // delete payment method functionality
    @DeleteMapping("/delete-payment/{paymentMethodId}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable Long paymentMethodId) {
        User currentUser = userService.getCurrentUser();

        PaymentMethod paymentMethodToDelete = currentUser.getPaymentMethods().stream()
                .filter(pm -> pm.getId().equals(paymentMethodId))
                .findFirst()
                .orElse(null);

        if (paymentMethodToDelete != null) {
            currentUser.getPaymentMethods().remove(paymentMethodToDelete);
            userService.save(currentUser);
            return ResponseEntity.ok("Payment method deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment method not found.");
        }
    }

    // add payment method
    @PostMapping("/add-payment")
    public String addPaymentMethod(
        @ModelAttribute PaymentMethod paymentMethod,
        @RequestParam(required = false, defaultValue = "edit-profile") String redirect,
        Model model) {
    
        User currentUser = userService.getCurrentUser();
    
        // 3 payment methods condition
        if (currentUser.getPaymentMethods().size() >= 3) {
            model.addAttribute("error", "You can only save up to 3 payment methods.");
            model.addAttribute("user", userService.getCurrentUserForDisplay());
            return "fragments/payment-methods :: payment-methods-section";
        }
    
        // validate payment method
        if (!userService.isPaymentMethodValid(paymentMethod)) {
            model.addAttribute("error", "Invalid payment method details. Please check your inputs.");
            model.addAttribute("user", userService.getCurrentUserForDisplay());
            return "fragments/payment-methods :: payment-methods-section";
        }
    
        // add payment method
        PaymentMethod encryptedPaymentMethod = userService.createEncryptedPaymentMethod(paymentMethod);
        encryptedPaymentMethod.setUser(currentUser);
        currentUser.getPaymentMethods().add(encryptedPaymentMethod);
        userService.save(currentUser);
    
        // payment method fragment for edit profile
        model.addAttribute("user", userService.getCurrentUserForDisplay());
        return "fragments/payment-methods :: payment-methods-section";
    }    

    // get payment methods for edit profile
    @GetMapping("/payment-methods")
    public String getPaymentMethods(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "fragments/payment-methods :: payment-methods-section";
    }    
}