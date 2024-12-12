package com.team9.cinema.controller;

import com.team9.cinema.model.Promotions;
import com.team9.cinema.model.User;
import com.team9.cinema.service.EmailService;
import com.team9.cinema.service.PromotionsService;
import com.team9.cinema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/manage-promotions")
public class PromotionsController {

    @Autowired
    private PromotionsService promotionsService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // display manage promotions page
    @GetMapping
    public String showManagePromotions(Model model) {
        model.addAttribute("promotion", new Promotions());
        model.addAttribute("promotionsList", promotionsService.getAllPromotions()); // add list of promotions
        model.addAttribute("subscribedUsers", userService.getSubscribedUsers()); // list subscribed users
        return "admin/manage-promotions";
    }

    // save to db
    @PostMapping("/create")
    public String createPromotion(@ModelAttribute("promotion") Promotions promotion) {
        promotionsService.createPromotion(promotion);
        return "redirect:/admin/manage-promotions";
    }

    // delete from db
    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionsService.deletePromotionById(id);
        return "redirect:/admin/manage-promotions";
    }

    // send email
    @PostMapping("/send-email/{id}")
    public String sendPromotionEmail(@PathVariable Long id, Model model) {
        // get promotion from id
        Promotions promotion = promotionsService.getPromotionById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        // get list of subscribed users
        List<User> subscribedUsers = userService.getSubscribedUsers();

        // send email to all subbed users
        subscribedUsers.forEach(user -> {
            String subject = "Exclusive Promotion: " + promotion.getTitle();
            String message = String.format(
                    "Dear %s %s,\n\n%s\n\nUse the promotion code \"%s\" to save %.0f%% on your next booking!\n\nBest Regards,\nCinema Team",
                    user.getFirstName(), user.getLastName(), promotion.getMessage(), promotion.getCode(), promotion.getPercentage());

            emailService.sendEmail(user.getEmail(), subject, message);
        });

        // success
        model.addAttribute("message", "Promotional email sent successfully to all subscribed users!");

        return "redirect:/admin/manage-promotions";
    }

}
