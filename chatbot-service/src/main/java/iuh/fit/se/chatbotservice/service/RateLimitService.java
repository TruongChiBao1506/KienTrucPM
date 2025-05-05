package iuh.fit.se.chatbotservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    // Giới hạn 15 requests mỗi phút theo quy định của Gemini 2.0 Flash
    private static final int MAX_REQUESTS_PER_MINUTE = 15;

    // Lưu trữ số lượng request trong 1 phút
    private final Map<String, AtomicInteger> requestCountsPerMinute = new ConcurrentHashMap<>();

    // Lưu thời gian reset của mỗi key
    private final Map<String, Long> resetTimeMap = new ConcurrentHashMap<>();

    public boolean tryConsume(String key) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentMinute = currentTimeMillis / 60000;

        // Tạo key mới cho phút hiện tại
        String minuteKey = key + ":" + currentMinute;

        // Kiểm tra và reset nếu đã sang phút mới
        resetTimeMap.computeIfAbsent(key, k -> currentMinute);
        if (resetTimeMap.get(key) < currentMinute) {
            resetTimeMap.put(key, currentMinute);
            requestCountsPerMinute.remove(minuteKey);
        }

        // Tạo và tăng counter nếu chưa có
        AtomicInteger counter = requestCountsPerMinute.computeIfAbsent(minuteKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        // Kiểm tra nếu vượt quá giới hạn thì giảm counter và trả về false
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            counter.decrementAndGet();
            return false;
        }

        return true;
    }
}