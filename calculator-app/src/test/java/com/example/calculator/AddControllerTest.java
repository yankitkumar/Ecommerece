package com.example.calculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddController.class)
class AddControllerTest {

    @Autowired
    MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    AddService unused;

    @Test
    void add() throws Exception {
        org.mockito.Mockito.when(unused.add(new java.math.BigDecimal("2"), new java.math.BigDecimal("3")))
                .thenReturn(new java.math.BigDecimal("5"));
        mvc.perform(get("/api/calculator/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(5));
    }
}
