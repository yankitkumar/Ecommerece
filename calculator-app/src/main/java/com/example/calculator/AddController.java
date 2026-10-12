package com.example.calculator;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculator/add")
public class AddController {

    private final AddService service;

    public AddController(AddService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, BigDecimal> add(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        return Map.of("result", service.add(a, b));
    }
}
