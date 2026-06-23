package com.example.emsdept.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GcpServicesConfig {

    @Value("${spanner.project-id}")
    private String projectId;

    @Value("${firestore.database-id:emsdatabase}")
    private String databaseId;

    public static final String DEPARTMENT_EVENTS_TOPIC = "department-events";
    public static final String EMPLOYEE_EVENTS_TOPIC = "employee-events";
    public static final String DEPARTMENT_EVENTS_SUB_DEPT = "department-events-sub-dept";
    public static final String EMPLOYEE_EVENTS_SUB_DEPT = "employee-events-sub-dept";

    @Bean
    public Firestore firestore() {
        log.info("Initializing Firestore for project: {} and database: {}", projectId, databaseId);
        FirestoreOptions options = FirestoreOptions.newBuilder()
                .setProjectId(projectId)
                .setDatabaseId(databaseId)
                .build();
        return options.getService();
    }

    @PostConstruct
    public void initializePubSubResources() {
        log.info("Initializing Pub/Sub topics and subscriptions for project: {}", projectId);

        // 1. Create department-events topic
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            TopicName topicName = TopicName.of(projectId, DEPARTMENT_EVENTS_TOPIC);
            try {
                topicAdminClient.getTopic(topicName);
                log.info("Pub/Sub Topic '{}' already exists.", DEPARTMENT_EVENTS_TOPIC);
            } catch (Exception e) {
                topicAdminClient.createTopic(topicName);
                log.info("Pub/Sub Topic '{}' created successfully.", DEPARTMENT_EVENTS_TOPIC);
            }
        } catch (Exception e) {
            log.error("Failed to check/create Pub/Sub Topic '{}': {}", DEPARTMENT_EVENTS_TOPIC, e.getMessage());
        }

        // 2. Create employee-events topic (if not created by ems-spanner)
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            TopicName topicName = TopicName.of(projectId, EMPLOYEE_EVENTS_TOPIC);
            try {
                topicAdminClient.getTopic(topicName);
                log.info("Pub/Sub Topic '{}' already exists.", EMPLOYEE_EVENTS_TOPIC);
            } catch (Exception e) {
                topicAdminClient.createTopic(topicName);
                log.info("Pub/Sub Topic '{}' created successfully.", EMPLOYEE_EVENTS_TOPIC);
            }
        } catch (Exception e) {
            log.error("Failed to check/create Pub/Sub Topic '{}': {}", EMPLOYEE_EVENTS_TOPIC, e.getMessage());
        }

        // 3. Create department-events-sub-dept subscription
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, DEPARTMENT_EVENTS_SUB_DEPT);
            TopicName topicName = TopicName.of(projectId, DEPARTMENT_EVENTS_TOPIC);
            try {
                subscriptionAdminClient.getSubscription(subName);
                log.info("Pub/Sub Subscription '{}' already exists.", DEPARTMENT_EVENTS_SUB_DEPT);
            } catch (Exception e) {
                subscriptionAdminClient.createSubscription(subName, topicName, PushConfig.getDefaultInstance(), 10);
                log.info("Pub/Sub Subscription '{}' created successfully.", DEPARTMENT_EVENTS_SUB_DEPT);
            }
        } catch (Exception e) {
            log.error("Failed to check/create Pub/Sub Subscription '{}': {}", DEPARTMENT_EVENTS_SUB_DEPT, e.getMessage());
        }

        // 4. Create employee-events-sub-dept subscription
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, EMPLOYEE_EVENTS_SUB_DEPT);
            TopicName topicName = TopicName.of(projectId, EMPLOYEE_EVENTS_TOPIC);
            try {
                subscriptionAdminClient.getSubscription(subName);
                log.info("Pub/Sub Subscription '{}' already exists.", EMPLOYEE_EVENTS_SUB_DEPT);
            } catch (Exception e) {
                subscriptionAdminClient.createSubscription(subName, topicName, PushConfig.getDefaultInstance(), 10);
                log.info("Pub/Sub Subscription '{}' created successfully.", EMPLOYEE_EVENTS_SUB_DEPT);
            }
        } catch (Exception e) {
            log.error("Failed to check/create Pub/Sub Subscription '{}': {}", EMPLOYEE_EVENTS_SUB_DEPT, e.getMessage());
        }
    }
}
