package com.team9.cinema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String director;

    @Column(nullable = false)
    private String producer;

    @Column(nullable = false)
    private String cast;

    @Column(nullable = true)
    private String synopsis;

    @Column(nullable = true)
    private String trailerUrl;  // embeded code (youtube) only not full link

    @Column(nullable = true)
    private String trailerImageUrl;

    @Column(nullable = true)
    private String mpaaRating;

    @Column(nullable = true)
    private String posterUrl;

    @Column(nullable = true)
    private String reviews;

    @Column(nullable = true)
    private String status;

    @Column(nullable = true)
    @ElementCollection
    private List<String> showDates;

}
