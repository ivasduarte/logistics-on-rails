# USEI12 - Minimal Backbone Network

## Tests

### Test 1: MST on Simple Connected Graph

```java
@Test
public void testMinimalBackbone_SimpleGraph() {
    System.out.println("Test: MST on Simple Connected Graph");

    // Create a simple connected graph
    // Topology:
    //     A ---(5)--- B
    //     |           |
    //    (1)         (3)
    //     |           |
    //     C ---(2)--- D
    
    Station stationA = new Station("1", "Station A", 0.0, 0.0, 0.0, 0.0);
    Station stationB = new Station("2", "Station B", 0.0, 0.0, 100.0, 0.0);
    Station stationC = new Station("3", "Station C", 0.0, 0.0, 0.0, 100.0);
    Station stationD = new Station("4", "Station D", 0.0, 0.0, 100.0, 100.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    graph.addVertex(stationD);
    
    graph.addEdge(stationA, stationB, new ConnectionData(5.0, 100.0, 10.0));
    graph.addEdge(stationA, stationC, new ConnectionData(1.0, 100.0, 10.0));
    graph.addEdge(stationB, stationD, new ConnectionData(3.0, 100.0, 10.0));
    graph.addEdge(stationC, stationD, new ConnectionData(2.0, 100.0, 10.0));

    controller.loadNetworkFromGraph(graph);

    // Compute MST
    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    // Verify MST properties
    assertNotNull(mst, "MST should not be null");
    assertEquals(4, mst.numVertices(), "MST should have all 4 vertices");
    assertEquals(3, countUniqueEdges(mst), "MST should have exactly 3 edges (V-1)");

    // Calculate total distance
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    double totalDistance = stats.get("totalDistance");
    
    // Expected MST: A-C (1) + C-D (2) + B-D (3) = 6
    assertEquals(6.0, totalDistance, 0.01, "Total MST distance should be 6.0");
}

private int countUniqueEdges(Graph<Station, ConnectionData> graph) {
    Set<String> uniqueEdges = new HashSet<>();
    for (Edge<Station, ConnectionData> edge : graph.edges()) {
        String key = edge.getVOrigin().getNodeID().compareTo(edge.getVTarget().getNodeID()) < 0
            ? edge.getVOrigin().getNodeID() + "-" + edge.getVTarget().getNodeID() : edge.getVTarget().getNodeID() + "-" + edge.getVOrigin().getNodeID();
            uniqueEdges.add(key);
    }
    return uniqueEdges.size();
}
```
### Test 2: MST on Linear Chain
```java
@Test
public void testMinimalBackbone_LinearChain() {
    System.out.println("Test: MST on Linear Chain");

    // A --- B --- C --- D
    Station stationA = new Station("1", "A", 0.0, 0.0, 0.0, 0.0);
    Station stationB = new Station("2", "B", 0.0, 0.0, 10.0, 0.0);
    Station stationC = new Station("3", "C", 0.0, 0.0, 20.0, 0.0);
    Station stationD = new Station("4", "D", 0.0, 0.0, 30.0, 0.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    graph.addVertex(stationD);
    
    graph.addEdge(stationA, stationB, new ConnectionData(10.0, 100.0, 5.0));
    graph.addEdge(stationB, stationC, new ConnectionData(10.0, 100.0, 5.0));
    graph.addEdge(stationC, stationD, new ConnectionData(10.0, 100.0, 5.0));

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    assertEquals(4, mst.numVertices(), "Should have all 4 vertices");
    assertEquals(3, countUniqueEdges(mst), "Should have exactly 3 edges");
    
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    assertEquals(30.0, stats.get("totalDistance"), 0.01, "Total distance should be 30.0");
}
```

### Test 3: MST with Triangle (Cycle)
```java
@Test
public void testMinimalBackbone_Triangle() {
    System.out.println("Test: MST with Triangle");

    // Triangle with different edge weights
    //      A
    //     /|\
    //   (1) (3)
    //   /   |  \
    //  B---(2)---C
    
    Station stationA = new Station("1", "A", 0.0, 0.0, 50.0, 0.0);
    Station stationB = new Station("2", "B", 0.0, 0.0, 0.0, 100.0);
    Station stationC = new Station("3", "C", 0.0, 0.0, 100.0, 100.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    
    graph.addEdge(stationA, stationB, new ConnectionData(1.0, 100.0, 5.0));
    graph.addEdge(stationB, stationC, new ConnectionData(2.0, 100.0, 5.0));
    graph.addEdge(stationA, stationC, new ConnectionData(3.0, 100.0, 5.0));

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    assertEquals(3, mst.numVertices());
    assertEquals(2, countUniqueEdges(mst), "MST should have 2 edges (not include heaviest)");
    
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    // Should select A-B (1) and B-C (2), excluding A-C (3)
    assertEquals(3.0, stats.get("totalDistance"), 0.01, "Should exclude heaviest edge");
}
```

### Test 4: MST on Disconnected Graph
```java
@Test
public void testMinimalBackbone_DisconnectedGraph() {
    System.out.println("Test: MST on Disconnected Graph");

    // Component 1: A --- B
    // Component 2: C --- D
    
    Station stationA = new Station("1", "A", 0.0, 0.0, 0.0, 0.0);
    Station stationB = new Station("2", "B", 0.0, 0.0, 10.0, 0.0);
    Station stationC = new Station("3", "C", 0.0, 0.0, 100.0, 0.0);
    Station stationD = new Station("4", "D", 0.0, 0.0, 110.0, 0.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    graph.addVertex(stationD);
    
    graph.addEdge(stationA, stationB, new ConnectionData(10.0, 100.0, 5.0));
    graph.addEdge(stationC, stationD, new ConnectionData(10.0, 100.0, 5.0));
    // No connection between components

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    assertEquals(4, mst.numVertices(), "Should have all 4 vertices");
    assertEquals(2, countUniqueEdges(mst), "Should have 2 edges (one per component)");
    
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    assertEquals(20.0, stats.get("totalDistance"), 0.01);
}
```

### Test 5: MST on Single Vertex
```java
@Test
public void testMinimalBackbone_SingleVertex() {
    System.out.println("Test: MST on Single Vertex");

    Station stationA = new Station("1", "A", 0.0, 0.0, 0.0, 0.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    assertEquals(1, mst.numVertices());
    assertEquals(0, countUniqueEdges(mst), "Single vertex should have no edges");
    
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    assertEquals(0.0, stats.get("totalDistance"), 0.01);
}
```

### Test 6: MST on Complete Graph (Dense)
```java
@Test
public void testMinimalBackbone_CompleteGraph() {
    System.out.println("Test: MST on Complete Graph");

    // Complete graph K4 with all possible edges
    Station stationA = new Station("1", "A", 0.0, 0.0, 0.0, 0.0);
    Station stationB = new Station("2", "B", 0.0, 0.0, 10.0, 0.0);
    Station stationC = new Station("3", "C", 0.0, 0.0, 10.0, 10.0);
    Station stationD = new Station("4", "D", 0.0, 0.0, 0.0, 10.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    graph.addVertex(stationD);
    
    // Add all possible edges with different weights
    graph.addEdge(stationA, stationB, new ConnectionData(1.0, 100.0, 5.0));
    graph.addEdge(stationA, stationC, new ConnectionData(5.0, 100.0, 5.0));
    graph.addEdge(stationA, stationD, new ConnectionData(4.0, 100.0, 5.0));
    graph.addEdge(stationB, stationC, new ConnectionData(2.0, 100.0, 5.0));
    graph.addEdge(stationB, stationD, new ConnectionData(6.0, 100.0, 5.0));
    graph.addEdge(stationC, stationD, new ConnectionData(3.0, 100.0, 5.0));

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    assertEquals(4, mst.numVertices());
    assertEquals(3, countUniqueEdges(mst), "MST should have 3 edges");
    
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    // Expected: A-B (1) + B-C (2) + C-D (3) = 6
    assertEquals(6.0, stats.get("totalDistance"), 0.01);
}
```

### Test 7: MST with Equal Weight Edges
```java
@Test
public void testMinimalBackbone_EqualWeights() {
    System.out.println("Test: MST with Equal Weight Edges");

    // All edges have the same weight
    Station stationA = new Station("1", "A", 0.0, 0.0, 0.0, 0.0);
    Station stationB = new Station("2", "B", 0.0, 0.0, 10.0, 0.0);
    Station stationC = new Station("3", "C", 0.0, 0.0, 20.0, 0.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    
    graph.addEdge(stationA, stationB, new ConnectionData(10.0, 100.0, 5.0));
    graph.addEdge(stationB, stationC, new ConnectionData(10.0, 100.0, 5.0));
    graph.addEdge(stationA, stationC, new ConnectionData(10.0, 100.0, 5.0));

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();

    assertEquals(3, mst.numVertices());
    assertEquals(2, countUniqueEdges(mst), "MST should have 2 edges");
    
    Map<String, Double> stats = controller.calculateMSTStatistics(mst);
    assertEquals(20.0, stats.get("totalDistance"), 0.01, "Any valid MST should have distance 20");
}
```

### Test 8: MST Export to DOT Format
```java
@Test
public void testMinimalBackbone_ExportDOT() {
    System.out.println("Test: MST Export to DOT Format");

    Station stationA = new Station("1", "Brussels", 50.85, 4.35, 1000.0, 1000.0);
    Station stationB = new Station("2", "Antwerp", 51.22, 4.40, 1200.0, 1200.0);
    Station stationC = new Station("3", "Ghent", 51.05, 3.72, 800.0, 1200.0);

    Graph<Station, ConnectionData> graph = new MapGraph<>(false);
    graph.addVertex(stationA);
    graph.addVertex(stationB);
    graph.addVertex(stationC);
    
    graph.addEdge(stationA, stationB, new ConnectionData(45.0, 100.0, 10.0));
    graph.addEdge(stationA, stationC, new ConnectionData(55.0, 100.0, 12.0));
    graph.addEdge(stationB, stationC, new ConnectionData(60.0, 100.0, 15.0));

    controller.loadNetworkFromGraph(graph);

    Graph<Station, ConnectionData> mst = controller.computeMinimalBackbone();
    
    String dotFilename = "test_mst.dot";
    controller.exportMinimalBackboneToGraphviz(mst, dotFilename);

    // Verify file exists
    File dotFile = new File(dotFilename);
    assertTrue(dotFile.exists(), "DOT file should be created");
    
    // Verify file content
    try (BufferedReader reader = new BufferedReader(new FileReader(dotFile))) {
        String content = reader.lines().collect(Collectors.joining("\n"));
        
        assertTrue(content.contains("graph MinimalBackboneNetwork"), "Should have graph header");
        assertTrue(content.contains("layout=neato"), "Should use neato layout");
        assertTrue(content.contains("Brussels"), "Should contain station names");
        assertTrue(content.contains("pos="), "Should contain position attributes");
        
    } catch (IOException e) {
        fail("Error reading DOT file: " + e.getMessage());
    } finally {
        dotFile.delete(); // Cleanup
    }
}
```

## Implementation

The implementation uses **Prim's Algorithm** with a **priority queue** to efficiently compute the Minimum Spanning Tree.

### Key Implementation Details

**1. Greedy Selection:**
- Always selects the minimum-weight edge connecting visited to unvisited vertices
- Guarantees optimal (minimum total weight) solution

**2. Priority Queue:**
- Uses `PriorityQueue<Edge>` sorted by distance
- Efficiently extracts minimum-weight edge in $\mathcal{O}(\log E)$ time

**3. Visited Tracking:**
- Uses `HashSet<Station>` to track visited vertices
- $\mathcal{O}(1)$ lookup time

**4. Undirected Graph:**
- MST is always undirected (even if input graph is directed)
- Considers both incoming and outgoing edges

**5. Disconnected Components:**
- Handles disconnected graphs gracefully
- Creates a minimum spanning forest (multiple trees)

