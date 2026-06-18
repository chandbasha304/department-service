package com.example.emsdept.config;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SpannerConfig {

    @Value("${spanner.project-id}")
    private String projectId;

    @Value("${spanner.instance-id}")
    private String instanceName;

    @Value("${spanner.database}")
    private String database;

    @Bean
    public DatabaseClient databaseClient() {
        log.info("[CONFIG] Initializing Spanner DatabaseClient with Project: {}, Instance: {}, Database: {}", 
                projectId, instanceName, database);
        SpannerOptions options = SpannerOptions.newBuilder().build();
        Spanner spanner = options.getService();
        DatabaseId db = DatabaseId.of(projectId, instanceName, database);
        log.info("[CONFIG] Google Cloud Spanner connection initialized successfully.");
        return spanner.getDatabaseClient(db);
    }
}
