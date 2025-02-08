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
public class RetryJobConfiguration {

    public static final String RETRY_JOB = "RETRY_JOB";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AtomicInteger retryCount = new AtomicInteger(0);

    @Bean
    public Step stepRetry() {
        log.info("------------------ Init stepRetry -----------------");

        return new StepBuilder("stepRetry", jobRepository)
            .<String, String>chunk(2, transactionManager)
            .reader(() -> {
                log.info("Reading item...");
                return "item"; // 정상적으로 아이템을 리턴
            })
            .processor(item -> {
                int attempt = retryCount.incrementAndGet();
                log.info("Processing item: {} (attempt: {})", item, attempt);

                // 예외 발생 조건
                if (attempt < 3) {
                    log.warn("Retrying... (attempt: {})", attempt);
                    throw new IllegalStateException("Retry Exception");
                }

                log.info("Processing completed.");
                return item; // 정상적으로 처리된 아이템 리턴
            })
            .writer(items -> {
                if (items.isEmpty()) {
                    throw new IllegalStateException("Writer Exception");
                }
                log.info("Writing items: {}", items); // 아이템을 기록
            }).faultTolerant()
            .retry(IllegalStateException.class)
            .retryLimit(2)
            .build();
    }

    @Bean
    public Job retryJob() {
        log.info("------------------ Init retryJob -----------------");

        return new JobBuilder(RETRY_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(stepRetry())
            .build();
    }
}
