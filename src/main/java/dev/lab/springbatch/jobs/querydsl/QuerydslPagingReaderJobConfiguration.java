package dev.lab.springbatch.jobs.querydsl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.jobs.domain.Movie;
import dev.lab.springbatch.jobs.domain.QMovie;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QuerydslPagingReaderJobConfiguration {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String QUERYDSL_PAGING_CHUNK_JOB = "QUERYDSL_PAGING_CHUNK_JOB";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public QuerydslPagingItemReader<Movie> movieQuerydslPagingItemReader() {
        return new QuerydslPagingItemReaderBuilder<Movie>()
            .name("movieQuerydslPagingItemReader")
            .entityManagerFactory(entityManagerFactory)
            .chunkSize(CHUNK_SIZE)
            .querySupplier(jpaQueryFactory ->
                jpaQueryFactory.select(
                    QMovie.movie
                )
                .from(QMovie.movie)
                .where(QMovie.movie.year.gt("2020")
            ))
            .build();
    }

    @Bean
    public FlatFileItemWriter<Movie> movieQuerydslFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Movie>()
            .name("movieQuerydslFlatFileItemWriter")
            .resource(new FileSystemResource("./output/movie_new_v5.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Title", "Genre", "Year")
            .build();
    }

    @Bean
    public Step movieQuerydslPagingStep() {
        log.info("------------------ Init movieQuerydslPagingStep -----------------");

        return new StepBuilder("movieQuerydslPagingStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(movieQuerydslPagingItemReader())
            .writer(movieQuerydslFlatFileItemWriter())
            .build();
    }

    @Bean
    public Job movieQuerydslPagingJob() {
        log.info("------------------ Init movieQuerydslPagingJob -----------------");
        return new JobBuilder(QUERYDSL_PAGING_CHUNK_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(movieQuerydslPagingStep())
            .build();
    }
}
