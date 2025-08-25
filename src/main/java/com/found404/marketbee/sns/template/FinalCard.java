package com.found404.marketbee.sns.template;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "final_cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_uuid", nullable = false, length = 36)
    private String storeUuid;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


}