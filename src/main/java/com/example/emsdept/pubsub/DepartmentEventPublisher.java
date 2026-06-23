package com.example.emsdept.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.TopicName;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DepartmentEventPublisher {

    @Value("${spanner.project-id}")
    private String projectId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publishDepartmentEvent(Long departmentId, String action) {
        TopicName topicName = TopicName.of(projectId, "department-events");
        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topicName).build();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("action", action);
            payload.put("departmentId", departmentId);
            payload.put("timestamp", System.currentTimeMillis());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            ByteString data = ByteString.copyFromUtf8(jsonPayload);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            log.info("Published Pub/Sub department event. Action: {}, DepartmentId: {}, MessageId: {}", action, departmentId, messageId);
        } catch (Exception e) {
            log.error("Failed to publish department event to Pub/Sub: {}", e.getMessage(), e);
        } finally {
            if (publisher != null) {
                try {
                    publisher.shutdown();
                } catch (Exception e) {
                    log.error("Error shutting down publisher: {}", e.getMessage());
                }
            }
        }
    }
}
