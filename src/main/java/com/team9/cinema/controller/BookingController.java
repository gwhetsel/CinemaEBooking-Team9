package com.team9.cinema.controller;

import com.team9.cinema.model.*;
import com.team9.cinema.repository.BookingRepository;
import com.team9.cinema.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PromotionsService promotionsService;

    @Autowired
    private EmailService emailService;

    // display movie and info before booking
    @GetMapping("/{id}")
    public String showMovieForBooking(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        model.addAttribute("movie", movie);
        return "booking/select-showtime";
    }

    // display select showtime page
    @GetMapping("/{id}/showtimes")
    public String selectShowtime(@PathVariable("id") Long movieId, Model model) {
        Movie movie = movieService.getMovieById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", movie.getShowDates());
        return "booking/select-showtime";
    }

    // display select seats page
    @GetMapping("/{id}/seats")
    public String selectSeats(@PathVariable("id") Long movieId, @RequestParam("showtime") String showtime, Model model) {
        Movie movie = movieService.getMovieById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));
        model.addAttribute("movie", movie);
        model.addAttribute("showtime", showtime);
        return "booking/select-seats";
    }

    // display order summary page
    @GetMapping("/order-summary")
    public String showOrderSummary(@RequestParam("title") String title,
                                   @RequestParam("showtime") String showtime,
                                   @RequestParam("seats") String seats,
                                   Model model) {
        model.addAttribute("title", title);
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", Arrays.asList(seats.split(",")));
        return "booking/order-summary";
    }

    // display checkout page
    @GetMapping("/checkout")
    public String checkout(@RequestParam("title") String title,
                           @RequestParam("showtime") String showtime,
                           @RequestParam("seats") String seats,
                           @RequestParam("cost") String cost,
                           Model model,
                           HttpSession session) {
        User user = userService.getCurrentUserForDisplay();
        if (user == null) {
            return "redirect:/login";
        }

        // reset promo status for new checkout
        session.removeAttribute("promotionApplied");
        session.removeAttribute("discountedTotalCost");

        // fees and taxes
        BigDecimal salesTaxRate = BigDecimal.valueOf(0.07); // 7% tax
        BigDecimal onlineFee = BigDecimal.valueOf(2.50); // 2.50 fee

        // store original ticket price in session
        BigDecimal originalCost = new BigDecimal(cost);
        session.setAttribute("originalTotalCost", originalCost);

        // calculate tax and fees initially without discount
        BigDecimal salesTax = originalCost.multiply(salesTaxRate);
        BigDecimal totalCost = originalCost.add(salesTax).add(onlineFee);

        // model additions
        model.addAttribute("user", user);
        model.addAttribute("movieTitle", title);
        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("ticketCost", originalCost);
        model.addAttribute("salesTax", salesTax.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("onlineFee", onlineFee);
        model.addAttribute("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP));

        return "booking/checkout";
    }

    // display confirmation
    @PostMapping("/confirmation")
    public String confirmPayment(@RequestParam("movieTitle") String movieTitle,
                                 @RequestParam("showtime") String showtime,
                                 @RequestParam("seats") String seats,
                                 @RequestParam("selectedPaymentMethod") Long paymentMethodId,
                                 HttpSession session,
                                 Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        PaymentMethod selectedPaymentMethod = currentUser.getPaymentMethods()
                .stream()
                .filter(pm -> pm.getId().equals(paymentMethodId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        String maskedCardNumber = selectedPaymentMethod.getMaskedCardNumber();
        if (maskedCardNumber == null || maskedCardNumber.isEmpty()) {
            maskedCardNumber = "Unknown Card Number";
        }

        try {
            // get costs from session
            BigDecimal ticketCost = (BigDecimal) session.getAttribute("originalTotalCost");
            BigDecimal discountedTicketCost = (BigDecimal) session.getAttribute("discountedTotalCost");
            BigDecimal discountAmount = (BigDecimal) session.getAttribute("discountAmount");
            if (discountedTicketCost == null) {
                discountedTicketCost = ticketCost; // no discount
                discountAmount = BigDecimal.ZERO; // no discount
            }

            // fees and taxes
            BigDecimal salesTaxRate = BigDecimal.valueOf(0.07); // 7% tax
            BigDecimal onlineFee = BigDecimal.valueOf(2.50); // $2.50 fee

            // caluclate tax and fees
            BigDecimal salesTax = discountedTicketCost.multiply(salesTaxRate);
            BigDecimal totalCost = discountedTicketCost.add(salesTax).add(onlineFee);

            // save booking to db
            Booking booking = new Booking();
            booking.setUser(currentUser);
            booking.setMovieTitle(movieTitle);
            booking.setShowtime(showtime);
            booking.setSeats(seats);
            booking.setTotalCost(totalCost);
            booking.setPaymentMethod(maskedCardNumber);
            booking.setBookingDate(LocalDateTime.now());

            bookingRepository.save(booking);

            // model additions
            model.addAttribute("movieTitle", movieTitle);
            model.addAttribute("showtime", showtime);
            model.addAttribute("seats", seats);
            model.addAttribute("ticketCost", discountedTicketCost.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("discountAmount", discountAmount.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("salesTax", salesTax.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("onlineFee", onlineFee);
            model.addAttribute("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP));
            model.addAttribute("paymentMethodDetails", maskedCardNumber);

            // booking confirmation email
            String subject = "Booking Confirmation";
            String emailBody = String.format(
                    """
                    Dear %s %s,
                    
                    Thank you for your booking. Here are the details:
                    
                    Movie: %s
                    Showtime: %s
                    Seats: %s
                    Original Ticket Cost: $%.2f
                    Discount Amount: $%.2f
                    Discounted Ticket Cost: $%.2f
                    Online Booking Fee: $%.2f
                    Sales Tax: $%.2f
                    Total Cost: $%.2f
                    Payment Method: %s
                    
                    Enjoy your movie!
                    Best Regards,
                    Cinema Team
                    """,
                    currentUser.getFirstName(),
                    currentUser.getLastName(),
                    movieTitle,
                    showtime,
                    seats,
                    ticketCost.doubleValue(),
                    discountAmount.doubleValue(),
                    discountedTicketCost.doubleValue(),
                    onlineFee.doubleValue(),
                    salesTax.doubleValue(),
                    totalCost.doubleValue(),
                    maskedCardNumber
            );
            emailService.sendEmail(currentUser.getEmail(), subject, emailBody);

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while processing your booking. Please try again.");
            return "booking/checkout";
        }

        return "booking/confirmation";
    }


    // booking history display
    @GetMapping("/history")
    public String viewBookingHistory(Model model) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.getBookingHistory(currentUser);
        model.addAttribute("bookings", bookings);
        return "booking/history";
    }

    // get seats already booked
    @GetMapping("/booked")
    @ResponseBody
    public ResponseEntity<List<String>> getBookedSeats(@RequestParam String movieTitle, @RequestParam String showtime) {
        try {
            List<String> bookedSeats = bookingRepository.findBookedSeats(movieTitle, showtime);
            return ResponseEntity.ok(bookedSeats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // apply promotions
    @GetMapping("/apply-promotion")
    public ResponseEntity<?> applyPromotion(@RequestParam("code") String code, HttpSession session) {
        // check if already applied
        if (session.getAttribute("promotionApplied") != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A promotion has already been applied.");
        }

        Optional<Promotions> promotion = promotionsService.findPromotionByCode(code);
        if (promotion.isPresent()) {
            Object originalCostAttr = session.getAttribute("originalTotalCost");
            if (originalCostAttr == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Original cost not found in session.");
            }

            BigDecimal originalCost = new BigDecimal(originalCostAttr.toString());
            BigDecimal discountPercentage = BigDecimal.valueOf(promotion.get().getPercentage());
            BigDecimal discountAmount = originalCost.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
            BigDecimal discountedCost = originalCost.subtract(discountAmount);

            // store discount details in session
            session.setAttribute("discountedTotalCost", discountedCost);
            session.setAttribute("discountAmount", discountAmount);
            session.setAttribute("promotionApplied", true);

            return ResponseEntity.ok(Map.of(
                    "message", "Promotion applied successfully!",
                    "discount", promotion.get().getPercentage(),
                    "originalPrice", originalCost,
                    "discountAmount", discountAmount,
                    "discountedPrice", discountedCost
            ));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid promotion code.");
    }

    // refund order functionality
    @DeleteMapping("/{bookingId}/{userId}")
    public ResponseEntity<Void> refundBooking(@PathVariable Long bookingId, @PathVariable Long userId) {
        try {
            bookingService.deleteBookingByIdAndUserId(bookingId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }    
}
