package dev.lab.springbatch.jobs.basic;

import org.springframework.batch.item.file.transform.LineAggregator;

import dev.lab.springbatch.jobs.domain.Movie;

public class MovieLineAggregator implements LineAggregator<Movie> {

    @Override
    public String aggregate(Movie item) {
        return item.getTitle() + "," + item.getGenre() + "," + item.getYear();
    }
}
