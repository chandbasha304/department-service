package com.example.emsdept.repository;

import com.example.emsdept.entity.Department;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class DepartmentRepository {
    private final DatabaseClient dbClient;

    public DepartmentRepository(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    public List<Department> findAll() {
        log.info("[REPO] DepartmentRepository.findAll started.");
        Statement stmt = Statement.of("SELECT * FROM departments");
        List<Department> list = new ArrayList<>();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            log.error("[REPO] DepartmentRepository.findAll failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] DepartmentRepository.findAll completed. Found size: {}", list.size());
        return list;
    }

    public Department save(Department department) {
        log.info("[REPO] DepartmentRepository.save started for department ID: {}, name: {}", department.getId(), department.getName());
        if (department.getId() == null) {
            department.setId(System.currentTimeMillis() + new java.util.Random().nextInt(1000));
            log.info("[REPO] DepartmentRepository.save generated new ID: {}", department.getId());
        }
        Mutation mutation = Mutation.newInsertOrUpdateBuilder("departments")
                .set("id").to(department.getId())
                .set("name").to(department.getName())
                .set("description").to(department.getDescription())
                .build();
        try {
            dbClient.write(Arrays.asList(mutation));
        } catch (Exception e) {
            log.error("[REPO] DepartmentRepository.save failed for ID: {}. Error: {}", department.getId(), e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] DepartmentRepository.save write mutation completed for ID: {}", department.getId());
        return department;
    }

    public Page<Department> findAll(Pageable pageable) {
        log.info("[REPO] DepartmentRepository.findAll(Pageable) started. Limit: {}, Offset: {}", pageable.getPageSize(), pageable.getOffset());
        long total = count();
        List<Department> content = new ArrayList<>();
        if (total > 0) {
            Statement stmt = Statement.newBuilder("SELECT * FROM departments LIMIT @limit OFFSET @offset")
                    .bind("limit").to(pageable.getPageSize())
                    .bind("offset").to(pageable.getOffset())
                    .build();
            try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
                while (rs.next()) {
                    content.add(mapRow(rs));
                }
            } catch (Exception e) {
                log.error("[REPO] DepartmentRepository.findAll(Pageable) query execution failed: {}", e.getMessage(), e);
                throw e;
            }
        }
        log.info("[REPO] DepartmentRepository.findAll(Pageable) returning page with size: {}, total elements: {}", content.size(), total);
        return new PageImpl<>(content, pageable, total);
    }

    public long count() {
        log.info("[REPO] DepartmentRepository.count started.");
        Statement stmt = Statement.of("SELECT COUNT(*) FROM departments");
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                long count = rs.getLong(0);
                log.info("[REPO] DepartmentRepository.count result: {}", count);
                return count;
            }
        } catch (Exception e) {
            log.error("[REPO] DepartmentRepository.count failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] DepartmentRepository.count returning 0.");
        return 0;
    }

    public Optional<Department> findById(Long id) {
        log.info("[REPO] DepartmentRepository.findById started. Querying ID: {}", id);
        Statement stmt = Statement.newBuilder("SELECT * FROM departments WHERE id = @id")
                .bind("id").to(id)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                Department dept = mapRow(rs);
                log.info("[REPO] DepartmentRepository.findById found: {}", dept.getName());
                return Optional.of(dept);
            }
        } catch (Exception e) {
            log.error("[REPO] DepartmentRepository.findById failed for ID: {}. Error: {}", id, e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] DepartmentRepository.findById NOT found ID: {}", id);
        return Optional.empty();
    }

    public boolean existsById(Long id) {
        log.info("[REPO] DepartmentRepository.existsById started. Checking ID: {}", id);
        Statement stmt = Statement.newBuilder("SELECT 1 FROM departments WHERE id = @id")
                .bind("id").to(id)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            boolean exists = rs.next();
            log.info("[REPO] DepartmentRepository.existsById ID: {} exists={}", id, exists);
            return exists;
        } catch (Exception e) {
            log.error("[REPO] DepartmentRepository.existsById failed for ID: {}. Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public List<Object[]> getEmployeeCountsByDepartment() {
        log.info("[REPO] DepartmentRepository.getEmployeeCountsByDepartment started.");
        Statement stmt = Statement.of(
                "SELECT d.name, COUNT(e.id) " +
                "FROM departments d LEFT OUTER JOIN employees e ON e.department_id = d.id " +
                "GROUP BY d.name"
        );
        List<Object[]> result = new ArrayList<>();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            while (rs.next()) {
                String name = rs.getString(0);
                long count = rs.getLong(1);
                log.info("[REPO] DepartmentRepository.getEmployeeCountsByDepartment mapping: {}={}", name, count);
                result.add(new Object[]{name, count});
            }
        } catch (Exception e) {
            log.error("[REPO] DepartmentRepository.getEmployeeCountsByDepartment failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] DepartmentRepository.getEmployeeCountsByDepartment completed. Result size: {}", result.size());
        return result;
    }

    private Department mapRow(ResultSet rs) {
        Department dept = new Department();
        dept.setId(rs.getLong("id"));
        dept.setName(rs.getString("name"));
        dept.setDescription(rs.isNull("description") ? null : rs.getString("description"));
        return dept;
    }
}