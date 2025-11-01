package com.found404.marketbee.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByPlaceId(String placeId);
    boolean existsByPlaceId(String placeId);
    List<Store> findByPlaceIdIn(Collection<String> placeIds);
    @Query(value = "SELECT *, " +
            "ST_Distance_Sphere(point(longitude, latitude), point(:lon, :lat)) as distance " +
            "FROM stores " +
            "WHERE id != :myStoreId " +
            "HAVING distance <= :radius " +
            "ORDER BY distance " +
            "LIMIT 15",
            nativeQuery = true)
    List<Store> findNearbyStores(
            @Param("lat") BigDecimal latitude,
            @Param("lon") BigDecimal longitude,
            @Param("radius") int radius,
            @Param("myStoreId") Long myStoreId
    );
}