package dev.lab.springbatch.jobs.mybatis;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import dev.lab.springbatch.jobs.domain.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisCustomWriterJobConfiguration {

    public static final int CHUNK_SIZE = 100;
    public static final String ENCODING = "UTF-8";
    public static final String MYBATIS_WRITER_JOB = "MYBATIS_CUSTOM_WRITER_JOB";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CustomItemWriter customItemWriter;

    @Bean
    public FlatFileItemReader<Movie> customFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<Movie>()
            .name("customFlatFileItemReader")
            .resource(new ClassPathResource("./movies.csv"))
            .encoding(ENCODING)
            .delimited().delimiter(",")
            .names("title", "genre", "year")
            .targetType(Movie.class)
            .build();
    }

    @Bean
    public Step customFlatFileStep() {
        log.info("------------------ Init customFlatFileStep -----------------");

        return new StepBuilder("customFlatFileStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(customFlatFileItemReader())
            .writer(customItemWriter)
            .build();
    }

    @Bean
    public Job customFlatFileJob() {
        log.info("------------------ Init customFlatFileJob -----------------");

        return new JobBuilder(MYBATIS_WRITER_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(customFlatFileStep())
            .build();
    }
}
