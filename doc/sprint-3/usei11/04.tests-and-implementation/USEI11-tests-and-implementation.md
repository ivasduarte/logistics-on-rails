# USEI11 - Directed Line Upgrade Plan

## Tests

The following tests validate the algorithms used in USEI11 (Topological Sort and Cycle Detection). These tests reference and build upon the test methodology from `AlgorithmsTest.java`.

---

### Foundation Algorithm Tests (from AlgorithmsTest.java)

#### Test 1: Depth-First Search (Foundation for Topological Sort)

The DFS algorithm is the foundation for both topological sort and cycle detection.
```java
/**
 * Test of DepthFirstSearch method, of class Algorithms.
 * Extracted from AlgorithmsTest.java - testDepthFirstSearch()
 */
@Test
public void testDepthFirstSearch() {
    System.out.println("Test of DepthFirstSearch");

    // Test 1: Non-existent vertex
    assertNull(Algorithms.DepthFirstSearch(completeMap, "LX"), 
        "Should be null if vertex does not exist");

    // Test 2: Single isolated vertex
    LinkedList<String> path = Algorithms.DepthFirstSearch(incompleteMap, "Faro");
    assertEquals(1, path.size(), "Should be just one");
    assertEquals("Faro", path.peekFirst());

    // Test 3: Connected component traversal
    path = Algorithms.DepthFirstSearch(incompleteMap, "Porto");
    assertEquals(7, path.size(), "Should give seven vertices");
    assertEquals("Porto", path.removeFirst(), "DepthFirst Porto");
    
    // Test 4: Verify component isolation
    path = Algorithms.DepthFirstSearch(incompleteMap, "Viseu");
    List<String> expected = new LinkedList<>(Arrays.asList("Viseu", "Guarda", "Castelo Branco"));
    assertEquals(expected, path, "DepthFirst Viseu");
}
```

**Key Insight:** DFS visits each vertex exactly once and explores all reachable vertices, establishing the foundation for $\mathcal{O}(V + E)$ complexity.

---

### USEI11 Specific Algorithm Tests

#### Test 2: Topological Sort on Simple DAG
```java
@Test
public void testTopologicalSort_SimpleDAG() {
    System.out.println("Test: Topological Sort on Simple DAG");

    // Create directed acyclic graph
    // Topology: A -> B, A -> C, B -> D, C -> D
    Graph<String, Integer> dag = new MapGraph<>(true);
    dag.addVertex("A");
    dag.addVertex("B");
    dag.addVertex("C");
    dag.addVertex("D");
    
    dag.addEdge("A", "B", 1);
    dag.addEdge("A", "C", 1);
    dag.addEdge("B", "D", 1);
    dag.addEdge("C", "D", 1);

    LinkedList<String> order = Algorithms.topologicalSort(dag);

    // Verify success
    assertNotNull(order, "Topological sort should succeed on DAG");
    assertEquals(4, order.size(), "Should contain all 4 vertices");
    
    // Verify dependencies are respected
    int indexA = order.indexOf("A");
    int indexB = order.indexOf("B");
    int indexC = order.indexOf("C");
    int indexD = order.indexOf("D");
    
    assertTrue(indexA < indexB, "A must come before B");
    assertTrue(indexA < indexC, "A must come before C");
    assertTrue(indexB < indexD, "B must come before D");
    assertTrue(indexC < indexD, "C must come before D");
}
```

**Validates:** Correct ordering respects all directed dependencies (A→B means A before B).

---

#### Test 3: Topological Sort with Cycle Detection
```java
@Test
public void testTopologicalSort_WithCycle() {
    System.out.println("Test: Topological Sort with Cycle");

    // Create graph with cycle: A -> B -> C -> A
    Graph<String, Integer> cyclicGraph = new MapGraph<>(true);
    cyclicGraph.addVertex("A");
    cyclicGraph.addVertex("B");
    cyclicGraph.addVertex("C");
    
    cyclicGraph.addEdge("A", "B", 1);
    cyclicGraph.addEdge("B", "C", 1);
    cyclicGraph.addEdge("C", "A", 1); // Creates cycle

    LinkedList<String> order = Algorithms.topologicalSort(cyclicGraph);

    // Should return null when cycle exists
    assertNull(order, "Topological sort should return null when cycles exist");
}
```

**Validates:** Algorithm correctly detects cycles and returns null.

---

#### Test 4: Cycle Detection on DAG (No Cycles)
```java
@Test
public void testDetectCycles_OnDAG() {
    System.out.println("Test: Cycle Detection on DAG");

    // Create a DAG
    Graph<String, Integer> dag = new MapGraph<>(true);
    dag.addVertex("Porto");
    dag.addVertex("Aveiro");
    dag.addVertex("Coimbra");
    dag.addVertex("Lisboa");
    
    dag.addEdge("Porto", "Aveiro", 75);
    dag.addEdge("Porto", "Coimbra", 120);
    dag.addEdge("Aveiro", "Lisboa", 200);
    dag.addEdge("Coimbra", "Lisboa", 200);

    ArrayList<LinkedList<String>> cycles = Algorithms.detectCycles(dag);

    assertNotNull(cycles, "Cycle detection should not return null");
    assertTrue(cycles.isEmpty(), "No cycles should be detected in DAG");
}
```

**Validates:** No false positives – DAGs correctly identified as cycle-free.

---

#### Test 5: Cycle Detection with Single Cycle
```java
@Test
public void testDetectCycles_SingleCycle() {
    System.out.println("Test: Detect Single Cycle");

    // Create graph with one cycle: Porto -> Aveiro -> Coimbra -> Porto
    Graph<String, Integer> cyclicGraph = new MapGraph<>(true);
    cyclicGraph.addVertex("Porto");
    cyclicGraph.addVertex("Aveiro");
    cyclicGraph.addVertex("Coimbra");
    
    cyclicGraph.addEdge("Porto", "Aveiro", 75);
    cyclicGraph.addEdge("Aveiro", "Coimbra", 60);
    cyclicGraph.addEdge("Coimbra", "Porto", 120); // Creates cycle

    ArrayList<LinkedList<String>> cycles = Algorithms.detectCycles(cyclicGraph);

    assertNotNull(cycles, "Cycle detection should not return null");
    assertEquals(1, cycles.size(), "Should detect exactly one cycle");
    
    LinkedList<String> cycle = cycles.get(0);
    assertTrue(cycle.size() >= 3, "Cycle should contain at least 3 vertices");
    
    // Verify cycle is closed (first == last)
    assertEquals(cycle.getFirst(), cycle.getLast(), 
        "Cycle should be closed (first == last)");
    
    // Verify all three cities are in the cycle
    assertTrue(cycle.contains("Porto"), "Cycle should contain Porto");
    assertTrue(cycle.contains("Aveiro"), "Cycle should contain Aveiro");
    assertTrue(cycle.contains("Coimbra"), "Cycle should contain Coimbra");
}
```

**Validates:** Correct cycle reconstruction with proper closure (first vertex == last vertex).

---

#### Test 6: Cycle Detection with Multiple Cycles
```java
@Test
public void testDetectCycles_MultipleCycles() {
    System.out.println("Test: Detect Multiple Cycles");

    // Create graph with two separate cycles
    // Cycle 1: A -> B -> A
    // Cycle 2: C -> D -> E -> C
    Graph<String, Integer> multiCycleGraph = new MapGraph<>(true);
    multiCycleGraph.addVertex("A");
    multiCycleGraph.addVertex("B");
    multiCycleGraph.addVertex("C");
    multiCycleGraph.addVertex("D");
    multiCycleGraph.addVertex("E");
    
    // Cycle 1
    multiCycleGraph.addEdge("A", "B", 1);
    multiCycleGraph.addEdge("B", "A", 1);
    
    // Cycle 2
    multiCycleGraph.addEdge("C", "D", 1);
    multiCycleGraph.addEdge("D", "E", 1);
    multiCycleGraph.addEdge("E", "C", 1);

    ArrayList<LinkedList<String>> cycles = Algorithms.detectCycles(multiCycleGraph);

    assertNotNull(cycles, "Cycle detection should not return null");
    assertTrue(cycles.size() >= 2, "Should detect at least 2 cycles");
}
```

**Validates:** Algorithm detects multiple independent cycles.

---

#### Test 7: Topological Sort on Disconnected Components
```java
@Test
public void testTopologicalSort_DisconnectedComponents() {
    System.out.println("Test: Topological Sort on Disconnected Components");

    // Create graph with disconnected components
    // Component 1: A -> B
    // Component 2: C -> D
    // Isolated: E
    Graph<String, Integer> disconnected = new MapGraph<>(true);
    disconnected.addVertex("A");
    disconnected.addVertex("B");
    disconnected.addVertex("C");
    disconnected.addVertex("D");
    disconnected.addVertex("E");
    
    disconnected.addEdge("A", "B", 1);
    disconnected.addEdge("C", "D", 1);

    LinkedList<String> order = Algorithms.topologicalSort(disconnected);

    assertNotNull(order, "Should handle disconnected components");
    assertEquals(5, order.size(), "All vertices should be in order");
    
    // Verify component dependencies
    int indexA = order.indexOf("A");
    int indexB = order.indexOf("B");
    int indexC = order.indexOf("C");
    int indexD = order.indexOf("D");
    
    assertTrue(indexA < indexB, "A must come before B");
    assertTrue(indexC < indexD, "C must come before D");
    assertTrue(order.contains("E"), "E should be in the order");
}
```

**Validates:** Algorithm handles disconnected graph components correctly.

---

#### Test 8: Empty Graph Edge Case
```java
@Test
public void testTopologicalSort_EmptyGraph() {
    System.out.println("Test: Topological Sort on Empty Graph");

    Graph<String, Integer> emptyGraph = new MapGraph<>(true);
    LinkedList<String> order = Algorithms.topologicalSort(emptyGraph);

    assertNotNull(order, "Empty graph should return empty list");
    assertTrue(order.isEmpty(), "Empty graph order should be empty");
}
```

**Validates:** Proper handling of edge case (empty graph).

---

#### Test 9: Single Vertex
```java
@Test
public void testTopologicalSort_SingleVertex() {
    System.out.println("Test: Topological Sort on Single Vertex");

    Graph<String, Integer> singleVertex = new MapGraph<>(true);
    singleVertex.addVertex("OnlyStation");
    
    LinkedList<String> order = Algorithms.topologicalSort(singleVertex);

    assertNotNull(order, "Single vertex should have valid order");
    assertEquals(1, order.size(), "Should contain one vertex");
    assertEquals("OnlyStation", order.get(0), "Should be the only station");
}
```

**Validates:** Single vertex correctly returns list with one element.

---

#### Test 10: Complex Railway Dependencies
```java
@Test
public void testTopologicalSort_ComplexRailwayDependencies() {
    System.out.println("Test: Complex Railway Upgrade Dependencies");

    // Realistic upgrade scenario
    // MainHub -> Junction1 -> Terminal1
    // MainHub -> Junction2 -> Terminal2
    // Junction1, Junction2 -> Terminal3
    Graph<String, Integer> upgradeGraph = new MapGraph<>(true);
    upgradeGraph.addVertex("MainHub");
    upgradeGraph.addVertex("Junction1");
    upgradeGraph.addVertex("Junction2");
    upgradeGraph.addVertex("Terminal1");
    upgradeGraph.addVertex("Terminal2");
    upgradeGraph.addVertex("Terminal3");
    
    upgradeGraph.addEdge("MainHub", "Junction1", 1);
    upgradeGraph.addEdge("MainHub", "Junction2", 1);
    upgradeGraph.addEdge("Junction1", "Terminal1", 1);
    upgradeGraph.addEdge("Junction2", "Terminal2", 1);
    upgradeGraph.addEdge("Junction1", "Terminal3", 1);
    upgradeGraph.addEdge("Junction2", "Terminal3", 1);

    LinkedList<String> order = Algorithms.topologicalSort(upgradeGraph);

    assertNotNull(order, "Should find valid upgrade order");
    assertEquals(6, order.size(), "All 6 stations should be in order");
    
    // Verify MainHub comes first
    assertEquals("MainHub", order.get(0), "MainHub should be upgraded first");
    
    // Verify all dependencies
    int idxMainHub = order.indexOf("MainHub");
    int idxJunction1 = order.indexOf("Junction1");
    int idxJunction2 = order.indexOf("Junction2");
    int idxTerminal1 = order.indexOf("Terminal1");
    int idxTerminal2 = order.indexOf("Terminal2");
    int idxTerminal3 = order.indexOf("Terminal3");
    
    assertTrue(idxMainHub < idxJunction1, "MainHub before Junction1");
    assertTrue(idxMainHub < idxJunction2, "MainHub before Junction2");
    assertTrue(idxJunction1 < idxTerminal1, "Junction1 before Terminal1");
    assertTrue(idxJunction2 < idxTerminal2, "Junction2 before Terminal2");
    assertTrue(idxJunction1 < idxTerminal3, "Junction1 before Terminal3");
    assertTrue(idxJunction2 < idxTerminal3, "Junction2 before Terminal3");
}
```

**Validates:** Realistic railway scenario with multiple dependency levels.

---

## Implementation

### Class Algorithms.java

The implementation for USEI11 algorithms is present in your `Algorithms.java` file:

#### 1. Topological Sort Implementation
```java
/**
 * Performs topological sort on a directed acyclic graph (DAG).
 * If the graph contains cycles, returns null.
 *
 * @param g Graph instance (must be directed)
 * @return LinkedList with vertices in topological order, or null if cycles exist
 * @throws IllegalArgumentException if graph is not directed
 */
public static <V, E> LinkedList<V> topologicalSort(Graph<V, E> g) {
    if (g == null) {
        throw new IllegalArgumentException("Graph is null.");
    }

    if (!g.isDirected()) {
        throw new IllegalArgumentException("Topological sort only applies to directed graphs.");
    }

    int n = g.numVertices();
    if (n == 0) return new LinkedList<>();

    // State: 0 = WHITE (not visited), 1 = GRAY (in progress), 2 = BLACK (done)
    int[] state = new int[n];
    LinkedList<V> result = new LinkedList<>();
    boolean[] hasCycle = {false};

    // Process all vertices (handles disconnected components)
    for (V vertex : g.vertices()) {
        if (state[g.key(vertex)] == 0) {
            topologicalSortDFS(g, vertex, state, result, hasCycle);
            if (hasCycle[0]) {
                return null; // Cycle detected
            }
        }
    }

    return result;
}

/**
 * DFS helper for topological sort with cycle detection.
 */
private static <V, E> void topologicalSortDFS(Graph<V, E> g, V vertex,
                                              int[] state,
                                              LinkedList<V> result,
                                              boolean[] hasCycle) {
    int vKey = g.key(vertex);
    state[vKey] = 1; // GRAY - mark as being processed

    for (V neighbor : g.adjVertices(vertex)) {
        int nKey = g.key(neighbor);

        if (state[nKey] == 1) {
            // Back edge detected -> cycle found
            hasCycle[0] = true;
            return;
        }

        if (state[nKey] == 0) {
            topologicalSortDFS(g, neighbor, state, result, hasCycle);
            if (hasCycle[0]) return;
        }
    }

    state[vKey] = 2; // BLACK - mark as processed
    result.addFirst(vertex); // Add to front (reverse finish order)
}
```


#### 2. Cycle Detection Implementation
```java
/**
 * Detects all cycles in a directed graph.
 *
 * @param g Graph instance (must be directed)
 * @return ArrayList containing all detected cycles. Each cycle is a LinkedList of vertices.
 *         Returns empty list if no cycles exist.
 * @throws IllegalArgumentException if graph is not directed
 */
public static <V, E> ArrayList<LinkedList<V>> detectCycles(Graph<V, E> g) {
    if (g == null) {
        throw new IllegalArgumentException("Graph is null.");
    }

    if (!g.isDirected()) {
        throw new IllegalArgumentException("Cycle detection for directed graphs only.");
    }

    ArrayList<LinkedList<V>> cycles = new ArrayList<>();
    int n = g.numVertices();
    if (n == 0) return cycles;

    // State: 0 = WHITE, 1 = GRAY, 2 = BLACK
    int[] state = new int[n];

    @SuppressWarnings("unchecked")
    V[] parent = (V[]) new Object[n];

    for (V vertex : g.vertices()) {
        if (state[g.key(vertex)] == 0) {
            detectCyclesDFS(g, vertex, state, parent, cycles);
        }
    }

    return cycles;
}

/**
 * DFS helper for cycle detection.
 */
private static <V, E> void detectCyclesDFS(Graph<V, E> g, V vertex,
                                           int[] state, V[] parent,
                                           ArrayList<LinkedList<V>> cycles) {
    int vKey = g.key(vertex);
    state[vKey] = 1; // GRAY - mark as being processed

    for (V neighbor : g.adjVertices(vertex)) {
        int nKey = g.key(neighbor);

        if (state[nKey] == 1) {
            // Back edge found - cycle detected
            // Reconstruct the cycle from vertex back to neighbor
            LinkedList<V> cycle = new LinkedList<>();
            Set<V> visited = new HashSet<>();
            V current = vertex;

            cycle.addFirst(current);
            visited.add(current);

            while (current != null && !current.equals(neighbor)) {
                int currentKey = g.key(current);
                current = parent[currentKey];

                if (current != null) {
                    if (visited.contains(current)) break;
                    cycle.addFirst(current);
                    visited.add(current);
                }
            }

            // Close the cycle
            cycle.add(vertex);
            cycles.add(cycle);

        } else if (state[nKey] == 0) {
            parent[nKey] = vertex;
            detectCyclesDFS(g, neighbor, state, parent, cycles);
        }
    }

    state[vKey] = 2; // BLACK - mark as fully processed
}
```