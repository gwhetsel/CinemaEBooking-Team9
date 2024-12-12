package com.team9.cinema.service;

import com.team9.cinema.model.Movie;
import com.team9.cinema.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    // Implement search functionality
    public List<Movie> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    // get all the movies
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // get a movie by the id
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    // add a movie
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // update the movie
    public Movie updateMovie(Long id, Movie movieDetails) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setTitle(movieDetails.getTitle());
        movie.setCategory(movieDetails.getCategory());
        movie.setCast(movieDetails.getCast());
        movie.setDirector(movieDetails.getDirector());
        movie.setProducer(movieDetails.getProducer());
        movie.setSynopsis(movieDetails.getSynopsis());
        movie.setTrailerUrl(movieDetails.getTrailerUrl());
        movie.setTrailerImageUrl(movieDetails.getTrailerImageUrl());
        movie.setMpaaRating(movieDetails.getMpaaRating());
        movie.setShowDates(movieDetails.getShowDates());
        movie.setReviews(movieDetails.getReviews());
        movie.setPosterUrl(movieDetails.getPosterUrl());
        movie.setStatus(movieDetails.getStatus());
        movie.setGenre(movieDetails.getGenre());

        return movieRepository.save(movie);
    }

    // delete movie
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    // get the currently running
    public List<Movie> getCurrentlyRunningMovies() {
        return movieRepository.findByStatus("currently_running");
    }

    // get the coming soon
    public List<Movie> getComingSoonMovies() {
        return movieRepository.findByStatus("coming_soon");
    }

    // filter by genre functionality for currently running
    public List<Movie> filterCurrentlyRunningMoviesByGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return getCurrentlyRunningMovies();
        }

        return movieRepository.findAll().stream()
                .filter(movie -> movie.getStatus().equalsIgnoreCase("currently_running"))
                .filter(movie -> genres.stream().anyMatch(genre -> movie.getGenre().toLowerCase().contains(genre.toLowerCase())))
                .collect(Collectors.toList());
    }

    // coming soon filter by genre
    public List<Movie> filterComingSoonMoviesByGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return getComingSoonMovies();
        }

        return movieRepository.findByStatus("coming_soon").stream()
                .filter(movie -> genres.stream()
                        .anyMatch(genre -> movie.getGenre().toLowerCase().contains(genre.toLowerCase())))
                .collect(Collectors.toList());
    }
}