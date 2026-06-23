package com.example.emsdept.pubsub;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.example.emsdept.service.DepartmentService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmployeeAndDeptSubscriber {

    @Value("${spanner.project-id}")
    private String projectId;

    private final Firestore firestore;
    private final DepartmentService departmentService;
    
    private final List<Subscriber> subscribers = new ArrayList<>();

    public EmployeeAndDeptSubscriber(Firestore firestore, @Lazy DepartmentService departmentService) {
        this.firestore = firestore;
        this.departmentService = departmentService;
    }

    @PostConstruct
    public void startSubscribers() {
        startSub("employee-events-sub-dept");
        startSub("department-events-sub-dept");
    }

    private void startSub(String subId) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subId);
        
        MessageReceiver receiver = (message, consumer) -> {
            String payload = message.getData().toStringUtf8();
            log.info("Received Pub/Sub message on {}: {}", subId, payload);
            
            try {
                recalculateAndSyncAnalytics();
                consumer.ack();
            } catch (Exception e) {
                log.error("Failed to process Pub/Sub message on {}: {}", subId, e.getMessage(), e);
                consumer.nack();
            }
        };

        try {
            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            subscriber.startAsync().awaitRunning();
            subscribers.add(subscriber);
            log.info("Started Pub/Sub Subscriber for: {}", subscriptionName);
        } catch (Exception e) {
            log.error("Failed to start Pub/Sub Subscriber for '{}': {}", subId, e.getMessage(), e);
        }
    }

    public synchronized void recalculateAndSyncAnalytics() {
        log.info("Recalculating department analytics and syncing to Firestore...");
        try {
            long totalDepartments = departmentService.countDepartments();
            List<Map<String, Object>> departmentCounts = departmentService.getDepartmentEmployeeCounts();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDepartments", totalDepartments);
            stats.put("departmentCounts", departmentCounts);
            stats.put("lastUpdated", System.currentTimeMillis());

            firestore.collection("analytics")
                    .document("department-stats")
                    .set(stats)
                    .get();

            log.info("Successfully updated Firestore analytics/department-stats.");
        } catch (Exception e) {
            log.error("Error recalculating and syncing department analytics to Firestore: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stopSubscribers() {
        for (Subscriber subscriber : subscribers) {
            if (subscriber != null) {
                try {
                    subscriber.stopAsync().awaitTerminated();
                } catch (Exception e) {
                    log.error("Error stopping subscriber: {}", e.getMessage());
                }
            }
        }
        log.info("Stopped all Pub/Sub Subscribers.");
    }
}
