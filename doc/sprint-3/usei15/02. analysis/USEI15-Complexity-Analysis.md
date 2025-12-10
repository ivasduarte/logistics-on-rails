# USEI15 - Risk-Aware Shortest Path

### Objective

Calculate the shortest path between two stations, using the Bellman-Ford.

The Bellman-Ford algorithm is used for its robustness, but to guarantee a finite result and ignore negative cycles in
the calculation phase, the Risk-Aware implementation uses the absolute value of the edge cost in the calculation graph.

---

### Complexity Analysis

The complexity is dominated by the Bellman-Ford Relaxation phase, which depends on the number of vertices ($V$) and
the number of edges ($E$).

Dominated by three-phases: pre-processing, relaxation and reconstruction.

**Phase 1: Pre-Processing**

This phase occurs in `NetworkController.shortestPathBellmanFord()` and aims to create an auxiliary graph with
non-negative weights (Absolute Cost) for the execution of the Bellman-Ford scheme.

```java
public Double shortestPathBellmanFord(Station vOrig, Station vDest, ...) {
    // Create a temporary graph with Double weights (ABSOLUTE VALUE of cost)
    Graph<Station, Double> costGraph = new MapGraph<>(true);

    // Copy all vertices
    for (Station station : graph.vertices()) {
        costGraph.addVertex(station);
    }

    // Copy all edges, using ABSOLUTE VALUE of cost to avoid negative cycles
    for (Edge<Station, ConnectionData> edge : graph.edges()) {
        // ...
        Double cost = Math.abs(edge.getWeight().getCost());
        costGraph.addEdge(origin, target, cost);
    }
    // ... call Algorithms.shortestPathBellmanFord
}
```

**Analysis:**

- Iterate and copy all vertices ($In$): $\mathcal{O}(V)$
- Iterating over all edges ( _AND_ Calculation of the absolute value (Abs), and addition to the new
  graph: $\mathcal{O}(E)$

**Analysis of this phase: $\mathcal{O}(V + E)$**

---

**Phase 2: Bellman-Ford Execution Phase (Relaxation)**

This phase is executed in `Algorithms.bellmanFord()`, which in turn calls `relaxation()`.

```java
private static <V, E> void relaxation(Graph<V, E> g, E[] dist, ...) {
    int numVerts = g.numVertices();

    // relax all edges V-1 times
    for (int i = 1; i < numVerts; i++) {
        boolean changed = false;
        // loop for all edges in graph
        for (Edge<V, E> edge : g.edges()) {
            // ... relax operation
        }
        if (!changed) break;
    }
}
```

**Analysis:**

- External Cycle: Repeats the relaxation. $V - 1$sometimes, where $V$. It is the number of stations.
- Internal Loop: In each iteration, it iterates over all the... $E$ edges. The relaxation operation is $\mathcal{O}(1)$.

$$\text{Relaxation} = \sum_{i=1}^{V-1} \mathcal{O}(E) = \mathcal{O}(V \cdot E)$$

**Analysis of this phase: $\mathcal{O}(V \cdot E)$**


---

**Phase 3: Verification and Reconstruction**

These phases occur `Algorithms.bellmanFord()` after relaxation.


| Operation                          | Code / Method                       | Complexity                                                                                                                                                           |  
|:-----------------------------------|:------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------|  
| **Cycle Verification**             | `checkNegativeCycles(...)`          | $O(E)$                                                                                                                                                               |  
| **Reconstruction Path**            | `reconstructPaths(...)`             | $About (V²)$                                                                                                                                                         |  
| **Extraction of the final result** | `shortestPathBellmanFord(...)`      | $O(1)$                                                                                                                                                               |  


**Combined complexity of these paths: $About (V²)$**


---

### Final Metrics


Global temporal complexity is determined by the dominant step:

$$\mathcal{O}(\text{Pre-processing}) + \mathcal{O}(\text{Relaxation}) + \mathcal{O}(\text{Reconstruction})$$

$$\mathcal{O}(V + E) + \mathcal{O}(V \cdot E) + \mathcal{O}(V^2)$$

Since $\mathcal{O}(V \cdot E)$, if the remaining terms dominate, the final complexity is:

**Temporal Complexity: $\mathcal{O}(V \cdot E)$**