package com.demo.bookingsystem.utils;

import com.demo.bookingsystem.domain.entity.Unit;
import com.demo.bookingsystem.domain.enums.AccommodationType;
import com.demo.bookingsystem.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class UnitDataLoader implements CommandLineRunner {

    private final UnitRepository unitRepository;

    private static final Random random = new Random();

    @Override
    public void run(String... args) {
        if (unitRepository.count() < 100) {
            List<Unit> randomUnits = new ArrayList<>();
            for (int i = 0; i < 90; i++) {
                Unit unit = new Unit();
                unit.setRooms(random.nextInt(5) + 1);
                unit.setType(AccommodationType.valueOf(randomType()));
                unit.setFloor(random.nextInt(20));
                unit.setCost(50 + random.nextDouble() * 200);
                unit.setDescription("Random unit " + i);
                randomUnits.add(unit);
            }
            unitRepository.saveAll(randomUnits);
        }
    }

    private String randomType() {
        String[] types = {"HOME", "FLAT", "APARTMENTS"};
        return types[random.nextInt(types.length)];
    }
}
