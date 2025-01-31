package dev.lab.springbatch.jobs.mybatis;

import org.springframework.batch.item.ItemProcessor;

import dev.lab.springbatch.jobs.domain.Movie;

public class LowerCaseItemProcessor implements ItemProcessor<Movie, Movie> {
    @Override
    public Movie process(Movie item) throws Exception {
        item.setTitle(item.getTitle().toLowerCase());
        item.setGenre(item.getGenre().toLowerCase());
        return item;
    }
}
