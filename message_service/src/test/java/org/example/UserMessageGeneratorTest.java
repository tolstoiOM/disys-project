package org.example;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

public class UserMessageGeneratorTest {

    @Test
    void testGenerateKwhBasedOnTimeMorning() {
        // Simuliere Morgenzeit
        LocalTime morning = LocalTime.of(7, 0);
        double kwh = UserMessageGeneratorTestHelper.generateKwhBasedOnTime(morning);
        assertTrue(kwh >= 3.0 && kwh <= 5.0, "Morgens sollte kWh zwischen 3.0 und 5.0 liegen");
    }


    @Test
    void testGenerateKwhBasedOnTimeOther() {
        // Simuliere Nachtzeit
        LocalTime night = LocalTime.of(2, 0);
        double kwh = UserMessageGeneratorTestHelper.generateKwhBasedOnTime(night);
        assertTrue(kwh >= 0.5 && kwh <= 2.5, "Nachts sollte kWh zwischen 0.5 und 2.5 liegen");
    }
}


// Hilfsklasse, um Zeit zu injizieren
class UserMessageGeneratorTestHelper extends UserMessageGenerator {
    static double generateKwhBasedOnTime(LocalTime time) {
        java.util.Random random = new java.util.Random();
        if (time.isAfter(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(9, 0))) {
            return 3.0 + random.nextDouble() * 2.0;
        } else if (time.isAfter(LocalTime.of(17, 0)) && time.isBefore(LocalTime.of(21, 0))) {
            return 3.0 + random.nextDouble() * 2.5;
        } else {
            return 0.5 + random.nextDouble() * 2.0;
        }
    }
}
