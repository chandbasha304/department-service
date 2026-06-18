package com.example.emsdept.repository;

import com.example.emsdept.entity.UserSession;
import com.google.cloud.Timestamp;
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
public class UserSessionRepository {
    private final DatabaseClient dbClient;

    public UserSessionRepository(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<UserSession> findByEmailAndIsActiveTrue(String email) {
        log.info("[REPO] UserSessionRepository.findByEmailAndIsActiveTrue started. Email: {}", email);
        Statement stmt = Statement.newBuilder(
                "SELECT * FROM user_sessions WHERE email=@email AND is_active=true"
        ).bind("email").to(email).build();

        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                UserSession session = new UserSession();
                session.setId(rs.getLong("id"));
                session.setEmail(rs.getString("email"));
                session.setToken(rs.getString("token"));
                session.setIsActive(rs.getBoolean("is_active"));
                session.setCreatedAt(rs.getTimestamp("created_at").toSqlTimestamp().toLocalDateTime());
                log.info("[REPO] UserSessionRepository.findByEmailAndIsActiveTrue found active session ID: {}", session.getId());
                return Optional.of(session);
            }
        } catch (Exception e) {
            log.error("[REPO] UserSessionRepository.findByEmailAndIsActiveTrue failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] UserSessionRepository.findByEmailAndIsActiveTrue no active session found for: {}", email);
        return Optional.empty();
    }

    public Optional<UserSession> findLatestSessionByToken(String token) {

        log.info("[REPO] Searching token: {}", token);

        Statement stmt = Statement.newBuilder(
                "SELECT id, email, token, is_active, created_at " +
                        "FROM user_sessions " +
                        "WHERE STARTS_WITH(token, @token) " +
                        "ORDER BY created_at DESC " +
                        "LIMIT 1"
        ).bind("token").to(token).build();

        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {

            boolean found = rs.next();

            log.info("[REPO] Session found: {}", found);

            if (found) {

                UserSession session = new UserSession();

                session.setId(rs.getLong("id"));
                session.setEmail(rs.getString("email"));
                session.setToken(rs.getString("token"));
                session.setIsActive(rs.getBoolean("is_active"));
                session.setCreatedAt(
                        rs.getTimestamp("created_at")
                                .toSqlTimestamp()
                                .toLocalDateTime()
                );

                return Optional.of(session);
            }

        } catch (Exception e) {
            log.error("[REPO] Query failed", e);
            throw e;
        }

        return Optional.empty();
    }



    public void save(UserSession session) {
        if (session.getId() == null) {
            session.setId(System.currentTimeMillis() + new java.util.Random().nextInt(1000));
            log.info("[REPO] UserSessionRepository.save generated new ID: {}", session.getId());
        }
        log.info("[REPO] save UserSession started. ID: {}, Email: {}", session.getId(), session.getEmail());
        Mutation mutation = Mutation.newInsertOrUpdateBuilder("user_sessions")
                .set("id").to(session.getId())
                .set("email").to(session.getEmail())
                .set("token").to(session.getToken())
                .set("is_active").to(session.getIsActive())
                .set("created_at").to(Timestamp.of(java.sql.Timestamp.valueOf(session.getCreatedAt())))
                .build();
        try {
            dbClient.write(Arrays.asList(mutation));
        } catch (Exception e) {
            log.error("[REPO] UserSessionRepository.save failed for ID: {}. Error: {}", session.getId(), e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] save UserSession mutation completed for ID: {}", session.getId());
    }
}