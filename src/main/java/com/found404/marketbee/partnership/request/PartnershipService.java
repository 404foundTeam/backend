package com.found404.marketbee.partnership.request;

import com.found404.marketbee.partnership.Partnership;
import com.found404.marketbee.partnership.PartnershipRepository;
import com.found404.marketbee.partnership.request.dto.PartnershipRequest;
import com.found404.marketbee.partnership.request.dto.PartnershipResponse;
import com.found404.marketbee.partnership.request.dto.SearchedStore;
import com.found404.marketbee.store.Store;
import com.found404.marketbee.store.StoreRepository;
import com.found404.marketbee.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnershipService {

    private final PartnershipKakaoClient kakaoClient;
    private final PartnershipRepository partnershipRepository;
    private final StoreRepository storeRepository;

    private static final int INITIAL_LOAD_RADIUS = 1000;
    private static final int CATEGORY_SEARCH_RADIUS = 2000;

    @Transactional(readOnly = true)
    public List<SearchedStore> searchStores(String keyword, String category, String longitude, String latitude, User currentUser) {
        Store userStore = currentUser.getStore();
        if (userStore == null) {
            throw new IllegalStateException("가게를 등록한 사용자만 제휴 기능을 사용할 수 있습니다.");
        }

        if ((keyword != null && !keyword.isBlank()) || (category != null && !category.isBlank())) {
            String finalCategoryCode = null;
            if (category != null && !category.isBlank()) {
                if ("same".equalsIgnoreCase(category)) {
                    finalCategoryCode = userStore.getCategory();
                } else {
                    Category.findByCode(category)
                            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 카테고리 코드입니다: " + category));
                    finalCategoryCode = category;
                }
            }

            List<Map<String, Object>> searchResult;
            if (keyword != null && !keyword.isBlank()) {
                searchResult = kakaoClient.searchByKeyword(keyword, longitude, latitude, finalCategoryCode);
            } else {
                searchResult = kakaoClient.searchByCategory(finalCategoryCode, longitude, latitude, CATEGORY_SEARCH_RADIUS);
            }
            if (searchResult.isEmpty()) {
                return List.of();
            }

            List<String> placeIds = searchResult.stream()
                    .map(item -> (String) item.get("id"))
                    .toList();

            return storeRepository.findByPlaceIdIn(placeIds).stream()
                    .filter(store -> !store.getId().equals(userStore.getId()))
                    .map(SearchedStore::from)
                    .collect(Collectors.toList());

        } else {
            return storeRepository.findNearbyStores(
                            userStore.getLatitude(),
                            userStore.getLongitude(),
                            INITIAL_LOAD_RADIUS,
                            userStore.getId()
                    ).stream()
                    .map(SearchedStore::from)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public PartnershipResponse createPartnershipRequest(PartnershipRequest requestDto, User currentUser) {
        Store requesterStore = currentUser.getStore();
        if (requesterStore == null) {
            throw new IllegalStateException("가게를 등록한 사용자만 제휴를 요청할 수 있습니다.");
        }

        if (requesterStore.getId().equals(requestDto.partnerStoreId())) {
            throw new IllegalArgumentException("자신의 가게에는 제휴를 요청할 수 없습니다.");
        }

        if (requestDto.endDate().isBefore(requestDto.startDate())) {
            throw new IllegalArgumentException("제휴 종료일은 시작일보다 이전 날짜일 수 없습니다.");
        }

        Store partnerStore = storeRepository.findById(requestDto.partnerStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다. ID: " + requestDto.partnerStoreId()));

        Partnership partnership = Partnership.builder()
                .requesterStore(requesterStore)
                .partnerStore(partnerStore)
                .purpose(requestDto.purpose())
                .details(requestDto.details())
                .startDate(requestDto.startDate())
                .endDate(requestDto.endDate())
                .build();

        Partnership savedPartnership = partnershipRepository.save(partnership);

        return new PartnershipResponse(savedPartnership.getId(), "제휴 요청이 성공적으로 전송되었습니다.");
    }
}