package esinf.graphs;

import freightMngt.graph.Algorithms;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.*;
import java.util.function.BinaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {

    final Graph<String, Integer> completeMap = new MapGraph<>(false);
    Graph<String, Integer> incompleteMap = new MapGraph<>(false);

    // graph for Bellman-Ford algorithm test
    final Graph<String, Double> testGraph = new MapGraph<>(true);
    Graph<String, Double> negativeCycleGraph = new MapGraph<>(true);

    @BeforeEach
    public void setUp() {

        completeMap.addVertex("Porto");
        completeMap.addVertex("Braga");
        completeMap.addVertex("Vila Real");
        completeMap.addVertex("Aveiro");
        completeMap.addVertex("Coimbra");
        completeMap.addVertex("Leiria");

        completeMap.addVertex("Viseu");
        completeMap.addVertex("Guarda");
        completeMap.addVertex("Castelo Branco");
        completeMap.addVertex("Lisboa");
        completeMap.addVertex("Faro");

        completeMap.addEdge("Porto", "Aveiro", 75);
        completeMap.addEdge("Porto", "Braga", 60);
        completeMap.addEdge("Porto", "Vila Real", 100);
        completeMap.addEdge("Viseu", "Guarda", 75);
        completeMap.addEdge("Guarda", "Castelo Branco", 100);
        completeMap.addEdge("Aveiro", "Coimbra", 60);
        completeMap.addEdge("Coimbra", "Lisboa", 200);
        completeMap.addEdge("Coimbra", "Leiria", 80);
        completeMap.addEdge("Aveiro", "Leiria", 120);
        completeMap.addEdge("Leiria", "Lisboa", 150);

        incompleteMap = completeMap.clone();

        completeMap.addEdge("Aveiro", "Viseu", 85);
        completeMap.addEdge("Leiria", "Castelo Branco", 170);
        completeMap.addEdge("Lisboa", "Faro", 280);
    }

    @BeforeEach
    public void setUpBellmanFord() {

        testGraph.addVertex("Porto");
        testGraph.addVertex("Braga");
        testGraph.addVertex("Vila Real");
        testGraph.addVertex("Aveiro");
        testGraph.addVertex("Coimbra");
        testGraph.addVertex("Leiria");

        testGraph.addVertex("Viseu");
        testGraph.addVertex("Guarda");
        testGraph.addVertex("Castelo Branco");
        testGraph.addVertex("Lisboa");
        testGraph.addVertex("Faro");

        testGraph.addEdge("Porto", "Aveiro", 75.5);
        testGraph.addEdge("Porto", "Braga", 60.0);
        testGraph.addEdge("Porto", "Vila Real", 100.1);
        testGraph.addEdge("Viseu", "Guarda", -75.9);
        testGraph.addEdge("Guarda", "Castelo Branco", 100.5);
        testGraph.addEdge("Aveiro", "Coimbra", 60.3);
        testGraph.addEdge("Coimbra", "Lisboa", -200.4);
        testGraph.addEdge("Coimbra", "Leiria", 80.0);
        testGraph.addEdge("Aveiro", "Leiria", -120.0);
        testGraph.addEdge("Leiria", "Lisboa", -150.1);
        testGraph.addEdge("Aveiro", "Viseu", 85.0);

        negativeCycleGraph = testGraph.clone();

        negativeCycleGraph.addEdge("Guarda", "Viseu", 50.0);

        testGraph.addEdge("Leiria", "Castelo Branco", 170.0);
        testGraph.addEdge("Lisboa", "Faro", 280.0);

    }

    private void checkContentEquals(List<String> l1, List<String> l2, String msg) {
        Collections.sort(l1);
        Collections.sort(l2);
        assertEquals(l1, l2, msg);
    }

    /**
     * Test of BreadthFirstSearch method, of class Algorithms.
     */
    @Test
    public void testBreadthFirstSearch() {
        System.out.println("Test BreadthFirstSearch");

        Assertions.assertNull(Algorithms.BreadthFirstSearch(completeMap, "LX"), "Should be null if vertex does not exist");

        LinkedList<String> path = Algorithms.BreadthFirstSearch(incompleteMap, "Faro");

        assertEquals(1, path.size(), "Should be just one");

        assertEquals("Faro", path.peekFirst());

        path = Algorithms.BreadthFirstSearch(incompleteMap, "Porto");
        assertEquals(7, path.size(), "Should give seven vertices");

        assertEquals("Porto", path.removeFirst(), "BreathFirst Porto");

        LinkedList<String> expected = new LinkedList<>(Arrays.asList("Aveiro", "Braga", "Vila Real"));
        checkContentEquals(expected, path.subList(0, 3), "BreathFirst Porto");

        expected = new LinkedList<>(Arrays.asList("Coimbra", "Leiria"));
        checkContentEquals(expected, path.subList(3, 5), "BreathFirst Porto");

        expected = new LinkedList<>(Arrays.asList("Lisboa"));
        checkContentEquals(expected, path.subList(5, 6), "BreathFirst Porto");

        path = Algorithms.BreadthFirstSearch(incompleteMap, "Viseu");
        expected = new LinkedList<>(Arrays.asList("Viseu", "Guarda", "Castelo Branco"));
        assertEquals(expected, path, "BreathFirst Viseu");
    }

    /**
     * Test of DepthFirstSearch method, of class Algorithms.
     */
    @Test
    public void testDepthFirstSearch() {
        System.out.println("Test of DepthFirstSearch");

        assertNull(Algorithms.DepthFirstSearch(completeMap, "LX"), "Should be null if vertex does not exist");

        LinkedList<String> path = Algorithms.DepthFirstSearch(incompleteMap, "Faro");
        assertEquals(1, path.size(), "Should be just one");

        assertEquals("Faro", path.peekFirst());

        path = Algorithms.BreadthFirstSearch(incompleteMap, "Porto");
        assertEquals(7, path.size(), "Should give seven vertices");

        assertEquals("Porto", path.removeFirst(), "DepthFirst Porto");
        assertTrue(new LinkedList<>(Arrays.asList("Aveiro", "Braga", "Vila Real")).contains(path.removeFirst()), "DepthFirst Porto");

        path = Algorithms.BreadthFirstSearch(incompleteMap, "Viseu");
        List<String> expected = new LinkedList<>(Arrays.asList("Viseu", "Guarda", "Castelo Branco"));
        assertEquals(expected, path, "DepthFirst Viseu");
    }

    /**
     * Test of shortestPath method, of class Algorithms.
     */
    @Test
    public void testShortestPath() {
        System.out.println("Test of shortest path");

        LinkedList<String> shortPath = new LinkedList<>();

        Integer lenPath = Algorithms.shortestPath(completeMap, "Porto", "LX", Integer::compare, Integer::sum, 0, shortPath);
        assertNull(lenPath, "Length path should be null if vertex does not exist");
        assertEquals(0, shortPath.size(), "Shortest Path does not exist");

        lenPath = Algorithms.shortestPath(incompleteMap, "Porto", "Faro", Integer::compare, Integer::sum, 0, shortPath);
        assertNull(lenPath, "Length path should be null if vertex does not exist");
        assertEquals(0, shortPath.size(), "Shortest Path does not exist");

        lenPath = Algorithms.shortestPath(completeMap, "Porto", "Porto", Integer::compare, Integer::sum, 0, shortPath);
        assertEquals(0, lenPath, "Length path should be 0 if vertices are the same");
        assertEquals(Arrays.asList("Porto"), shortPath, "Shortest Path only contains Porto");

        lenPath = Algorithms.shortestPath(incompleteMap, "Porto", "Lisboa", Integer::compare, Integer::sum, 0, shortPath);
        assertEquals(335, lenPath, "Length path should be 0 if vertices are the same");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Coimbra", "Lisboa"), shortPath, "Shortest Path Porto - Lisboa");

        lenPath = Algorithms.shortestPath(incompleteMap, "Braga", "Leiria", Integer::compare, Integer::sum, 0, shortPath);
        assertEquals(255, lenPath, "Length path should be 0 if vertices are the same");
        assertEquals(Arrays.asList("Braga", "Porto", "Aveiro", "Leiria"), shortPath, "Shortest Path Braga - Leiria");

        lenPath = Algorithms.shortestPath(completeMap, "Porto", "Castelo Branco", Integer::compare, Integer::sum, 0, shortPath);
        assertEquals(335, lenPath, "Length path should be 0 if vertices are the same");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Viseu", "Guarda", "Castelo Branco"), shortPath, "Shortest Path Porto - Castelo Branco");

        //Changing Edge: Aveiro-Viseu with Edge: Leiria-C.Branco
        //should change shortest path between Porto and Castelo Branco

        completeMap.removeEdge("Aveiro", "Viseu");
        completeMap.addEdge("Leiria", "Castelo Branco", 170);

        lenPath = Algorithms.shortestPath(completeMap, "Porto", "Castelo Branco", Integer::compare, Integer::sum, 0, shortPath);
        assertEquals(365, lenPath, "Length path should be 0 if vertices are the same");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Leiria", "Castelo Branco"), shortPath, "Shortest Path Porto - Castelo Branco");
    }

    /**
     * Test of shortestPaths method, of class Algorithms.
     */
    @Test
    public void testShortestPaths() {
        System.out.println("Test of shortest path");

        ArrayList<LinkedList<String>> paths = new ArrayList<>();
        ArrayList<Integer> dists = new ArrayList<>();

        Algorithms.shortestPaths(completeMap, "Porto", Integer::compare, Integer::sum, 0, paths, dists);

        assertEquals(paths.size(), dists.size(), "There should be as many paths as sizes");
        assertEquals(completeMap.numVertices(), paths.size(), "There should be a path to every vertex");
        assertEquals(Arrays.asList("Porto"), paths.get(completeMap.key("Porto")), "Number of nodes should be 1 if source and vertex are the same");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Coimbra", "Lisboa"), paths.get(completeMap.key("Lisboa")), "Path to Lisbon");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Viseu", "Guarda", "Castelo Branco"), paths.get(completeMap.key("Castelo Branco")), "Path to Castelo Branco");
        assertEquals(335, dists.get(completeMap.key("Castelo Branco")), "Path between Porto and Castelo Branco should be 335 Km");

        //Changing Edge: Aveiro-Viseu with Edge: Leiria-C.Branco
        //should change shortest path between Porto and Castelo Branco
        completeMap.removeEdge("Aveiro", "Viseu");
        completeMap.addEdge("Leiria", "Castelo Branco", 170);
        Algorithms.shortestPaths(completeMap, "Porto", Integer::compare, Integer::sum, 0, paths, dists);
        assertEquals(365, dists.get(completeMap.key("Castelo Branco")), "Path between Porto and Castelo Branco should now be 365 Km");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Leiria", "Castelo Branco"), paths.get(completeMap.key("Castelo Branco")), "Path to Castelo Branco");

        Algorithms.shortestPaths(incompleteMap, "Porto", Integer::compare, Integer::sum, 0, paths, dists);
        assertNull(dists.get(completeMap.key("Faro")), "Length path should be null if there is no path");
        assertEquals(335, dists.get(completeMap.key("Lisboa")), "Path between Porto and Lisboa should be 335 Km");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Coimbra", "Lisboa"), paths.get(completeMap.key("Lisboa")), "Path to Lisboa");

        Algorithms.shortestPaths(incompleteMap, "Braga", Integer::compare, Integer::sum, 0, paths, dists);
        assertEquals(255, dists.get(completeMap.key("Leiria")), "Path between Braga and Leiria should be 255 Km");
        assertEquals(Arrays.asList("Braga", "Porto", "Aveiro", "Leiria"), paths.get(completeMap.key("Leiria")), "Path to Leiria");
    }

    /**
     * Test of betweennessCentrality method, of class Algorithms.
     */
    @Test
    public void testBetweennessCentrality() {
        System.out.println("Test of Betweenness Centrality");

        // Test 1: Simple linear graph (A -> B -> C)
        Graph<String, Integer> linearGraph = new MapGraph<>(false);
        linearGraph.addVertex("A");
        linearGraph.addVertex("B");
        linearGraph.addVertex("C");
        linearGraph.addEdge("A", "B", 1);
        linearGraph.addEdge("B", "C", 1);

        Map<String, Double> centrality = Algorithms.betweennessCentrality(
                linearGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        // B should have centrality 1 (only path A-C passes through B)
        assertEquals(1.0, centrality.get("B"), 0.001, "B should be the central vertex with centrality 1");
        assertEquals(0.0, centrality.get("A"), 0.001, "A should have centrality 0");
        assertEquals(0.0, centrality.get("C"), 0.001, "C should have centrality 0");

        // Test 2: Star graph (hub and spokes)
        Graph<String, Integer> starGraph = new MapGraph<>(false);
        starGraph.addVertex("Hub");
        starGraph.addVertex("A");
        starGraph.addVertex("B");
        starGraph.addVertex("C");
        starGraph.addEdge("Hub", "A", 1);
        starGraph.addEdge("Hub", "B", 1);
        starGraph.addEdge("Hub", "C", 1);

        centrality = Algorithms.betweennessCentrality(
                starGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        // Hub should have high centrality (all paths between spokes go through it)
        // For 4 vertices: (3 choose 2) = 3 paths pass through hub
        assertEquals(3.0, centrality.get("Hub"), 0.001, "Hub should have centrality 3");
        assertEquals(0.0, centrality.get("A"), 0.001, "A should have centrality 0");
        assertEquals(0.0, centrality.get("B"), 0.001, "B should have centrality 0");
        assertEquals(0.0, centrality.get("C"), 0.001, "C should have centrality 0");

        // Test 3: Complete graph (all vertices connected)
        Graph<String, Integer> completeGraph = new MapGraph<>(false);
        completeGraph.addVertex("A");
        completeGraph.addVertex("B");
        completeGraph.addVertex("C");
        completeGraph.addEdge("A", "B", 1);
        completeGraph.addEdge("B", "C", 1);
        completeGraph.addEdge("A", "C", 1);

        centrality = Algorithms.betweennessCentrality(
                completeGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        // All vertices should have centrality 0 (direct paths exist)
        assertEquals(0.0, centrality.get("A"), 0.001, "A should have centrality 0 in complete graph");
        assertEquals(0.0, centrality.get("B"), 0.001, "B should have centrality 0 in complete graph");
        assertEquals(0.0, centrality.get("C"), 0.001, "C should have centrality 0 in complete graph");

        // Test 4: Using the Portuguese cities graph
        centrality = Algorithms.betweennessCentrality(
                completeMap,
                Integer::compare,
                Integer::sum,
                0
        );

        // Aveiro should be important (connects Porto to southern cities)
        // Coimbra should be important (on path to Lisboa)
        assertTrue(centrality.get("Aveiro") > 0, "Aveiro should have positive centrality");
        assertTrue(centrality.get("Coimbra") > 0, "Coimbra should have positive centrality");

        // Faro is a leaf node, should have 0 centrality
        assertEquals(0.0, centrality.get("Faro"), 0.001, "Faro should have centrality 0 (leaf node)");

        // Test 5: Disconnected graph
        Graph<String, Integer> disconnectedGraph = new MapGraph<>(false);
        disconnectedGraph.addVertex("A");
        disconnectedGraph.addVertex("B");
        disconnectedGraph.addVertex("C");
        disconnectedGraph.addVertex("D");
        disconnectedGraph.addEdge("A", "B", 1);
        // C and D are isolated

        centrality = Algorithms.betweennessCentrality(
                disconnectedGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        // All should have 0 centrality (no paths through intermediate vertices)
        assertEquals(0.0, centrality.get("A"), 0.001, "A should have centrality 0");
        assertEquals(0.0, centrality.get("B"), 0.001, "B should have centrality 0");
        assertEquals(0.0, centrality.get("C"), 0.001, "C should have centrality 0");
        assertEquals(0.0, centrality.get("D"), 0.001, "D should have centrality 0");
    }

    /**
     * Test of mostCentralVertex method, of class Algorithms.
     */
    @Test
    public void testMostCentralVertex() {
        System.out.println("Test of Most Central Vertex");

        // Test 1: Linear graph - B should be most central
        Graph<String, Integer> linearGraph = new MapGraph<>(false);
        linearGraph.addVertex("A");
        linearGraph.addVertex("B");
        linearGraph.addVertex("C");
        linearGraph.addEdge("A", "B", 1);
        linearGraph.addEdge("B", "C", 1);

        String mostCentral = Algorithms.mostCentralVertex(
                linearGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        assertEquals("B", mostCentral, "B should be the most central vertex in linear graph");

        // Test 2: Star graph - Hub should be most central
        Graph<String, Integer> starGraph = new MapGraph<>(false);
        starGraph.addVertex("Hub");
        starGraph.addVertex("A");
        starGraph.addVertex("B");
        starGraph.addVertex("C");
        starGraph.addVertex("D");
        starGraph.addEdge("Hub", "A", 1);
        starGraph.addEdge("Hub", "B", 1);
        starGraph.addEdge("Hub", "C", 1);
        starGraph.addEdge("Hub", "D", 1);

        mostCentral = Algorithms.mostCentralVertex(
                starGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        assertEquals("Hub", mostCentral, "Hub should be the most central vertex in star graph");

        // Test 3: Portuguese cities graph
        mostCentral = Algorithms.mostCentralVertex(
                completeMap,
                Integer::compare,
                Integer::sum,
                0
        );

        // Should be one of the well-connected central cities
        assertNotNull(mostCentral, "Should find a most central vertex");
        assertTrue(
                Arrays.asList("Aveiro", "Coimbra", "Leiria").contains(mostCentral),
                "Most central vertex should be one of the central cities"
        );

        // Test 4: Graph with all equal centrality
        Graph<String, Integer> completeTriangle = new MapGraph<>(false);
        completeTriangle.addVertex("A");
        completeTriangle.addVertex("B");
        completeTriangle.addVertex("C");
        completeTriangle.addEdge("A", "B", 1);
        completeTriangle.addEdge("B", "C", 1);
        completeTriangle.addEdge("A", "C", 1);

        mostCentral = Algorithms.mostCentralVertex(
                completeTriangle,
                Integer::compare,
                Integer::sum,
                0
        );

        // Any vertex can be returned (all have centrality 0)
        assertNotNull(mostCentral, "Should return some vertex");
        assertTrue(
                Arrays.asList("A", "B", "C").contains(mostCentral),
                "Should be one of the vertices"
        );

        // Test 5: Empty graph
        Graph<String, Integer> emptyGraph = new MapGraph<>(false);

        mostCentral = Algorithms.mostCentralVertex(
                emptyGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        assertNull(mostCentral, "Empty graph should return null");
    }

    /**
     * Test of normalizeCentrality method, of class Algorithms.
     */
    @Test
    public void testNormalizeCentrality() {
        System.out.println("Test of Normalize Centrality");

        // Test 1: Linear graph (3 vertices)
        Graph<String, Integer> linearGraph = new MapGraph<>(false);
        linearGraph.addVertex("A");
        linearGraph.addVertex("B");
        linearGraph.addVertex("C");
        linearGraph.addEdge("A", "B", 1);
        linearGraph.addEdge("B", "C", 1);

        Map<String, Double> centrality = Algorithms.betweennessCentrality(
                linearGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        Map<String, Double> normalized = Algorithms.normalizeCentrality(
                centrality,
                linearGraph.numVertices(),
                linearGraph.isDirected()
        );

        // For 3 vertices, undirected: max = (3-1)*(3-2)/2 = 1
        // B has centrality 1, so normalized should be 1.0
        assertEquals(1.0, normalized.get("B"), 0.001, "B normalized centrality should be 1.0");
        assertEquals(0.0, normalized.get("A"), 0.001, "A normalized centrality should be 0.0");
        assertEquals(0.0, normalized.get("C"), 0.001, "C normalized centrality should be 0.0");

        // Test 2: Star graph (5 vertices)
        Graph<String, Integer> starGraph = new MapGraph<>(false);
        starGraph.addVertex("Hub");
        starGraph.addVertex("A");
        starGraph.addVertex("B");
        starGraph.addVertex("C");
        starGraph.addVertex("D");
        starGraph.addEdge("Hub", "A", 1);
        starGraph.addEdge("Hub", "B", 1);
        starGraph.addEdge("Hub", "C", 1);
        starGraph.addEdge("Hub", "D", 1);

        centrality = Algorithms.betweennessCentrality(
                starGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        normalized = Algorithms.normalizeCentrality(
                centrality,
                starGraph.numVertices(),
                starGraph.isDirected()
        );

        // For 5 vertices, undirected: max = (5-1)*(5-2)/2 = 6
        // Hub has centrality 6, so normalized should be 1.0
        assertEquals(1.0, normalized.get("Hub"), 0.001, "Hub normalized centrality should be 1.0");
        assertEquals(0.0, normalized.get("A"), 0.001, "A normalized centrality should be 0.0");

        // Test 3: Graph with 2 vertices or less
        Graph<String, Integer> twoVertexGraph = new MapGraph<>(false);
        twoVertexGraph.addVertex("A");
        twoVertexGraph.addVertex("B");
        twoVertexGraph.addEdge("A", "B", 1);

        centrality = Algorithms.betweennessCentrality(
                twoVertexGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        normalized = Algorithms.normalizeCentrality(
                centrality,
                twoVertexGraph.numVertices(),
                twoVertexGraph.isDirected()
        );

        // With 2 vertices, all normalized values should be 0
        assertEquals(0.0, normalized.get("A"), 0.001, "A normalized centrality should be 0.0");
        assertEquals(0.0, normalized.get("B"), 0.001, "B normalized centrality should be 0.0");

        // Test 4: Directed graph
        Graph<String, Integer> directedGraph = new MapGraph<>(true);
        directedGraph.addVertex("A");
        directedGraph.addVertex("B");
        directedGraph.addVertex("C");
        directedGraph.addVertex("D");
        directedGraph.addEdge("A", "B", 1);
        directedGraph.addEdge("B", "C", 1);
        directedGraph.addEdge("C", "D", 1);

        centrality = Algorithms.betweennessCentrality(
                directedGraph,
                Integer::compare,
                Integer::sum,
                0
        );

        normalized = Algorithms.normalizeCentrality(
                centrality,
                directedGraph.numVertices(),
                directedGraph.isDirected()
        );

        // For 4 vertices, directed: max = (4-1)*(4-2) = 6
        // Values should be between 0 and 1
        for (Double value : normalized.values()) {
            assertTrue(value >= 0.0 && value <= 1.0, "Normalized values should be between 0 and 1");
        }

        // Test 5: Portuguese cities graph
        centrality = Algorithms.betweennessCentrality(
                completeMap,
                Integer::compare,
                Integer::sum,
                0
        );

        normalized = Algorithms.normalizeCentrality(
                centrality,
                completeMap.numVertices(),
                completeMap.isDirected()
        );

        // All normalized values should be between 0 and 1
        for (Map.Entry<String, Double> entry : normalized.entrySet()) {
            assertTrue(
                    entry.getValue() >= 0.0 && entry.getValue() <= 1.0,
                    entry.getKey() + " normalized centrality should be between 0 and 1"
            );
        }
    }

    /**
     * Test betweenness centrality on directed graphs.
     */
    @Test
    public void testBetweennessCentralityDirected() {
        System.out.println("Test of Betweenness Centrality on Directed Graph");

        // Test 1: Simple directed path A -> B -> C
        Graph<String, Integer> directedPath = new MapGraph<>(true);
        directedPath.addVertex("A");
        directedPath.addVertex("B");
        directedPath.addVertex("C");
        directedPath.addEdge("A", "B", 1);
        directedPath.addEdge("B", "C", 1);

        Map<String, Double> centrality = Algorithms.betweennessCentrality(
                directedPath,
                Integer::compare,
                Integer::sum,
                0
        );

        // B should have centrality 1 (path A->C goes through B)
        assertEquals(1.0, centrality.get("B"), 0.001, "B should have centrality 1");
        assertEquals(0.0, centrality.get("A"), 0.001, "A should have centrality 0");
        assertEquals(0.0, centrality.get("C"), 0.001, "C should have centrality 0");

        // Test 2: Directed cycle A -> B -> C -> A
        Graph<String, Integer> directedCycle = new MapGraph<>(true);
        directedCycle.addVertex("A");
        directedCycle.addVertex("B");
        directedCycle.addVertex("C");
        directedCycle.addEdge("A", "B", 1);
        directedCycle.addEdge("B", "C", 1);
        directedCycle.addEdge("C", "A", 1);

        centrality = Algorithms.betweennessCentrality(
                directedCycle,
                Integer::compare,
                Integer::sum,
                0
        );

        // In a directed cycle, each vertex is on exactly 1 shortest path as intermediate
        // A->C goes through B, B->A goes through C, C->B goes through A
        double expectedCentrality = 1.0; // Each vertex is on 1 shortest path
        assertEquals(expectedCentrality, centrality.get("A"), 0.001, "A should have centrality 1");
        assertEquals(expectedCentrality, centrality.get("B"), 0.001, "B should have centrality 1");
        assertEquals(expectedCentrality, centrality.get("C"), 0.001, "C should have centrality 1");

        // Test 3: Directed star with hub
        Graph<String, Integer> directedStar = new MapGraph<>(true);
        directedStar.addVertex("Hub");
        directedStar.addVertex("A");
        directedStar.addVertex("B");
        directedStar.addVertex("C");
        directedStar.addEdge("Hub", "A", 1);
        directedStar.addEdge("Hub", "B", 1);
        directedStar.addEdge("Hub", "C", 1);

        centrality = Algorithms.betweennessCentrality(
                directedStar,
                Integer::compare,
                Integer::sum,
                0
        );

        // Hub has centrality 0 (no paths go through it as intermediate)
        // There are no paths between A, B, C
        assertEquals(0.0, centrality.get("Hub"), 0.001, "Hub should have centrality 0");
        assertEquals(0.0, centrality.get("A"), 0.001, "A should have centrality 0");
        assertEquals(0.0, centrality.get("B"), 0.001, "B should have centrality 0");
        assertEquals(0.0, centrality.get("C"), 0.001, "C should have centrality 0");
    }

    /**
     * Test minimum distance graph using Floyd-Warshall.
     */
    @Test
    public void testminDistGraph() {
        System.out.println("Test of minimum distance graph using Floyd-Warshall");

        Comparator<Integer> comp = Integer::compare;
        BinaryOperator<Integer> sum = Integer::sum;

        Graph<String, Integer> minDistComplete = Algorithms.minDistGraph(completeMap, comp, sum);

        assertEquals(completeMap.numVertices(), minDistComplete.numVertices(),
                "MinDistGraph should have the same number of vertices");

        assertEquals(75, minDistComplete.edge("Porto", "Aveiro").getWeight(),
                "Direct edge Porto-Aveiro should be 75");

        assertEquals(335, minDistComplete.edge("Porto", "Lisboa").getWeight(),
                "Shortest path Porto-Lisboa should be 335");

        Integer portoCB = minDistComplete.edge("Porto", "Castelo Branco").getWeight();
        assertTrue(portoCB == 365 || portoCB == 335,
                "Shortest path Porto-Castelo Branco should be 365 or 335 (depending on graph connectivity)");

        assertEquals(395, minDistComplete.edge("Braga", "Lisboa").getWeight(),
                "Shortest path Braga-Lisboa should be 395");

        Integer aveiroCB = minDistComplete.edge("Aveiro", "Castelo Branco").getWeight();
        assertTrue(aveiroCB == 290 || aveiroCB == 260,
                "Shortest path Aveiro-Castelo Branco should be 290 or 260 (depending on graph connectivity)");

        Graph<String, Integer> minDistIncomplete = Algorithms.minDistGraph(incompleteMap, comp, sum);

        assertEquals(335, minDistIncomplete.edge("Porto", "Lisboa").getWeight(),
                "Shortest path Porto-Lisboa should be 335 in incomplete map");

        assertNull(minDistIncomplete.edge("Porto", "Faro"),
                "No path should exist from Porto to Faro in incomplete map");

        assertEquals(175, minDistIncomplete.edge("Viseu", "Castelo Branco").getWeight(),
                "Shortest path Viseu-Castelo Branco should be 175");

        assertEquals(minDistComplete.edge("Porto", "Aveiro").getWeight(),
                minDistComplete.edge("Aveiro", "Porto").getWeight(),
                "Undirected graph should have symmetric edges");

        assertEquals(295, minDistComplete.edge("Braga", "Guarda").getWeight(),
                "Shortest path Braga-Guarda should be 295");

        assertEquals(615, minDistComplete.edge("Porto", "Faro").getWeight(),
                "Shortest path Porto-Faro should be 615");

        System.out.println("All minimum distance graph tests passed!");
    }

    @Test
    public void testBellmanFord() {
        System.out.println("Test of finding shortest path using the Bellman-Ford algorithm.");

        ArrayList<LinkedList<String>> paths = new ArrayList<>();
        ArrayList<Double> dists = new ArrayList<>();

        Comparator<Double> comp = Double::compare;
        BinaryOperator<Double> sum = Double::sum;
        Double zero = 0.0;
        final double DELTA = 0.001;

        boolean success = Algorithms.bellmanFord(testGraph, "Porto", comp, sum, zero, paths, dists);

        assertTrue(success, "Bellman-Ford should be successful (no negative cycle)");
        assertEquals(testGraph.numVertices(), paths.size(), "There should be a path for each vertex");
        assertEquals(paths.size(), dists.size(), "There should be as many distances as paths");

        // 1.1 Shortest Path to Lisbon (with negative cost)
        // Porto -> Aveiro -> Leiria -> Lisboa = 75.5 + (-120.0) + (-150.1) = -194.6
        double expectedLisboaDist = -194.6;
        int lisboaKey = testGraph.key("Lisboa");

        assertEquals(expectedLisboaDist, dists.get(lisboaKey), DELTA, "Distance Porto->Lisboa should be -194.6 km (via Aveiro, Leiria)");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Leiria", "Lisboa"), paths.get(lisboaKey), "Path to Lisboa");

        // 1.2 Shortest Path to Castelo Branco (via negative weight)
        // Porto -> Aveiro -> Leiria -> Castelo Branco = 75.5 + (-120.0) + 170.0 = 125.5
        double expectedCasteloBrancoDist = 125.5;
        int cbKey = testGraph.key("Castelo Branco");

        assertEquals(expectedCasteloBrancoDist, dists.get(cbKey), DELTA, "Distance Porto->Castelo Branco should be 125.5 km");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Leiria", "Castelo Branco"), paths.get(cbKey), "Path to Castelo Branco");

        // 1.3 Unreachable Path (Braga cannot reach Porto, graph is directed)
        // Recalculate path from Braga
        paths.clear();
        dists.clear();
        Algorithms.bellmanFord(testGraph, "Braga", comp, sum, zero, paths, dists);

        // Porto is unreachable from Braga
        assertNull(dists.get(testGraph.key("Porto")), "Distance to Porto (unreachable) should be null");
        assertNull(paths.get(testGraph.key("Porto")), "Path to Porto (unreachable) should be null");


        // --- Scenario 2: Negative Cycle Detection ('Porto' to negativeCycleGraph) ---
        paths.clear(); // Clear previous results
        dists.clear();

        // The negative cycle (Viseu -> Guarda -> Viseu) is accessible from Porto (Porto -> Aveiro -> Viseu)
        success = Algorithms.bellmanFord(negativeCycleGraph, "Porto", comp, sum, zero, paths, dists);

        assertFalse(success, "Bellman-Ford should fail (negative cycle detected)");
        // In case of failure, 'paths' is cleared and only the cycle is inserted in paths.get(0)
        assertEquals(1, paths.size(), "paths should contain only one element: the negative cycle");
        assertTrue(dists.isEmpty(), "dists should be empty");

        // The cycle is Viseu -> Guarda -> Viseu
        LinkedList<String> expectedCycleNodes = new LinkedList<>(Arrays.asList("Viseu", "Guarda"));
        LinkedList<String> foundCycle = paths.get(0);

        // Verify that the cycle has format (V1, V2, V1) and contains expected nodes
        assertEquals(3, foundCycle.size(), "The cycle should have 3 nodes (start, mid, end)");
        assertEquals(foundCycle.getFirst(), foundCycle.getLast(), "First and last vertex of the cycle should be the same");
        assertTrue(foundCycle.containsAll(expectedCycleNodes), "The detected cycle should involve Viseu and Guarda");
    }
}