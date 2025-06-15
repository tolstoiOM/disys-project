package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProducerMessageGeneratorTest {

    @Test
    void testCalculateProductionSunny() {
        double result = ProducerMessageGenerator.calculateProduction("Sunny");
        assertTrue(result >= 1.8 && result <= 2.2, "Sunny sollte zwischen 1.8 und 2.2 kWh liegen");
    }

    @Test
    void testRandomVariationRange() {
        for (int i = 0; i < 100; i++) {
            double variation = ProducerMessageGenerator.randomVariation();
            assertTrue(variation >= -0.2 && variation <= 0.2, "Variation sollte zwischen -0.2 und 0.2 liegen");
        }
    }
}