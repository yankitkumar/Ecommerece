package com.example.calculator;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class SubtractService {

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }
}
