package com.team9.cinema.repository;

import com.team9.cinema.model.Booking;
import com.team9.cinema.model.User;
import com.team9.cinema.summary.BookingSummary; // Correctly imported BookingSummary
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // get all bookings for that user
    List<Booking> findByUser(User user);

    // seats without the ticket type added
    @Query("SELECT b.seats FROM Booking b WHERE b.movieTitle = :movieTitle AND b.showtime = :showtime")
    List<String> findRawBookedSeats(@Param("movieTitle") String movieTitle, @Param("showtime") String showtime);

    default List<String> findBookedSeats(String movieTitle, String showtime) {
        List<String> rawSeats = findRawBookedSeats(movieTitle, showtime);

        return rawSeats.stream()
                .filter(seats -> seats != null && !seats.trim().isEmpty()) // null/empty strings
                .flatMap(seats -> Stream.of(seats.split(","))) // comma split
                .map(seat -> seat.split("-")[0].trim()) // extract seat before -
                .filter(seatCode -> !seatCode.isEmpty()) // exclude empty seats
                .collect(Collectors.toList());
    }

    // get booking summary for admins
    @Query("SELECT new com.team9.cinema.summary.BookingSummary(b.id, b.bookingDate, b.movieTitle, b.seats, b.showtime, b.totalCost, b.user.id) FROM Booking b")
    List<BookingSummary> findAllBookingSummaries();

    // delete for refunding ticket
    @Modifying
    @Transactional
    @Query("DELETE FROM Booking b WHERE b.id = :bookingId AND b.user.id = :userId")
    void deleteByIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);
}
