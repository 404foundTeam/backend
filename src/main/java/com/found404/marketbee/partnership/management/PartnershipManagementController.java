package com.found404.marketbee.partnership.management;

import com.found404.marketbee.partnership.management.dto.PartnershipDetail;
import com.found404.marketbee.partnership.management.dto.PartnershipList;
import com.found404.marketbee.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/partnership")
@RequiredArgsConstructor
public class PartnershipManagementController {

    private final PartnershipManagementService managementService;

    @GetMapping("/active")
    public ResponseEntity<List<PartnershipList>> getActivePartnerships(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(managementService.getActivePartnerships(currentUser));
    }

    @GetMapping("/request/sent")
    public ResponseEntity<List<PartnershipList>> getSentRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(managementService.getSentRequests(currentUser));
    }

    @GetMapping("/request/received")
    public ResponseEntity<List<PartnershipList>> getReceivedRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(managementService.getReceivedRequests(currentUser));
    }

    @GetMapping("/content/{partnershipId}")
    public ResponseEntity<PartnershipDetail> getPartnershipDetails(@PathVariable UUID partnershipId, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(managementService.getPartnershipDetails(partnershipId, currentUser));
    }

    @PostMapping("/accept/{partnershipId}")
    public ResponseEntity<Void> acceptRequest(@PathVariable UUID partnershipId, @AuthenticationPrincipal User currentUser) {
        managementService.acceptRequest(partnershipId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{partnershipId}")
    public ResponseEntity<Void> rejectRequest(@PathVariable UUID partnershipId, @AuthenticationPrincipal User currentUser) {
        managementService.rejectRequest(partnershipId, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{partnershipId}")
    public ResponseEntity<Void> deleteSentRequest(@PathVariable UUID partnershipId, @AuthenticationPrincipal User currentUser) {
        managementService.deleteSentRequest(partnershipId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hide/{partnershipId}")
    public ResponseEntity<Void> hideCompletedPartnership(@PathVariable UUID partnershipId, @AuthenticationPrincipal User currentUser) {
        managementService.hideCompletedPartnership(partnershipId, currentUser);
        return ResponseEntity.noContent().build();
    }
}