package com.found404.marketbee.store;

import com.found404.marketbee.store.dto.StoreCreateReq;
import com.found404.marketbee.store.dto.StoreCreateResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository repo;

    public StoreCreateResp create(StoreCreateReq req) {
        Optional<Store> existing = repo.findByPlaceId(req.placeId());
        if (existing.isPresent()) {
            Store existStore = existing.get();
            return new StoreCreateResp(
                    existStore.getStoreUuid(),
                    existStore.getPlaceName(),
            false
            );
        }

        Store s = new Store();
        s.setStoreUuid(UUID.randomUUID().toString());
        s.setPlaceId(req.placeId());
        s.setPlaceName(req.placeName());
        s.setRoadAddress(req.roadAddress());
        s.setLongitude(req.longitude());
        s.setLatitude(req.latitude());

        repo.save(s);
        return new StoreCreateResp(s.getStoreUuid(),s.getPlaceName() , true);
    }
}