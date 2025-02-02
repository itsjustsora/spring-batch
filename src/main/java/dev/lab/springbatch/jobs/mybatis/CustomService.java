package dev.lab.springbatch.jobs.mybatis;

import java.util.Map;

import org.springframework.stereotype.Service;

import dev.lab.springbatch.jobs.domain.Movie;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomService {

    public Map<String, String> processToOtherService(Movie item) {
        log.info("Call API to OtherService....");

        return Map.of("code", "200", "message", "OK");
    }
}
