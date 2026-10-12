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
@RequestMapping("/api/calculator")
public class CalculatorController {

    private final CalculatorService service;

    public CalculatorController(CalculatorService service) {
        this.service = service;
    }

    @GetMapping("/add")
    public Map<String, BigDecimal> add(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.add(a, b));
    }

    @GetMapping("/subtract")
    public Map<String, BigDecimal> subtract(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.subtract(a, b));
    }

    @GetMapping("/multiply")
    public Map<String, BigDecimal> multiply(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.multiply(a, b));
    }

    @GetMapping("/divide")
    public Map<String, BigDecimal> divide(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.divide(a, b));
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Map<String, String>> handleArithmetic(ArithmeticException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
