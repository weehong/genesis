package com.resetrix.horaion.modules.branch.entities;

import com.resetrix.horaion.modules.company.entities.Company;
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
@Table(name = "branches", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "branch_code"})
})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid",
            nullable = false,
            unique = true,
            updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email_address")
    private String emailAddress;

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
