package dev.lab.springbatch.configuration;

import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import dev.lab.springbatch.domain.Movie;

public class MovieItemSqlParameterSourceProvider implements ItemSqlParameterSourceProvider<Movie> {
    @Override
    public SqlParameterSource createSqlParameterSource(Movie item) {
        return new BeanPropertySqlParameterSource(item);
    }
}
