package com.example.emsdept.repository;

import com.example.emsdept.entity.Department;
import com.example.emsdept.entity.Employee;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class EmployeeRepository {
    private final DatabaseClient dbClient;
    private final DepartmentRepository departmentRepository;

    public EmployeeRepository(DatabaseClient dbClient, DepartmentRepository departmentRepository) {
        this.dbClient = dbClient;
        this.departmentRepository = departmentRepository;
    }

    public boolean existsByEmail(String email) {
        log.info("[REPO] EmployeeRepository.existsByEmail checking email: {}", email);
        Statement stmt = Statement.newBuilder("SELECT 1 FROM employees WHERE email = @email")
                .bind("email").to(email)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            boolean exists = rs.next();
            log.info("[REPO] EmployeeRepository.existsByEmail email: {} exists={}", email, exists);
            return exists;
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.existsByEmail failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean existsByPhone(String phone) {
        log.info("[REPO] EmployeeRepository.existsByPhone checking phone: {}", phone);
        Statement stmt = Statement.newBuilder("SELECT 1 FROM employees WHERE phone = @phone")
                .bind("phone").to(phone)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            boolean exists = rs.next();
            log.info("[REPO] EmployeeRepository.existsByPhone phone: {} exists={}", phone, exists);
            return exists;
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.existsByPhone failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Optional<Employee> findById(Long id) {
        log.info("[REPO] EmployeeRepository.findById started for ID: {}", id);
        Statement stmt = Statement.newBuilder("SELECT * FROM employees WHERE id = @id")
                .bind("id").to(id)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                Employee emp = mapRow(rs);
                log.info("[REPO] EmployeeRepository.findById found employee. ID: {}, Email: {}", emp.getId(), emp.getEmail());
                return Optional.of(emp);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findById failed for ID: {}. Error: {}", id, e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findById NOT found ID: {}", id);
        return Optional.empty();
    }

    public Optional<Employee> findByEmail(String email) {
        log.info("[REPO] EmployeeRepository.findByEmail started for email: {}", email);
        Statement stmt = Statement.newBuilder("SELECT * FROM employees WHERE email = @email")
                .bind("email").to(email)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                Employee emp = mapRow(rs);
                log.info("[REPO] EmployeeRepository.findByEmail found employee. ID: {}, Email: {}", emp.getId(), emp.getEmail());
                return Optional.of(emp);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findByEmail failed for email: {}. Error: {}", email, e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findByEmail NOT found email: {}", email);
        return Optional.empty();
    }

    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        log.info("[REPO] EmployeeRepository.findByEmployeeCode started for code: {}", employeeCode);
        Statement stmt = Statement.newBuilder("SELECT * FROM employees WHERE employee_code = @code")
                .bind("code").to(employeeCode)
                .build();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                Employee emp = mapRow(rs);
                log.info("[REPO] EmployeeRepository.findByEmployeeCode found employee. ID: {}, Code: {}", emp.getId(), emp.getEmployeeCode());
                return Optional.of(emp);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findByEmployeeCode failed for code: {}. Error: {}", employeeCode, e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findByEmployeeCode NOT found code: {}", employeeCode);
        return Optional.empty();
    }

    public List<Employee> findAllById(Iterable<Long> ids) {
        List<Employee> list = new ArrayList<>();
        List<Long> idList = new ArrayList<>();
        ids.forEach(idList::add);
        log.info("[REPO] EmployeeRepository.findAllById queried ids: {}", idList);
        if (idList.isEmpty()) return list;

        Statement.Builder builder = Statement.newBuilder("SELECT * FROM employees WHERE id IN UNNEST(@ids)");
        builder.bind("ids").toInt64Array(idList.stream().mapToLong(Long::longValue).toArray());
        try (ResultSet rs = dbClient.singleUse().executeQuery(builder.build())) {
            while (rs.next()) {
                Employee emp = mapRow(rs);
                log.info("[REPO] EmployeeRepository.findAllById found employee: id={}, email={}", emp.getId(), emp.getEmail());
                list.add(emp);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findAllById failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findAllById returned size: {}", list.size());
        return list;
    }

    public Employee save(Employee employee) {
        log.info("[REPO] EmployeeRepository.save started for ID: {}, email: {}", employee.getId(), employee.getEmail());
        if (employee.getId() == null) {
            employee.setId(System.currentTimeMillis() + new java.util.Random().nextInt(1000));
            log.info("[REPO] EmployeeRepository.save generated new ID: {}", employee.getId());
        }
        Mutation mutation = Mutation.newInsertOrUpdateBuilder("employees")
                .set("id").to(employee.getId())
                .set("employee_code").to(employee.getEmployeeCode())
                .set("first_name").to(employee.getFirstName())
                .set("last_name").to(employee.getLastName())
                .set("email").to(employee.getEmail())
                .set("phone").to(employee.getPhone())
                .set("designation").to(employee.getDesignation())
                .set("salary").to(employee.getSalary())
                .set("joining_date").to(employee.getJoiningDate() != null ? com.google.cloud.Date.fromYearMonthDay(
                        employee.getJoiningDate().getYear(),
                        employee.getJoiningDate().getMonthValue(),
                        employee.getJoiningDate().getDayOfMonth()) : null)
                .set("status").to(employee.getStatus())
                .set("department_id").to(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .build();
        try {
            dbClient.write(Arrays.asList(mutation));
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.save failed for ID: {}. Error: {}", employee.getId(), e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.save write mutation completed for ID: {}", employee.getId());
        return employee;
    }

    public List<Employee> saveAll(Iterable<Employee> employees) {
        log.info("[REPO] EmployeeRepository.saveAll started.");
        List<Mutation> mutations = new ArrayList<>();
        List<Employee> saved = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.getId() == null) {
                employee.setId(System.currentTimeMillis() + new java.util.Random().nextInt(1000));
                log.info("[REPO] EmployeeRepository.saveAll generated ID: {}", employee.getId());
            }
            Mutation mutation = Mutation.newInsertOrUpdateBuilder("employees")
                    .set("id").to(employee.getId())
                    .set("employee_code").to(employee.getEmployeeCode())
                    .set("first_name").to(employee.getFirstName())
                    .set("last_name").to(employee.getLastName())
                    .set("email").to(employee.getEmail())
                    .set("phone").to(employee.getPhone())
                    .set("designation").to(employee.getDesignation())
                    .set("salary").to(employee.getSalary())
                    .set("joining_date").to(employee.getJoiningDate() != null ? com.google.cloud.Date.fromYearMonthDay(
                            employee.getJoiningDate().getYear(),
                            employee.getJoiningDate().getMonthValue(),
                            employee.getJoiningDate().getDayOfMonth()) : null)
                    .set("status").to(employee.getStatus())
                    .set("department_id").to(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                    .build();
            mutations.add(mutation);
            saved.add(employee);
        }
        try {
            dbClient.write(mutations);
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.saveAll failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.saveAll write mutation completed for size: {}", saved.size());
        return saved;
    }

    public Page<Employee> findAll(Pageable pageable) {
        log.info("[REPO] EmployeeRepository.findAll(Pageable) started. Limit: {}, Offset: {}", pageable.getPageSize(), pageable.getOffset());
        long total = count();
        List<Employee> content = new ArrayList<>();
        if (total > 0) {
            Statement stmt = Statement.newBuilder("SELECT * FROM employees LIMIT @limit OFFSET @offset")
                    .bind("limit").to(pageable.getPageSize())
                    .bind("offset").to(pageable.getOffset())
                    .build();
            try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
                while (rs.next()) {
                    content.add(mapRow(rs));
                }
            } catch (Exception e) {
                log.error("[REPO] EmployeeRepository.findAll(Pageable) query execution failed: {}", e.getMessage(), e);
                throw e;
            }
        }
        log.info("[REPO] EmployeeRepository.findAll(Pageable) returning page with size: {}, total elements: {}", content.size(), total);
        return new PageImpl<>(content, pageable, total);
    }

    public void delete(Employee employee) {
        log.info("[REPO] EmployeeRepository.delete started for employee ID: {}", employee.getId());
        Mutation mutation = Mutation.delete("employees", com.google.cloud.spanner.Key.of(employee.getId()));
        try {
            dbClient.write(Arrays.asList(mutation));
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.delete failed for ID: {}. Error: {}", employee.getId(), e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.delete completed for ID: {}", employee.getId());
    }

    public void deleteAll(Iterable<Employee> employees) {
        log.info("[REPO] EmployeeRepository.deleteAll started.");
        List<Mutation> mutations = new ArrayList<>();
        for (Employee employee : employees) {
            log.info("[REPO] EmployeeRepository.deleteAll adding delete mutation for ID: {}", employee.getId());
            mutations.add(Mutation.delete("employees", com.google.cloud.spanner.Key.of(employee.getId())));
        }
        try {
            dbClient.write(mutations);
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.deleteAll failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.deleteAll write delete mutations completed.");
    }

    public long count() {
        log.info("[REPO] EmployeeRepository.count started.");
        Statement stmt = Statement.of("SELECT COUNT(*) FROM employees");
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                long count = rs.getLong(0);
                log.info("[REPO] EmployeeRepository.count result: {}", count);
                return count;
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.count failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.count returning 0.");
        return 0;
    }

    public List<String> findDistinctDesignations() {
        log.info("[REPO] EmployeeRepository.findDistinctDesignations started.");
        Statement stmt = Statement.of("SELECT DISTINCT designation FROM employees ORDER BY designation");
        List<String> list = new ArrayList<>();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            while (rs.next()) {
                if (!rs.isNull(0)) {
                    list.add(rs.getString(0));
                }
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findDistinctDesignations failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findDistinctDesignations returned size: {}", list.size());
        return list;
    }

    public Page<Employee> searchEmployees(String search, Pageable pageable) {
        log.info("[REPO] EmployeeRepository.searchEmployees started for keyword: {}, Limit: {}, Offset: {}", search, pageable.getPageSize(), pageable.getOffset());
        String searchLike = "%" + search.toLowerCase() + "%";
        
        Statement countStmt = Statement.newBuilder(
                "SELECT COUNT(*) FROM employees e LEFT OUTER JOIN departments d ON e.department_id = d.id " +
                "WHERE LOWER(e.first_name) LIKE @search " +
                "   OR LOWER(e.last_name) LIKE @search " +
                "   OR LOWER(e.email) LIKE @search " +
                "   OR LOWER(e.phone) LIKE @search " +
                "   OR LOWER(e.designation) LIKE @search " +
                "   OR LOWER(d.name) LIKE @search"
        ).bind("search").to(searchLike).build();

        long total = 0;
        try (ResultSet rs = dbClient.singleUse().executeQuery(countStmt)) {
            if (rs.next()) {
                total = rs.getLong(0);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.searchEmployees count query failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.searchEmployees found total match count: {}", total);

        List<Employee> content = new ArrayList<>();
        if (total > 0) {
            Statement selectStmt = Statement.newBuilder(
                    "SELECT e.* FROM employees e LEFT OUTER JOIN departments d ON e.department_id = d.id " +
                    "WHERE LOWER(e.first_name) LIKE @search " +
                    "   OR LOWER(e.last_name) LIKE @search " +
                    "   OR LOWER(e.email) LIKE @search " +
                    "   OR LOWER(e.phone) LIKE @search " +
                    "   OR LOWER(e.designation) LIKE @search " +
                    "   OR LOWER(d.name) LIKE @search " +
                    "LIMIT @limit OFFSET @offset"
            ).bind("search").to(searchLike)
             .bind("limit").to(pageable.getPageSize())
             .bind("offset").to(pageable.getOffset())
             .build();
            try (ResultSet rs = dbClient.singleUse().executeQuery(selectStmt)) {
                while (rs.next()) {
                    content.add(mapRow(rs));
                }
            } catch (Exception e) {
                log.error("[REPO] EmployeeRepository.searchEmployees select query failed: {}", e.getMessage(), e);
                throw e;
            }
        }
        log.info("[REPO] EmployeeRepository.searchEmployees returning page with size: {}", content.size());
        return new PageImpl<>(content, pageable, total);
    }

    public Page<Employee> findByFirstNameContainingIgnoreCase(String keyword, Pageable pageable) {
        log.info("[REPO] EmployeeRepository.findByFirstNameContainingIgnoreCase keyword: {}, Limit: {}, Offset: {}", keyword, pageable.getPageSize(), pageable.getOffset());
        String searchLike = "%" + keyword.toLowerCase() + "%";
        
        Statement countStmt = Statement.newBuilder(
                "SELECT COUNT(*) FROM employees WHERE LOWER(first_name) LIKE @search"
        ).bind("search").to(searchLike).build();

        long total = 0;
        try (ResultSet rs = dbClient.singleUse().executeQuery(countStmt)) {
            if (rs.next()) {
                total = rs.getLong(0);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findByFirstNameContainingIgnoreCase count query failed: {}", e.getMessage(), e);
            throw e;
        }

        List<Employee> content = new ArrayList<>();
        if (total > 0) {
            Statement selectStmt = Statement.newBuilder(
                    "SELECT * FROM employees WHERE LOWER(first_name) LIKE @search LIMIT @limit OFFSET @offset"
            ).bind("search").to(searchLike)
             .bind("limit").to(pageable.getPageSize())
             .bind("offset").to(pageable.getOffset())
             .build();
            try (ResultSet rs = dbClient.singleUse().executeQuery(selectStmt)) {
                while (rs.next()) {
                    content.add(mapRow(rs));
                }
            } catch (Exception e) {
                log.error("[REPO] EmployeeRepository.findByFirstNameContainingIgnoreCase select query failed: {}", e.getMessage(), e);
                throw e;
            }
        }
        log.info("[REPO] EmployeeRepository.findByFirstNameContainingIgnoreCase returning page with size: {}", content.size());
        return new PageImpl<>(content, pageable, total);
    }

    public BigDecimal findAverageSalary() {
        log.info("[REPO] EmployeeRepository.findAverageSalary started.");
        Statement stmt = Statement.of("SELECT AVG(salary) FROM employees");
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next() && !rs.isNull(0)) {
                BigDecimal avg = BigDecimal.valueOf(rs.getDouble(0));
                log.info("[REPO] EmployeeRepository.findAverageSalary result: {}", avg);
                return avg;
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findAverageSalary failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findAverageSalary returning 0.");
        return BigDecimal.ZERO;
    }

    public Optional<Employee> findTopByOrderBySalaryDesc() {
        log.info("[REPO] EmployeeRepository.findTopByOrderBySalaryDesc started.");
        Statement stmt = Statement.of("SELECT * FROM employees ORDER BY salary DESC LIMIT 1");
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            if (rs.next()) {
                Employee emp = mapRow(rs);
                log.info("[REPO] EmployeeRepository.findTopByOrderBySalaryDesc found employee: {} {}, salary: {}", emp.getFirstName(), emp.getLastName(), emp.getSalary());
                return Optional.of(emp);
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.findTopByOrderBySalaryDesc failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.findTopByOrderBySalaryDesc found nothing.");
        return Optional.empty();
    }

    public List<Object[]> countByDesignation() {
        log.info("[REPO] EmployeeRepository.countByDesignation started.");
        Statement stmt = Statement.of("SELECT designation, COUNT(*) FROM employees GROUP BY designation");
        List<Object[]> list = new ArrayList<>();
        try (ResultSet rs = dbClient.singleUse().executeQuery(stmt)) {
            while (rs.next()) {
                String desig = rs.getString(0);
                long count = rs.getLong(1);
                log.info("[REPO] EmployeeRepository.countByDesignation mapping: {}={}", desig, count);
                list.add(new Object[]{desig, count});
            }
        } catch (Exception e) {
            log.error("[REPO] EmployeeRepository.countByDesignation failed: {}", e.getMessage(), e);
            throw e;
        }
        log.info("[REPO] EmployeeRepository.countByDesignation completed. Returned size: {}", list.size());
        return list;
    }

    private Employee mapRow(ResultSet rs) {
        Employee emp = new Employee();
        emp.setId(rs.getLong("id"));
        emp.setEmployeeCode(rs.getString("employee_code"));
        emp.setFirstName(rs.getString("first_name"));
        emp.setLastName(rs.getString("last_name"));
        emp.setEmail(rs.getString("email"));
        emp.setPhone(rs.getString("phone"));
        emp.setDesignation(rs.getString("designation"));
        emp.setSalary(rs.getBigDecimal("salary"));
        
        com.google.cloud.Date date = rs.getDate("joining_date");
        emp.setJoiningDate(LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()));
        
        emp.setStatus(rs.getString("status"));
        
        if (!rs.isNull("department_id")) {
            long deptId = rs.getLong("department_id");
            Optional<Department> dept = departmentRepository.findById(deptId);
            dept.ifPresent(emp::setDepartment);
        }
        return emp;
    }
}
