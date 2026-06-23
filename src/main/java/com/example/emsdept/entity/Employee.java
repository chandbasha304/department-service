package com.example.emsdept.entity;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Persistable<Long> {

    @Id
    private Long id;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    public void markNotNew() {
        this.isNew = false;
    }

    private String employeeCode;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String designation;

    private BigDecimal salary;

    private LocalDate joiningDate;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
