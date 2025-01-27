package dev.lab.springbatch.configuration;

import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.domain.Movie;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaPagingReaderJobConfiguration {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public JpaPagingItemReader<Movie> jpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<Movie>()
            .name("jpaPagingItemReader")
            .queryString("SELECT m FROM Movie m WHERE m.year > :year ORDER BY m.id DESC")
            .pageSize(CHUNK_SIZE)
            .entityManagerFactory(entityManagerFactory)
            .parameterValues(Collections.singletonMap("year", "2000"))
            .build();
    }

    @Bean
    public FlatFileItemWriter<Movie> flatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Movie>()
            .name("flatFileItemWriter")
            .resource(new FileSystemResource("./output/movie_new_v2.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Title", "Genre", "Year")
            .build();
    }

    @Bean
    public Step jpaPagingStep() {
        log.info("==================== Init jpaPagingStep ====================");

        return new StepBuilder("jpaPagingStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(jpaPagingItemReader())
            .writer(flatFileItemWriter())
            .build();
    }

    @Bean
    public Job jpaPagingJob() {
        log.info("==================== Init jpaPagingJob ====================");
        return new JobBuilder("jpaPagingJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(jpaPagingStep())
            .build();
    }
}
