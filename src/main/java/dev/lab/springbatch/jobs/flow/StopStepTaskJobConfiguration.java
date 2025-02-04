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
public class StopStepTaskJobConfiguration {

    public static final String STOP_STEP_TASK = "STOP_STEP_TASK";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "stepStop01")
    public Step stepStop01() {
        log.info("------------------ Init stepStop01 -----------------");

        return new StepBuilder("stepStop01", jobRepository)
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

    @Bean(name = "stepStop02")
    public Step stepStop02() {
        log.info("------------------ Init stepStop02 -----------------");

        return new StepBuilder("stepStop02", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("Execute Step 02 Tasklet ...");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Job stopStepJob() {
        log.info("------------------ Init stopStepJob -----------------");

        return new JobBuilder(STOP_STEP_TASK, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(stepStop01()) // step01 먼저 수행
            .on("FAILED").stop() // step01의 결과가 실패일 때 중지
            .from(stepStop01()).on("COMPLETED").to(stepStop02()) // step01의 결과가 완료일 때 step02 수행
            .end()
            .build();
    }
}
