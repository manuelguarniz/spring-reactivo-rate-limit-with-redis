package com.miempresa.redis.infrastructure.adapter.in.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para operaciones relacionadas con el tiempo
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class TimeController {

  @GetMapping("/time")
  public ResponseEntity<Map<String, Object>> getCurrentTime() {
    log.debug("Time request received");

    Map<String, Object> response = new HashMap<>();

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    response.put("currentTime", now.format(formatter));
    response.put("timestamp", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    response.put("timezone", "Local");
    response.put("message", "Current server time");

    log.debug("Time response generated: {}", now.format(formatter));
    return ResponseEntity.ok(response);
  }
}