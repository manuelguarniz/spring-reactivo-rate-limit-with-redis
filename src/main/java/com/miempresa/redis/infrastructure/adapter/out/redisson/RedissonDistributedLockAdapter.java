package com.miempresa.redis.infrastructure.adapter.out.redisson;

import com.miempresa.redis.application.port.out.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * Adaptador de infraestructura para locks distribuidos usando Redisson
 * Implementa el puerto de salida DistributedLockPort
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLockAdapter implements DistributedLockPort {

  private final RedissonClient redisson;

  @Override
  public Mono<Boolean> tryLock(String lockKey, long timeout, TimeUnit timeUnit) {
    log.debug("Attempting to acquire lock: {} with timeout: {} {}", lockKey, timeout, timeUnit);

    return Mono.fromCallable(() -> {
      RLock lock = redisson.getLock(lockKey);
      try {
        boolean acquired = lock.tryLock(timeout, timeUnit);
        if (acquired) {
          log.debug("Lock acquired successfully: {}", lockKey);
        } else {
          log.debug("Failed to acquire lock: {} (timeout)", lockKey);
        }
        return acquired;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Lock acquisition interrupted: {}", lockKey);
        return false;
      }
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error acquiring lock: {}", lockKey, error))
        .onErrorReturn(false);
  }

  @Override
  public Mono<Void> unlock(String lockKey) {
    log.debug("Releasing lock: {}", lockKey);

    return Mono.fromCallable(() -> {
      RLock lock = redisson.getLock(lockKey);
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.debug("Lock released successfully: {}", lockKey);
      } else {
        log.warn("Attempted to release lock not held by current thread: {}", lockKey);
      }
      return null;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .doOnError(error -> log.error("Error releasing lock: {}", lockKey, error))
        .onErrorComplete();
  }

  @Override
  public Mono<Boolean> isLocked(String lockKey) {
    log.debug("Checking if lock is held: {}", lockKey);

    return Mono.fromCallable(() -> {
      RLock lock = redisson.getLock(lockKey);
      boolean isLocked = lock.isLocked();
      log.debug("Lock {} is locked: {}", lockKey, isLocked);
      return isLocked;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error checking lock status: {}", lockKey, error))
        .onErrorReturn(false);
  }

  @Override
  public Mono<DistributedLockPort.LockInfo> getLockInfo(String lockKey) {
    log.debug("Getting lock info: {}", lockKey);

    return Mono.fromCallable(() -> {
      RLock lock = redisson.getLock(lockKey);

      return (DistributedLockPort.LockInfo) new RedissonLockInfo(lockKey, lock);
    })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> log.error("Error getting lock info: {}", lockKey, error))
        .onErrorReturn(null);
  }

  /**
   * Implementación de LockInfo para Redisson
   */
  private static class RedissonLockInfo implements DistributedLockPort.LockInfo {
    private final String lockKey;
    private final RLock lock;

    public RedissonLockInfo(String lockKey, RLock lock) {
      this.lockKey = lockKey;
      this.lock = lock;
    }

    @Override
    public String getLockKey() {
      return lockKey;
    }

    @Override
    public boolean isLocked() {
      return lock.isLocked();
    }

    @Override
    public long getHoldCount() {
      return lock.getHoldCount();
    }

    @Override
    public long getRemainingTime() {
      try {
        return lock.remainTimeToLive();
      } catch (Exception e) {
        return -1;
      }
    }
  }
}