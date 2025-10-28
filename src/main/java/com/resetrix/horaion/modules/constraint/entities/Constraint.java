package com.resetrix.horaion.modules.constraint.entities;

import com.resetrix.horaion.modules.constraint.properties.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "constraints")
public class Constraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid",
            nullable = false,
            unique = true,
            updatable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sentence", length = 1000, nullable = false)
    private String sentence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schemas", columnDefinition = "jsonb")
    private Schema fields;

    @Column(name = "schemas_hash", length = 64)
    private String fieldsHash;

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
