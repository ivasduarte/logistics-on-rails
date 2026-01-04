# USEI11 - Directed Line Upgrade Plan

### Objective

Compute a valid ordering of railway stations for upgrade scheduling based on directed track dependencies. If circular dependencies (cycles) exist, identify all stations and links involved in those cycles.

**Implementation Requirement:** This implementation uses the existing **`DepthFirstSearch`** methods from `Algorithms.java` as the foundation.

---

### Complexity Analysis

The analysis is divided into two main algorithms: **Topological Sort using DFS** (for DAG ordering) and **Cycle Detection using DFS** (when topological sort fails).

Both algorithms use the **existing `DepthFirstSearch` method** as their foundation.

---

## Foundation: DepthFirstSearch Method

**Implementation:** `Algorithms.DepthFirstSearch()`

The existing DFS method performs depth-first traversal:
```java
public static  LinkedList DepthFirstSearch(Graph g, V vert) {
    LinkedList qdfs = new LinkedList<>();
    boolean[] visited = new boolean[g.numVertices()];
    DepthFirstSearch(g, vert, visited, qdfs);
    return qdfs;
}

private static  void DepthFirstSearch(Graph g, V vOrig, 
                                            boolean[] visited, LinkedList qdfs) {
    if (visited[vOrigKey]) return;
    
    qdfs.add(vOrig);
    visited[vOrigKey] = true;
    
    for (V vAdj : g.adjVertices(vOrig)) {
        DepthFirstSearch(g, vAdj, visited, qdfs);
    }
}
```

**Complexity:** $\mathcal{O}(V + E)$ for each connected component



---

## Algorithm 1: Topological Sort Using DFS

**Implementation:** `Algorithms.topologicalSort()`

Uses the existing `DepthFirstSearch` method to compute topological order.
```java
public static  LinkedList topologicalSort(Graph g) {
    // Phase 1: Check for cycles using DFS pattern
    if (hasCyclesUsingDFS(g)) {  // O(V + E)
        return null;
    }

    // Phase 2: Compute topological order using DepthFirstSearch
    return computeTopologicalOrderWithDFS(g);  // O(V + E)
}
```

### Phase-by-Phase Analysis

**Phase 1: Cycle Detection Using DFS Pattern**
```java
private static  boolean hasCyclesUsingDFS(Graph g) {
    int[] state = new int[n];  // O(V)
    
    for (V vertex : g.vertices()) {              // O(V)
        if (state[g.key(vertex)] == 0) {
            if (detectCycleViaDFS(g, vertex, state)) {  // DFS pattern
                return true;
            }
        }
    }
    return false;
}

private static  boolean detectCycleViaDFS(Graph g, V vertex, int[] state) {
    state[vKey] = 1;

    for (V neighbor : g.adjVertices(vertex)) {   // Same pattern as DepthFirstSearch
        if (state[nKey] == 1) {
            return true;  // Back edge -> cycle
        }
        if (state[nKey] == 0) {
            if (detectCycleViaDFS(g, neighbor, state)) {
                return true;
            }
        }
    }

    state[vKey] = 2; 
    return false;
}
```

**Analysis:**
- Initialization: $\mathcal{O}(V)$
- Each vertex visited once: $\mathcal{O}(V)$
- Each edge examined once: $\mathcal{O}(E)$

**Complexity:** $\mathcal{O}(V + E)$

---

**Phase 2: Topological Order Using DepthFirstSearch**
```java
private static  LinkedList computeTopologicalOrderWithDFS(Graph g) {
    boolean[] globalVisited = new boolean[n];  // O(V)
    LinkedList result = new LinkedList<>();

    for (V vertex : g.vertices()) {              // O(V)
        if (!globalVisited[g.key(vertex)]) {
            // Use existing DepthFirstSearch to get reachable vertices
            LinkedList dfsOrder = new LinkedList<>();
            DepthFirstSearch(g, vertex, globalVisited, dfsOrder);  // O(V + E) total
            
            // Add in reverse order for topological sort
            for (int i = dfsOrder.size() - 1; i >= 0; i--) {  // O(vertices in DFS)
                result.addFirst(dfsOrder.get(i));
            }
        }
    }
    return result;
}
```

**Analysis:**
- The **existing `DepthFirstSearch`** is called for each connected component
- Total across all calls: Each vertex visited once, each edge examined once
- Reversal and insertion: $\mathcal{O}(V)$

**Complexity:** $\mathcal{O}(V + E)$

---

**Combined Topological Sort:**
$$\text{Phase 1 (Cycle Detection)} + \text{Phase 2 (DFS-based Ordering)}$$
$$= \mathcal{O}(V + E) + \mathcal{O}(V + E) = \mathcal{O}(V + E)$$

---

## Algorithm 2: Cycle Detection Using DFS Pattern

**Implementation:** `Algorithms.detectCycles()`

Uses the same traversal pattern as `DepthFirstSearch` to detect cycles.
```java
    public static <V, E> ArrayList<LinkedList<V>> detectCycles(Graph<V, E> g) {
    if (g == null) { // O(1)
        throw new IllegalArgumentException("Graph is null.");
    }

    if (!g.isDirected()) { // O(1)
        throw new IllegalArgumentException("Cycle detection for directed graphs only.");
    }

    ArrayList<LinkedList<V>> cycles = new ArrayList<>(); // O(1)
    int n = g.numVertices(); // O(1)
    if (n == 0) return cycles;

    int[] state = new int[n]; // O(V) 

    // Marks who discovered who (A -> B, A is B's parent)
    Map<V, V> parent = new HashMap<>(); // O(1)

    // Main DFS Loop
    for (V vertex : g.vertices()) { // O(V) 
        if (state[g.key(vertex)] == 0) { 
            findCyclesWithDFS(g, vertex, state, parent, cycles); // O(V + E)
        }
    }

    return cycles; 
}

private static  void findCyclesWithDFS(Graph g, V vertex,
                                              int[] state, V[] parent,
                                              ArrayList<LinkedList> cycles) {
    state[vKey] = 1; 

    // Same traversal pattern as DepthFirstSearch
    for (V neighbor : g.adjVertices(vertex)) {   // O(E) total
        if (state[nKey] == 1) {
            // Reconstruct cycle  // O(V) worst case
            LinkedList cycle = reconstructCycle(...);
            cycles.add(cycle);
        } else if (state[nKey] == 0) {
            parent[nKey] = vertex;
            findCyclesWithDFS(g, neighbor, state, parent, cycles);
        }
    }

    state[vKey] = 2; 
}
```

**Analysis:**
- Follows the same traversal pattern as `DepthFirstSearch`
- Each vertex visited once: $\mathcal{O}(V)$
- Each edge examined once: $\mathcal{O}(E)$
- Cycle reconstruction: $\mathcal{O}(V)$ per cycle (worst case)

**Complexity:** $\mathcal{O}(V + E)$ for typical graphs with few cycles

---

## Final Complexity Summary

| Operation | Complexity |
|:----------|:-----------|
| **Topological Sort** | $\mathcal{O}(V + E)$ |
| **Cycle Detection** | $\mathcal{O}(V + E)$ |


### Overall USEI11 Complexity

**Success Case (DAG):**
$$\mathcal{O}(V + E)$$

**Failure Case (Cycles):**
$$\mathcal{O}(V + E)$$