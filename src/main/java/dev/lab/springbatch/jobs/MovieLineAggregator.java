package dev.lab.springbatch.jobs;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.item.file.transform.LineAggregator;

import dev.lab.springbatch.domain.Movie;

public class MovieLineAggregator implements LineAggregator<Movie> {

    @Override
    public String aggregate(Movie item) {
        return item.getTitle() + "," + item.getGenre() + "," + item.getYear();
    }
}
