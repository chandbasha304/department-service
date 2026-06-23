package com.example.emsdept.controller;

import com.example.emsdept.entity.Department;
import com.example.emsdept.service.DepartmentService;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;
    private final Firestore firestore;

    @GetMapping
    public ResponseEntity<Page<Department>>
    getDepartmentInformation(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        log.info("[START] DepartmentController.getDepartmentInformation - page: {}, size: {}", page, size);
        long start = System.currentTimeMillis();
        Page<Department> res = departmentService.getAllDepartments(page, size);
        log.info("[END] DepartmentController.getDepartmentInformation - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getDepartmentAnalytics() {
        log.info("[START] DepartmentController.getDepartmentAnalytics");
        long start = System.currentTimeMillis();
        try {
            var docRef = firestore.collection("analytics").document("department-stats");
            var document = docRef.get().get();
            if (document.exists()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    log.info("[END] DepartmentController.getDepartmentAnalytics (Firestore Cache Hit) - time taken: {}ms", System.currentTimeMillis() - start);
                    return ResponseEntity.ok(data);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch department analytics from Firestore, falling back to Spanner: " + e.getMessage());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDepartments", departmentService.countDepartments());
        stats.put("departmentCounts", departmentService.getDepartmentEmployeeCounts());

        try {
            firestore.collection("analytics").document("department-stats").set(stats);
        } catch (Exception e) {
            System.err.println("Failed to cache department stats to Firestore: " + e.getMessage());
        }

        log.info("[END] DepartmentController.getDepartmentAnalytics (Firestore Cache Miss/DB Query) - time taken: {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(stats);
    }





}