package dev.lab.springbatch.configuration;

import static javax.xml.transform.OutputKeys.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.domain.Movie;
import dev.lab.springbatch.jobs.AggregateMovieProcessor;
import dev.lab.springbatch.jobs.MovieFooter;
import dev.lab.springbatch.jobs.MovieHeader;
import dev.lab.springbatch.jobs.MovieLineAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileJobConfiguration {

    public static final int CHUNK_SIZE = 100;
    public static final String ENCODING = "UTF-8";

    private final ConcurrentHashMap<String, Integer> aggregateInfos = new ConcurrentHashMap<>();

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    private final ItemProcessor<Movie, Movie> itemProcessor = new AggregateMovieProcessor(aggregateInfos);

    @Bean
    public Step flatFileStep() {
        log.info("==================== Init flatFileStep ====================");

        return new StepBuilder("flatFileStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(flatFileItemReader())
            .processor(itemProcessor)
            .writer(flatFileItemWriter())
            .build();
    }

    @Bean
    public Job flatFileJob() {
        log.info("==================== Init flatFileJob ====================");
        return new JobBuilder("flatFileJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(flatFileStep())
            .build();
    }

    @Bean
    public FlatFileItemReader<Movie> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<Movie>()
            .name("flatFileItemReader")
            .resource(new ClassPathResource("./movies.csv"))
            .encoding(ENCODING)
            .delimited().delimiter(",")
            .names("title", "genre", "year")
            .targetType(Movie.class)
            .build();
    }

    @Bean
    public FlatFileItemWriter<Movie> flatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Movie>()
            .name("flatFileItemWriter")
            .resource(new FileSystemResource("./output/movie_new.csv"))
            .encoding(ENCODING)
            .delimited().delimiter(",")
            .names("title", "genre", "year")
            .append(false)
            .lineAggregator(new MovieLineAggregator())
            .headerCallback(new MovieHeader())
            .footerCallback(new MovieFooter(aggregateInfos))
            .build();

    }


}
