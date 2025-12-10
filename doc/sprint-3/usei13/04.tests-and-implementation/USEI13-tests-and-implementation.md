# USEI13 - Rail Hub Centrality Analysis

## Tests

### Test 1: Test whole algorithm functionality

```java
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
 * Test of harmonicCentrality method, of class Algorithms.
 */
@Test
public void testHarmonicCentrality() {
    System.out.println("Test of Harmonic Centrality");

    // Scenario 1: Simple Linear Graph (A - B - C) with weight 1
    Graph<String, Integer> linearGraph = new MapGraph<>(false);
    linearGraph.addVertex("A");
    linearGraph.addVertex("B");
    linearGraph.addVertex("C");
    linearGraph.addEdge("A", "B", 1);
    linearGraph.addEdge("B", "C", 1);

    Map<String, Double> centrality = Algorithms.harmonicCentrality(
            linearGraph,
            Integer::compare,
            Integer::sum,
            0,
            Integer::doubleValue
    );

    // Expected calculations (Normalized by N-1 = 2):
    // B: dist(A)=1, dist(C)=1. Sum = 1/1 + 1/1 = 2. Norm = 2/2 = 1.0
    assertEquals(1.0, centrality.get("B"), 0.001, "B (center of line) should have centrality 1.0");

    // A: dist(B)=1, dist(C)=2. Sum = 1/1 + 1/2 = 1.5. Norm = 1.5/2 = 0.75
    assertEquals(0.75, centrality.get("A"), 0.001, "A should have centrality 0.75");
    assertEquals(0.75, centrality.get("C"), 0.001, "C should have centrality 0.75");

    // Scenario 2: Star Graph (Hub connected to A, B, C)
    Graph<String, Integer> starGraph = new MapGraph<>(false);
    starGraph.addVertex("Hub");
    starGraph.addVertex("A");
    starGraph.addVertex("B");
    starGraph.addVertex("C");
    starGraph.addEdge("Hub", "A", 1);
    starGraph.addEdge("Hub", "B", 1);
    starGraph.addEdge("Hub", "C", 1);

    centrality = Algorithms.harmonicCentrality(
            starGraph,
            Integer::compare,
            Integer::sum,
            0,
            Integer::doubleValue
    );

    // Hub (N=4, normalization factor = 3)
    // Hub reaches everyone with dist 1. Sum = 1+1+1 = 3. Norm = 3/3 = 1.0
    assertEquals(1.0, centrality.get("Hub"), 0.001, "Hub should have maximum centrality");

    // Leaves (A, B, C): dist(Hub)=1, dist(Others)=2. Sum = 1 + 0.5 + 0.5 = 2. Norm = 2/3
    double expectedSpoke = 2.0 / 3.0;
    assertEquals(expectedSpoke, centrality.get("A"), 0.001, "Star leaves should have centrality approx 0.66");

    // Scenario 3: Disconnected Graph
    Graph<String, Integer> disconnected = new MapGraph<>(false);
    disconnected.addVertex("A");
    disconnected.addVertex("B");
    disconnected.addVertex("Isolated");
    disconnected.addEdge("A", "B", 1);

    centrality = Algorithms.harmonicCentrality(
            disconnected,
            Integer::compare,
            Integer::sum,
            0,
            Integer::doubleValue
    );

    // Isolated Vertex: Sum = 0
    assertEquals(0.0, centrality.get("Isolated"), 0.001, "Isolated vertex should have 0 centrality");

    // A (N=3, norm=2): dist(B)=1. Sum=1. Norm=0.5
    assertEquals(0.5, centrality.get("A"), 0.001, "A should have 0.5 (only one connection in a 3-node graph)");

    // Scenario 4: Directed Graph with Double weights
    // A -> B (2.0) -> C (4.0)
    // Path A->C cost is 6.0
    Graph<String, Double> weightedGraph = new MapGraph<>(true);
    weightedGraph.addVertex("A");
    weightedGraph.addVertex("B");
    weightedGraph.addVertex("C");
    weightedGraph.addEdge("A", "B", 2.0);
    weightedGraph.addEdge("B", "C", 4.0);

    Map<String, Double> weightedCentrality = Algorithms.harmonicCentrality(
            weightedGraph,
            Double::compare,
            Double::sum,
            0.0,
            Double::doubleValue
    );

    // A (N=3, norm=2):
    // A->B (2.0) -> inv = 0.5
    // A->C (6.0) -> inv = 1/6 (~0.166)
    // Sum = 0.5 + 0.1666... = 0.666...
    // Norm = 0.666... / 2 = 0.333...
    double expectedA = (0.5 + (1.0/6.0)) / 2.0;
    assertEquals(expectedA, weightedCentrality.get("A"), 0.001, "Verification of calculation with decimal weights");

    // C reaches no one
    assertEquals(0.0, weightedCentrality.get("C"), 0.001, "C is a sink, should have 0 centrality");
}

/**
 * Test of calculateStrength method, of class Algorithms.
 */
@Test
public void testCalculateStrength() {
    System.out.println("Test of Calculate Strength");

    // Scenario 1: Simple Undirected Graph with Integer weights
    // Topology: A --(1)-- B --(2)-- C
    Graph<String, Integer> undirectedGraph = new MapGraph<>(false);
    undirectedGraph.addVertex("A");
    undirectedGraph.addVertex("B");
    undirectedGraph.addVertex("C");
    undirectedGraph.addEdge("A", "B", 1);
    undirectedGraph.addEdge("B", "C", 2);

    Map<String, Double> strength = Algorithms.calculateStrength(
            undirectedGraph,
            Integer::doubleValue
    );

    // A is connected to B (weight 1) -> Strength should be 1.0
    assertEquals(1.0, strength.get("A"), 0.001, "A strength should be 1.0");

    // B is connected to A (1) and C (2) -> Strength should be 1 + 2 = 3.0
    assertEquals(3.0, strength.get("B"), 0.001, "B strength should be 3.0 (sum of incident edge weights)");

    // C is connected to B (weight 2) -> Strength should be 2.0
    assertEquals(2.0, strength.get("C"), 0.001, "C strength should be 2.0");


    // Scenario 2: Directed Graph with Double weights (Out-Strength)
    // Topology: Source --(10.5)--> Middle --(2.5)--> Sink
    //           Source --(5.0)--> Sink
    Graph<String, Double> directedGraph = new MapGraph<>(true);
    directedGraph.addVertex("Source");
    directedGraph.addVertex("Middle");
    directedGraph.addVertex("Sink");
    directedGraph.addEdge("Source", "Middle", 10.5);
    directedGraph.addEdge("Source", "Sink", 5.0);
    directedGraph.addEdge("Middle", "Sink", 2.5);

    Map<String, Double> dirStrength = Algorithms.calculateStrength(
            directedGraph,
            Double::doubleValue
    );

    // Source has two outgoing edges: 10.5 + 5.0 = 15.5
    assertEquals(15.5, dirStrength.get("Source"), 0.001, "Source strength should be sum of outgoing weights (15.5)");

    // Middle has one outgoing edge: 2.5
    assertEquals(2.5, dirStrength.get("Middle"), 0.001, "Middle strength should be 2.5");

    // Sink has no outgoing edges -> Strength 0.0
    assertEquals(0.0, dirStrength.get("Sink"), 0.001, "Sink strength should be 0.0 (no outgoing edges)");


    // Scenario 3: Isolated Vertex
    Graph<String, Integer> isolatedGraph = new MapGraph<>(false);
    isolatedGraph.addVertex("Lonely");

    Map<String, Double> isoStrength = Algorithms.calculateStrength(
            isolatedGraph,
            Integer::doubleValue
    );

    assertEquals(0.0, isoStrength.get("Lonely"), 0.001, "Isolated vertex strength should be 0.0");


    // Scenario 4: Integration with existing dataset (completeMap)
    // Checking 'Porto': Connected to Aveiro(75), Braga(60), Vila Real(100)
    // Total expected: 75 + 60 + 100 = 235
    Map<String, Double> mapStrength = Algorithms.calculateStrength(
            completeMap,
            Integer::doubleValue
    );

    assertNotNull(mapStrength.get("Porto"), "Porto should exist in strength map");
    assertEquals(235.0, mapStrength.get("Porto"), 0.001, "Porto strength should be 235 (sum of 75+60+100)");

    // Checking a leaf node 'Faro': Connected only to Lisboa(280)
    assertEquals(280.0, mapStrength.get("Faro"), 0.001, "Faro strength should be 280");
}
```


## Implementation

### Class Algorithms.java

```java
/**
 * Computes shortest-path distance from a source vertex to all reachable
 * vertices of a graph g with non-negative edge weights
 * This implementation uses Dijkstra's algorithm
 *
 * @param g        Graph instance
 * @param vOrig    Vertex that will be the source of the path
 * @param visited  set of previously visited vertices
 * @param pathKeys minimum path vertices keys
 * @param dist     minimum distances
 */
private static <V, E> void shortestPathDijkstra(Graph<V, E> g, V vOrig,
                                                Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                                boolean[] visited, V[] pathKeys, E[] dist) {

    int vOriginI = g.key(vOrig); // get origin index

    if (vOriginI < 0) {
        return; // vertex doesn't exist
    }

    // initialize distances and pathKeys to null
    for (int i = 0; i < g.numVertices(); i++) {
        dist[i] = null;
        pathKeys[i] = null;
        visited[i] = false;
    }

    dist[vOriginI] = zero; // distance from origin to itself is zero
    pathKeys[vOriginI] = vOrig;

    // get vertex with minimum distance
    PriorityQueue<V> pq = new PriorityQueue<>((v1,v2) -> {
        int i1 = g.key(v1);
        int i2 = g.key(v2);

        E dist1 = dist[i1];
        E dist2 = dist[i2];

        // handle null distances
        if (dist1 == null && dist2 == null) return 0;
        if (dist1 == null) return 1; // v1 is farther
        if (dist2 == null) return -1; // v2 is farther

        // compare vertices by their distance
        return ce.compare(dist[i1], dist[i2]);
    });

    pq.offer(vOrig); // add origin to queue

    // process vertices
    while(!pq.isEmpty()) {
        V v = pq.poll();
        int vI = g.key(v);

        // if visited skip
        if (visited[vI]) continue;

        // mark as visited
        visited[vI] = true;

        // if distance is null stop
        if (dist[vI] == null) break;

        // relax all adjacent edges
        for (V vAdj : g.adjVertices(v)) {
            int vAdjI = g.key(vAdj);

            if (!visited[vAdjI]){
                Edge<V, E> e = g.edge(v, vAdj);
                E eWeight = e.getWeight();

                // calculate new distance
                E newDist = sum.apply(dist[vI], eWeight);

                if (dist[vAdjI] == null || ce.compare(newDist, dist[vAdjI]) < 0) {
                    dist[vAdjI] = newDist;
                    pathKeys[vAdjI] = v;
                    pq.offer(vAdj); // add to queue for processing
                }
            }
        }
    }
}


/**
 * Shortest-path between two vertices
 *
 * @param g         graph
 * @param vOrig     origin vertex
 * @param vDest     destination vertex
 * @param ce        comparator between elements of type E
 * @param sum       sum two elements of type E
 * @param zero      neutral element of the sum in elements of type E
 * @param shortPath returns the vertices which make the shortest path
 * @return if vertices exist in the graph and are connected, true, false otherwise
 */
public static <V, E> E shortestPath(Graph<V, E> g, V vOrig, V vDest,
                                    Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                    LinkedList<V> shortPath) {

    // return if origin and/or destination isn't valid'
    if (!g.validVertex(vOrig) || !g.validVertex(vDest)) return null;

    shortPath.clear(); // clear path list

    if (vOrig.equals(vDest)) {
        shortPath.add(vOrig);
        return zero;
    }

    int numV = g.numVertices();
    boolean[] visited = new boolean[numV];

    @SuppressWarnings("unchecked")
    V[] pathKeys = (V[]) new Object[numV];

    @SuppressWarnings("unchecked")
    E[] dist = (E[]) new Object[numV];

    // perform Dijkstra's algorithm
    shortestPathDijkstra(g, vOrig, ce, sum, zero, visited, pathKeys, dist);

    // extract path from pathKeys
    getPath(g, vOrig, vDest, pathKeys, shortPath);

    // extract shortest distance to destination
    int vDestI = g.key(vDest);

    // if path doesn't exist return null
    if (shortPath.isEmpty()) return null;

    // return shortest distance to destination
    return dist[vDestI];
}

/** Shortest-path between a vertex and all other vertices
 *
 * @param g graph
 * @param vOrig start vertex
 * @param ce comparator between elements of type E
 * @param sum sum two elements of type E
 * @param zero neutral element of the sum in elements of type E
 * @param paths returns all the minimum paths
 * @param dists returns the corresponding minimum distances
 * @return if vOrig exists in the graph true, false otherwise
 */
public static <V, E> boolean shortestPaths(Graph<V, E> g, V vOrig,
                                           Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                           ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

    // return false if origin isn't valid
    if (!g.validVertex(vOrig)) return false;

    int numV = g.numVertices();

    // initialize auxiliary structures for dijkstra
    boolean[] visited = new boolean[numV];

    @SuppressWarnings("unchecked")
    V[] pathKeys = (V[])  new Object[numV];

    @SuppressWarnings("unchecked")
    E[] dist = (E[])  new Object[numV];

    // clear and fill return lists with null
    paths.clear();
    dists.clear();

    for (int i = 0; i < numV; i++) {
        paths.add(null);
        dists.add(null);
    }

    // execute dijkstra
    shortestPathDijkstra(g, vOrig, ce, sum, zero, visited, pathKeys, dist);

    for (V vDest : g.vertices()) {
        int vDestI = g.key(vDest);

        // store distance
        dists.set(vDestI, dist[vDestI]);

        // skip if null distance
        if (dist[vDestI] == null) continue;

        // get and store path
        LinkedList<V> shortPath = new LinkedList<>();
        getPath(g, vOrig, vDest, pathKeys, shortPath);
        paths.set(vDestI, shortPath);
    }

    return true;
}

/**
 * Computes the betweenness centrality for each vertex in the given graph.
 * Betweenness centrality is a measure of the number of shortest paths
 * passing through a vertex. This implementation accumulates centrality
 * scores by iterating through all vertices and considering them as potential
 * sources of shortest paths.
 *
 * @param g   The graph for which betweenness centrality is to be calculated.
 *            It should contain both vertices and edges.
 * @param ce  A comparator for comparing edge weights, used to determine the
 *            shortest paths in the graph.
 * @param sum A binary operator that specifies how to combine the weights
 *            of multiple edges (e.g., addition for summing the weights along a path).
 * @param zero The zero-weight value, representing the absence of weight.
 *             Typically, it is the additive identity for the type of edge weights being used.
 * @return A map that associates each vertex with its corresponding betweenness
 *         centrality value. The map keys are the vertices of the graph, and
 *         the values are the computed centrality scores.
 */
public static <V, E> Map<V, Double> betweennessCentrality(Graph<V, E> g,
                                                          Comparator<E> ce,
                                                          BinaryOperator<E> sum,
                                                          E zero) {

    Map<V, Double> centrality = new HashMap<>();

    // initialize centrality to 0 for all vertices
    for (V v : g.vertices()) {
        centrality.put(v, 0.0);
    }

    // run from each vertex as a source
    for (V source : g.vertices()) {

        // execute shortestPaths to get all shortest paths from source
        ArrayList<LinkedList<V>> paths = new ArrayList<>();
        ArrayList<E> dists = new ArrayList<>();

        shortestPaths(g, source, ce, sum, zero, paths, dists);

        // count how many shortest paths pass through each vertex
        Map<V, Integer> pathCount = new HashMap<>();
        for (V v : g.vertices()) {
            pathCount.put(v, 0);
        }

        // for each destination
        for (int i = 0; i < paths.size(); i++) {
            LinkedList<V> path = paths.get(i);

            if (path != null && path.size() > 2) {
                // count intermediate vertices (exclude source and destination)
                for (int j = 1; j < path.size() - 1; j++) {
                    V intermediate = path.get(j);
                    pathCount.put(intermediate, pathCount.get(intermediate) + 1);
                }
            }
        }

        // add to centrality scores
        for (Map.Entry<V, Integer> entry : pathCount.entrySet()) {
            V vertex = entry.getKey();
            if (!vertex.equals(source)) {
                centrality.put(vertex, centrality.get(vertex) + entry.getValue());
            }
        }
    }

    // for undirected graphs, divide by 2 (each edge counted twice)
    if (!g.isDirected()) {
        for (V v : g.vertices()) {
            centrality.put(v, centrality.get(v) / 2.0);
        }
    }

    return centrality;
}

/**
 * Finds and returns the most central vertex in the provided graph
 * based on betweenness centrality scores.
 *
 * @param <V> the type representing the vertices of the graph
 * @param <E> the type representing the edges of the graph
 * @param g the graph in which to find the most central vertex
 * @param ce a comparator to compare the edge weights
 * @param sum a binary operator to sum edge weights
 * @param zero the identity element for the sum operator, representing zero edge weight
 * @return the vertex with the highest betweenness centrality score, or null if the graph has no vertices
 */
public static <V, E> V mostCentralVertex(Graph<V, E> g,
                                         Comparator<E> ce,
                                         BinaryOperator<E> sum,
                                         E zero) {

    Map<V, Double> centrality = betweennessCentrality(g, ce, sum, zero);

    V mostCentral = null;
    double maxCentrality = Double.NEGATIVE_INFINITY;

    for (Map.Entry<V, Double> entry : centrality.entrySet()) {
        if (entry.getValue() > maxCentrality) {
            maxCentrality = entry.getValue();
            mostCentral = entry.getKey();
        }
    }

    return mostCentral;
}

/**
 * Normalizes the centrality values of vertices in a graph.
 * The normalization depends on the number of vertices in the graph and
 * whether the graph is directed or undirected.
 *
 * @param centrality a map containing centrality values of vertices where keys are vertices
 *                   and values are their corresponding centrality values
 * @param numVertices the total number of vertices in the graph
 * @param isDirected a boolean indicating whether the graph is directed; true for directed graphs,
 *                   false for undirected graphs
 * @return a map containing the normalized centrality values where keys represent vertices and
 *         values are their corresponding normalized centrality values
 */
public static <V> Map<V, Double> normalizeCentrality(Map<V, Double> centrality,
                                                     int numVertices,
                                                     boolean isDirected) {

    Map<V, Double> normalized = new HashMap<>();

    if (numVertices <= 2) {
        for (V v : centrality.keySet()) {
            normalized.put(v, 0.0);
        }
        return normalized;
    }

    // maximum possible centrality
    double normFactor = isDirected ?
            (numVertices - 1) * (numVertices - 2) :
            (numVertices - 1) * (numVertices - 2) / 2.0;

    for (Map.Entry<V, Double> entry : centrality.entrySet()) {
        normalized.put(entry.getKey(), entry.getValue() / normFactor);
    }

    return normalized;
}

/**
 * Calculates the harmonic centrality of each vertex in a given graph.
 * Harmonic centrality is a measure that reflects the influence of a vertex
 * in a graph, based on the reciprocal of the shortest path distances to
 * all other vertices. The centrality values are normalized by the total
 * number of vertices in the graph.
 *
 * @param <V> the vertex type of the graph
 * @param <E> the edge type of the graph
 * @param g the graph for which harmonic centrality is to be computed
 * @param ce a comparator to compare edge weights
 * @param sum a binary operator used to sum edge weights
 * @param zero the zero-equivalent value for edge weights
 * @param convertToDouble a function that converts edge weights to their double representation
 * @return a map where keys are vertices and values are their computed harmonic centrality scores
 */
public static <V, E> Map<V, Double> harmonicCentrality(Graph<V, E> g,
                                                       Comparator<E> ce,
                                                       BinaryOperator<E> sum,
                                                       E zero,
                                                       Function<E, Double> convertToDouble) {
    Map<V, Double> map = new HashMap<>();

    for (V vOrig : g.vertices()) {
        ArrayList<LinkedList<V>> paths = new ArrayList<>();
        ArrayList<E> dists = new ArrayList<>();

        // execute shortest paths to get all shortest paths from vOrig
        shortestPaths(g, vOrig, ce, sum, zero, paths, dists);

        double hCentrality = 0;

        for (E d : dists) {
            // calculate harmonic centrality only for non-zero distances
            if (d != null && ce.compare(d, zero) != 0) {
                double val = convertToDouble.apply(d);
                if (val != 0) {
                    hCentrality += 1.0 / val;
                }
            }
        }

        // normalize harmonic centrality by the number of vertices
        if (g.numVertices() > 1) {
            hCentrality /= (g.numVertices() - 1);
        }

        map.put(vOrig, hCentrality);
    }
    return map;
}

/**
 * Calculates the Strength of all vertices in the graph.
 * Strength is defined as the sum of the weights of the edges connected to a vertex.
 *
 * @param g               The graph instance
 * @param convertToDouble Function to convert the edge weight E to Double
 * @return Map containing the strength value for each vertex
 */
public static <V, E> Map<V, Double> calculateStrength(Graph<V, E> g, Function<E, Double> convertToDouble) {
    Map<V, Double> strengthMap = new HashMap<>();

    if (g == null) {
        return strengthMap;
    }

    for (V v : g.vertices()) {
        double vertexStrength = 0.0;

        // goes through all adjacent vertices to sum the weights of its edges
        for (V adj : g.adjVertices(v)) {
            Edge<V, E> edge = g.edge(v, adj);
            if (edge != null && edge.getWeight() != null) {
                vertexStrength += convertToDouble.apply(edge.getWeight());
            }
        }

        strengthMap.put(v, vertexStrength);
    }

    return strengthMap;
}
```