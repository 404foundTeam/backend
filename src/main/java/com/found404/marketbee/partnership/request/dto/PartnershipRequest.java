package com.found404.marketbee.partnership.request.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PartnershipRequest(
        @NotNull
        Long partnerStoreId,

        @NotBlank
        @Size(max = 255)
        String purpose,

        @NotBlank
        String details,

        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {}