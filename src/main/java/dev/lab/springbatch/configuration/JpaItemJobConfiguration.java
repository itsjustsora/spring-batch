package dev.lab.springbatch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.domain.Movie;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JpaItemJobConfiguration {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

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
    public JpaItemWriter<Movie> jpaItemWriter() {
        return new JpaItemWriterBuilder<Movie>()
            .entityManagerFactory(entityManagerFactory)
            .usePersist(true)
            .build();
    }

    @Bean
    public Step flatFileStep() {
        log.info("==================== Init flatFileStep ====================");

        return new StepBuilder("flatFileStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(flatFileItemReader())
            .writer(jpaItemWriter())
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
}
