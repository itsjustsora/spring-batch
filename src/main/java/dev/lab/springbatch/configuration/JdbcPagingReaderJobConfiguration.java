package dev.lab.springbatch.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import dev.lab.springbatch.domain.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Configuration
public class JdbcPagingReaderJobConfiguration {

    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";

    // @Autowired
    DataSource dataSource;

    @Bean
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, title, genre, year");
        queryProvider.setFromClause("from movie");
        queryProvider.setWhereClause("where year >= :year");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.DESCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    public JdbcPagingItemReader<Movie> jdbcPagingItemReader() throws Exception {
        Map<String, Object> parameterValue = new HashMap<>();
        parameterValue.put("year", 1990);

        return new JdbcPagingItemReaderBuilder<Movie>()
            .name("jdbcPagingItemReader")
            .fetchSize(CHUNK_SIZE)
            .dataSource(dataSource)
            .rowMapper(new BeanPropertyRowMapper<>(Movie.class))
            .queryProvider(queryProvider())
            .parameterValues(parameterValue)
            .build();
    }

    @Bean
    public FlatFileItemWriter<Movie> flatFileItemWriter() throws Exception {
        return new FlatFileItemWriterBuilder<Movie>()
            .name("movieFlatFileItemWriter")
            .resource(new FileSystemResource("./output/movie_new_v1.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Title", "Genre", "Year")
            .build();
    }

    @Bean
    public Step movieJdbcPagingStep(JobRepository jobRepository, DataSourceTransactionManager transactionManager) throws
        Exception {
        log.info("==================== Init movieJdbcPagingStep ====================");

        return new StepBuilder("movieJdbcPagingStep", jobRepository)
            .<Movie, Movie>chunk(CHUNK_SIZE, transactionManager)
            .reader(jdbcPagingItemReader())
            .writer(flatFileItemWriter())
            .build();
    }

    @Bean
    public Job movieJdbcPagingJob(Step movieJdbcPagingStep, JobRepository jobRepository) {
        log.info("==================== Init movieJdbcPagingJob ====================");

        return new JobBuilder("movieJdbcPagingJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(movieJdbcPagingStep)
            .build();
    }
}
