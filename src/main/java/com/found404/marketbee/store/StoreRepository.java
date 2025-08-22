package com.found404.marketbee.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByPlaceId(String placeId);
    boolean existsByPlaceId(String placeId);
}