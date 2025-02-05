package dev.lab.springbatch.jobs.flow;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NextStepTaskJobConfiguration {

    public static final String NEXT_STEP_TASK = "NEXT_STEP_TASK";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;

    @Bean(name = "step01")
    public Step step01() {
        log.info("------------------ Init step01 -----------------");

        /**
         * 람다 쓰기 전
         * .tasklet(new Tasklet() {
         *                     @Override
         *                     public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
         *                         log.info("Execute Step 01 Tasklet ...");
         *                         return RepeatStatus.FINISHED;
         *                     }
         *                 }, transactionManager)
         *                 .build();
         */
        return new StepBuilder("step01", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("Execute Step 01 Tasklet ...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepExecutionListener)
            .build();
    }

    @Bean(name = "step02")
    public Step step02() {
        log.info("------------------ Init step02 -----------------");

        return new StepBuilder("step02", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("Execute Step 02 Tasklet ...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepExecutionListener)
            .build();
    }

    @Bean
    public Job nextStepJob() {
        log.info("------------------ Init nextStepJob -----------------");

        return new JobBuilder(NEXT_STEP_TASK, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(step01())
            .next(step02()) // Start 스텝이 수행하고 난 뒤 이동할 곳
            .listener(jobExecutionListener)
            .build();
    }
}
