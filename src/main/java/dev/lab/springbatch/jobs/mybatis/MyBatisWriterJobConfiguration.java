package dev.lab.springbatch.jobs.mybatis;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.jobs.domain.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisWriterJobConfiguration {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String MYBATIS_WRITER_JOB = "MYBATIS_WRITER_JOB";

    private final SqlSessionFactory sqlSessionFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public FlatFileItemReader<Movie> mybatisFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<Movie>()
            .name("mybatisFlatFileItemReader")
            .resource(new ClassPathResource("./movies.csv"))
            .encoding(ENCODING)
            .delimited().delimiter(",")
            .names("title", "genre", "year")
            .targetType(Movie.class)
            .build();
    }

    @Bean
    public MyBatisBatchItemWriter<Movie> myBatisBatchItemWriter() {
        return new MyBatisBatchItemWriterBuilder<Movie>()
            .sqlSessionFactory(sqlSessionFactory)
            .statementId("dev.lab.springbatch.jobs.mybatis.insertMovies")
            .itemToParameterConverter(item -> {
                Map<String, Object> params = new HashMap<>();
                params.put("title", item.getTitle());
                params.put("genre", item.getGenre());
                params.put("year", item.getYear());
                return params;
            })
            .build();
    }

    @Bean
    public Step mybatisCursorStep() {
        log.info("------------------ Init mybatisCursorStep -----------------");

        return new StepBuilder("mybatisCursorStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(mybatisFlatFileItemReader())
            .writer(myBatisBatchItemWriter())
            .build();
    }

    @Bean
    public Job movieMyBatisCursorPagingJob() {
        log.info("------------------ Init movieMyBatisCursorPagingJob -----------------");

        return new JobBuilder(MYBATIS_WRITER_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mybatisCursorStep())
            .build();
    }
}
