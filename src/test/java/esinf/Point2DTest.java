package esinf;

import org.junit.jupiter.api.Test;
import spatialQuerying.trees.kd.Point2D;

import static org.junit.jupiter.api.Assertions.*;

class Point2DTest {

    @Test
    void testConstructorAndGetters() {
        Point2D point = new Point2D(40.5, -8.6);
        assertEquals(40.5, point.getStationLatitude());
        assertEquals(-8.6, point.getStationLongitude());
    }

    @Test
    void testEquals() {
        Point2D p1 = new Point2D(10.0, 20.0);
        Point2D p2 = new Point2D(10.0, 20.0);
        Point2D p3 = new Point2D(10.0000001, 20.0); // Slightly different
        Point2D p4 = new Point2D(5.0, 5.0);

        assertEquals(p1, p2, "Points with same coordinates should be equal");
        assertNotEquals(p1, p3, "Points with different latitudes should not be equal");
        assertNotEquals(p1, p4, "Completely different points should not be equal");
        assertNotEquals(p1, null, "Should not be equal to null");
        assertNotEquals(p1, "String", "Should not be equal to other object types");
    }

    @Test
    void testHashCode() {
        Point2D p1 = new Point2D(10.0, 20.0);
        Point2D p2 = new Point2D(10.0, 20.0);

        assertEquals(p1.hashCode(), p2.hashCode(), "Hashcodes de objetos iguais devem ser iguais");
    }

    @Test
    void testToString() {
        Point2D p = new Point2D(41.1234567, -8.6543211);
        String result = p.toString();
        assertTrue(result.contains("41,123457") || result.contains("41.123457"),
                "Should format with 6 decimal places (check locale . or ,)");
    }
}