package com.projects.cafe_winchester_backend.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {

    private final RedissonClient redissonClient;
    private static final long DEFAULT_WAIT_TIME = 10;
    private static final long DEFAULT_LEASE_TIME = 30;
    private static final String LOCK_PREFIX = "cafe_winchester:lock:";

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean acquireLock(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
            return lock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);   // this method blocks until the default wait time is reached and if lock could not be acquired it returns false
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // As when the InterruptedException is caught the Interrupted status is reset. Hence to preserve the Interrupted Status for this Thread, it is Interrupted again here
            return false;
        }
    }

    public void releaseLock(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
