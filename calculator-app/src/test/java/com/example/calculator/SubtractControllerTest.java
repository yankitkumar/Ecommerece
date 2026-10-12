package com.example.calculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubtractController.class)
class SubtractControllerTest {

    @Autowired
    MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    SubtractService unused;

    @Test
    void subtract() throws Exception {
        org.mockito.Mockito.when(unused.subtract(new java.math.BigDecimal("10"), new java.math.BigDecimal("4")))
                .thenReturn(new java.math.BigDecimal("6"));
        mvc.perform(get("/api/calculator/subtract").param("a", "10").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(6));
    }
}
