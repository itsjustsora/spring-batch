package dev.lab.springbatch.jobs.basic;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Configuration
@RequiredArgsConstructor
public class ChunkJobConfiguration {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    @Bean
    public Step chunkStep() {
        return new StepBuilder("chunkStep", jobRepository)
            .<String, String>chunk(10, transactionManager)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
    }

    @Bean
    public Job chunkJob() {
        return new JobBuilder("chunkJob", jobRepository)
            .start(chunkStep())
            .build();
    }

    @Bean
    public ItemReader<String> itemReader() {
        return new ListItemReader<>(List.of("Apple", "Banana", "Orange", "Pear", "Grape"));
    }

    @Bean
    public ItemProcessor<String, String> itemProcessor() {
        return String::toUpperCase;
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> items.forEach(System.out::println);
    }
}
