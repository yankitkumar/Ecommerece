package com.example.calculator;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculator/multiply")
public class MultiplyController {

    private final MultiplyService service;

    public MultiplyController(MultiplyService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, BigDecimal> multiply(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.multiply(a, b));
    }
}
