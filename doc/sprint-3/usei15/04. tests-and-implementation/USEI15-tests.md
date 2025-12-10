# USEI15 - Risk-Aware Shortest Path

## Tests

### Test 1: Test whole algorithm functionality

```java

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

```


## Implementation

### Class Algorithms.java

```java

public static <V, E> boolean bellmanFord(Graph<V, E> g, V vOrig,
                                             Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                             ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

        if (!g.validVertex(vOrig)) return false;

        int numVerts = g.numVertices();
        int sourceKey = g.key(vOrig);
        if (sourceKey == -1) return false;

        // initialize data structures
        E[] dist = (E[]) new Object[numVerts];
        int[] predecessors = new int[numVerts];
        Arrays.fill(predecessors, -1);

        paths.clear();
        dists.clear();

        // initialize return lists
        for (int i = 0; i < numVerts; i++) {
            dists.add(null);
            paths.add(null);
        }

        // the distance from the source to itself is zero
        dist[sourceKey] = zero;
        dists.set(sourceKey, zero);

        // relaxation phase
        relaxation(g, dist, sum, ce, predecessors);

        // check for negative cycles
        LinkedList<V> cycle = checkNegativeCycles(g, dist, sum, ce, predecessors);

        if (cycle != null) {
            // negative cycle detected
            paths.clear();
            dists.clear();
            paths.add(cycle); // store cycle in the first position
            return false; // failure due to negative cycle
        }

        // no cycles found - reconstruct paths
        reconstructPaths(g, sourceKey, dist, predecessors, paths, dists);

        return true; // success
    }

    /**
     * Performs the relaxation steps of Bellman-Ford algorithm (V - 1) times.
     */
    private static <V, E> void relaxation(Graph<V, E> g, E[] dist, BinaryOperator<E> sum,
                                          Comparator<E> ce, int[] predecessors) {

        int numVerts = g.numVertices();

        // relax all edges V-1 times
        for (int i = 1; i < numVerts; i++) {
            boolean changed = false;

            for (Edge<V, E> edge : g.edges()) {
                int u = g.key(edge.getVOrigin());
                int v = g.key(edge.getVTarget());

                E edgeWeight = edge.getWeight();

                // only relax if source vertex has been reached
                if (dist[u] != null) {
                    E newDist = sum.apply(dist[u], edgeWeight);

                    // if a shorter path has been found, update distance
                    if (dist[v] == null || ce.compare(newDist, dist[v]) < 0) {
                        dist[v] = newDist;
                        predecessors[v] = u;
                        changed = true;
                    }
                }
            }

            // if no distance changed, terminate
            if (!changed) break;
        }
    }

    /**
     * Check for negative cycles in the graph.
     * Performs one more iteration to detect if any distance can still be improved.
     * ONLY checks edges that are reachable from the source (dist[u] != null).
     *
     * @return The cycle as a LinkedList if found, otherwise null
     */
    private static <V, E> LinkedList<V> checkNegativeCycles(Graph<V, E> g, E[] dist, BinaryOperator<E> sum,
                                                            Comparator<E> ce, int[] predecessors) {

        // if any distance improves on reachable edges, there's a negative cycle
        for (Edge<V, E> edge : g.edges()) {
            int u = g.key(edge.getVOrigin());
            int v = g.key(edge.getVTarget());

            E edgeWeight = edge.getWeight();

            // only check edges where source vertex is reachable (dist[u] != null)
            if (dist[u] != null) {
                E newDist = sum.apply(dist[u], edgeWeight);

                // If we can still improve the distance, there's a negative cycle
                if (dist[v] == null || ce.compare(newDist, dist[v]) < 0) {
                    // negative cycle detected in the reachable part of the graph
                    return buildCyclePath(g, v, predecessors);
                }
            }
        }
        return null; // no negative cycle in reachable part
    }

    /**
     * Reconstructs the specific negative cycle.
     */
    private static <V, E> LinkedList<V> buildCyclePath(Graph<V, E> g, int startKey, int[] predecessors) {
        int numVerts = g.numVertices();

        int current = startKey;
        for (int i = 0; i < numVerts; i++) {
            if (predecessors[current] == -1) break;
            current = predecessors[current];
        }

        int cycleStart = current; // mark the starting point
        LinkedList<V> cycle = new LinkedList<>();

        // build the cycle by following predecessors until we return to start
        do {
            cycle.addFirst(g.vertex(current));
            current = predecessors[current];
        } while (current != cycleStart && current != -1);

        // add the starting vertex again to close the cycle
        if (current == cycleStart) {
            cycle.addFirst(g.vertex(cycleStart));
        }

        return cycle;
    }

    /**
     * Reconstructs all shortest paths from source to all reachable destinations.
     */
    private static <V, E> void reconstructPaths(Graph<V, E> g, int sourceKey, E[] dist, int[] predecessors,
                                                ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

        int numVerts = g.numVertices();

        for (int i = 0; i < numVerts; i++) {
            dists.set(i, dist[i]);

            if (dist[i] != null) {
                LinkedList<V> path = new LinkedList<>();
                int current = i;

                // reconstruct the path from destination to source
                while (current != -1) {
                    path.addFirst(g.vertex(current));
                    if (current == sourceKey) break;
                    current = predecessors[current];
                }
                paths.set(i, path);
            }
        }
    }

    /**
     * Computes the shortest path between two vertices
     * with support for negative edge weights and negative cycle detection.
     *
     * @param g         Graph instance
     * @param vOrig     Origin vertex
     * @param vDest     Destination vertex
     * @param ce        Comparator between elements of type E
     * @param sum       Sum two elements of type E
     * @param zero      Neutral element of the sum
     * @param shortPath Returns the shortest path (or cycle if detected)
     * @return          The shortest distance, or null if the negative cycle exists or the path doesn't exist
     */
    public static <V, E> E shortestPathBellmanFord(Graph<V, E> g, V vOrig, V vDest,
                                                   Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                                   LinkedList<V> shortPath) {

        shortPath.clear();

        ArrayList<LinkedList<V>> paths = new ArrayList<>();
        ArrayList<E> dists = new ArrayList<>();

        boolean success = bellmanFord(g, vOrig, ce, sum, zero, paths, dists);

        if (!success) {
            // negative cycle detected - cycle is in paths.get(0)
            if (!paths.isEmpty() && paths.get(0) != null) {
                shortPath.addAll(paths.get(0));
            }
            return null; // no valid shortest path due to the negative cycle
        }

        int destKey = g.key(vDest);
        if (destKey == -1 || paths.get(destKey) == null) {
            return null; // destination unreachable
        }

        shortPath.addAll(paths.get(destKey));
        return dists.get(destKey);
    }

```