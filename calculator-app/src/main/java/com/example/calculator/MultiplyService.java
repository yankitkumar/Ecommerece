package com.example.calculator;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class MultiplyService {

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }
}
