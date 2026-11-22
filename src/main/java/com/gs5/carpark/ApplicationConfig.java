package com.gs5.carpark;

import com.gs5.carpark.application.CheckOutInteractor;
import com.gs5.carpark.application.GetOccupancyInteractor;
import com.gs5.carpark.application.ParkingCheckInInteractor;
import com.gs5.carpark.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {


    @Bean
    public CarPark carPark() {
        return new CarPark();
    }

    @Bean
    public GetOccupancyUseCase getOccupancyUseCase(CarPark carPark) {
        return new GetOccupancyInteractor(carPark);
    }

    @Bean
    public ParkingCheckInUseCase parkingCheckInUseCase(CarPark carPark) {
        return new ParkingCheckInInteractor(carPark);
    }

    @Bean
    public ParkingCheckOutUseCase parkingCheckOutUseCase(CarPark carPark) {
        return new CheckOutInteractor(carPark);
    }
}
