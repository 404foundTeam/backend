package com.found404.marketbee.partnership;

import com.found404.marketbee.store.Store;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "partnerships")
public class Partnership {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_store_id", nullable = false)
    private Store requesterStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_store_id", nullable = false)
    private Store partnerStore;

    @Column(nullable = false, length = 255)
    private String purpose;

    @Lob
    @Column(nullable = false)
    private String details;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnershipStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "requester_hidden", nullable = false)
    private boolean requesterHidden = false;

    @Column(name = "partner_hidden", nullable = false)
    private boolean partnerHidden = false;

    public enum PartnershipStatus {
        PENDING,
        ACTIVE,
        REJECTED,
        COMPLETED
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = PartnershipStatus.PENDING;
        }
    }
}