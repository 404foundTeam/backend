package com.found404.marketbee.sns.template;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinalCardRepository extends JpaRepository<FinalCard, Long> {
    Page<FinalCard> findByStoreUuidOrderByCreatedAtDesc(String storeUuid, Pageable pageable);
    boolean existsByStoreUuidAndUrl(String storeUuid, String url);
}