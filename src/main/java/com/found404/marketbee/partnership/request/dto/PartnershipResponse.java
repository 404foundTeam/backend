package com.found404.marketbee.partnership.request.dto;

import java.util.UUID;

public record PartnershipResponse(
        UUID partnershipId,
        String message
) {
}