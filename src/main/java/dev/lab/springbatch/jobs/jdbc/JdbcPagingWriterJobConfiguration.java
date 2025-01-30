package dev.lab.springbatch.jobs.jdbc;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.jobs.domain.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Configuration
@RequiredArgsConstructor
public class JdbcPagingWriterJobConfiguration {

    public static final int CHUNK_SIZE = 100;
    public static final String ENCODING = "UTF-8";

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    // @Autowired
    private DataSource dataSource;

    @Bean
    public FlatFileItemReader<Movie> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<Movie>()
            .name("FlatFileItemReader")
            .resource(new ClassPathResource("./movies.csv"))
            .encoding(ENCODING)
            .delimited().delimiter(",")
            .names("title", "genre", "year")
            .targetType(Movie.class)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<Movie> jdbcBatchItemWriter() {
        return new JdbcBatchItemWriterBuilder<Movie>()
            .dataSource(dataSource)
            .sql("INSERT INTO movie2 (title, genre, year) VALUES (:title, :genre, :year)")
            .itemSqlParameterSourceProvider(new MovieItemSqlParameterSourceProvider())
            .build();
    }

    @Bean
    public Step pagingWriterStep() {
        log.info("==================== Init pagingWriterStep ====================");

        return new StepBuilder("pagingWriterStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(flatFileItemReader())
            .writer(jdbcBatchItemWriter())
            .build();
    }

    @Bean
    public Job pagingWriterJob() {
        log.info("==================== Init pagingWriterJob ====================");
        return new JobBuilder("pagingWriterJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(pagingWriterStep())
            .build();
    }
}
