package my.demo.service;

import jakarta.inject.Singleton;
import my.demo.model.CarType;

import java.util.EnumMap;
import java.util.Map;

@Singleton
public class FleetInventory {

    private final Map<CarType, Integer> capacityByType;

    public FleetInventory() {
        EnumMap<CarType, Integer> map = new EnumMap<>(CarType.class);
        map.put(CarType.SEDAN, 3);
        map.put(CarType.SUV, 2);
        map.put(CarType.VAN, 1);
        this.capacityByType = map;
    }

    // package-private constructor for testing
    FleetInventory(Map<CarType, Integer> capacityByType) {
        this.capacityByType = new EnumMap<>(capacityByType);
    }

    public int getCapacity(CarType carType) {
        return capacityByType.getOrDefault(carType, 0);
    }
}
