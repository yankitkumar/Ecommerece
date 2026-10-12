package com.example.calculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MultiplyController.class)
class MultiplyControllerTest {

    @Autowired
    MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    MultiplyService unused;

    @Test
    void multiply() throws Exception {
        org.mockito.Mockito.when(unused.multiply(new java.math.BigDecimal("6"), new java.math.BigDecimal("7")))
                .thenReturn(new java.math.BigDecimal("42"));
        mvc.perform(get("/api/calculator/multiply").param("a", "6").param("b", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(42));
    }
}
