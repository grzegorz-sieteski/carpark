package com.gs5.carpark.adapter;

import com.gs5.carpark.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ParkingResource.class)
class ParkingResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetOccupancyUseCase getOccupancyUseCase;
    @MockitoBean
    private ParkingCheckInUseCase parkingCheckInUseCase;
    @MockitoBean
    private ParkingCheckOutUseCase parkingCheckOutUseCase;

    @Test
    void shouldReturn200WhenVehicleIsParked() throws Exception {
        // given
        final var timeIn = Instant.parse("2026-11-22T12:00:00Z");
        final var useCaseResponse = new ParkingCheckInUseCase.ParkingCheckResponse(
                new RegistrationNumber("LON 11111"),
                timeIn
        );

        when(parkingCheckInUseCase.execute(any(Vehicle.class)))
                .thenReturn(Optional.of(useCaseResponse));

        final var requestJson = """
                {
                    "vehicleReg": "LON 11111",
                    "vehicleType": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleReg").value("LON 11111"))
                .andExpect(jsonPath("$.timeIn").value("2026-11-22T12:00:00Z"));
    }

    @Test
    void shouldReturn409WhenNoAvailableParkingSpaces() throws Exception {
        // given
        when(parkingCheckInUseCase.execute(any(Vehicle.class)))
                .thenReturn(Optional.empty());

        final var requestJson = """
                {
                    "vehicleReg": "LON 11111",
                    "vehicleType": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No free space or vehicle already parked"));
    }

    @Test
    void shouldReturn200WithBillWhenVehicleChecksOut() throws Exception {
        // given
        final var timeIn = Instant.parse("2026-11-22T12:00:00Z");
        final var timeOut = Instant.parse("2026-11-22T12:10:00Z");
        final var bill = new Bill(
                new RegistrationNumber("LON 11111"),
                new BigDecimal("12.00"),
                timeIn,
                timeOut
        );
        final var useCaseResponse = new ParkingCheckOutUseCase.ParkingCheckOutResponse(bill);

        when(parkingCheckOutUseCase.execute(any(RegistrationNumber.class)))
                .thenReturn(Optional.of(useCaseResponse));

        final var requestJson = """
            {
                "vehicleReg": "LON 11111"
            }
            """;

        // when & then
        mockMvc.perform(post("/parking/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleReg").value("LON 11111"))
                .andExpect(jsonPath("$.vehicleCharge").value(12.00))
                .andExpect(jsonPath("$.timeIn").value("2026-11-22T12:00:00Z"))
                .andExpect(jsonPath("$.timeOut").value("2026-11-22T12:10:00Z"));
    }

    @Test
    void shouldReturn404WhenNoActiveParkingSessionFound() throws Exception {
        // given
        when(parkingCheckOutUseCase.execute(any(RegistrationNumber.class)))
                .thenReturn(Optional.empty());

        final var requestJson = """
            {
                "vehicleReg": "LON 99999"
            }
            """;

        // when & then
        mockMvc.perform(post("/parking/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No active parking session found for this vehicle"));
    }

    @Test
    void shouldReturn200WithParkingInfo() throws Exception {
        // given
        when(getOccupancyUseCase.get()).thenReturn(new Occupancy(1, 1));

        // when & then
        mockMvc.perform(get("/parking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpaces").value(1))
                .andExpect(jsonPath("$.occupiedSpaces").value(1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldReturn400WhenCheckInRegistrationIsBlank(String registration) throws Exception {
        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInRequest(registration, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("vehicleReg is required"));

        verifyNoInteractions(parkingCheckInUseCase);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 4})
    void shouldReturn400WhenVehicleTypeIsInvalid(int vehicleType) throws Exception {
        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInRequest("LON 11111", vehicleType)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("vehicleType must be between 1 and 3"));

        verifyNoInteractions(parkingCheckInUseCase);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void shouldAcceptEveryValidVehicleType(int vehicleType) throws Exception {
        final var useCaseResponse = new ParkingCheckInUseCase.ParkingCheckResponse(
                new RegistrationNumber("LON 11111"),
                Instant.parse("2026-11-22T12:00:00Z")
        );
        when(parkingCheckInUseCase.execute(any(Vehicle.class))).thenReturn(Optional.of(useCaseResponse));

        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInRequest("LON 11111", vehicleType)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldReturn400WhenCheckOutRegistrationIsBlank(String registration) throws Exception {
        mockMvc.perform(post("/parking/bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkOutRequest(registration)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("vehicleReg is required"));

        verifyNoInteractions(parkingCheckOutUseCase);
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parkingCheckInUseCase);
    }

    private static String checkInRequest(String registration, int vehicleType) {
        return """
                {"vehicleReg": %s, "vehicleType": %d}
                """.formatted(jsonValue(registration), vehicleType);
    }

    private static String checkOutRequest(String registration) {
        return """
                {"vehicleReg": %s}
                """.formatted(jsonValue(registration));
    }

    private static String jsonValue(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}