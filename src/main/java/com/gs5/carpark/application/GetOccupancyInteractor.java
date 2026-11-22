package com.gs5.carpark.application;

import com.gs5.carpark.domain.CarPark;
import com.gs5.carpark.domain.GetOccupancyUseCase;
import com.gs5.carpark.domain.Occupancy;

public final class GetOccupancyInteractor implements GetOccupancyUseCase {

    private final CarPark carPark;

    public GetOccupancyInteractor(CarPark carPark) {
        this.carPark = carPark;
    }


    @Override
    public Occupancy get() {
        return carPark.occupancy();
    }
}
