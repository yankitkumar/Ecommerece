package com.example.calculator;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculator/divide")
public class DivideController {

    private final DivideService service;

    public DivideController(DivideService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, BigDecimal> divide(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.divide(a, b));
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Map<String, String>> handleArithmetic(ArithmeticException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
