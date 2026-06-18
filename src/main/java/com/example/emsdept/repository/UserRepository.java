package com.example.emsdept.repository;

import com.example.emsdept.entity.User;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepository {
    private final DatabaseClient dbClient;

    public UserRepository(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    public void save(User user) {
        log.info("[REPO] UserRepository.save started for User ID: {}, Email: {}", user.getId(), user.getEmail());
        if (user.getId() == null) {
            user.setId(System.currentTimeMillis() + new java.util.Random().nextInt(1000));
            log.info("[REPO] UserRepository.save generated ID: {}", user.getId());
        }
        Mutation mutation = Mutation.newInsertOrUpdateBuilder("users")
                .set("id").to(user.getId())
                .set("email").to(user.getEmail())
                .set("first_name").to(user.getFirstName())
                .set("last_name").to(user.getLastName())
                .set("password").to(user.getPassword())
                .set("phone").to(user.getPhone())
                .set("role").to(user.getRole())
                .set("status").to(user.getStatus())
                .build();
        try {
            dbClient.write(Arrays.asList(mutation));
        } catch (Exception e) {
            log.error("[REPO] UserRepository.save failed for User ID: {}. Error: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] UserRepository.save mutation completed for User ID: {}", user.getId());
    }

    public Optional<User> findByEmail(String email) {
        log.info("[REPO] UserRepository.findByEmail started. Email: {}", email);
        Statement stmt = Statement.newBuilder("SELECT * FROM users WHERE email=@email")
                .bind("email").to(email)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPassword(rs.getString("password"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                log.info("[REPO] UserRepository.findByEmail found user. ID: {}, Role: {}", user.getId(), user.getRole());
                return Optional.of(user);
            }
        } catch (Exception e) {
            log.error("[REPO] UserRepository.findByEmail failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] UserRepository.findByEmail user NOT found for email: {}", email);
        return Optional.empty();
    }
}