package com.found404.marketbee.partnership.request;

import com.found404.marketbee.partnership.request.dto.PartnershipRequest;
import com.found404.marketbee.partnership.request.dto.PartnershipResponse;
import com.found404.marketbee.partnership.request.dto.SearchedStore;
import com.found404.marketbee.partnership.request.dto.UserStoreInfoDto;
import com.found404.marketbee.store.Store;
import com.found404.marketbee.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/partnership")
@RequiredArgsConstructor
public class PartnershipController {

    private final PartnershipService partnershipService;

    @GetMapping
    public ResponseEntity<UserStoreInfoDto> getUserStoreInfo(@AuthenticationPrincipal User currentUser) {
        Store userStore = currentUser.getStore();
        if (userStore == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserStoreInfoDto.from(userStore));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchedStore>> searchStores(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String category,
            @RequestParam String longitude, @RequestParam String latitude, @AuthenticationPrincipal User currentUser) {
        List<SearchedStore> stores = partnershipService.searchStores(keyword, category, longitude, latitude, currentUser);
        return ResponseEntity.ok(stores);
    }

    @PostMapping("/request")
    public ResponseEntity<PartnershipResponse> requestPartnership(@Valid @RequestBody PartnershipRequest requestDto, @AuthenticationPrincipal User currentUser) {
        PartnershipResponse response = partnershipService.createPartnershipRequest(requestDto, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBusinessException(RuntimeException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}