package com.example.calculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void add() throws Exception {
        mvc.perform(get("/api/calculator/add").param("a", "2").param("b", "3.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(5.5));
    }

    @Test
    void subtract() throws Exception {
        mvc.perform(get("/api/calculator/subtract").param("a", "10").param("b", "4"))
                .andExpect(jsonPath("$.result").value(6));
    }

    @Test
    void multiply() throws Exception {
        mvc.perform(get("/api/calculator/multiply").param("a", "6").param("b", "7"))
                .andExpect(jsonPath("$.result").value(42));
    }

    @Test
    void divide() throws Exception {
        mvc.perform(get("/api/calculator/divide").param("a", "10").param("b", "4"))
                .andExpect(jsonPath("$.result").value(2.5));
    }

    @Test
    void divideByZeroReturns400() throws Exception {
        mvc.perform(get("/api/calculator/divide").param("a", "1").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Division by zero"));
    }
}
