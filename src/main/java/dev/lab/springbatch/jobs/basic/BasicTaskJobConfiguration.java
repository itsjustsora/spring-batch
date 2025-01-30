package dev.lab.springbatch.jobs.basic;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Configuration
@RequiredArgsConstructor
public class BasicTaskJobConfiguration {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    @Bean
    public Tasklet greetingTasklet() {
        return new GreetingTask();
    }

    @Bean
    public Step step() {
        log.info("---------------------- Init step01 ----------------------");

        return new StepBuilder("step01", jobRepository)
            .tasklet(greetingTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Job job() {
        log.info("---------------------- Init job01 ----------------------");

        return new JobBuilder("job01", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(step())
            .build();
    }
}
