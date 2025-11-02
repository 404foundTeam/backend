package com.found404.marketbee.partnership.management;

import com.found404.marketbee.partnership.Partnership;
import com.found404.marketbee.partnership.PartnershipRepository;
import com.found404.marketbee.partnership.management.dto.PartnershipDetail;
import com.found404.marketbee.partnership.management.dto.PartnershipList;
import com.found404.marketbee.store.Store;
import com.found404.marketbee.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnershipManagementService {

    private final PartnershipRepository partnershipRepository;

    private Store getUserStore(User currentUser) {
        Store store = currentUser.getStore();
        if (store == null) {
            throw new IllegalStateException("가게를 등록한 사용자만 이용할 수 있습니다.");
        }
        return store;
    }

    @Transactional
    public List<PartnershipList> getActivePartnerships(User currentUser) {
        Store store = getUserStore(currentUser);
        partnershipRepository.updateExpiredPartnershipsToCompleted(LocalDate.now());
        LocalDate sevenDaysLater = LocalDate.now().plusDays(7);
        List<Partnership> partnerships = partnershipRepository.findActiveAndCompletedPartnerships(store.getId(), sevenDaysLater);
        return partnerships.stream()
                .map(p -> PartnershipList.from(p, store.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartnershipList> getSentRequests(User currentUser) {
        Store store = getUserStore(currentUser);
        List<Partnership> partnerships = partnershipRepository.findSentPartnershipRequests(store.getId());
        return partnerships.stream()
                .map(p -> PartnershipList.from(p, store.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartnershipList> getReceivedRequests(User currentUser) {
        Store store = getUserStore(currentUser);
        List<Partnership> partnerships = partnershipRepository.findReceivedPartnershipRequests(store.getId());
        return partnerships.stream()
                .map(p -> PartnershipList.from(p, store.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartnershipDetail getPartnershipDetails(UUID partnershipId, User currentUser) {
        Store store = getUserStore(currentUser);
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제휴입니다."));

        boolean isUserInvolved = partnership.getRequesterStore().getId().equals(store.getId()) ||
                partnership.getPartnerStore().getId().equals(store.getId());

        if (!isUserInvolved) {
            throw new SecurityException("제휴 내용을 조회할 권한이 없습니다.");
        }

        return PartnershipDetail.from(partnership, store.getId());
    }

    @Transactional
    public void acceptRequest(UUID partnershipId, User currentUser) {
        Store store = getUserStore(currentUser);
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제휴 요청입니다."));

        if (!partnership.getPartnerStore().getId().equals(store.getId())) {
            throw new SecurityException("요청을 수락할 권한이 없습니다.");
        }

        partnership.setStatus(Partnership.PartnershipStatus.ACTIVE);
        partnershipRepository.save(partnership);
    }

    @Transactional
    public void rejectRequest(UUID partnershipId, User currentUser) {
        Store store = getUserStore(currentUser);
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제휴 요청입니다."));

        if (!partnership.getPartnerStore().getId().equals(store.getId())) {
            throw new SecurityException("요청을 거절할 권한이 없습니다.");
        }

        partnership.setStatus(Partnership.PartnershipStatus.REJECTED);
        partnershipRepository.save(partnership);
    }

    @Transactional
    public void deleteSentRequest(UUID partnershipId, User currentUser) {
        Store store = getUserStore(currentUser);
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제휴 요청입니다."));

        if (!partnership.getRequesterStore().getId().equals(store.getId())) {
            throw new SecurityException("요청을 삭제할 권한이 없습니다.");
        }

        Partnership.PartnershipStatus currentStatus = partnership.getStatus();
        if (currentStatus != Partnership.PartnershipStatus.PENDING && currentStatus != Partnership.PartnershipStatus.REJECTED) {
            throw new IllegalStateException("대기중이거나 거절된 요청만 삭제할 수 있습니다. 현재 상태: " + currentStatus);
        }

        partnershipRepository.delete(partnership);
    }

    @Transactional
    public void hideCompletedPartnership(UUID partnershipId, User currentUser) {
        Store store = getUserStore(currentUser);
        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제휴입니다."));
        
        Partnership.PartnershipStatus currentStatus = partnership.getStatus();
        if (currentStatus != Partnership.PartnershipStatus.COMPLETED) {
            throw new IllegalStateException("만료된 요청만 숨길 수 있습니다. 현재 상태: " + currentStatus);
        }

        if (partnership.getRequesterStore().getId().equals(store.getId())) {
            partnership.setRequesterHidden(true);
        } else if (partnership.getPartnerStore().getId().equals(store.getId())) {
            partnership.setPartnerHidden(true);
        } else {
            throw new SecurityException("해당 제휴를 숨길 권한이 없습니다.");
        }

        if (partnership.isRequesterHidden() && partnership.isPartnerHidden()) {
            partnershipRepository.delete(partnership);
        } else {
            partnershipRepository.save(partnership);
        }
    }
}