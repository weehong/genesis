package com.resetrix.horaion.modules.department.entities;

import com.resetrix.horaion.modules.branch.entities.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "departments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "department_code"})
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid",
            nullable = false,
            unique = true,
            updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "soft_delete",
            nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean softDelete = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        if (softDelete == null) {
            softDelete = false;
        }
    }
}
