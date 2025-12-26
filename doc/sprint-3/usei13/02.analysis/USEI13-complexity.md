# USEI13 - Rail Hub Centrality Analysis

### Objective

Identify and rank the most important railway stations (hubs) in the network based on multiple centrality metrics: Betweenness Centrality, Harmonic Centrality, Strength, and Degree. The final Hub Score is calculated as a weighted combination of these normalized metrics.

---

### Complexity Analysis

The complexity is dominated by the calculation of centrality metrics, particularly Betweenness Centrality and Harmonic Centrality, both of which require computing shortest paths from multiple source vertices.

The analysis is divided into four main phases: metric calculation, normalization, ranking, and export.

**Phase 1: Betweenness Centrality Calculation**

This phase occurs in `Algorithms.betweennessCentrality()` and requires computing shortest paths from every vertex to all other vertices.

```java
public static <V, E> Map<V, Double> betweennessCentrality(Graph<V, E> g,
                                                          Comparator<E> ce,
                                                          BinaryOperator<E> sum,
                                                          E zero) {
    // Initialize centrality to 0 for all vertices
    for (V v : g.vertices()) {
        centrality.put(v, 0.0);
    }
    
    // Run from each vertex as a source
    for (V source : g.vertices()) {
        // Execute shortestPaths using Dijkstra
        shortestPaths(g, source, ce, sum, zero, paths, dists);
        
        // Count paths through each intermediate vertex
        for (LinkedList<V> path : paths) {
            if (path != null && path.size() > 2) {
                for (int j = 1; j < path.size() - 1; j++) {
                    // Increment centrality for intermediate vertices
                }
            }
        }
    }
}
```

**Analysis:**

- **Outer Loop**: Iterates over all $V$ vertices as sources
- **Inner Operation**: For each source, `shortestPaths()` calls Dijkstra's algorithm which has complexity $\mathcal{O}((V + E) \log V)$ using a priority queue
- **Path Processing**: For each of the $V$ destinations, examines paths of length up to $V$, giving $\mathcal{O}(V^2)$ in the worst case

$$\text{Betweenness} = V \times [\mathcal{O}((V + E) \log V) + \mathcal{O}(V^2)]$$

Since $\mathcal{O}((V + E) \log V)$ dominates for sparse graphs and $\mathcal{O}(V^2)$ for dense graphs:

**Analysis of this phase: $\mathcal{O}(V \times (V + E) \log V)$ for sparse graphs, $\mathcal{O}(V^3)$ for dense graphs**

---

**Phase 2: Harmonic Centrality Calculation**

This phase is executed in `Algorithms.harmonicCentrality()`, which also requires computing shortest paths from every vertex.

```java
public static <V, E> Map<V, Double> harmonicCentrality(Graph<V, E> g,
                                                       Comparator<E> ce,
                                                       BinaryOperator<E> sum,
                                                       E zero,
                                                       Function<E, Double> convertToDouble) {
    for (V vOrig : g.vertices()) {
        // Execute shortest paths from vOrig
        shortestPaths(g, vOrig, ce, sum, zero, paths, dists);
        
        double hCentrality = 0;
        for (E d : dists) {
            if (d != null && ce.compare(d, zero) != 0) {
                hCentrality += 1.0 / convertToDouble.apply(d);
            }
        }
        
        // Normalize by number of vertices
        hCentrality /= (g.numVertices() - 1);
    }
}
```

**Analysis:**

- **Outer Loop**: Iterates over all $V$ vertices
- **Dijkstra Execution**: $\mathcal{O}((V + E) \log V)$ per source vertex
- **Distance Processing**: $\mathcal{O}(V)$ to sum reciprocals of distances

$$\text{Harmonic} = V \times [\mathcal{O}((V + E) \log V) + \mathcal{O}(V)]$$

**Analysis of this phase: $\mathcal{O}(V \times (V + E) \log V)$**

---

**Phase 3: Strength and Degree Calculation**

These metrics are computed in `Algorithms.calculateStrength()` and directly in `HubService.calculateHubScores()`.

```java
public static <V, E> Map<V, Double> calculateStrength(Graph<V, E> g, 
                                                       Function<E, Double> convertToDouble) {
    for (V v : g.vertices()) {
        double vertexStrength = 0.0;
        
        // Sum weights of all adjacent edges
        for (V adj : g.adjVertices(v)) {
            Edge<V, E> edge = g.edge(v, adj);
            vertexStrength += convertToDouble.apply(edge.getWeight());
        }
        
        strengthMap.put(v, vertexStrength);
    }
}
```

**Analysis:**

- **Strength Calculation**: Iterates over all vertices and their adjacent edges: $\mathcal{O}(V + E)$
- **Degree Calculation**: Simply counts adjacent vertices for each vertex: $\mathcal{O}(V + E)$

**Analysis of this phase: $\mathcal{O}(V + E)$**

---

**Phase 4: Normalization and Hub Score Calculation**

This phase occurs in `HubService.calculateHubScores()` and `HubService.normalizeMap()`.

```java
private <V> Map<V, Double> normalizeMap(Map<V, Double> rawMap) {
    // Find maximum value
    double maxVal = 0.0;
    for (Double val : rawMap.values()) {
        if (val > maxVal) maxVal = val;
    }
    
    // Normalize by dividing by maximum
    for (Map.Entry<V, Double> entry : rawMap.entrySet()) {
        normalized.put(entry.getKey(), entry.getValue() / maxVal);
    }
}
```

**Analysis:**

- **Betweenness Normalization**: $\mathcal{O}(V)$ using formula-based normalization
- **Strength Normalization**: $\mathcal{O}(V)$ to find max and normalize
- **Hub Score Calculation**: $\mathcal{O}(V)$ to compute weighted combination

**Analysis of this phase: $\mathcal{O}(V)$**

---

**Phase 5: Ranking and Export**

These operations occur in `HubService.getTopHubs()` and `NetworkController.exportHubsToCSV()`.

| Operation                  | Method                        | Complexity     |
|:---------------------------|:------------------------------|:---------------|
| **Sorting by Hub Score**   | `getTopHubs(...)`            | $\mathcal{O}(V \log V)$ |
| **Extracting Top N**       | `list.subList(0, n)`         | $\mathcal{O}(1)$ |
| **CSV Export**             | `exportHubsToCSV(...)`       | $\mathcal{O}(N)$ |

**Combined complexity of this phase: $\mathcal{O}(V \log V)$**

---

### Final Metrics

The global temporal complexity is determined by the dominant steps:

$$\mathcal{O}(\text{Betweenness}) + \mathcal{O}(\text{Harmonic}) + \mathcal{O}(\text{Strength}) + \mathcal{O}(\text{Normalization}) + \mathcal{O}(\text{Ranking})$$

$$\mathcal{O}(V \times (V + E) \log V) + \mathcal{O}(V \times (V + E) \log V) + \mathcal{O}(V + E) + \mathcal{O}(V) + \mathcal{O}(V \log V)$$

Since the first two terms dominate:

$$2 \times \mathcal{O}(V \times (V + E) \log V) = \mathcal{O}(V \times (V + E) \log V)$$

**For sparse graphs** where $E = \mathcal{O}(V)$:
$$\mathcal{O}(V^2 \log V)$$

**For dense graphs** where $E = \mathcal{O}(V^2)$:
$$\mathcal{O}(V^3 \log V)$$

**Temporal Complexity: $\mathcal{O}(V \times (V + E) \log V)$**