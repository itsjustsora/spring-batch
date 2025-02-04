package dev.lab.springbatch.jobs.flow;

import java.util.Random;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
public class OnStepTaskJobConfiguration {

    public static final String ON_STEP_TASK = "ON_STEP_TASK";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "stepOn01")
    public Step stepOn01() {
        log.info("------------------ Init stepOn01 -----------------");

        return new StepBuilder("stepOn01", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("Execute Step 01 Tasklet ...");

                Random random = new Random();
                int value = random.nextInt(100);

                if (value % 2 == 0) {
                    return RepeatStatus.FINISHED;
                } else   {
                    throw new RuntimeException("[Error] This value is odd: " + value);
                }
            }, transactionManager)
            .build();
    }

    @Bean(name = "stepOn02")
    public Step stepOn02() {
        log.info("------------------ Init stepOn02 -----------------");

        return new StepBuilder("stepOn02", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("Execute Step 02 Tasklet ...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean(name = "stepOn03")
    public Step stepOn03() {
        log.info("------------------ Init stepOn03 -----------------");

        return new StepBuilder("stepOn03", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("Execute Step 03 Tasklet ...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Job onStepJob() {
        log.info("------------------ Init onStepJob -----------------");

        // on과 from을 활용해 step의 종료 조건에 따라 원하는 플로우 처리
        return new JobBuilder(ON_STEP_TASK, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(stepOn01()) // step01 먼저 수행
            .on("FAILED").to(stepOn03()) // step01의 결과가 실패일 때 step03 수행
            .from(stepOn01()).on("COMPLETED").to(stepOn02()) // step01의 결과가 완료일 때 step02 수행
            .end()
            .build();
    }
}
