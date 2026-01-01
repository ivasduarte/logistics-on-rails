package esinf.graphs;

import freightMngt.controller.NetworkController;
import freightMngt.domain.ConnectionData;
import freightMngt.domain.Station;
import freightMngt.domain.UpgradeOrderResult;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class USEI11Test {

    private NetworkController controller;

    @BeforeEach
    public void setUp() {
        controller = new NetworkController();
    }

    /**
     * Test 1: Empty network (no vertices)
     */
    @Test
    public void testComputeUpgradeOrder_EmptyNetwork() {
        System.out.println("Test Upgrade Order: Empty Network");

        UpgradeOrderResult expected = UpgradeOrderResult.error("Network not loaded");
        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertUpgradeOrderResultEquals(expected, result);
    }

    /**
     * Test 2: Undirected graph (not suitable for upgrade planning)
     */
    @Test
    public void testComputeUpgradeOrder_UndirectedGraph() {
        System.out.println("Test Upgrade Order: Undirected Graph");

        Graph<Station, ConnectionData> undirectedGraph = new MapGraph<>(false);
        Station s1 = new Station("1", "Station A", 0, 0, 0, 0);
        Station s2 = new Station("2", "Station B", 0, 0, 0, 0);

        undirectedGraph.addVertex(s1);
        undirectedGraph.addVertex(s2);
        undirectedGraph.addEdge(s1, s2, new ConnectionData(10, 100, 5));

        setGraphInController(controller, undirectedGraph);

        UpgradeOrderResult expected = UpgradeOrderResult.error("Graph must be directed for upgrade planning");
        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertUpgradeOrderResultEquals(expected, result);
    }

    /**
     * Test 3: Simple DAG (Directed Acyclic Graph) - Valid topological order
     * A → B → C
     */
    @Test
    public void testComputeUpgradeOrder_SimpleDAG() {
        System.out.println("Test Upgrade Order: Simple DAG");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationC, new ConnectionData(15, 150, 7));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Build expected result
        LinkedList<Station> expectedOrder = new LinkedList<>();
        expectedOrder.add(stationA);
        expectedOrder.add(stationB);
        expectedOrder.add(stationC);
        UpgradeOrderResult expected = UpgradeOrderResult.success(expectedOrder);

        assertUpgradeOrderResultEquals(expected, result);
    }

    /**
     * Test 4: Complex DAG with multiple paths
     *     A → C
     *   ↙   ↘
     * B  →   D
     */
    @Test
    public void testComputeUpgradeOrder_ComplexDAG() {
        System.out.println("Test Upgrade Order: Complex DAG");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);
        Station stationD = new Station("D", "Station D", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);
        testGraph.addVertex(stationD);
        testGraph.addEdge(stationA, stationC, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationA, stationD, new ConnectionData(20, 200, 10));
        testGraph.addEdge(stationB, stationA, new ConnectionData(15, 150, 7));
        testGraph.addEdge(stationB, stationD, new ConnectionData(25, 250, 12));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Verify success
        assertTrue(result.isSuccess());
        assertNotNull(result.getUpgradeOrder());
        assertEquals(4, result.getUpgradeOrder().size());

        // Verify dependencies: B → A → C and B → A → D
        int indexA = result.getUpgradeOrder().indexOf(stationA);
        int indexB = result.getUpgradeOrder().indexOf(stationB);
        int indexC = result.getUpgradeOrder().indexOf(stationC);
        int indexD = result.getUpgradeOrder().indexOf(stationD);

        assertTrue(indexB < indexA, "B should come before A");
        assertTrue(indexA < indexC, "A should come before C");
        assertTrue(indexA < indexD, "A should come before D");
    }

    /**
     * Test 5: Graph with cycle - should detect and report
     * A → B → C → A (cycle)
     */
    @Test
    public void testComputeUpgradeOrder_SimpleCycle() {
        System.out.println("Test Upgrade Order: Simple Cycle");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationC, new ConnectionData(15, 150, 7));
        testGraph.addEdge(stationC, stationA, new ConnectionData(20, 200, 10));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Verify failure
        assertFalse(result.isSuccess());
        assertNull(result.getUpgradeOrder());
        assertNotNull(result.getCycles());
        assertFalse(result.getCycles().isEmpty());
        assertNotNull(result.getStationsInCycles());

        // Verify all stations are in cycles
        assertEquals(3, result.getStationsInCycles().size());
        assertTrue(result.getStationsInCycles().contains(stationA));
        assertTrue(result.getStationsInCycles().contains(stationB));
        assertTrue(result.getStationsInCycles().contains(stationC));
    }

    /**
     * Test 6: Graph with multiple cycles
     * A → B → A (cycle 1)
     * C → D → C (cycle 2)
     */
    @Test
    public void testComputeUpgradeOrder_MultipleCycles() {
        System.out.println("Test Upgrade Order: Multiple Cycles");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);
        Station stationD = new Station("D", "Station D", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);
        testGraph.addVertex(stationD);
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationA, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationC, stationD, new ConnectionData(15, 150, 7));
        testGraph.addEdge(stationD, stationC, new ConnectionData(15, 150, 7));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Verify failure
        assertFalse(result.isSuccess());
        assertNull(result.getUpgradeOrder());
        assertNotNull(result.getCycles());
        assertTrue(result.getCycles().size() >= 2, "At least 2 cycles should be found");

        // Verify all 4 stations are in cycles
        assertEquals(4, result.getStationsInCycles().size());
        assertTrue(result.getStationsInCycles().contains(stationA));
        assertTrue(result.getStationsInCycles().contains(stationB));
        assertTrue(result.getStationsInCycles().contains(stationC));
        assertTrue(result.getStationsInCycles().contains(stationD));
    }

    /**
     * Test 7: Graph with cycle and acyclic parts
     * A → B → C (acyclic)
     * D → E → D (cycle)
     */
    @Test
    public void testComputeUpgradeOrder_MixedCyclicAcyclic() {
        System.out.println("Test Upgrade Order: Mixed Cyclic/Acyclic");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);
        Station stationD = new Station("D", "Station D", 0, 0, 0, 0);
        Station stationE = new Station("E", "Station E", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);
        testGraph.addVertex(stationD);
        testGraph.addVertex(stationE);
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationC, new ConnectionData(15, 150, 7));
        testGraph.addEdge(stationD, stationE, new ConnectionData(20, 200, 10));
        testGraph.addEdge(stationE, stationD, new ConnectionData(20, 200, 10));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Verify failure (any cycle means failure)
        assertFalse(result.isSuccess());
        assertNull(result.getUpgradeOrder());
        assertNotNull(result.getCycles());
        assertNotNull(result.getStationsInCycles());

        // Only D and E should be in cycles
        assertEquals(2, result.getStationsInCycles().size());
        assertTrue(result.getStationsInCycles().contains(stationD));
        assertTrue(result.getStationsInCycles().contains(stationE));
        assertFalse(result.getStationsInCycles().contains(stationA));
        assertFalse(result.getStationsInCycles().contains(stationB));
        assertFalse(result.getStationsInCycles().contains(stationC));
    }

    /**
     * Test 8: Single station (trivial case)
     */
    @Test
    public void testComputeUpgradeOrder_SingleStation() {
        System.out.println("Test Upgrade Order: Single Station");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);

        setGraphInController(controller, testGraph);

        LinkedList<Station> expectedOrder = new LinkedList<>();
        expectedOrder.add(stationA);
        UpgradeOrderResult expected = UpgradeOrderResult.success(expectedOrder);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertUpgradeOrderResultEquals(expected, result);
    }

    /**
     * Test 9: Disconnected components (no cycles)
     * A → B    C → D (two separate chains)
     */
    @Test
    public void testComputeUpgradeOrder_DisconnectedComponents() {
        System.out.println("Test Upgrade Order: Disconnected Components");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);
        Station stationD = new Station("D", "Station D", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);
        testGraph.addVertex(stationD);
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationC, stationD, new ConnectionData(15, 150, 7));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Verify success
        assertTrue(result.isSuccess());
        assertNotNull(result.getUpgradeOrder());
        assertEquals(4, result.getUpgradeOrder().size());

        // Verify partial order (A before B, C before D)
        int indexA = result.getUpgradeOrder().indexOf(stationA);
        int indexB = result.getUpgradeOrder().indexOf(stationB);
        int indexC = result.getUpgradeOrder().indexOf(stationC);
        int indexD = result.getUpgradeOrder().indexOf(stationD);

        assertTrue(indexA < indexB, "A should come before B");
        assertTrue(indexC < indexD, "C should come before D");
    }

    /**
     * Test 10: Self-loop (station depends on itself)
     * A → A
     */
    @Test
    public void testComputeUpgradeOrder_SelfLoop() {
        System.out.println("Test Upgrade Order: Self Loop");

        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);

        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addEdge(stationA, stationA, new ConnectionData(10, 100, 5));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        // Verify failure
        assertFalse(result.isSuccess());
        assertNull(result.getUpgradeOrder());
        assertNotNull(result.getCycles());
        assertNotNull(result.getStationsInCycles());
        assertTrue(result.getStationsInCycles().contains(stationA));
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Custom assertion method that compares UpgradeOrderResult objects field by field
     */
    private void assertUpgradeOrderResultEquals(UpgradeOrderResult expected, UpgradeOrderResult actual) {
        assertEquals(expected.isSuccess(), actual.isSuccess(), "Success flag mismatch");
        assertEquals(expected.getErrorMessage(), actual.getErrorMessage(), "Error message mismatch");

        // Compare upgrade order lists
        if (expected.getUpgradeOrder() == null) {
            assertNull(actual.getUpgradeOrder(), "Expected null upgrade order");
        } else {
            assertNotNull(actual.getUpgradeOrder(), "Expected non-null upgrade order");
            assertEquals(expected.getUpgradeOrder().size(), actual.getUpgradeOrder().size(),
                    "Upgrade order size mismatch");

            List<Station> expectedList = new ArrayList<>(expected.getUpgradeOrder());
            List<Station> actualList = new ArrayList<>(actual.getUpgradeOrder());

            for (int i = 0; i < expectedList.size(); i++) {
                assertEquals(expectedList.get(i).getNodeID(), actualList.get(i).getNodeID(),
                        "Station mismatch at position " + i);
            }
        }

        // For cycles and stations in cycles, we just verify they exist and contain the right stations
        // We don't check the exact structure since cycle detection order may vary
        if (expected.getCycles() != null) {
            assertNotNull(actual.getCycles(), "Expected non-null cycles");
            // Don't compare cycle structure, just verify cycles were detected
            assertFalse(actual.getCycles().isEmpty(), "Expected at least one cycle");
        }

        if (expected.getStationsInCycles() != null) {
            assertNotNull(actual.getStationsInCycles(), "Expected non-null stations in cycles");
            assertEquals(expected.getStationsInCycles().size(), actual.getStationsInCycles().size(),
                    "Stations in cycles count mismatch");

            // Compare sets by station IDs
            Set<String> expectedIds = new HashSet<>();
            for (Station s : expected.getStationsInCycles()) {
                expectedIds.add(s.getNodeID());
            }

            Set<String> actualIds = new HashSet<>();
            for (Station s : actual.getStationsInCycles()) {
                actualIds.add(s.getNodeID());
            }

            assertEquals(expectedIds, actualIds, "Stations in cycles mismatch");
        }
    }

    private void setGraphInController(NetworkController controller,
                                      Graph<Station, ConnectionData> graph) {
        try {
            java.lang.reflect.Field field = NetworkController.class.getDeclaredField("graph");
            field.setAccessible(true);
            field.set(controller, graph);
        } catch (Exception e) {
            fail("Could not set graph in controller: " + e.getMessage());
        }
    }
}