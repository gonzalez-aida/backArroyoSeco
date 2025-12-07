package mx.edu.uteq.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RateLimitService {

    private record RateLimitRecord(Instant firstAccess, int count) {} 

    private final Map<String, RateLimitRecord> requestMap = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 100; 
    private static final long TIME_PERIOD_SECONDS = 10; 
   
    public boolean tryConsume(String key) {
        Instant now = Instant.now();
        
        AtomicReference<Boolean> consumed = new AtomicReference<>(false);

        requestMap.compute(key, (k, currentRecord) -> {
            if (currentRecord == null) {
                consumed.set(true);
                return new RateLimitRecord(now, 1);
            }

            long elapsedSeconds = java.time.Duration.between(currentRecord.firstAccess(), now).getSeconds();

            if (elapsedSeconds >= TIME_PERIOD_SECONDS) {
                consumed.set(true);
                return new RateLimitRecord(now, 1);
            } 
            
            if (currentRecord.count() < MAX_REQUESTS) {
                consumed.set(true);
                return new RateLimitRecord(currentRecord.firstAccess(), currentRecord.count() + 1);
            } else {
                consumed.set(false);
                return currentRecord; 
            }
        });

        return consumed.get();
    }
}