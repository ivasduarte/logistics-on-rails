package esinf.graphs;

import java.util.*;
import java.util.function.BinaryOperator;
import static org.junit.jupiter.api.Assertions.*;
import freightMngt.graph.Algorithms;
import freightMngt.graph.Edge;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.Test;

class USEI12Test {

    /**
     * Test of minimumSpanningTree method, of class Algorithms.
     */
    @Test
    public void testMinimumSpanningTree() {
        System.out.println("Test of Minimum Spanning Tree");

        // Test 1: Simple triangle with different edge weights
        // Topology:
        //      A
        //     /|\
        //   (1) (3)
        //   /   |  \
        //  B---(2)---C
        // Expected MST: A-B (1) + B-C (2) = 3 (excludes A-C)

        Graph<String, Integer> triangleGraph = new MapGraph<>(false);
        triangleGraph.addVertex("A");
        triangleGraph.addVertex("B");
        triangleGraph.addVertex("C");
        triangleGraph.addEdge("A", "B", 1);
        triangleGraph.addEdge("B", "C", 2);
        triangleGraph.addEdge("A", "C", 3);

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                triangleGraph,
                Integer::compare
        );

        assertEquals(3, mst.numVertices(), "MST should have all 3 vertices");
        assertEquals(2, countUniqueEdges(mst), "MST should have 2 edges (V-1)");

        // Verify total weight
        int totalWeight = calculateTotalWeight(mst);
        assertEquals(3, totalWeight, "Total MST weight should be 3 (1+2, excluding edge 3)");

        // Test 2: Linear chain
        // A --- B --- C --- D (all edges weight 1)
        Graph<String, Integer> linearGraph = new MapGraph<>(false);
        linearGraph.addVertex("A");
        linearGraph.addVertex("B");
        linearGraph.addVertex("C");
        linearGraph.addVertex("D");
        linearGraph.addEdge("A", "B", 1);
        linearGraph.addEdge("B", "C", 1);
        linearGraph.addEdge("C", "D", 1);

        mst = Algorithms.minimumSpanningTree(linearGraph, Integer::compare);

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
        assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges");
        assertEquals(3, calculateTotalWeight(mst), "Total weight should be 3");

        // Test 3: Complete graph K4 with different weights
        Graph<String, Integer> completeGraph = new MapGraph<>(false);
        completeGraph.addVertex("A");
        completeGraph.addVertex("B");
        completeGraph.addVertex("C");
        completeGraph.addVertex("D");
        completeGraph.addEdge("A", "B", 1);
        completeGraph.addEdge("A", "C", 4);
        completeGraph.addEdge("A", "D", 5);
        completeGraph.addEdge("B", "C", 2);
        completeGraph.addEdge("B", "D", 6);
        completeGraph.addEdge("C", "D", 3);

        mst = Algorithms.minimumSpanningTree(completeGraph, Integer::compare);

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
        assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges");
        // Expected: A-B (1) + B-C (2) + C-D (3) = 6
        assertEquals(6, calculateTotalWeight(mst), "Total weight should be 6");

        // Test 4: Star graph (hub and spokes)
        Graph<String, Integer> starGraph = new MapGraph<>(false);
        starGraph.addVertex("Hub");
        starGraph.addVertex("A");
        starGraph.addVertex("B");
        starGraph.addVertex("C");
        starGraph.addEdge("Hub", "A", 1);
        starGraph.addEdge("Hub", "B", 2);
        starGraph.addEdge("Hub", "C", 3);

        mst = Algorithms.minimumSpanningTree(starGraph, Integer::compare);

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
        assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges");
        assertEquals(6, calculateTotalWeight(mst), "Total weight should be 6 (1+2+3)");

        // Test 5: Disconnected graph (two components)
        Graph<String, Integer> disconnectedGraph = new MapGraph<>(false);
        disconnectedGraph.addVertex("A");
        disconnectedGraph.addVertex("B");
        disconnectedGraph.addVertex("C");
        disconnectedGraph.addVertex("D");
        disconnectedGraph.addEdge("A", "B", 1);
        disconnectedGraph.addEdge("C", "D", 2);
        // No connection between (A,B) and (C,D)

        mst = Algorithms.minimumSpanningTree(disconnectedGraph, Integer::compare);

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
        assertEquals(2, countUniqueEdges(mst), "MST should have 2 edges (one per component)");
        assertEquals(3, calculateTotalWeight(mst), "Total weight should be 3 (1+2)");
    }

    /**
     * Test MST with equal weight edges.
     */
    @Test
    public void testMinimumSpanningTree_EqualWeights() {
        System.out.println("Test MST with Equal Weights");

        // All edges have weight 1
        Graph<String, Integer> equalWeightGraph = new MapGraph<>(false);
        equalWeightGraph.addVertex("A");
        equalWeightGraph.addVertex("B");
        equalWeightGraph.addVertex("C");
        equalWeightGraph.addVertex("D");
        equalWeightGraph.addEdge("A", "B", 1);
        equalWeightGraph.addEdge("A", "C", 1);
        equalWeightGraph.addEdge("B", "C", 1);
        equalWeightGraph.addEdge("B", "D", 1);
        equalWeightGraph.addEdge("C", "D", 1);

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                equalWeightGraph,
                Integer::compare
        );

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
        assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges");
        assertEquals(3, calculateTotalWeight(mst), "Total weight should be 3 (any valid MST)");
    }

    /**
     * Test MST on single vertex.
     */
    @Test
    public void testMinimumSpanningTree_SingleVertex() {
        System.out.println("Test MST on Single Vertex");

        Graph<String, Integer> singleVertex = new MapGraph<>(false);
        singleVertex.addVertex("A");

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                singleVertex,
                Integer::compare
        );

        assertEquals(1, mst.numVertices(), "MST should have 1 vertex");
        assertEquals(0, countUniqueEdges(mst), "MST should have 0 edges");
    }

    /**
     * Test MST on empty graph.
     */
    @Test
    public void testMinimumSpanningTree_EmptyGraph() {
        System.out.println("Test MST on Empty Graph");

        Graph<String, Integer> emptyGraph = new MapGraph<>(false);

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                emptyGraph,
                Integer::compare
        );

        assertNotNull(mst, "MST should not be null");
        assertEquals(0, mst.numVertices(), "MST should have 0 vertices");
        assertEquals(0, countUniqueEdges(mst), "MST should have 0 edges");
    }

    /**
     * Test MST with null graph.
     */
    @Test
    public void testMinimumSpanningTree_NullGraph() {
        System.out.println("Test MST on Null Graph");

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                null,
                Integer::compare
        );

        assertNotNull(mst, "MST should return empty graph, not null");
        assertEquals(0, mst.numVertices(), "MST should have 0 vertices");
    }

    /**
     * Test MST with Double weights.
     */
    @Test
    public void testMinimumSpanningTree_DoubleWeights() {
        System.out.println("Test MST with Double Weights");

        Graph<String, Double> doubleGraph = new MapGraph<>(false);
        doubleGraph.addVertex("A");
        doubleGraph.addVertex("B");
        doubleGraph.addVertex("C");
        doubleGraph.addVertex("D");
        doubleGraph.addEdge("A", "B", 1.5);
        doubleGraph.addEdge("A", "C", 2.0);
        doubleGraph.addEdge("B", "C", 1.0);
        doubleGraph.addEdge("B", "D", 3.5);
        doubleGraph.addEdge("C", "D", 2.5);

        Graph<String, Double> mst = Algorithms.minimumSpanningTree(
                doubleGraph,
                Double::compare
        );

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");

        // Expected: B-C (1.0) + A-B (1.5) + C-D (2.5) = 5.0
        double totalWeight = calculateTotalWeightDouble(mst);
        assertEquals(5.0, totalWeight, 0.001, "Total weight should be 5.0");
    }

    /**
     * Test MST preserves all vertices even in disconnected components.
     */
    @Test
    public void testMinimumSpanningTree_MultipleComponents() {
        System.out.println("Test MST with Multiple Disconnected Components");

        Graph<String, Integer> multiComponent = new MapGraph<>(false);
        // Component 1: A - B - C
        multiComponent.addVertex("A");
        multiComponent.addVertex("B");
        multiComponent.addVertex("C");
        multiComponent.addEdge("A", "B", 1);
        multiComponent.addEdge("B", "C", 2);

        // Component 2: D - E
        multiComponent.addVertex("D");
        multiComponent.addVertex("E");
        multiComponent.addEdge("D", "E", 3);

        // Component 3: F (isolated)
        multiComponent.addVertex("F");

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                multiComponent,
                Integer::compare
        );

        assertEquals(6, mst.numVertices(), "MST should have all 6 vertices");
        assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges (2 + 1 + 0)");
        assertEquals(6, calculateTotalWeight(mst), "Total weight should be 6 (1+2+3)");
    }

    /**
     * Test MST on directed graph (should treat as undirected).
     */
    @Test
    public void testMinimumSpanningTree_DirectedGraph() {
        System.out.println("Test MST on Directed Graph");

        // Create directed graph
        Graph<String, Integer> directedGraph = new MapGraph<>(true);
        directedGraph.addVertex("A");
        directedGraph.addVertex("B");
        directedGraph.addVertex("C");
        directedGraph.addEdge("A", "B", 1);
        directedGraph.addEdge("B", "C", 2);
        directedGraph.addEdge("C", "A", 3);

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                directedGraph,
                Integer::compare
        );

        assertEquals(3, mst.numVertices(), "MST should have all 3 vertices");
        assertEquals(2, countUniqueEdges(mst), "MST should have 2 edges");
        assertFalse(mst.isDirected(), "MST should be undirected");
    }

    /**
     * Test MST edge selection (greedy property).
     */
    @Test
    public void testMinimumSpanningTree_GreedySelection() {
        System.out.println("Test MST Greedy Edge Selection");

        // Create graph where greedy choice matters
        // Topology:
        //     A
        //    /|\
        //  (1)(5)(9)
        //  / | \
        // B  |  D
        //  \ | /
        //  (2)(3)
        //    \|/
        //     C

        Graph<String, Integer> greedyGraph = new MapGraph<>(false);
        greedyGraph.addVertex("A");
        greedyGraph.addVertex("B");
        greedyGraph.addVertex("C");
        greedyGraph.addVertex("D");
        greedyGraph.addEdge("A", "B", 1);
        greedyGraph.addEdge("B", "C", 2);
        greedyGraph.addEdge("A", "C", 5);
        greedyGraph.addEdge("C", "D", 3);
        greedyGraph.addEdge("A", "D", 9);

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                greedyGraph,
                Integer::compare
        );

        assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
        assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges");

        // Expected: A-B (1) + B-C (2) + C-D (3) = 6
        // Should NOT include A-C (5) or A-D (9)
        assertEquals(6, calculateTotalWeight(mst),
                "MST should select edges 1,2,3 (total 6), not 5 or 9");
    }

    /**
     * Test MST connectivity property.
     */
    @Test
    public void testMinimumSpanningTree_Connectivity() {
        System.out.println("Test MST Connectivity Property");

        Graph<String, Integer> connectedGraph = new MapGraph<>(false);
        connectedGraph.addVertex("A");
        connectedGraph.addVertex("B");
        connectedGraph.addVertex("C");
        connectedGraph.addVertex("D");
        connectedGraph.addVertex("E");

        // Create a connected graph
        connectedGraph.addEdge("A", "B", 1);
        connectedGraph.addEdge("A", "C", 2);
        connectedGraph.addEdge("B", "D", 3);
        connectedGraph.addEdge("C", "D", 4);
        connectedGraph.addEdge("D", "E", 5);

        Graph<String, Integer> mst = Algorithms.minimumSpanningTree(
                connectedGraph,
                Integer::compare
        );

        // MST should connect all vertices
        assertEquals(5, mst.numVertices(), "MST should have all 5 vertices");
        assertEquals(4, countUniqueEdges(mst), "MST should have exactly 4 edges (V-1)");

        // Verify all vertices are still reachable (using BFS)
        for (String vertex : mst.vertices()) {
            LinkedList<String> reachable = Algorithms.BreadthFirstSearch(mst, vertex);
            assertEquals(5, reachable.size(),
                    "All vertices should be reachable from " + vertex + " in MST");
        }
    }

// ============================================================================
// Helper Methods
// ============================================================================

    /**
     * Helper method to count unique edges in an undirected graph.
     * Avoids double-counting edges.
     */
    private int countUniqueEdges(Graph<String, Integer> graph) {
        Set<String> uniqueEdges = new HashSet<>();
        for (Edge<String, Integer> edge : graph.edges()) {
            String key = edge.getVOrigin().compareTo(edge.getVTarget()) < 0
                    ? edge.getVOrigin() + "-" + edge.getVTarget()
                    : edge.getVTarget() + "-" + edge.getVOrigin();
            uniqueEdges.add(key);
        }
        return uniqueEdges.size();
    }

    /**
     * Helper method to calculate total weight of all edges in MST.
     * Avoids double-counting in undirected graphs.
     */
    private int calculateTotalWeight(Graph<String, Integer> graph) {
        Set<String> countedEdges = new HashSet<>();
        int totalWeight = 0;

        for (Edge<String, Integer> edge : graph.edges()) {
            String key = edge.getVOrigin().compareTo(edge.getVTarget()) < 0
                    ? edge.getVOrigin() + "-" + edge.getVTarget()
                    : edge.getVTarget() + "-" + edge.getVOrigin();

            if (!countedEdges.contains(key)) {
                totalWeight += edge.getWeight();
                countedEdges.add(key);
            }
        }

        return totalWeight;
    }

    /**
     * Helper method to calculate total weight for Double-weighted graphs.
     */
    private double calculateTotalWeightDouble(Graph<String, Double> graph) {
        Set<String> countedEdges = new HashSet<>();
        double totalWeight = 0.0;

        for (Edge<String, Double> edge : graph.edges()) {
            String key = edge.getVOrigin().compareTo(edge.getVTarget()) < 0
                    ? edge.getVOrigin() + "-" + edge.getVTarget()
                    : edge.getVTarget() + "-" + edge.getVOrigin();

            if (!countedEdges.contains(key)) {
                totalWeight += edge.getWeight();
                countedEdges.add(key);
            }
        }

        return totalWeight;
    }

}