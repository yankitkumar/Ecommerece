package com.example.calculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DivideController.class)
@Import(DivideService.class)
class DivideControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void divide() throws Exception {
        mvc.perform(get("/api/calculator/divide").param("a", "10").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2.5));
    }

    @Test
    void divideByZeroReturns400() throws Exception {
        mvc.perform(get("/api/calculator/divide").param("a", "1").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Division by zero"));
    }
}
