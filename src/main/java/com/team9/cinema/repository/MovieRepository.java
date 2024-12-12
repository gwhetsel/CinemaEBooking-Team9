package com.team9.cinema.repository;

import com.team9.cinema.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByStatus(String status);
    List<Movie> findByStatusAndGenreContainingIgnoreCase(String status, String genre);
    List<Movie> findByStatusAndGenreIn(String status, List<String> genres);    
}