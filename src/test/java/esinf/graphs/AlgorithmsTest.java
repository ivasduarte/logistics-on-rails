package esinf.graphs;

import freightMngt.graph.Algorithms;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmsTest {

    final Graph<String, Integer> completeMap = new MapGraph<>(true);
    Graph<String, Integer> incompleteMap = new MapGraph<>(false);

    public void MapAlgorithmsTest() {}

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
        completeMap.addEdge("Aveiro", "Viseu", 85);
        completeMap.addEdge("Braga", "Porto", 60);

        incompleteMap = completeMap.clone();

        completeMap.addEdge("Porto", "Faro", 85);
        completeMap.addEdge("Leiria", "Lisboa", 100);
        completeMap.addEdge("Castelo Branco", "Guarda", 250);

    }

    private void checkContentEquals(List<String> list1, List<String> list2, String message) {
        Collections.sort(list1);
        Collections.sort(list2);
        assertEquals(list1, list2, message);
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
        assertEquals(List.of("Porto"), shortPath, "Shortest Path only contains Porto");

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
     * Tests shortest paths using the Bellman-Ford algorithm.
     */
    @Test
    public void testBellmanFord() {
        System.out.println("Testing Bellman-Ford algorithm of shortest paths.");

        ArrayList<LinkedList<String>> paths = new ArrayList<>();
        ArrayList<Integer> distances = new ArrayList<>();

        boolean success = Algorithms.shortestPaths(completeMap, "Porto", Integer::compare, Integer::sum, 0,  paths, distances);

        assertTrue(success, "Should return true for a graph with no negative cycles"); // NOVO
        assertEquals(paths.size(), distances.size(), "There should be as many paths as sizes");
        assertEquals(completeMap.numVertices(), paths.size(), "There should be a path to every vertex");
        assertEquals(Arrays.asList("Porto"), paths.get(completeMap.key("Porto")), "Number of nodes should be 1 if source and vertex are the same");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Coimbra", "Lisboa"), paths.get(completeMap.key("Lisboa")), "Path to Lisbon");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Viseu", "Guarda", "Castelo Branco"), paths.get(completeMap.key("Castelo Branco")), "Path to Castelo Branco");
        assertEquals(335, distances.get(completeMap.key("Castelo Branco")), "Path between Porto and Castelo Branco should be 335 Km");


        // modification of graph
        completeMap.removeEdge("Aveiro", "Viseu");
        completeMap.addEdge("Leiria", "Castelo Branco", 170);

        success = Algorithms.shortestPaths(completeMap, "Porto", Integer::compare, Integer::sum, 0, paths, distances);
        assertTrue(success, "Should return true for modified graph without negative cycles"); // NOVO
        assertEquals(365, distances.get(completeMap.key("Castelo Branco")), "Path between Porto and Castelo Branco should now be 365 Km");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Leiria", "Castelo Branco"), paths.get(completeMap.key("Castelo Branco")), "Path to Castelo Branco");

        // incomplete graph
        success = Algorithms.shortestPaths(incompleteMap, "Porto", Integer::compare, Integer::sum, 0, paths, distances);
        assertTrue(success, "Should return true even for disconnected graphs (no negative cycles)"); // NOVO
        assertNull(distances.get(completeMap.key("Faro")), "Length path should be null if there is no path");
        assertEquals(335, distances.get(completeMap.key("Lisboa")), "Path between Porto and Lisboa should be 335 Km");
        assertEquals(Arrays.asList("Porto", "Aveiro", "Coimbra", "Lisboa"), paths.get(completeMap.key("Lisboa")), "Path to Lisboa");

        success = Algorithms.shortestPaths(incompleteMap, "Braga", Integer::compare, Integer::sum, 0, paths, distances);
        assertTrue(success, "Should return true"); // NOVO
        assertEquals(255, distances.get(completeMap.key("Leiria")), "Path between Braga and Leiria should be 255 Km");
        assertEquals(Arrays.asList("Braga", "Porto", "Aveiro", "Leiria"), paths.get(completeMap.key("Leiria")), "Path to Leiria");

        // test negative cycle
        // porto --> aveiro (cost = 75) && aveiro --> porto (cost = -100)
        // cycle = 75 - 100 = -25

        completeMap.addEdge("Aveiro", "Porto", -100);

        success = Algorithms.shortestPaths(completeMap, "Porto", Integer::compare, Integer::sum, 0, paths, distances);

        assertFalse(success, "Should return false when a negative cycle is detected");

        // validate that cycle was returned in the first position
        assertFalse(paths.isEmpty(), "Paths should not be empty (should contain the cycle)");
        LinkedList<String> cycle = paths.get(0);
        assertNotNull(cycle, "The detected cycle should be returned");

        // cycle must have Porto and Aveiro
        assertTrue(cycle.contains("Porto") && cycle.contains("Aveiro"), "Cycle should contain involved vertices");

        // the first and last elements must be the same
        assertEquals(cycle.getFirst(), cycle.getLast(), "First and last vertex of a cycle must be the same");
    }
}
