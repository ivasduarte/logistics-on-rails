package freightMngt.controller;

import freightMngt.domain.Station;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaximumFlowTest {
    private Graph<Station, Integer> capacityGraph;
    private Station porto;
    private Station coimbra;
    private Station lisboa;
    private Station braga;
    private Station aveiro;
    private Station faro;

    @BeforeEach
    void setUp() {
        // Create stations
        porto = new Station("001", "Porto", 41.1579, -8.6291, 0.0, 0.0);
        braga = new Station("002", "Braga", 41.5454, -8.4265, 1.0, 1.0);
        aveiro = new Station("003", "Aveiro", 40.6443, -8.6455, 2.0, 2.0);
        coimbra = new Station("004", "Coimbra", 40.2033, -8.4103, 3.0, 3.0);
        lisboa = new Station("005", "Lisboa", 38.7223, -9.1393, 4.0, 4.0);
        faro = new Station("006", "Faro", 37.0194, -7.9322, 5.0, 5.0);

        // Create directed capacity graph
        capacityGraph = new MapGraph<>(true);
    }

    /**
     * Functional Test 1: Complete User Story
     *
     * Tests the entirety of the requirements of the User Story:
     * - Source Station: Porto
     * - Destination Station: Lisboa
     * - Multiple paths with different capacities
     * - Expected: Maximum flow throughput from Porto to Lisboa
     */
    @Test
    void testMaximumFlow() {

        // Arrange: Build mock railway network

        // Path 1
        capacityGraph.addEdge(porto, aveiro, 5);
        capacityGraph.addEdge(aveiro, coimbra, 3);
        capacityGraph.addEdge(coimbra, lisboa, 4);

        // Path 2
        capacityGraph.addEdge(porto, braga, 4);
        capacityGraph.addEdge(braga, coimbra, 2);

        // Path 3
        capacityGraph.addEdge(aveiro, lisboa, 2);

        // Act: Maximum Flow Calculation
        Integer maxFlow = MaximumFlow.maxFlow(capacityGraph, porto, lisboa);

        // Assert: Verify maximum flow value
        assertNotNull(maxFlow, "Maximum flow should not be null");
        assertEquals(6, maxFlow, "Maximum flow should be 6 trains/day.");
    }

    /**
     * Functional Test 2: No Connection Scenario
     *
     * Validates behavior when no route exists.
     */
    @Test
    void testMaximumFlow_NoConnection() {

        // Arrange: Disconnected stations
        capacityGraph.addVertex(porto);
        capacityGraph.addVertex(faro);
        capacityGraph.addEdge(porto, braga, 5);
        capacityGraph.addEdge(lisboa, faro, 3);
        // No path between Porto and Faro

        // Act
        Integer maxFlow = MaximumFlow.maxFlow(capacityGraph, porto, faro);

        // Assert
        assertNotNull(maxFlow, "Maximum flow should not be null");
        assertEquals(0, maxFlow, "Maximum flow should be 0 as there is no connection between Porto and Faro");
    }

    @Test
    void testMaxFlow_DirectConnection() {
        // Arrange
        capacityGraph.addEdge(porto, braga, 10);

        // Act
        Integer result = MaximumFlow.maxFlow(capacityGraph, porto, braga);

        // Assert
        assertEquals(10, result, "Maximum flow should equal edge capacity");
    }

    @Test
    void testMaxFlow_SourceEqualsSink() {
        // Arrange
        capacityGraph.addVertex(porto);

        // Act
        Integer result = MaximumFlow.maxFlow(capacityGraph, porto, porto);

        // Assert
        assertEquals(0, result, "Flow from a vertex to itself should be 0");
    }

    @Test
    void testFindAugmentationPath_SinglePath() {
        // Arrange: Simple path that requires one augmentation
        capacityGraph.addEdge(porto, braga, 5);
        capacityGraph.addEdge(braga, aveiro, 5);

        // Act
        Integer result = MaximumFlow.maxFlow(capacityGraph, porto, aveiro);

        // Assert: If path is found correctly, flow should be 5
        assertEquals(5, result, "Single augmenting path should be found");
    }

    @Test
    void testGetPathMinCap_BottleneckIdentification() {
        // Arrange: Path with clear bottleneck
        capacityGraph.addEdge(porto, braga, 10);
        capacityGraph.addEdge(braga, aveiro, 2);  // Bottleneck
        capacityGraph.addEdge(aveiro, coimbra, 10);

        // Act
        Integer result = MaximumFlow.maxFlow(capacityGraph, porto, coimbra);

        // Assert: Flow should be limited by bottleneck
        assertEquals(2, result, "Bottleneck capacity should limit flow");
    }
}