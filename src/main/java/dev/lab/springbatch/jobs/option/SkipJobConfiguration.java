package dev.lab.springbatch.jobs.option;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SkipJobConfiguration {

    public static final String SKP_JOB = "SKP_JOB";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AtomicInteger skipCount = new AtomicInteger(0);

    @Bean
    public Step stepSkip() {
        log.info("------------------ Init stepSkip -----------------");

        return new StepBuilder("stepSkip", jobRepository)
            .<Integer, Integer>chunk(1, transactionManager)
            .reader(() -> {
                int attempt = skipCount.incrementAndGet();
                log.info("Execute stepSkip attempt {}.", attempt);

                if (attempt % 2 == 1) {
                    log.warn("Skipping... (attempts: {})", attempt);
                    throw new IllegalStateException("Skip Exception");
                }

                return attempt;
            })
            .processor(item -> {
                log.info("Processing item: {}", item);
                return item;
            })
            .writer(items -> log.info("Writing items: {}", items))
            .faultTolerant()
            .skip(IllegalStateException.class)
            .skipLimit(5)
            .build();
    }

    @Bean
    public Job skipJob() {
        log.info("------------------ Init skipJob -----------------");

        return new JobBuilder(SKP_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(stepSkip())
            .build();
    }
}
