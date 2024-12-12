package com.team9.cinema.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingSummary {
    private Long id;
    private LocalDateTime bookingDate;
    private String movieTitle;
    private String seats;
    private String showtime;
    private BigDecimal totalCost;
    private Long userId;

    // constructor
    public BookingSummary(Long id, LocalDateTime bookingDate, String movieTitle, String seats, String showtime, BigDecimal totalCost, Long userId) {
        this.id = id;
        this.bookingDate = bookingDate;
        this.movieTitle = movieTitle;
        this.seats = seats;
        this.showtime = showtime;
        this.totalCost = totalCost;
        this.userId = userId;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}