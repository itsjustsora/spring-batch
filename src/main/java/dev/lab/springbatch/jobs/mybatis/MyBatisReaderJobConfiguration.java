package dev.lab.springbatch.jobs.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisReaderJobConfiguration {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String MYBATIS_CHUNK_JOB = "MYBATIS_CHUNK_JOB";

    private final SqlSessionFactory sqlSessionFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public MyBatisPagingItemReader<Movie> myBatisItemReader() {
        return new MyBatisPagingItemReaderBuilder<Movie>()
            .sqlSessionFactory(sqlSessionFactory)
            .pageSize(CHUNK_SIZE)
            .queryId("dev.lab.springbatch.jobs.mybatis.selectMovies")
            .build();
    }

    @Bean
    public FlatFileItemWriter<Movie> movieCursorFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Movie>()
            .name("movieCursorFlatFileItemWriter")
            .resource(new FileSystemResource("./output/movie_new_v4.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Title", "Genre", "Year")
            .build();
    }


    @Bean
    public Step movieJdbcCursorStep() {
        log.info("------------------ Init movieJdbcCursorStep -----------------");

        return new StepBuilder("movieJdbcCursorStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(myBatisItemReader())
            .writer(movieCursorFlatFileItemWriter())
            .build();
    }

    @Bean
    public Job customerJdbcCursorPagingJob() {
        log.info("------------------ Init movieJdbcCursorPagingJob -----------------");

        return new JobBuilder(MYBATIS_CHUNK_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(movieJdbcCursorStep())
            .build();
    }
}
