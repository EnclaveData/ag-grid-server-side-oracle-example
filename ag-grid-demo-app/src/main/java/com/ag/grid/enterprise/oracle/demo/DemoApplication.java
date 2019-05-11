package com.ag.grid.enterprise.oracle.demo;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@SpringBootApplication
public class DemoApplication {

    @Bean
    @Lazy
    public Ignite ignition() {
        final IgniteConfiguration configuration = new IgniteConfiguration();
        configuration.setClientMode(false);
        return Ignition.start(configuration);
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "ignite.datasource")
    DataSourceProperties igniteDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Lazy
    @DependsOn({"ignition"})
    public DataSource igniteDataSource() {
        final DataSource dataSource = igniteDataSourceProperties().initializeDataSourceBuilder().build();
        DatabasePopulator databasePopulator = new ResourceDatabasePopulator(new ClassPathResource("ignite-schema.sql"));
        DatabasePopulatorUtils.execute(databasePopulator, dataSource);
        return dataSource;
    }

    @Bean
    @Lazy
    public JdbcTemplate igniteJdbcTemplate() {
        return new JdbcTemplate(igniteDataSource());
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    @Lazy
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @Lazy
    public DataSource dataSource() {
        final DataSource dataSource = dataSourceProperties().initializeDataSourceBuilder().build();
        return dataSource;
    }

    @Bean
    @Lazy
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
