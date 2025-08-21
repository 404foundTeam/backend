package com.found404.marketbee.sns.template;

import com.found404.marketbee.sns.template.dto.FinalCardMyPageReq;
import com.found404.marketbee.sns.template.dto.FinalCardMyPageResp;
import com.found404.marketbee.sns.template.exception.DuplicateFinalCardException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinalCardService {
    private final FinalCardRepository finalCardRepository;

    @Transactional
    public void save(String storeUuid, String finalUrl) {
        if (storeUuid == null || storeUuid.isBlank()) {
            throw new IllegalArgumentException("storeUuid가 비어 있습니다.");
        }
        if (finalUrl == null || finalUrl.isBlank()) {
            throw new IllegalArgumentException("finalUrl이 비어 있습니다.");
        }
        if (finalCardRepository.existsByStoreUuidAndUrl(storeUuid, finalUrl)) {
            throw new DuplicateFinalCardException();
        }
        FinalCard card = FinalCard.builder()
                .storeUuid(storeUuid)
                .url(finalUrl)
                .build();

        finalCardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public FinalCardMyPageResp list(String storeUuid, int page, int size) {
        Page<FinalCard> p = finalCardRepository.findByStoreUuidOrderByCreatedAtDesc(
                storeUuid, PageRequest.of(page, size));
        List<FinalCardMyPageReq> items = p.getContent().stream()
                .map(fc -> new FinalCardMyPageReq(fc.getId(), fc.getUrl(), fc.getCreatedAt()))
                .toList();
        return new FinalCardMyPageResp(items, p.getNumber(), p.getSize(), p.getTotalElements());
    }

}
