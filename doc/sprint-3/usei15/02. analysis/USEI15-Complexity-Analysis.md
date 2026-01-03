# USEI15 - Complexity Analysis: Bellman-Ford

### Index
- [Objective](#objective)
- [Complexity Analysis](#complexity-analysis)
- [Summary of Metrics](#summary-of-metrics)
- [Conclusion](#conclusion)

---

## Objective

The Bellman-Ford algorithm is used to find the shortest paths from a single source vertex to all other vertices in a 
graph that may contain negative weights. Unlike Dijkstra's algorithm, Bellman-Ford is able to detect the presence of 
negative weight cycles.

---

## Complexity Analysis

The algorithm's execution is divided into four main phases: 
1. [Initialization](#1-initialization)
2. [Edge Relaxation](#2-edge-relaxation-relaxation)
3. [Negative Cycle Detection]()
4. [Path Reconstruction]()


### 1. Initialization

At this stage, the auxiliary data structures (`dist` and `predecessors`) are prepared.

```java
// Inicialização de estruturas de dados
E[] dist = (E[]) new Object[numVerts];
int[] predecessors = new int[numVerts];
Arrays.fill(predecessors, -1);
// ...
dist[sourceKey] = zero;
```

- **Analysis:** The algorithm traverses the number of vertices.$|V|$to initialize the distance and predecessor arrays.
- **Complexity of this phase:** $O(V)$


### 2. Edge Relaxation (`relaxation`)

This is the central phase of the algorithm, where distances are progressively optimized.

```java
// Relaxar todas as arestas V-1 vezes
for (int i = 1; i < numVerts; i++) {
boolean changed = false;
    for (Edge<V, E> edge : g.edges()) {
        // ... lógica de relaxação ...
        if (dist[v] == null || ce.compare(newDist, dist[v]) < 0) {
dist[v] = newDist;
predecessors[v] = u;
changed = true;
        }
        }
        if (!changed) break; // Otimização se não houver alterações
}
```

- **Analysis:** The outer loop executes $|V| - 1$ times. Within each iteration, the algorithm traverses all edges. $|E|$of the graph. 
- **Cost:** The total work is the product produced by the number of iterations and the number of edges.
- **Complexity of this phase:** $O(V \times E)$


### 3. Negative Cycle Detection (`checkNegativeCycles`)

After the $|V| - 1$ in several iterations, the algorithm checks if it is still possible to reduce any distance.

```java
for (Edge<V, E> edge : g.edges()) {
    // ... se pudermos melhorar a distância, existe um ciclo negativo ...
    if (dist[v] == null || ce.compare(newDist, dist[v]) < 0) {
        return buildCyclePath(g, v, predecessors);
    }
}
```

- **Analysis:** Performs an additional pass through all edges. If a distance can be reduced, a negative cycle has been detected.
- **Complexity of this phase:** $O(E)$


### 4. Path Reconstruction (`reconstructPaths`)

If no negative cycles are found, the algorithm reconstructs the final paths.

- **Analysis:** For each of the $|V|$ vertices, the algorithm may have to backtrack in the array of predecessors to the 
origin (in the worst case, traversing $|V|$).
- **Complexity of this phase:** $O(V^2)$

---

## Summary of Metrics


| Metric                  | Complexity      | Notes                                                                     |  
|:------------------------|:----------------|:--------------------------------------------------------------------------|  
| Total Time (Worst Case) | $O(V \times E)$ | Dominated by relaxation iterations on the edges.                          |  
| Time (Dense Graph)      | $O(V^3)$        | In a dense graph,$E \approx V^2$ Therefore, the complexity becomes cubic. |  
| Auxiliary Space         | $O(V)$          | Necessary for storing the distances and predecessors of each vertex.      |
| Cycle Detection         | -               | It can identify and reconstruct negative weight cycles.                   |

---

## Conclusion

The Bellman-Ford algorithm implemented in `Algorithms` guarantees the correct solution for shortest paths in graphs 
with negative weights, with a time complexity of **$O(V \times E)$**. Although it is slower than Dijkstra ($O(E \log V)$,
its robustness against negative weights and its ability to detect cycles make it essential for complex transport or 
logistics networks.