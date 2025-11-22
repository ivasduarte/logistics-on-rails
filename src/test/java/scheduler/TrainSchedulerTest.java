package scheduler;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Comprehensive unit tests for TrainScheduler
 * Tests all core functionality without using Mockito
 */
class TrainSchedulerTest {

    // ==========================================
    // HELPER CLASSES TESTS
    // ==========================================

    @Test
    @DisplayName("Test Train total weight calculation")
    void testTrain_TotalWeightCalculation() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Test Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 5000, 80000, 120, 100),
                new TrainScheduler.Locomotive(2, "Loc2", 5000, 80000, 120, 100)
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 20000, 50000, 100, 40000),
                new TrainScheduler.Wagon(2, 20000, 50000, 100, 30000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        double expectedWeight = 80000 + 80000 + 20000 + 40000 + 20000 + 30000;
        assertEquals(expectedWeight, train.getTotalWeight(), 0.01);
    }

    @Test
    @DisplayName("Test Train total power calculation")
    void testTrain_TotalPowerCalculation() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Test Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 5000, 80000, 120, 100),
                new TrainScheduler.Locomotive(2, "Loc2", 3000, 70000, 120, 100)
        );

        train.setLocomotives(locomotives);

        assertEquals(8000, train.getTotalPower(), 0.01);
    }

    @Test
    @DisplayName("Test Train max locomotive speed")
    void testTrain_MaxLocomotiveSpeed() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Test Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 5000, 80000, 140, 120),
                new TrainScheduler.Locomotive(2, "Loc2", 5000, 80000, 160, 100)
        );

        train.setLocomotives(locomotives);

        assertEquals(100, train.getMaxLocomotiveSpeed(), 0.01);
    }

    @Test
    @DisplayName("Test Train max wagon speed")
    void testTrain_MaxWagonSpeed() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Test Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 20000, 50000, 100, 40000),
                new TrainScheduler.Wagon(2, 20000, 50000, 80, 30000)
        );

        train.setWagons(wagons);

        assertEquals(80, train.getMaxWagonSpeed(), 0.01);
    }

    @Test
    @DisplayName("Test Wagon current load doesn't exceed payload")
    void testWagon_LoadLimitedByPayload() {
        TrainScheduler.Wagon wagon = new TrainScheduler.Wagon(
                1, 20000, 50000, 100, 60000);

        assertEquals(50000, wagon.getCurrentLoad(), 0.01);
    }

    @Test
    @DisplayName("Test Route segment management")
    void testRoute_SegmentManagement() {
        TrainScheduler.Route route = new TrainScheduler.Route(
                1, 1, 2, "Station A", "Station B"
        );

        TrainScheduler.RouteSegment segment1 = new TrainScheduler.RouteSegment(
                1, 10000, 100000, 1, true
        );
        TrainScheduler.RouteSegment segment2 = new TrainScheduler.RouteSegment(
                2, 15000, 100000, 2, false
        );

        route.addSegment(segment1);
        route.addSegment(segment2);

        assertEquals(2, route.getSegments().size());
        assertEquals(1, route.getSegments().get(0).getId());
        assertEquals(2, route.getSegments().get(1).getId());
    }

    @Test
    @DisplayName("Test Route siding management")
    void testRoute_SidingManagement() {
        TrainScheduler.Route route = new TrainScheduler.Route(
                1, 1, 2, "Station A", "Station B"
        );

        TrainScheduler.Siding siding = new TrainScheduler.Siding(
                1, 5000, 500, 1
        );

        route.addSiding(siding);

        assertEquals(1, route.getSidings().size());
        assertEquals(1, route.getSidings().get(0).getId());
    }

    // ==========================================
    // SPEED CALCULATION TESTS
    // ==========================================

    @Test
    @DisplayName("Test speed calculation with low power-to-weight ratio")
    void testCalculateTravelSpeed_LowPowerRatio() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createHeavyTrain();
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.now();

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);

        for (TrainScheduler.RouteSegment segment : route.getSegments()) {
            assertTrue(segment.getPermittedSpeed() < 84);
        }

        scheduler.close();
    }

    @Test
    @DisplayName("Test speed calculation with high power-to-weight ratio")
    void testCalculateTravelSpeed_HighPowerRatio() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createLightTrain();
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.now();

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);

        for (TrainScheduler.RouteSegment segment : route.getSegments()) {
            System.out.println("DEBUG: Permitted speed = " + segment.getPermittedSpeed());
            System.out.println("DEBUG: Power/Weight ratio = " + train.getTotalPower() / train.getTotalWeight());
            assertTrue(segment.getPermittedSpeed() > 90);
        }

        scheduler.close();
    }

    @Test
    @DisplayName("Test speed calculation respects locomotive max speed")
    void testCalculateTravelSpeed_LocomotiveSpeedLimit() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createTrainWithSlowLocomotive();
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.now();

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);

        for (TrainScheduler.RouteSegment segment : route.getSegments()) {
            assertTrue(segment.getPermittedSpeed() <= 60 * 0.85);
        }

        scheduler.close();
    }

    @Test
    @DisplayName("Test speed calculation respects wagon max speed")
    void testCalculateTravelSpeed_WagonSpeedLimit() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createTrainWithSlowWagons();
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.now();

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);

        for (TrainScheduler.RouteSegment segment : route.getSegments()) {
            assertTrue(segment.getPermittedSpeed() <= 80 * 0.85);
        }

        scheduler.close();
    }

    @Test
    @DisplayName("Test speed calculation with weight exceeding segment limit")
    void testCalculateTravelSpeed_ExceedsWeightLimit() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createVeryHeavyTrain();
        TrainScheduler.Route route = createMockRouteWithWeightLimit(50000);
        LocalDateTime departureTime = LocalDateTime.now();

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);
        scheduler.calculateTravelSpeed(scheduledTrain);

        assertNotNull(scheduledTrain.getRoute().getSegments().get(0).getPermittedSpeed());
        assertTrue(scheduledTrain.getRoute().getSegments().get(0).getPermittedSpeed() > 0);

        scheduler.close();
    }

    // ==========================================
    // PASSAGE TIME CALCULATION TESTS
    // ==========================================

    @Test
    @DisplayName("Test passage time calculation includes departure and arrival")
    void testCalculatePassageTimes_CompleteJourney() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createMockTrain(1);
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.of(2025, 11, 22, 10, 0);

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);
        scheduler.calculatePassageTimes(scheduledTrain);

        List<TrainScheduler.PassageTime> passages = scheduledTrain.getPassageTimes();

        assertTrue(passages.size() >= 3);
        assertEquals("DEPARTURE", passages.get(0).getType());
        assertEquals("ARRIVAL", passages.get(passages.size() - 1).getType());
        assertEquals(departureTime, passages.get(0).getTime());

        scheduler.close();
    }

    @Test
    @DisplayName("Test passage times are chronologically ordered")
    void testCalculatePassageTimes_ChronologicalOrder() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createMockTrain(1);
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.of(2025, 11, 22, 10, 0);

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);
        scheduler.calculatePassageTimes(scheduledTrain);

        List<TrainScheduler.PassageTime> passages = scheduledTrain.getPassageTimes();

        for (int i = 1; i < passages.size(); i++) {
            assertTrue(
                    passages.get(i).getTime().isAfter(passages.get(i - 1).getTime()) ||
                            passages.get(i).getTime().equals(passages.get(i - 1).getTime())
            );
        }

        scheduler.close();
    }

    @Test
    @DisplayName("Test passage times cumulative distance increases")
    void testCalculatePassageTimes_CumulativeDistance() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = createMockTrain(1);
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime departureTime = LocalDateTime.now();

        TrainScheduler.ScheduledTrain scheduledTrain =
                new TrainScheduler.ScheduledTrain(train, route, departureTime);

        scheduler.calculateTravelSpeed(scheduledTrain);
        scheduler.calculatePassageTimes(scheduledTrain);

        List<TrainScheduler.PassageTime> passages = scheduledTrain.getPassageTimes();

        for (int i = 1; i < passages.size(); i++) {
            assertTrue(passages.get(i).getDistance() >= passages.get(i - 1).getDistance());
        }

        scheduler.close();
    }


    @Test
    @DisplayName("Edge case: Train with no locomotives")
    void testEdgeCase_TrainWithNoLocomotives() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Empty Operator", LocalDate.now(), 1
        );

        train.setLocomotives(new ArrayList<>());
        train.setWagons(Arrays.asList(
                new TrainScheduler.Wagon(1, 20000, 50000, 100, 30000)
        ));

        assertEquals(0, train.getTotalPower());
        assertEquals(120, train.getMaxLocomotiveSpeed()); // Default value
    }

    @Test
    @DisplayName("Edge case: Train with no wagons")
    void testEdgeCase_TrainWithNoWagons() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Loco Only Operator", LocalDate.now(), 1
        );

        train.setLocomotives(Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 5000, 80000, 120, 100)
        ));
        train.setWagons(new ArrayList<>());

        assertEquals(80000, train.getTotalWeight());
        assertEquals(120, train.getMaxWagonSpeed()); // Default value
    }

    @Test
    @DisplayName("Boundary: Zero speed calculation")
    void testBoundary_ZeroSpeed() {
        TrainScheduler scheduler = new TrainScheduler();
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Zero Power", LocalDate.now(), 1
        );

        train.setLocomotives(Arrays.asList(
                new TrainScheduler.Locomotive(1, "Dead", 0, 80000, 120, 100)
        ));
        train.setWagons(Arrays.asList(
                new TrainScheduler.Wagon(1, 20000, 50000, 100, 30000)
        ));

        TrainScheduler.Route route = createMockRoute();
        TrainScheduler.ScheduledTrain scheduled =
                new TrainScheduler.ScheduledTrain(train, route, LocalDateTime.now());

        assertDoesNotThrow(() -> scheduler.calculateTravelSpeed(scheduled));

        scheduler.close();
    }

    // ==========================================
    // PASSAGE TIME AND CROSSING POINT TESTS
    // ==========================================

    @Test
    @DisplayName("Test PassageTime properties")
    void testPassageTime_Properties() {
        LocalDateTime time = LocalDateTime.of(2025, 11, 22, 10, 30);
        TrainScheduler.PassageTime pt = new TrainScheduler.PassageTime(
                "Test Location", time, 15000, "PASSAGE"
        );

        assertEquals("Test Location", pt.getLocation());
        assertEquals(time, pt.getTime());
        assertEquals(15000, pt.getDistance());
        assertEquals("PASSAGE", pt.getType());
    }

    @Test
    @DisplayName("Test CrossingOperation properties")
    void testCrossingOperation_Properties() {
        TrainScheduler.CrossingPoint cp = new TrainScheduler.CrossingPoint(
                1, "SIDING", 5000, "Siding 1"
        );

        TrainScheduler.CrossingOperation co = new TrainScheduler.CrossingOperation(
                1, 2, 10, cp
        );

        assertEquals(1, co.getTrain1Id());
        assertEquals(2, co.getTrain2Id());
        assertEquals(10, co.getSegmentId());
        assertEquals("SIDING", co.getCrossingPoint().getType());
        assertEquals(5000, co.getCrossingPoint().getPosition());
        assertEquals("Siding 1", co.getCrossingPoint().getName());
    }

    @Test
    @DisplayName("Test ScheduledTrain getters and setters")
    void testScheduledTrain_GettersSetters() {
        TrainScheduler.Train train = createMockTrain(1);
        TrainScheduler.Route route = createMockRoute();
        LocalDateTime time = LocalDateTime.of(2025, 11, 22, 10, 0);

        TrainScheduler.ScheduledTrain st = new TrainScheduler.ScheduledTrain(
                train, route, time
        );

        assertEquals(train, st.getTrain());
        assertEquals(route, st.getRoute());
        assertEquals(time, st.getDepartureTime());
        assertTrue(st.getPassageTimes().isEmpty());

        List<TrainScheduler.PassageTime> passages = Arrays.asList(
                new TrainScheduler.PassageTime("A", time, 0, "DEPARTURE"),
                new TrainScheduler.PassageTime("B", time.plusHours(1), 50000, "ARRIVAL")
        );

        st.setPassageTimes(passages);
        assertEquals(2, st.getPassageTimes().size());
    }

    @Test
    @DisplayName("Test RouteSegment electric vs non-electric")
    void testRouteSegment_ElectricProperty() {
        TrainScheduler.RouteSegment electric = new TrainScheduler.RouteSegment(
                1, 10000, 100000, 1, true
        );
        TrainScheduler.RouteSegment nonElectric = new TrainScheduler.RouteSegment(
                2, 10000, 100000, 1, false
        );

        assertTrue(electric.isElectric);
        assertFalse(nonElectric.isElectric);
    }

    @Test
    @DisplayName("Test Route endpoint modification")
    void testRoute_EndpointModification() {
        TrainScheduler.Route route = new TrainScheduler.Route(
                1, 1, 2, "Start", "End"
        );

        assertEquals(2, route.getEndEndpointId());
        assertEquals("End", route.getEndName());

        route.setEndEndpointId(5);
        route.setEndName("New End");

        assertEquals(5, route.getEndEndpointId());
        assertEquals("New End", route.getEndName());
    }

    @Test
    @DisplayName("Test Endpoint properties")
    void testEndpoint_Properties() {
        TrainScheduler.Endpoint endpoint = new TrainScheduler.Endpoint(5, "Central Station");

        assertEquals(5, endpoint.id);
        assertEquals("Central Station", endpoint.name);
    }

    @Test
    @DisplayName("Test RailLine properties")
    void testRailLine_Properties() {
        TrainScheduler.RailLine railLine = new TrainScheduler.RailLine(
                1, "Main Line", 10, 20
        );

        assertEquals(1, railLine.id);
        assertEquals("Main Line", railLine.name);
        assertEquals(10, railLine.startId);
        assertEquals(20, railLine.endId);
    }

    @Test
    @DisplayName("Test RailLineInfo properties")
    void testRailLineInfo_Properties() {
        TrainScheduler.RailLineInfo info = new TrainScheduler.RailLineInfo(
                1, "Express Line", "City A", "City B"
        );

        assertEquals(1, info.id);
        assertEquals("Express Line", info.name);
        assertEquals("City A", info.startName);
        assertEquals("City B", info.endName);
    }

    @Test
    @DisplayName("Test Siding properties")
    void testSiding_Properties() {
        TrainScheduler.Siding siding = new TrainScheduler.Siding(
                1, 5000.0, 500.0, 10
        );

        assertEquals(1, siding.getId());
        assertEquals(5000.0, siding.getPosition());
        assertEquals(10, siding.getSegmentId());
    }

    @Test
    @DisplayName("Test CrossingPoint properties")
    void testCrossingPoint_Properties() {
        TrainScheduler.CrossingPoint cp = new TrainScheduler.CrossingPoint(
                1, "STATION", 1000.0, "Main Station"
        );

        assertEquals("STATION", cp.getType());
        assertEquals(1000.0, cp.getPosition());
        assertEquals("Main Station", cp.getName());
    }

    // ==========================================
    // HELPER METHODS FOR TEST DATA
    // ==========================================

    private TrainScheduler.Train createMockTrain(int id) {
        TrainScheduler.Train train = new TrainScheduler.Train(
                id, "Test Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(id, "Loc" + id, 5000, 80000, 120, 100)
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(id, 20000, 50000, 100, 30000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        return train;
    }

    private TrainScheduler.Train createHeavyTrain() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Heavy Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 3000, 80000, 120, 100)
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 30000, 50000, 100, 45000),
                new TrainScheduler.Wagon(2, 30000, 50000, 100, 45000),
                new TrainScheduler.Wagon(3, 30000, 50000, 100, 45000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        return train;
    }

    private TrainScheduler.Train createLightTrain() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Light Operator", LocalDate.now(), 1
        );
        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 2000000, 30000, 120, 100), // 2 MW per locomotive
                new TrainScheduler.Locomotive(2, "Loc2", 2000000, 30000, 120, 100)
        );
        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 10000, 50000, 100, 5000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        return train;
    }

    private TrainScheduler.Train createTrainWithSlowLocomotive() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Slow Loc Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "SlowLoc", 5000, 80000, 80, 60)
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 20000, 50000, 120, 30000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        return train;
    }

    private TrainScheduler.Train createTrainWithSlowWagons() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Slow Wagon Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "FastLoc", 5000, 80000, 140, 120)
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 20000, 50000, 80, 30000),
                new TrainScheduler.Wagon(2, 20000, 50000, 80, 30000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        return train;
    }

    private TrainScheduler.Train createVeryHeavyTrain() {
        TrainScheduler.Train train = new TrainScheduler.Train(
                1, "Very Heavy Operator", LocalDate.now(), 1
        );

        List<TrainScheduler.Locomotive> locomotives = Arrays.asList(
                new TrainScheduler.Locomotive(1, "Loc1", 5000, 90000, 120, 100)
        );

        List<TrainScheduler.Wagon> wagons = Arrays.asList(
                new TrainScheduler.Wagon(1, 40000, 60000, 100, 55000),
                new TrainScheduler.Wagon(2, 40000, 60000, 100, 55000),
                new TrainScheduler.Wagon(3, 40000, 60000, 100, 55000)
        );

        train.setLocomotives(locomotives);
        train.setWagons(wagons);

        return train; // Total weight > 350 tonnes
    }

    private TrainScheduler.Route createMockRoute() {
        TrainScheduler.Route route = new TrainScheduler.Route(
                1, 1, 2, "Station A", "Station B"
        );

        TrainScheduler.RouteSegment segment1 = new TrainScheduler.RouteSegment(
                1, 10000, 100000, 1, true
        );
        TrainScheduler.RouteSegment segment2 = new TrainScheduler.RouteSegment(
                2, 15000, 120000, 1, false
        );

        route.addSegment(segment1);
        route.addSegment(segment2);

        return route;
    }

    private TrainScheduler.Route createMockRouteWithWeightLimit(double maxWeight) {
        TrainScheduler.Route route = new TrainScheduler.Route(
                1, 1, 2, "Station A", "Station B"
        );

        TrainScheduler.RouteSegment segment1 = new TrainScheduler.RouteSegment(
                1, 10000, maxWeight, 1, true
        );

        route.addSegment(segment1);

        return route;
    }
}