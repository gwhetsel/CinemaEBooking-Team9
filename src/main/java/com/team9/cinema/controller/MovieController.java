package com.team9.cinema.controller;

import com.team9.cinema.model.Movie;
import com.team9.cinema.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    // list the movies
    @GetMapping
    public String listMovies(Model model, @RequestParam(value = "adminPortal", required = false) Boolean adminPortal) {
        List<Movie> allMovies = movieService.getAllMovies();

        // filter currently playing
        List<Movie> currentlyRunning = allMovies.stream()
                .filter(movie -> "currently_running".equalsIgnoreCase(movie.getStatus()))
                .collect(Collectors.toList());

        // filter coming soon
        List<Movie> comingSoon = allMovies.stream()
                .filter(movie -> "coming_soon".equalsIgnoreCase(movie.getStatus()))
                .collect(Collectors.toList());

        // add to model
        model.addAttribute("currentlyRunning", currentlyRunning);
        model.addAttribute("comingSoon", comingSoon);

        // if adminPortal true, show add movie button
        if (adminPortal != null && adminPortal) {
            model.addAttribute("adminPortal", true);
        } else {
            model.addAttribute("adminPortal", false);
        }

        return "movies/list-movies";  // render movie list
    }

    // view the movie details
    @GetMapping("/{id}")
    public String viewMovieDetails(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        model.addAttribute("movie", movie);
        return "movies/movie-details";  // render page
    }

    // search movies by title
    @GetMapping("/search")
    public String searchMovies(@RequestParam("title") String title, Model model) {
        List<Movie> movies = movieService.searchMoviesByTitle(title);

        // filter currently running
        List<Movie> currentlyRunning = movies.stream()
                .filter(movie -> "currently_running".equalsIgnoreCase(movie.getStatus()))
                .collect(Collectors.toList());

        // filter coming soon
        List<Movie> comingSoon = movies.stream()
                .filter(movie -> "coming_soon".equalsIgnoreCase(movie.getStatus()))
                .collect(Collectors.toList());

        // add to model
        model.addAttribute("currentlyRunning", currentlyRunning);
        model.addAttribute("comingSoon", comingSoon);

        return "movies/list-movies";
    }

    // add a movie display
    @GetMapping("/add")
    public String showAddMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "movies/add-movie";
    }

    // add a movie
    @PostMapping("/add")
    public String addMovie(@ModelAttribute("movie") Movie movie) {
        movieService.addMovie(movie);
        return "redirect:/movies?adminPortal=true";
    }

    // display edit option
    @GetMapping("/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        model.addAttribute("movie", movie);
        return "movies/edit-movie";  // render
    }

    // update the movie properly
    @PostMapping("/edit/{id}")
    public String updateMovie(@PathVariable Long id, @ModelAttribute("movie") Movie movieDetails) {
        movieService.updateMovie(id, movieDetails);  // access movie service
        return "redirect:/movies?adminPortal=true";  // back to admin movie list
    }

    // delete movie functionality
    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/movies?adminPortal=true";  // back to admin movie list
    }

    // filter by the genre
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<?> filterMovies(
            @RequestParam String category,
            @RequestParam(required = false) List<String> genres) {
        try {
            List<Movie> movies;
            if (genres == null || genres.isEmpty()) {
                movies = category.equalsIgnoreCase("currentlyRunning")
                        ? movieService.getCurrentlyRunningMovies()
                        : movieService.getComingSoonMovies();
            } else {
                movies = category.equalsIgnoreCase("currentlyRunning")
                        ? movieService.filterCurrentlyRunningMoviesByGenres(genres)
                        : movieService.filterComingSoonMoviesByGenres(genres);
            }
            return ResponseEntity.ok(movies); // return json response
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"Invalid category\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"An unexpected error occurred.\"}");
        }
    }
}