package com.miempresa.redis.application.port.out;

import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Puerto de salida para funcionalidades de locks distribuidos
 * Define cómo el sistema obtiene y maneja locks distribuidos
 */
public interface DistributedLockPort {

  /**
   * Intenta obtener un lock distribuido
   * 
   * @param lockKey  Clave del lock
   * @param timeout  Timeout en milisegundos
   * @param timeUnit Unidad de tiempo del timeout
   * @return Mono con true si se obtuvo el lock, false en caso contrario
   */
  Mono<Boolean> tryLock(String lockKey, long timeout, TimeUnit timeUnit);

  /**
   * Libera un lock distribuido
   * 
   * @param lockKey Clave del lock
   * @return Mono que se completa cuando se libera el lock
   */
  Mono<Void> unlock(String lockKey);

  /**
   * Verifica si un lock está activo
   * 
   * @param lockKey Clave del lock
   * @return Mono con true si el lock está activo
   */
  Mono<Boolean> isLocked(String lockKey);

  /**
   * Obtiene información del lock
   * 
   * @param lockKey Clave del lock
   * @return Mono con información del lock
   */
  Mono<LockInfo> getLockInfo(String lockKey);

  /**
   * Información del lock distribuido
   */
  interface LockInfo {
    String getLockKey();

    boolean isLocked();

    long getHoldCount();

    long getRemainingTime();
  }
}