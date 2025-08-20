package com.found404.marketbee.sns.template;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FreeUsageService {
    private final StringRedisTemplate redisTemplate;
    private static final int FREE_LIMIT = 5;

    public int getRemainingCount(String uuid) {
        String key = buildKey(uuid);
        String value = redisTemplate.opsForValue().get(key);
        int used = (value != null) ? Integer.parseInt(value) : 0;
        return FREE_LIMIT - used;
    }

    public void increaseUsage(String uuid) {
        String key = buildKey(uuid);
        Long used = redisTemplate.opsForValue().increment(key);

        if (used != null && used == 1) {
            redisTemplate.expireAt(key, getEndOfMonth());
        }

        if (used != null && used > FREE_LIMIT) {
            throw new RuntimeException("이번 달 무료 사용 횟수를 모두 소진했습니다.");
        }
    }

    private String buildKey(String uuid) {
        String month = YearMonth.now().toString().replace("-", "");
        return "free-usage:" + uuid + ":" + month;
    }

    private Date getEndOfMonth() {
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();
        return Date.from(endOfMonth.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    }
}
