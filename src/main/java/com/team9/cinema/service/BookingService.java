package com.team9.cinema.service;

import com.team9.cinema.model.Booking;
import com.team9.cinema.model.Promotions;
import com.team9.cinema.model.User;
import com.team9.cinema.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.team9.cinema.summary.BookingSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PromotionsService promotionsService;

    // get the booking history
    public List<Booking> getBookingHistory(User user) {
        return bookingRepository.findByUser(user);
    }

    // save the booking
    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }

    // get all the bookings
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // get all the booking summaries
    public List<BookingSummary> getAllBookingSummaries() {
        return bookingRepository.findAllBookingSummaries();
    }

    // calculate the discounted price
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, String discountCode) {
        if (discountCode == null || discountCode.isEmpty()) {
            return originalPrice;
        }

        // get discounted price from promotion service
        Optional<Promotions> promotion = promotionsService.findPromotionByCode(discountCode);
        if (promotion.isPresent()) {
            BigDecimal discountPercentage = BigDecimal.valueOf(promotion.get().getPercentage());
            BigDecimal discountAmount = originalPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
            return originalPrice.subtract(discountAmount);
        }

        return originalPrice; // invalid, so return original price
    }

    // refunding ticket
    public void deleteBookingByIdAndUserId(Long bookingId, Long userId) {
        bookingRepository.deleteByIdAndUserId(bookingId, userId);
    }
}