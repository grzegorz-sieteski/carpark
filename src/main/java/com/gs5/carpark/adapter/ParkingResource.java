package com.gs5.carpark.adapter;


import com.gs5.carpark.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

import static com.gs5.carpark.adapter.ParkingResource.ParkingCheckOutResponse.fromDomain;
import static com.gs5.carpark.adapter.ParkingResource.ParkingResponse.fromDomain;


@RestController
@RequestMapping("/")
public final class ParkingResource {

    private final GetOccupancyUseCase getOccupancyUseCase;
    private final ParkingCheckInUseCase parkingCheckInUseCase;
    private final ParkingCheckOutUseCase parkingCheckOutUseCase;

    public ParkingResource(GetOccupancyUseCase getOccupancyUseCase, ParkingCheckInUseCase parkingCheckInUseCase, ParkingCheckOutUseCase parkingCheckOutUseCase) {
        this.getOccupancyUseCase = getOccupancyUseCase;
        this.parkingCheckInUseCase = parkingCheckInUseCase;
        this.parkingCheckOutUseCase = parkingCheckOutUseCase;
    }

    @GetMapping("parking")
    public ResponseEntity<ParkingInfoResponse> getParkingInfo() {
        final var occupancy = getOccupancyUseCase.get();
        return ResponseEntity.ok(new ParkingInfoResponse(occupancy.availableSpaces(), occupancy.occupiedSpaces()));
    }

    public record ParkingInfoResponse(int availableSpaces, int occupiedSpaces) {}

    @PostMapping("/parking")
    public ResponseEntity<?> parking(@Valid @RequestBody ParkingRequest request) {
        return parkingCheckInUseCase.execute(request.toDomain())
                .<ResponseEntity<?>>map(domain -> ResponseEntity.ok(fromDomain(domain)))
                .orElseGet(() -> problem(HttpStatus.CONFLICT, "No free space or vehicle already parked"));
    }

    public record ParkingRequest(
            @NotBlank(message = "vehicleReg is required") String vehicleReg,
            @Range(min = 1, max = 3, message = "vehicleType must be between 1 and 3") int vehicleType) {

        public Vehicle toDomain() {
            return Vehicle.of(new RegistrationNumber(vehicleReg), vehicleType);
        }
    }

    public record ParkingResponse(String vehicleReg, Instant timeIn) {
        public static ParkingResponse fromDomain(ParkingCheckInUseCase.ParkingCheckResponse response) {
            return new ParkingResponse(response.vehicleReg().number(), response.timeIn());
        }
    }

    @PostMapping("/parking/bill")
    public ResponseEntity<?> parkingBill(@Valid @RequestBody ParkingCheckOutRequest request) {
        return parkingCheckOutUseCase.execute(request.toDomain())
                .<ResponseEntity<?>>map(domain -> ResponseEntity.ok(fromDomain(domain)))
                .orElseGet(() -> problem(HttpStatus.NOT_FOUND, "No active parking session found for this vehicle"));
    }

    public record ParkingCheckOutRequest(@NotBlank(message = "vehicleReg is required") String vehicleReg) {
        public RegistrationNumber toDomain() {
            return new RegistrationNumber(vehicleReg);
        }
    }

    public record ParkingCheckOutResponse(String vehicleReg, BigDecimal vehicleCharge, Instant timeIn, Instant timeOut) {
        public static ParkingCheckOutResponse fromDomain(ParkingCheckOutUseCase.ParkingCheckOutResponse response) {
            final var bill = response.bill();
            return new ParkingCheckOutResponse(bill.registrationNumber().number(),
                    bill.vehicleCharge(),
                    bill.timeIn(),
                    bill.timeOut());
        }
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
