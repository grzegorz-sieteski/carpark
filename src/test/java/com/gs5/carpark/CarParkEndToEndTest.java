package com.gs5.carpark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarParkEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRunFullParkingFlow() throws Exception {
        mockMvc.perform(get("/parking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpaces").value(2))
                .andExpect(jsonPath("$.occupiedSpaces").value(0));

        mockMvc.perform(checkIn("  lon   11111 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleReg").value("LON 11111"));
        mockMvc.perform(checkIn("LON 11111")).andExpect(status().isConflict());
        mockMvc.perform(checkIn("LON 22222")).andExpect(status().isOk());
        mockMvc.perform(checkIn("LON 33333")).andExpect(status().isConflict());

        mockMvc.perform(get("/parking"))
                .andExpect(jsonPath("$.availableSpaces").value(0))
                .andExpect(jsonPath("$.occupiedSpaces").value(2));

        mockMvc.perform(checkOut("lon 11111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleReg").value("LON 11111"))
                .andExpect(jsonPath("$.vehicleCharge").value(0.00));
        mockMvc.perform(checkOut("lon 11111")).andExpect(status().isNotFound());
        mockMvc.perform(checkOut("LON 22222")).andExpect(status().isOk());

        mockMvc.perform(get("/parking"))
                .andExpect(jsonPath("$.availableSpaces").value(2))
                .andExpect(jsonPath("$.occupiedSpaces").value(0));
    }

    private static MockHttpServletRequestBuilder checkIn(String reg) {
        return post("/parking").contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleReg\": \"" + reg + "\", \"vehicleType\": 1}");
    }

    private static MockHttpServletRequestBuilder checkOut(String reg) {
        return post("/parking/bill").contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleReg\": \"" + reg + "\"}");
    }
}
