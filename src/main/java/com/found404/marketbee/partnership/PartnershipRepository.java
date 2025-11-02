package com.found404.marketbee.partnership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PartnershipRepository extends JpaRepository<Partnership, UUID> {
    @Query("SELECT p FROM Partnership p WHERE " +
            "(p.requesterStore.id = :storeId AND p.requesterHidden = false AND p.status IN ('ACTIVE', 'COMPLETED')) OR " +
            "(p.partnerStore.id = :storeId AND p.partnerHidden = false AND p.status IN ('ACTIVE', 'COMPLETED')) " +
            "ORDER BY CASE WHEN p.status = 'COMPLETED' THEN 1 " +
            "             WHEN p.status = 'ACTIVE' AND p.endDate BETWEEN CURRENT_DATE AND :sevenDaysLater THEN 2 " +
            "             ELSE 3 END, " +
            "p.endDate ASC")
    List<Partnership> findActiveAndCompletedPartnerships(@Param("storeId") Long storeId, @Param("sevenDaysLater") LocalDate sevenDaysLater);

    @Query("SELECT p FROM Partnership p WHERE p.requesterStore.id = :storeId AND p.status IN ('PENDING', 'REJECTED') " +
            "ORDER BY CASE WHEN p.status = 'REJECTED' THEN 1 ELSE 2 END, p.createdAt DESC")
    List<Partnership> findSentPartnershipRequests(@Param("storeId") Long storeId);

    @Query("SELECT p FROM Partnership p WHERE p.partnerStore.id = :storeId AND p.status = 'PENDING' ORDER BY p.createdAt DESC")
    List<Partnership> findReceivedPartnershipRequests(@Param("storeId") Long storeId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Partnership p SET p.status = 'COMPLETED' WHERE p.status = 'ACTIVE' AND p.endDate < :today")
    int updateExpiredPartnershipsToCompleted(@Param("today") LocalDate today);
}