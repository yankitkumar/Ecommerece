package com.example.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import org.springframework.stereotype.Service;

@Service
public class DivideService {

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a.divide(b, MathContext.DECIMAL64);
    }
}
