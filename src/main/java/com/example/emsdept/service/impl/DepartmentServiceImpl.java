package com.example.emsdept.service.impl;

import com.example.emsdept.entity.Department;
import com.example.emsdept.repository.DepartmentRepository;
import com.example.emsdept.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {


    private final DepartmentRepository departmentRepository;
    @Override
    public Page<Department> getAllDepartments(int page, int size) {
        log.info("[START] DepartmentServiceImpl.getAllDepartments - page: {}, size: {}", page, size);
        long start = System.currentTimeMillis();
        Pageable pageable =
                PageRequest.of(page, size);

        Page<Department> departments =
                departmentRepository.findAll(pageable);

        log.info("[END] DepartmentServiceImpl.getAllDepartments - count: {} in {}ms", departments.getNumberOfElements(), System.currentTimeMillis() - start);
        return departments;
    }

    @Override
    public long countDepartments() {
        log.info("[START] DepartmentServiceImpl.countDepartments");
        long start = System.currentTimeMillis();
        long count = departmentRepository.count();
        log.info("[END] DepartmentServiceImpl.countDepartments - result: {} in {}ms", count, System.currentTimeMillis() - start);
        return count;
    }

    @Override
    public List<Map<String, Object>> getDepartmentEmployeeCounts() {
        log.info("[START] DepartmentServiceImpl.getDepartmentEmployeeCounts");
        long start = System.currentTimeMillis();
        List<Object[]> results = departmentRepository.getEmployeeCountsByDepartment();
        List<Map<String, Object>> counts = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("count", row[1]);
            counts.add(map);
        }
        log.info("[END] DepartmentServiceImpl.getDepartmentEmployeeCounts - result size: {} in {}ms", counts.size(), System.currentTimeMillis() - start);
        return counts;
    }

}
