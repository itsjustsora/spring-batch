package dev.lab.springbatch.jobs;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreetingTask implements Tasklet, InitializingBean {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("---------------------- Task Execute ----------------------");
        log.info("GreetingTask: {}, {}", contribution, chunkContext);

        return RepeatStatus.FINISHED; // Tasklet 종료
    }

    @Override
    public void afterPropertiesSet()  {
        log.info("---------------------- After Properties Sets() ----------------------");
    }
}
