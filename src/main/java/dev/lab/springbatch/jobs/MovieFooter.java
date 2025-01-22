package dev.lab.springbatch.jobs;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.item.file.FlatFileFooterCallback;

public class MovieFooter implements FlatFileFooterCallback {

    private final ConcurrentHashMap<String, Integer> aggregateMap;

    public MovieFooter(ConcurrentHashMap<String, Integer> aggregateMap) {
        this.aggregateMap = aggregateMap;
    }

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write("총 영화 수: " + aggregateMap.get("TOTAL_MOVIES"));
        writer.write(System.lineSeparator());
    }
}
