package com.found404.marketbee.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.store WHERE u.userId = :userId")
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
}