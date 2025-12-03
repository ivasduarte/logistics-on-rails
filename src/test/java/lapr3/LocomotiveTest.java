package lapr3;

import org.junit.jupiter.api.Test;
import freightMngt.domain.Locomotive;

import static org.junit.jupiter.api.Assertions.*;

public class LocomotiveTest {

    @Test
    public void testLocomotiveConstructorAndGetters() {
        Locomotive locomotive = new Locomotive(1, "Test Loco", "TestMake", "TestModel",
                2020, 4, "TestBogies", 5000, 20.5, 3.2, 4.1,
                80000, 160, 120, 1, "Electric", 25000, 50,
                "TestOperator", 1435, 0);

        assertEquals(1, locomotive.getLocomotiveID());
        assertEquals("Test Loco", locomotive.getLocomotiveName());
        assertEquals("TestMake", locomotive.getMake());
        assertEquals("TestModel", locomotive.getModel());
        assertEquals(2020, locomotive.getServiceYear());
        assertEquals(4, locomotive.getNumberBogies());
        assertEquals("TestBogies", locomotive.getBogies());
        assertEquals(5000, locomotive.getPower());
        assertEquals(20.5, locomotive.getLength(), 0.001);
        assertEquals(3.2, locomotive.getWidth(), 0.001);
        assertEquals(4.1, locomotive.getHeight(), 0.001);
        assertEquals(80000, locomotive.getWeight());
        assertEquals(160, locomotive.getMaxSpeed());
        assertEquals(120, locomotive.getOperationalSpeed());
        assertEquals(1, locomotive.getTraction());
        assertEquals("Electric", locomotive.getType());
        assertEquals(25000, locomotive.getVoltage());
        assertEquals(50, locomotive.getFrequency());
        assertEquals("TestOperator", locomotive.getOperator());
        assertEquals(1435, locomotive.getGauge());
        assertEquals(0, locomotive.getFuel());
    }
}
