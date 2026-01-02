package esinf.graphs;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import freightMngt.graph.Algorithms;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;
import org.junit.jupiter.api.Test;

class USEI12Test {

    @Test
    public void testkruskalMST() {
        System.out.println("Test of Minimum Spanning Tree");

        // Test 1: Simple triangle with different edge weights
        Graph<String, Integer> triangleGraph = new MapGraph<>(false);
        triangleGraph.addVertex("A");
        triangleGraph.addVertex("B");
        triangleGraph.addVertex("C");
        triangleGraph.addEdge("A", "B", 1);
        triangleGraph.addEdge("B", "C", 2);
        triangleGraph.addEdge("A", "C", 3);

        Graph<String, Integer> expectedTriangle = new MapGraph<>(false);
        expectedTriangle.addVertex("A");
        expectedTriangle.addVertex("B");
        expectedTriangle.addVertex("C");
        expectedTriangle.addEdge("A", "B", 1);
        expectedTriangle.addEdge("B", "C", 2);

        Graph<String, Integer> mstTriangle = Algorithms.kruskalMST(triangleGraph, Integer::compare);
        assertEquals(expectedTriangle, mstTriangle);

        // Test 2: Linear chain
        Graph<String, Integer> linearGraph = new MapGraph<>(false);
        linearGraph.addVertex("A");
        linearGraph.addVertex("B");
        linearGraph.addVertex("C");
        linearGraph.addVertex("D");
        linearGraph.addEdge("A", "B", 1);
        linearGraph.addEdge("B", "C", 1);
        linearGraph.addEdge("C", "D", 1);

        Graph<String, Integer> expectedLinear = new MapGraph<>(false);
        expectedLinear.addVertex("A");
        expectedLinear.addVertex("B");
        expectedLinear.addVertex("C");
        expectedLinear.addVertex("D");
        expectedLinear.addEdge("A", "B", 1);
        expectedLinear.addEdge("B", "C", 1);
        expectedLinear.addEdge("C", "D", 1);

        Graph<String, Integer> mstLinear = Algorithms.kruskalMST(linearGraph, Integer::compare);
        assertEquals(expectedLinear, mstLinear);

        // Test 3: Complete graph K4
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

        Graph<String, Integer> expectedComplete = new MapGraph<>(false);
        expectedComplete.addVertex("A");
        expectedComplete.addVertex("B");
        expectedComplete.addVertex("C");
        expectedComplete.addVertex("D");
        expectedComplete.addEdge("A", "B", 1);
        expectedComplete.addEdge("B", "C", 2);
        expectedComplete.addEdge("C", "D", 3);

        Graph<String, Integer> mstComplete = Algorithms.kruskalMST(completeGraph, Integer::compare);
        assertEquals(expectedComplete, mstComplete);

        // Test 4: Star graph
        Graph<String, Integer> starGraph = new MapGraph<>(false);
        starGraph.addVertex("Hub");
        starGraph.addVertex("A");
        starGraph.addVertex("B");
        starGraph.addVertex("C");
        starGraph.addEdge("Hub", "A", 1);
        starGraph.addEdge("Hub", "B", 2);
        starGraph.addEdge("Hub", "C", 3);

        Graph<String, Integer> expectedStar = new MapGraph<>(false);
        expectedStar.addVertex("Hub");
        expectedStar.addVertex("A");
        expectedStar.addVertex("B");
        expectedStar.addVertex("C");
        expectedStar.addEdge("Hub", "A", 1);
        expectedStar.addEdge("Hub", "B", 2);
        expectedStar.addEdge("Hub", "C", 3);

        Graph<String, Integer> mstStar = Algorithms.kruskalMST(starGraph, Integer::compare);
        assertEquals(expectedStar, mstStar);

        // Test 5: Disconnected graph
        Graph<String, Integer> disconnectedGraph = new MapGraph<>(false);
        disconnectedGraph.addVertex("A");
        disconnectedGraph.addVertex("B");
        disconnectedGraph.addVertex("C");
        disconnectedGraph.addVertex("D");
        disconnectedGraph.addEdge("A", "B", 1);
        disconnectedGraph.addEdge("C", "D", 2);

        Graph<String, Integer> expectedDisconnected = new MapGraph<>(false);
        expectedDisconnected.addVertex("A");
        expectedDisconnected.addVertex("B");
        expectedDisconnected.addVertex("C");
        expectedDisconnected.addVertex("D");
        expectedDisconnected.addEdge("A", "B", 1);
        expectedDisconnected.addEdge("C", "D", 2);

        Graph<String, Integer> mstDisconnected = Algorithms.kruskalMST(disconnectedGraph, Integer::compare);
        assertEquals(expectedDisconnected, mstDisconnected);
    }

    @Test
    public void testkruskalMST_EqualWeights() {
        System.out.println("Test MST with Equal Weights");

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

        Graph<String, Integer> expected = new MapGraph<>(false);
        expected.addVertex("A");
        expected.addVertex("B");
        expected.addVertex("C");
        expected.addVertex("D");
        expected.addEdge("A", "B", 1);
        expected.addEdge("A", "C", 1);
        expected.addEdge("B", "D", 1);

        Graph<String, Integer> mst = Algorithms.kruskalMST(equalWeightGraph, Integer::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_SingleVertex() {
        System.out.println("Test MST on Single Vertex");

        Graph<String, Integer> singleVertex = new MapGraph<>(false);
        singleVertex.addVertex("A");

        Graph<String, Integer> expected = new MapGraph<>(false);
        expected.addVertex("A");

        Graph<String, Integer> mst = Algorithms.kruskalMST(singleVertex, Integer::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_EmptyGraph() {
        System.out.println("Test MST on Empty Graph");

        Graph<String, Integer> emptyGraph = new MapGraph<>(false);
        Graph<String, Integer> expected = new MapGraph<>(false);

        Graph<String, Integer> mst = Algorithms.kruskalMST(emptyGraph, Integer::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_NullGraph() {
        System.out.println("Test MST on Null Graph");

        Graph<String, Integer> expected = new MapGraph<>(false);
        Graph<String, Integer> mst = Algorithms.kruskalMST(null, Integer::compare);

        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_DoubleWeights() {
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

        Graph<String, Double> expected = new MapGraph<>(false);
        expected.addVertex("A");
        expected.addVertex("B");
        expected.addVertex("C");
        expected.addVertex("D");
        expected.addEdge("B", "C", 1.0);
        expected.addEdge("A", "B", 1.5);
        expected.addEdge("C", "D", 2.5);

        Graph<String, Double> mst = Algorithms.kruskalMST(doubleGraph, Double::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_MultipleComponents() {
        System.out.println("Test MST with Multiple Disconnected Components");

        Graph<String, Integer> multiComponent = new MapGraph<>(false);
        multiComponent.addVertex("A");
        multiComponent.addVertex("B");
        multiComponent.addVertex("C");
        multiComponent.addEdge("A", "B", 1);
        multiComponent.addEdge("B", "C", 2);
        multiComponent.addVertex("D");
        multiComponent.addVertex("E");
        multiComponent.addEdge("D", "E", 3);
        multiComponent.addVertex("F");

        Graph<String, Integer> expected = new MapGraph<>(false);
        expected.addVertex("A");
        expected.addVertex("B");
        expected.addVertex("C");
        expected.addVertex("D");
        expected.addVertex("E");
        expected.addVertex("F");
        expected.addEdge("A", "B", 1);
        expected.addEdge("B", "C", 2);
        expected.addEdge("D", "E", 3);

        Graph<String, Integer> mst = Algorithms.kruskalMST(multiComponent, Integer::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_DirectedGraph() {
        System.out.println("Test MST on Directed Graph");

        Graph<String, Integer> directedGraph = new MapGraph<>(true);
        directedGraph.addVertex("A");
        directedGraph.addVertex("B");
        directedGraph.addVertex("C");
        directedGraph.addEdge("A", "B", 1);
        directedGraph.addEdge("B", "C", 2);
        directedGraph.addEdge("C", "A", 3);

        Graph<String, Integer> expected = new MapGraph<>(false);
        expected.addVertex("A");
        expected.addVertex("B");
        expected.addVertex("C");
        expected.addEdge("A", "B", 1);
        expected.addEdge("B", "C", 2);

        Graph<String, Integer> mst = Algorithms.kruskalMST(directedGraph, Integer::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_GreedySelection() {
        System.out.println("Test MST Greedy Edge Selection");

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

        Graph<String, Integer> expected = new MapGraph<>(false);
        expected.addVertex("A");
        expected.addVertex("B");
        expected.addVertex("C");
        expected.addVertex("D");
        expected.addEdge("A", "B", 1);
        expected.addEdge("B", "C", 2);
        expected.addEdge("C", "D", 3);

        Graph<String, Integer> mst = Algorithms.kruskalMST(greedyGraph, Integer::compare);
        assertEquals(expected, mst);
    }

    @Test
    public void testkruskalMST_Connectivity() {
        System.out.println("Test MST Connectivity Property");

        Graph<String, Integer> connectedGraph = new MapGraph<>(false);
        connectedGraph.addVertex("A");
        connectedGraph.addVertex("B");
        connectedGraph.addVertex("C");
        connectedGraph.addVertex("D");
        connectedGraph.addVertex("E");
        connectedGraph.addEdge("A", "B", 1);
        connectedGraph.addEdge("A", "C", 2);
        connectedGraph.addEdge("B", "D", 3);
        connectedGraph.addEdge("C", "D", 4);
        connectedGraph.addEdge("D", "E", 5);

        Graph<String, Integer> expected = new MapGraph<>(false);
        expected.addVertex("A");
        expected.addVertex("B");
        expected.addVertex("C");
        expected.addVertex("D");
        expected.addVertex("E");
        expected.addEdge("A", "B", 1);
        expected.addEdge("A", "C", 2);
        expected.addEdge("B", "D", 3);
        expected.addEdge("D", "E", 5);

        Graph<String, Integer> mst = Algorithms.kruskalMST(connectedGraph, Integer::compare);
        assertEquals(expected, mst);
    }
}