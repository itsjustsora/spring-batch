package dev.lab.springbatch.jobs.mybatis;

import org.springframework.batch.item.ItemProcessor;

import dev.lab.springbatch.jobs.domain.Movie;

public class After20YearsItemProcessor implements ItemProcessor<Movie, Movie> {
    @Override
    public Movie process(Movie item) {
        int year = Integer.parseInt(item.getYear());
        int result = year + 20;

        item.setYear(String.valueOf(result));
        return item;
    }
}
