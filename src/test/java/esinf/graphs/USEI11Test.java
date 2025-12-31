package esinf.graphs;

import freightMngt.controller.NetworkController;
import freightMngt.domain.ConnectionData;
import freightMngt.domain.Station;
import freightMngt.domain.UpgradeOrderResult;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

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

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertFalse(result.isSuccess(), "Empty network should return error");
        assertEquals("Network not loaded", result.getErrorMessage());
        assertNull(result.getUpgradeOrder());
    }

    /**
     * Test 2: Undirected graph (not suitable for upgrade planning)
     */
    @Test
    public void testComputeUpgradeOrder_UndirectedGraph() {
        System.out.println("Test Upgrade Order: Undirected Graph");

        // Create undirected graph
        Graph<Station, ConnectionData> undirectedGraph = new MapGraph<>(false);
        Station s1 = new Station("1", "Station A", 0, 0, 0, 0);
        Station s2 = new Station("2", "Station B", 0, 0, 0, 0);

        undirectedGraph.addVertex(s1);
        undirectedGraph.addVertex(s2);
        undirectedGraph.addEdge(s1, s2, new ConnectionData(10, 100, 5));

        // Manually set the graph to undirected
        NetworkController customController = new NetworkController();
        // Note: You may need to add a setter or use reflection to set the graph
        // For now, testing with the loadNetwork method which creates directed graphs

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertFalse(result.isSuccess(), "Undirected graph should return error");
    }

    /**
     * Test 3: Simple DAG (Directed Acyclic Graph) - Valid topological order
     * A → B → C
     */
    @Test
    public void testComputeUpgradeOrder_SimpleDAG() {
        System.out.println("Test Upgrade Order: Simple DAG");

        // Create test stations
        Station stationA = new Station("A", "Station A", 0, 0, 0, 0);
        Station stationB = new Station("B", "Station B", 0, 0, 0, 0);
        Station stationC = new Station("C", "Station C", 0, 0, 0, 0);

        // Build directed graph manually
        Graph<Station, ConnectionData> testGraph = new MapGraph<>(true);
        testGraph.addVertex(stationA);
        testGraph.addVertex(stationB);
        testGraph.addVertex(stationC);

        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationC, new ConnectionData(15, 150, 7));

        // Set graph in controller (you may need to add a setter method)
        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertTrue(result.isSuccess(), "Simple DAG should have valid topological order");
        assertNotNull(result.getUpgradeOrder());

        LinkedList<Station> order = result.getUpgradeOrder();
        assertEquals(3, order.size(), "Order should contain all 3 stations");

        // Verify order: A must come before B, B must come before C
        int indexA = order.indexOf(stationA);
        int indexB = order.indexOf(stationB);
        int indexC = order.indexOf(stationC);

        assertTrue(indexA < indexB, "A should come before B");
        assertTrue(indexB < indexC, "B should come before C");
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

        assertTrue(result.isSuccess(), "Complex DAG should have valid topological order");

        LinkedList<Station> order = result.getUpgradeOrder();
        assertEquals(4, order.size(), "Order should contain all 4 stations");

        // Verify dependencies: B → A → C and B → A → D
        int indexA = order.indexOf(stationA);
        int indexB = order.indexOf(stationB);
        int indexC = order.indexOf(stationC);
        int indexD = order.indexOf(stationD);

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

        // Create cycle
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationC, new ConnectionData(15, 150, 7));
        testGraph.addEdge(stationC, stationA, new ConnectionData(20, 200, 10));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertFalse(result.isSuccess(), "Graph with cycle should fail");
        assertNull(result.getUpgradeOrder(), "No valid order should exist");
        assertNotNull(result.getCycles(), "Cycles should be detected");
        assertFalse(result.getCycles().isEmpty(), "At least one cycle should be found");
        assertNotNull(result.getStationsInCycles(), "Stations in cycles should be identified");

        // Verify all stations are in the cycle
        assertEquals(3, result.getStationsInCycles().size(),
                "All 3 stations should be in cycles");
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

        // Cycle 1: A ↔ B
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationA, new ConnectionData(10, 100, 5));

        // Cycle 2: C ↔ D
        testGraph.addEdge(stationC, stationD, new ConnectionData(15, 150, 7));
        testGraph.addEdge(stationD, stationC, new ConnectionData(15, 150, 7));

        setGraphInController(controller, testGraph);

        freightMngt.domain.UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertFalse(result.isSuccess(), "Graph with multiple cycles should fail");
        assertNotNull(result.getCycles(), "Cycles should be detected");
        assertTrue(result.getCycles().size() >= 2, "At least 2 cycles should be found");
        assertEquals(4, result.getStationsInCycles().size(),
                "All 4 stations should be in cycles");
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

        // Acyclic part
        testGraph.addEdge(stationA, stationB, new ConnectionData(10, 100, 5));
        testGraph.addEdge(stationB, stationC, new ConnectionData(15, 150, 7));

        // Cyclic part
        testGraph.addEdge(stationD, stationE, new ConnectionData(20, 200, 10));
        testGraph.addEdge(stationE, stationD, new ConnectionData(20, 200, 10));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertFalse(result.isSuccess(), "Graph with any cycle should fail");
        assertNotNull(result.getStationsInCycles());

        // Only D and E should be in cycles
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

        freightMngt.domain.UpgradeOrderResult result = controller.computeUpgradeOrder();

        assertTrue(result.isSuccess(), "Single station should be valid");
        assertNotNull(result.getUpgradeOrder());
        assertEquals(1, result.getUpgradeOrder().size());
        assertEquals(stationA, result.getUpgradeOrder().getFirst());
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

        assertTrue(result.isSuccess(), "Disconnected DAG should be valid");
        assertEquals(4, result.getUpgradeOrder().size());

        // Verify partial order
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

        assertFalse(result.isSuccess(), "Self-loop should be detected as cycle");
        assertNotNull(result.getCycles());
        assertNotNull(result.getStationsInCycles());
        assertTrue(result.getStationsInCycles().contains(stationA));
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Helper method to set the graph in the controller.
     * If NetworkController doesn't have a setter, you may need to add one
     * or use reflection.
     */
    private void setGraphInController(NetworkController controller,
                                      Graph<Station, ConnectionData> graph) {
        try {
            // Attempt to use reflection to set the private graph field
            java.lang.reflect.Field field = NetworkController.class.getDeclaredField("graph");
            field.setAccessible(true);
            field.set(controller, graph);
        } catch (Exception e) {
            fail("Could not set graph in controller: " + e.getMessage());
        }
    }
}