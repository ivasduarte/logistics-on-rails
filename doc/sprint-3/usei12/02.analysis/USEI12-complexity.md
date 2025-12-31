# USEI12 - Minimal Backbone Network

### Objective

Compute a minimum spanning tree (MST) over the railway network to identify a minimal backbone that connects all stations with minimum total track length. Export the result as a DOT file for geographic visualization.

---

### Complexity Analysis

The analysis focuses on **Prim's Algorithm with Priority Queue** for computing the Minimum Spanning Tree, followed by statistics calculation and DOT export.

---

## Algorithm: Minimum Spanning Tree (Prim's Algorithm)

**Implementation:** `Algorithms.minimumSpanningTree()`

This algorithm builds a minimum spanning tree by greedily selecting the minimum-weight edge that connects a visited vertex to an unvisited vertex.

**Approach:** Greedy algorithm with priority queue
- Start from an arbitrary vertex
- Maintain a priority queue of edges sorted by weight (distance)
- Repeatedly select minimum-weight edge connecting visited to unvisited vertices
- Continue until all vertices are visited
```java
public static  Graph minimumSpanningTree(Graph g, Comparator ce) {
    Graph mst = new MapGraph<>(false);

    // Add all vertices to MST
    for (V vertex : g.vertices()) {
        mst.addVertex(vertex);
    }

    if (g.numVertices() == 1) {
        return mst;
    }

    // Track visited vertices
    Set visited = new HashSet<>();

    // Priority queue to select minimum weight edge
    PriorityQueue<Edge> pq = new PriorityQueue<>((e1, e2) -> 
        ce.compare(e1.getWeight(), e2.getWeight()));

    // Start from first vertex
    V start = g.vertices().get(0);
    visited.add(start);
    addEdgesToQueue(g, start, visited, pq);

    // Process edges until MST is complete
    while (!pq.isEmpty() && visited.size() < g.numVertices()) {
        Edge edge = pq.poll();

        V vOrig = edge.getVOrigin();
        V vDest = edge.getVTarget();

        // Find new unvisited vertex
        V newVertex = null;
        if (!visited.contains(vDest)) {
            newVertex = vDest;
        } else if (!visited.contains(vOrig)) {
            newVertex = vOrig;
        }

        if (newVertex == null) continue;

        // Add edge to MST
        mst.addEdge(vOrig, vDest, edge.getWeight());
        visited.add(newVertex);
        addEdgesToQueue(g, newVertex, visited, pq);
    }

    return mst;
}

private static  void addEdgesToQueue(Graph g, V vertex, 
                                           Set visited, 
                                           PriorityQueue<Edge> pq) {
    // Add outgoing edges
    for (Edge edge : g.outgoingEdges(vertex)) {
        if (!visited.contains(edge.getVTarget())) {
            pq.offer(edge);
        }
    }

    // Add incoming edges (to handle undirected nature)
    for (Edge edge : g.incomingEdges(vertex)) {
        if (!visited.contains(edge.getVOrigin())) {
            pq.offer(edge);
        }
    }
}
```

### Phase-by-Phase Analysis

**Phase 1: Initialization**
```java
Graph mst = new MapGraph<>(false);  // O(1)
for (V vertex : g.vertices()) {            // O(V)
    mst.addVertex(vertex);                 // O(1) per vertex
}
Set visited = new HashSet<>();         // O(1)
PriorityQueue pq = new PriorityQueue<>(...);  // O(1)
```
- Create MST graph: $\mathcal{O}(1)$
- Add all vertices: $\mathcal{O}(V)$
- Create data structures: $\mathcal{O}(1)$

**Complexity:** $\mathcal{O}(V)$

---

**Phase 2: Initial Vertex Processing**
```java
V start = g.vertices().get(0);     // O(1)
visited.add(start);                // O(1)
addEdgesToQueue(g, start, visited, pq);  // O(degree(start) × log E)
```
- Select start vertex: $\mathcal{O}(1)$
- Mark as visited: $\mathcal{O}(1)$
- Add edges to priority queue: $\mathcal{O}(d \log E)$ where $d$ is degree

**Complexity:** $\mathcal{O}(d \log E)$

---

**Phase 3: Main MST Construction Loop**
```java
while (!pq.isEmpty() && visited.size() < g.numVertices()) {  // O(V) iterations
    Edge edge = pq.poll();                             // O(log E)
    
    // Find new unvisited vertex
    V newVertex = null;                                       // O(1)
    if (!visited.contains(vDest)) {                          // O(1)
        newVertex = vDest;
    } else if (!visited.contains(vOrig)) {
        newVertex = vOrig;
    }
    
    if (newVertex == null) continue;                         // O(1)
    
    // Add edge to MST
    mst.addEdge(vOrig, vDest, edge.getWeight());            // O(1)
    visited.add(newVertex);                                  // O(1)
    addEdgesToQueue(g, newVertex, visited, pq);             // O(degree × log E)
}
```

**Inside DFS:**
```java
for (Edge edge : g.outgoingEdges(vertex)) {   // Total O(E) across all calls
    if (!visited.contains(edge.getVTarget())) {      // O(1)
        pq.offer(edge);                              // O(log E)
    }
}
```

**Analysis:**

1. **Loop Iterations:**
    - The loop runs until all $V$ vertices are visited
    - Each iteration processes one vertex
    - **Total iterations:** $\mathcal{O}(V)$

2. **Priority Queue Poll Operations:**
    - `pq.poll()`: Extract minimum element
    - **Per operation:** $\mathcal{O}(\log E)$
    - **Total across all iterations:** $\mathcal{O}(V \log E)$

3. **Edge Addition to Priority Queue:**
    - `addEdgesToQueue()` is called once per vertex (when vertex is added to MST)
    - Each vertex has edges, and across all vertices there are $E$ edges total
    - Each `pq.offer()` operation: $\mathcal{O}(\log E)$
    - **Total:** $\mathcal{O}(E \log E)$

**Combined Phase 3:**
$$\mathcal{O}(V \log E + E \log E) = \mathcal{O}(E \log E)$$

Since $E = \mathcal{O}(V^2)$ at most, we have $\log E = \mathcal{O}(\log V)$:
$$\mathcal{O}(E \log E) = \mathcal{O}(E \log V)$$

**Total Complexity:** $\mathcal{O}(E \log V)$

---

## USEI12 Complete Flow

### Main Flow (in NetworkController)
```java
public void minimalBackboneNetwork() {
    // 1. Compute MST
    Graph mst = 
        networkController.computeMinimalBackbone();               // O(E log V)
    
    // 2. Calculate statistics
    Map stats = 
        networkController.calculateMSTStatistics(mst);            // O(V)
    
    // 3. Export to DOT file
    networkController.exportMinimalBackboneToGraphviz(mst, filename);  // O(V)
}
```

---

### Operation 1: Compute MST (Prim's Algorithm)

**Implementation:** `Algorithms.minimumSpanningTree()`
```java
public Graph computeMinimalBackbone() {
    Comparator distanceComparator = 
        Comparator.comparingDouble(ConnectionData::getDistance);  // O(1)

    return Algorithms.minimumSpanningTree(this.graph, distanceComparator);  // O(E log V)
}
```

**Complexity:** $\mathcal{O}(E \log V)$

---

### Operation 2: Calculate Statistics

**Implementation:** `NetworkController.calculateMSTStatistics()`
```java
public Map calculateMSTStatistics(Graph mst) {
    double totalDistance = 0.0;
    int edgeCount = 0;
    Set countedEdges = new HashSet<>();     // O(1)

    // Iterate through all edges in MST
    for (Edge edge : mst.edges()) {  // O(E_mst) = O(V-1)
        String edgeKey;
        if (edge.getVOrigin().getNodeID().compareTo(
            edge.getVTarget().getNodeID()) < 0) {   // O(1)
            edgeKey = edge.getVOrigin().getNodeID() + "--" + 
                      edge.getVTarget().getNodeID();
        } else {
            edgeKey = edge.getVTarget().getNodeID() + "--" + 
                      edge.getVOrigin().getNodeID();
        }

        if (!countedEdges.contains(edgeKey)) {      // O(1)
            totalDistance += edge.getWeight().getDistance();  // O(1)
            edgeCount++;
            countedEdges.add(edgeKey);              // O(1)
        }
    }

    stats.put("totalDistance", totalDistance);
    stats.put("numEdges", (double) edgeCount);
    stats.put("numStations", (double) mst.numVertices());

    return stats;
}
```

**Analysis:**
- For MST: number of edges = $V - 1$
- Iterate through all edges: $\mathcal{O}(V - 1) = \mathcal{O}(V)$
- Hash operations (contains, add): $\mathcal{O}(1)$ average

**Complexity:** $\mathcal{O}(V)$

---

### Operation 3: Export to DOT File

**Implementation:** `NetworkController.exportMinimalBackboneToGraphviz()`
```java
public void exportMinimalBackboneToGraphviz(Graph mst, 
                                            String filename) {
    StringBuilder sb = new StringBuilder();

    // Add header
    sb.append("graph MinimalBackboneNetwork {\n");     // O(1)
    sb.append("    layout=neato;\n");                  // O(1)
    // ... other header lines

    // Add all vertices with XY coordinates
    for (Station station : mst.vertices()) {           // O(V)
        String safeName = station.getStationName()
                         .replaceAll("\"", "\\\"");     // O(name length)
        sb.append(String.format(...));                 // O(1)
    }

    // Add all edges
    Set addedEdges = new HashSet<>();          // O(1)
    for (Edge edge : mst.edges()) {  // O(V-1)
        // Create canonical edge key                   // O(1)
        // Check if already added                      // O(1)
        // Append edge to StringBuilder                // O(1)
    }

    sb.append("}\n");

    // Write to file
    try (FileWriter fw = new FileWriter(filename)) {   // O(output size)
        fw.write(sb.toString());                       // O(V + E) = O(V)
    }
}
```

**Analysis:**
- Iterate through all vertices: $\mathcal{O}(V)$
- Iterate through all edges: $\mathcal{O}(V - 1) = \mathcal{O}(V)$
- String operations: $\mathcal{O}(1)$ per operation
- File write: $\mathcal{O}(\text{output size}) = \mathcal{O}(V + E) = \mathcal{O}(V)$ for MST

**Complexity:** $\mathcal{O}(V)$

---

## Final Complexity Summary

| Operation | Complexity |
|:----------|:-----------|
| **MST Computation** | $\mathcal{O}(E \log V)$ |
| **Statistics Calculation** | $\mathcal{O}(V)$ |
| **DOT Export** | $\mathcal{O}(V)$ |

### Overall USEI12 Complexity

**Time Complexity:**
$$\mathcal{O}(E \log V) + \mathcal{O}(V) + \mathcal{O}(V) = \mathcal{O}(E \log V)$$

