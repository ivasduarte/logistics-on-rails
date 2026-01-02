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
        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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
        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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

        LinkedList<Station> expectedOrder = new LinkedList<>();
        expectedOrder.add(stationA);
        expectedOrder.add(stationB);
        expectedOrder.add(stationC);
        UpgradeOrderResult expected = UpgradeOrderResult.success(expectedOrder);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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

        LinkedList<Station> expectedOrder = new LinkedList<>();
        expectedOrder.add(stationB);
        expectedOrder.add(stationA);
        expectedOrder.add(stationC);
        expectedOrder.add(stationD);
        UpgradeOrderResult expected = UpgradeOrderResult.success(expectedOrder);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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

        List<Station> expectedStationsInCycles = Arrays.asList(stationA, stationB, stationC);
        UpgradeOrderResult expected = UpgradeOrderResult.withCycles(null, expectedStationsInCycles);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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

        List<Station> expectedStationsInCycles = Arrays.asList(stationA, stationB, stationC, stationD);
        UpgradeOrderResult expected = UpgradeOrderResult.withCycles(null, expectedStationsInCycles);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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

        List<Station> expectedStationsInCycles = Arrays.asList(stationD, stationE);
        UpgradeOrderResult expected = UpgradeOrderResult.withCycles(null, expectedStationsInCycles);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
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

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
    }

    /**
     * Test 9: Disconnected components (no cycles)
     * A → B    C → D (two separate chains)
     */
    @Test
    public void testComputeUpgradeOrder_DisconnectedComponents() {
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
        testGraph.addEdge(stationC, stationD, new ConnectionData(15, 120, 6));

        setGraphInController(controller, testGraph);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertTrue(actual.isSuccess());
        assertNotNull(actual.getUpgradeOrder());
        assertEquals(4, actual.getUpgradeOrder().size());

        List<Station> order = actual.getUpgradeOrder();

        int idxA = order.indexOf(stationA);
        int idxB = order.indexOf(stationB);
        int idxC = order.indexOf(stationC);
        int idxD = order.indexOf(stationD);

        // A deve vir obrigatoriamente antes de B
        assertTrue(idxA < idxB, "Erro: A deve aparecer antes de B na ordenação.");

        // C deve vir obrigatoriamente antes de D
        assertTrue(idxC < idxD, "Erro: C deve aparecer antes de D na ordenação.");
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

        List<Station> expectedStationsInCycles = Arrays.asList(stationA);
        UpgradeOrderResult expected = UpgradeOrderResult.withCycles(null, expectedStationsInCycles);

        UpgradeOrderResult actual = controller.computeUpgradeOrder();

        assertEquals(expected, actual);
    }

    // ========================================================================
    // Helper Methods & Classes
    // ========================================================================
    

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