package dev.lab.springbatch.jobs;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.item.ItemProcessor;

import dev.lab.springbatch.domain.Movie;

public class AggregateMovieProcessor implements ItemProcessor<Movie, Movie> {

    private final ConcurrentHashMap<String, Integer> aggregateMovies;

    public AggregateMovieProcessor(ConcurrentHashMap<String, Integer> aggregateMovies) {
        this.aggregateMovies = aggregateMovies;
    }

    @Override
    public Movie process(Movie item) {
        aggregateMovies.putIfAbsent("TOTAL_MOVIES", 0);

        aggregateMovies.put("TOTAL_MOVIES", aggregateMovies.get("TOTAL_MOVIES") + 1);
        return item;
    }
}
