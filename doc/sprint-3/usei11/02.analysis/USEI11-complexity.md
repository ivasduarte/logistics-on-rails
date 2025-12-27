# USEI11 - Directed Line Upgrade Plan

### Objective

Compute a valid ordering of railway stations for upgrade scheduling based on directed track dependencies. If circular dependencies (cycles) exist, identify all stations and links involved in those cycles.

---

### Complexity Analysis

The analysis is divided into two main algorithms: **Topological Sort** (for DAG ordering) and **Cycle Detection** (when topological sort fails).

---

## Algorithm 1: Topological Sort

**Implementation:** `Algorithms.topologicalSort()`

This algorithm attempts to order all vertices in a directed acyclic graph (DAG) such that for every directed edge A→B, vertex A comes before vertex B in the ordering.

**Approach:** Depth-First Search (DFS) with vertex coloring
- **WHITE (0):** Vertex not yet visited
- **GRAY (1):** Vertex currently being processed
- **BLACK (2):** Vertex fully processed
```java
public static <V, E> LinkedList<V> topologicalSort(Graph<V, E> g) {
    int[] state = new int[n];  // WHITE, GRAY, BLACK states
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

private static <V, E> void topologicalSortDFS(Graph<V, E> g, V vertex,
                                              int[] state, LinkedList<V> result,
                                              boolean[] hasCycle) {
    state[vKey] = 1; // GRAY - mark as being processed

    for (V neighbor : g.adjVertices(vertex)) {
        if (state[nKey] == 1) {
            hasCycle[0] = true; // Back edge -> cycle found
            return;
        }
        if (state[nKey] == 0) {
            topologicalSortDFS(g, neighbor, state, result, hasCycle);
        }
    }

    state[vKey] = 2; // BLACK - mark as processed
    result.addFirst(vertex); // Add to front (reverse finish order)
}
```

### Phase-by-Phase Analysis

**Phase 1: Initialization**
```java
int[] state = new int[n];  // O(V)
```
- Allocate state array for all vertices
- **Complexity:** $\mathcal{O}(V)$

**Phase 2: DFS Traversal**
```java
for (V vertex : g.vertices()) {              // O(V)
    if (state[g.key(vertex)] == 0) {
        topologicalSortDFS(...);             // Each vertex visited once
    }
}
```

**Inside DFS:**
```java
for (V neighbor : g.adjVertices(vertex)) {   // Total O(E) across all calls
    if (state[nKey] == 1) {
        hasCycle[0] = true;                  // O(1)
    }
    if (state[nKey] == 0) {
        topologicalSortDFS(...);             // Recursive call
    }
}
```

**Analysis:**
- Each vertex is visited **exactly once** (WHITE → GRAY → BLACK)
- Each edge is examined **exactly once** during adjacency list traversal
- State checks are $\mathcal{O}(1)$ operations

**Total Complexity:**
- Vertex processing: $\mathcal{O}(V)$
- Edge processing: $\mathcal{O}(E)$

$$\text{Topological Sort} = \mathcal{O}(V + E)$$

---

## Algorithm 2: Cycle Detection

**Implementation:** `Algorithms.detectCycles()`

This algorithm finds all cycles in a directed graph using DFS with back-edge detection.
```java
public static <V, E> ArrayList<LinkedList<V>> detectCycles(Graph<V, E> g) {
    ArrayList<LinkedList<V>> cycles = new ArrayList<>();
    int[] state = new int[n];  // WHITE, GRAY, BLACK
    V[] parent = (V[]) new Object[n];

    for (V vertex : g.vertices()) {
        if (state[g.key(vertex)] == 0) {
            detectCyclesDFS(g, vertex, state, parent, cycles);
        }
    }
    return cycles;
}

private static <V, E> void detectCyclesDFS(Graph<V, E> g, V vertex,
                                           int[] state, V[] parent,
                                           ArrayList<LinkedList<V>> cycles) {
    state[vKey] = 1; // GRAY

    for (V neighbor : g.adjVertices(vertex)) {
        if (state[nKey] == 1) {
            // Back edge found - reconstruct cycle
            LinkedList<V> cycle = buildCyclePath(g, vertex, neighbor, parent);
            cycles.add(cycle);
        } else if (state[nKey] == 0) {
            parent[nKey] = vertex;
            detectCyclesDFS(g, neighbor, state, parent, cycles);
        }
    }

    state[vKey] = 2; // BLACK
}
```

### Phase-by-Phase Analysis

**Phase 1: Initialization**
```java
int[] state = new int[n];     // O(V)
V[] parent = new Object[n];    // O(V)
```
- **Complexity:** $\mathcal{O}(V)$

**Phase 2: DFS with Cycle Detection**
```java
for (V vertex : g.vertices()) {              // O(V)
    if (state[g.key(vertex)] == 0) {
        detectCyclesDFS(...);                // Visit each vertex once
    }
}
```

**Inside DFS:**
```java
for (V neighbor : g.adjVertices(vertex)) {   // Total O(E)
    if (state[nKey] == 1) {
        buildCyclePath(...);                 // O(V) worst case
    }
}
```

**Phase 3: Cycle Reconstruction**

When a back edge is detected, we reconstruct the cycle:
```java
private static <V, E> LinkedList<V> buildCyclePath(Graph<V, E> g, 
                                                    int startKey, 
                                                    int[] predecessors) {
    LinkedList<V> cycle = new LinkedList<>();
    int current = startKey;
    
    // Follow parent pointers to build cycle
    do {
        cycle.addFirst(g.vertex(current));
        current = predecessors[current];
    } while (current != cycleStart);
    
    cycle.addFirst(g.vertex(cycleStart)); // Close the cycle
    return cycle;
}
```

**Analysis:**
- In the **worst case**, a cycle can include all vertices: $\mathcal{O}(V)$
- In practice, cycles are typically much smaller
- Maximum number of cycles detected: $\mathcal{O}(E)$ (one per back edge)

**Total Complexity:**
- DFS traversal: $\mathcal{O}(V + E)$
- Cycle reconstruction (worst case): $\mathcal{O}(V \times \text{number of cycles})$
- In the worst case with many cycles: $\mathcal{O}(V \times E)$

$$\text{Cycle Detection} = \mathcal{O}(V + E) \text{ to } \mathcal{O}(V \times E)$$

**Practical Complexity:** $\mathcal{O}(V + E)$ for sparse graphs with few cycles

---

## USEI11 Complete Flow

### Scenario 1: Success (No Cycles - DAG)
```java
public UpgradeOrderResult computeUpgradeOrder() {
    // Attempt topological sort
    LinkedList<Station> order = Algorithms.topologicalSort(graph);  // O(V + E)
    
    if (order != null) {
        return UpgradeOrderResult.success(order);  // O(1)
    }
}
```

**Complexity:** $\mathcal{O}(V + E)$

---

### Scenario 2: Failure (Cycles Detected)
```java
public UpgradeOrderResult computeUpgradeOrder() {
    // Topological sort fails
    LinkedList<Station> order = Algorithms.topologicalSort(graph);  // O(V + E)
    
    if (order == null) {
        // Detect cycles
        ArrayList<LinkedList<Station>> cycles = 
            Algorithms.detectCycles(graph);  // O(V + E) to O(V × E)
        
        // Extract unique stations
        Set<Station> stationsInCycles = new HashSet<>();  // O(1)
        for (LinkedList<Station> cycle : cycles) {         // O(total vertices in cycles)
            stationsInCycles.addAll(cycle);
        }
        
        return UpgradeOrderResult.withCycles(
            cycles, new ArrayList<>(stationsInCycles)
        );
    }
}
```

**Complexity:** $\mathcal{O}(V + E) + \mathcal{O}(V + E) = \mathcal{O}(V + E)$

---

## Final Complexity Summary

| Operation | Best Case | Worst Case | Typical Case |
|:----------|:----------|:-----------|:-------------|
| **Topological Sort** | $\mathcal{O}(V + E)$ | $\mathcal{O}(V + E)$ | $\mathcal{O}(V + E)$ |
| **Cycle Detection** | $\mathcal{O}(V + E)$ | $\mathcal{O}(V \times E)$ | $\mathcal{O}(V + E)$ |
| **Extract Stations** | $\mathcal{O}(V)$ | $\mathcal{O}(V)$ | $\mathcal{O}(V)$ |

### Overall USEI11 Complexity

**Success Case (DAG):**
$$\mathcal{O}(V + E)$$

**Failure Case (Cycles):**
$$\mathcal{O}(V + E) \text{ (topological sort)} + \mathcal{O}(V + E) \text{ (cycle detection)} = \mathcal{O}(V + E)$$
