package com.miempresa.redis.application.port.out;

import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Puerto de salida para funcionalidades de cache distribuido
 * Define cómo el sistema maneja cache distribuido
 */
public interface DistributedCachePort {

  /**
   * Obtiene un valor del cache distribuido
   * 
   * @param key       Clave del cache
   * @param valueType Tipo de valor esperado
   * @param <T>       Tipo genérico del valor
   * @return Mono con el valor del cache o empty si no existe
   */
  <T> Mono<T> get(String key, Class<T> valueType);

  /**
   * Guarda un valor en el cache distribuido
   * 
   * @param key   Clave del cache
   * @param value Valor a guardar
   * @param <T>   Tipo genérico del valor
   * @return Mono que se completa cuando se guarda el valor
   */
  <T> Mono<Void> put(String key, T value);

  /**
   * Guarda un valor en el cache distribuido con TTL
   * 
   * @param key   Clave del cache
   * @param value Valor a guardar
   * @param ttl   Tiempo de vida en segundos
   * @param <T>   Tipo genérico del valor
   * @return Mono que se completa cuando se guarda el valor
   */
  <T> Mono<Void> put(String key, T value, long ttl);

  /**
   * Elimina un valor del cache distribuido
   * 
   * @param key Clave del cache
   * @return Mono que se completa cuando se elimina el valor
   */
  Mono<Void> remove(String key);

  /**
   * Verifica si existe una clave en el cache
   * 
   * @param key Clave del cache
   * @return Mono con true si existe la clave
   */
  Mono<Boolean> containsKey(String key);

  /**
   * Obtiene el TTL restante de una clave
   * 
   * @param key Clave del cache
   * @return Mono con el TTL restante en segundos, -1 si no tiene TTL, -2 si no
   *         existe
   */
  Mono<Long> getTtl(String key);

  /**
   * Establece el TTL de una clave existente
   * 
   * @param key Clave del cache
   * @param ttl Tiempo de vida en segundos
   * @return Mono que se completa cuando se establece el TTL
   */
  Mono<Void> expire(String key, long ttl);
}