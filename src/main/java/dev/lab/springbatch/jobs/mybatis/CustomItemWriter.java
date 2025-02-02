package dev.lab.springbatch.jobs.mybatis;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import dev.lab.springbatch.jobs.domain.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomItemWriter implements ItemWriter<Movie> {

    private final CustomService customService;

    @Override
    public void write(Chunk<? extends Movie> chunk) {
        for (Movie movie: chunk) {
            log.info("Call Process in MovieItemWriter...");

            customService.processToOtherService(movie);
        }
    }
}
