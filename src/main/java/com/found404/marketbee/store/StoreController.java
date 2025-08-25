package com.found404.marketbee.store;

import com.found404.marketbee.store.dto.StoreCreateReq;
import com.found404.marketbee.store.dto.StoreCreateResp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @PostMapping("/match")
    public ResponseEntity<StoreCreateResp> create(@RequestBody StoreCreateReq req) {
        StoreCreateResp resp = storeService.create(req);
        return new ResponseEntity<>(resp, resp.isNew() ? HttpStatus.CREATED : HttpStatus.OK);
    }
}