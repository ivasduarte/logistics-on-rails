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
     * Test minimum distance graph using Floyd-Warshall.
     */
    @Test
    public void testminDistGraph() {

        throw new UnsupportedOperationException("Not supported yet.");

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