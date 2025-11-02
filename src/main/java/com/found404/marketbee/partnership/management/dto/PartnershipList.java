package com.found404.marketbee.partnership.management.dto;

import com.found404.marketbee.partnership.Partnership;
import com.found404.marketbee.store.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@Builder
public class PartnershipList {

    private UUID partnershipId;
    private String partnerPlaceName;
    private String partnerStoreAddress;
    private String status;
    private String displayMessage;

    public static PartnershipList from(Partnership partnership, Long currentStoreId) {
        Store partnerStore = partnership.getRequesterStore().getId().equals(currentStoreId)
                ? partnership.getPartnerStore()
                : partnership.getRequesterStore();
        String message = generateDisplayMessage(partnership);

        return PartnershipList.builder()
                .partnershipId(partnership.getId())
                .partnerPlaceName(partnerStore.getPlaceName())
                .partnerStoreAddress(partnerStore.getRoadAddress())
                .status(partnership.getStatus().name())
                .displayMessage(message)
                .build();
    }

    private static String generateDisplayMessage(Partnership partnership) {
        if (partnership.getStatus() == Partnership.PartnershipStatus.COMPLETED) {
            return "만료됨";
        }
        if (partnership.getStatus() == Partnership.PartnershipStatus.REJECTED) {
            return "거절됨";
        }
        if (partnership.getStatus() == Partnership.PartnershipStatus.PENDING) {
            return "대기중";
        }
        if (partnership.getStatus() == Partnership.PartnershipStatus.ACTIVE) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), partnership.getEndDate());
            if (daysUntilExpiry >= 0 && daysUntilExpiry <= 7) {
                return daysUntilExpiry + "일 뒤 계약이 만료됩니다.";
            }
            return String.format("%s ~ %s", partnership.getStartDate(), partnership.getEndDate());
        }
        return "";
    }
}
