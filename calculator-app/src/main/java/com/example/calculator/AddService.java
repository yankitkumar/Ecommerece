package com.example.calculator;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class AddService {

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }
}
