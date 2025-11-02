package com.found404.marketbee.partnership.management.dto;

import com.found404.marketbee.partnership.Partnership;
import com.found404.marketbee.store.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class PartnershipDetail {

    private UUID partnershipId;
    private String purpose;
    private String details;
    private LocalDate startDate;
    private LocalDate endDate;
    private String partnerPlaceName;

    public static PartnershipDetail from(Partnership partnership, Long currentStoreId) {
        Store partnerStore = partnership.getRequesterStore().getId().equals(currentStoreId)
                ? partnership.getPartnerStore() : partnership.getRequesterStore();

        return PartnershipDetail.builder()
                .partnershipId(partnership.getId())
                .purpose(partnership.getPurpose())
                .details(partnership.getDetails())
                .startDate(partnership.getStartDate())
                .endDate(partnership.getEndDate())
                .partnerPlaceName(partnerStore.getPlaceName())
                .build();
    }
}