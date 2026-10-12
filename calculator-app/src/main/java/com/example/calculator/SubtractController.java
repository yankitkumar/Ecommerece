package com.example.calculator;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculator/subtract")
public class SubtractController {

    private final SubtractService service;

    public SubtractController(SubtractService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, BigDecimal> subtract(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.subtract(a, b));
    }
}
