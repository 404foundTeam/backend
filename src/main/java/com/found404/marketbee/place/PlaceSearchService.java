package com.found404.marketbee.place;

import com.found404.marketbee.place.dto.PlaceSearchResp;
import com.found404.marketbee.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoClient kakaoClient;
    private final StoreRepository repo;

    public PlaceSearchResp searchByCoord(double x, double y, int radius) {
        int sizePerCat = 5;
        int page = 1;

        List<Map<String, Object>> docs = kakaoClient.searchByCoord(
                String.valueOf(x), String.valueOf(y), "FD6", radius, page, sizePerCat
        );
        docs.addAll(kakaoClient.searchByCoord(
                String.valueOf(x), String.valueOf(y), "CE7", radius, page, sizePerCat
        ));

        var unique = docs.stream()
                .collect(Collectors.toMap(
                        d -> (String) d.get("id"),
                        d -> d,
                        (d1, d2) -> d1,
                        LinkedHashMap::new
                ))
                .values().stream().limit(5).toList();

        List<PlaceSearchResp.Item> items = unique.stream().map(d ->
                new PlaceSearchResp.Item(
                        (String) d.get("id"),
                        (String) d.get("place_name"),
                        (String) d.get("road_address_name"),
                        toBD((String) d.get("x")),
                        toBD((String) d.get("y"))
                )
        ).toList();

        return new PlaceSearchResp(items);
    }

    private static BigDecimal toBD(String s) {
        return s == null ? null : new BigDecimal(s);
    }
}
