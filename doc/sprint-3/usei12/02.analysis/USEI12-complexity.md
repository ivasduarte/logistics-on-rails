# USEI12 - Minimal Backbone Network

### Objective

Compute a minimum spanning tree (MST) over the railway network to identify a minimal backbone that connects all stations with minimum total track length. Export the result as a DOT file for geographic visualization.

---

### Complexity Analysis

The analysis focuses on **Kruskal's Algorithm with Union-Find** for computing the Minimum Spanning Tree, followed by statistics calculation and DOT export.

---

## Algorithm: Minimum Spanning Tree (Kruskal's Algorithm)

**Implementation:** `Algorithms.kruskalMST()`

This algorithm builds a minimum spanning tree by sorting all edges by weight and greedily selecting edges that connect different components, using a Union-Find data structure to efficiently track connected components.

**Approach:** Greedy algorithm with Union-Find data structure
- Sort all edges by weight (distance) in ascending order
- Use Union-Find to track which vertices belong to the same connected component
- For each edge in sorted order:
   - Check if the edge's endpoints are in different components (find operation)
   - If yes, add the edge to MST and merge the components (union operation)
- Continue until V-1 edges are added or all edges are processed

```java
public static <V, E> Graph<V, E> kruskalMST(Graph<V, E> g, Comparator<E> ce) {
    if (g == null || g.numVertices() == 0) {
        return new MapGraph<>(false);
    }

    Graph<V, E> mst = new MapGraph<>(false);
    List<Edge<V, E>> allEdges = new ArrayList<>();

    // Prepare the graph
    for (V v : g.vertices()) {
        mst.addVertex(v);
        // Take only the outgoing edges
        for (Edge<V, E> e : g.outgoingEdges(v)) {
            allEdges.add(e);
        }
    }
    
    // Sort all edges by weight
    allEdges.sort((e1, e2) -> ce.compare(e1.getWeight(), e2.getWeight()));

    // Map to associate each vertex to the "root" of their component
    Map<V, V> parent = new HashMap<>();
    for (V v : g.vertices()) {
        parent.put(v, v); // each vertex starts in its own component
    }

    int edgesInMst = 0;
    for (Edge<V, E> edge : allEdges) {
        // If all vertices are connected, finish
        if (edgesInMst == g.numVertices() - 1) break;

        V root1 = find(parent, edge.getVOrigin());
        V root2 = find(parent, edge.getVTarget());

        // If the two roots are different, no cycle is created
        if (!root1.equals(root2)) {
            mst.addEdge(edge.getVOrigin(), edge.getVTarget(), edge.getWeight());
            union(parent, root1, root2);
            edgesInMst++;
        }
    }

    return mst;
}

// Find the root of the component
private static <V> V find(Map<V, V> parent, V v) {
    if (parent.get(v).equals(v)) {
        return v;
    }
    // Path compression
    parent.put(v, find(parent, parent.get(v)));
    return parent.get(v);
}

// Union: merge two components
private static <V> void union(Map<V, V> parent, V root1, V root2) {
    parent.put(root1, root2);
}
```

### Phase-by-Phase Analysis

**Phase 1: Initialization**
```java
Graph<V, E> mst = new MapGraph<>(false);      // O(1)
List<Edge<V, E>> allEdges = new ArrayList<>(); // O(1)

for (V v : g.vertices()) {                     // O(V) iterations
    mst.addVertex(v);                          // O(1) per vertex
    for (Edge<V, E> e : g.outgoingEdges(v)) {  // Total O(E) across all vertices
        allEdges.add(e);                       // O(1) per edge
    }
}
```
- Create MST graph: $\mathcal{O}(1)$
- Add all vertices: $\mathcal{O}(V)$
- Collect all edges: $\mathcal{O}(E)$

**Complexity:** $\mathcal{O}(V + E)$

---

**Phase 2: Edge Sorting**
```java
allEdges.sort((e1, e2) -> ce.compare(e1.getWeight(), e2.getWeight()));
```
- Sort all edges by weight using comparison-based sorting (Timsort in the case of Java)
- The sorting algorithm divides the problem recursively: $\log_2 E$ levels of recursion
- At each level, all $E$ elements are processed for merging
- Sorting E edges: $E \times \log E = \mathcal{O}(E \log E)$

**Complexity:** $\mathcal{O}(E \log E)$

---

**Phase 3: Union-Find Initialization**
```java
Map<V, V> parent = new HashMap<>();            // O(1)
for (V v : g.vertices()) {                     // O(V)
    parent.put(v, v);                          // O(1) per vertex
}
```
- Initialize parent map: $\mathcal{O}(1)$
- Set each vertex as its own parent: $\mathcal{O}(V)$

**Complexity:** $\mathcal{O}(V)$

---

**Phase 4: MST Construction Loop**
```java
int edgesInMst = 0;
for (Edge<V, E> edge : allEdges) {             // O(E) iterations
        if (edgesInMst == g.numVertices() - 1) break;  // O(1)

V root1 = find(parent, edge.getVOrigin());     // O(log V) worst case
V root2 = find(parent, edge.getVTarget());     // O(log V) worst case

    if (!root1.equals(root2)) {                    // O(1)
        mst.addEdge(edge.getVOrigin(), edge.getVTarget(), 
                    edge.getWeight());             // O(1)
union(parent, root1, root2);               // O(1)
edgesInMst++;                              // O(1)
        }
        }
```

**Analysis:**

1. **Loop Iterations:**
    - Iterates through all sorted edges
    - Early termination after V-1 edges are added to MST
    - **Worst case iterations:** $\mathcal{O}(E)$

2. **Find Operations:**
    - `find()` with path compression
    - **Per operation:** $\mathcal{O}(\log V)$ (conservative analysis)
    - With Path Compression, the complexity is better than $\mathcal{O}(\log V)$, but we use this value as a conservative limit for analysis.

3. **Union Operations:**
    - Simple parent reassignment
    - **Per operation:** $\mathcal{O}(1)$

4. **Edge Addition to MST:**
    - `mst.addEdge()`: $\mathcal{O}(1)$

**Per Iteration Complexity:** $\mathcal{O}(\log V)$

**Total Phase 4 Complexity:** $\mathcal{O}(E \log V)$

---

**Total Complexity:** $\mathcal{O}(E \log E)$

---

## USEI12 Complete Flow

### Main Flow (in NetworkController)
```java
public void minimalBackboneNetwork() {
    // 1. Compute MST
    Graph<Station, ConnectionData> mst = 
        networkController.computeMinimalBackbone();               // O(E log V)
    
    // 2. Calculate statistics
    Map<String, Double> stats = 
        networkController.calculateMSTStatistics(mst);            // O(V)
    
    // 3. Export to DOT file
    networkController.exportMinimalBackboneToGraphviz(mst, filename);  // O(V)
}
```

---

### Operation 1: Compute MST (Kruskal's Algorithm)

**Implementation:** `NetworkController.computeMinimalBackbone()`
```java
public Graph<Station, ConnectionData> computeMinimalBackbone() {
    Comparator<ConnectionData> distanceComparator = 
        Comparator.comparingDouble(ConnectionData::getDistance);  // O(1)

    return Algorithms.kruskalMST(this.graph, distanceComparator);  // O(E log V)
}
```

**Complexity:** $\mathcal{O}(E \log V)$

---

### Operation 2: Calculate Statistics

**Implementation:** `NetworkController.calculateMSTStatistics()`
```java
public Map<String, Double> calculateMSTStatistics(Graph<Station, ConnectionData> mst) {
    Map<String, Double> stats = new HashMap<>();

    double totalDistance = 0.0;
    int edgeCount = 0;
    Set<String> countedEdges = new HashSet<>();     // O(1)

    // Iterate through all edges in MST
    for (Edge<Station, ConnectionData> edge : mst.edges()) {  // O(E_mst) = O(V-1)
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
    public void exportMinimalBackboneToGraphviz(Graph<Station, ConnectionData> mst, String filename) {
    StringBuilder sb = new StringBuilder();

    sb.append("graph MinimalBackboneNetwork {\n");
    sb.append("    layout=neato;\n");
    sb.append("    overlap=false;\n");
    sb.append("    sep=\"+25\";\n");
    sb.append("    splines=true;\n");

    sb.append("    node [shape=circle, style=filled, fillcolor=\"#66ccff\", fixedsize=false, fontsize=10];\n");
    sb.append("    edge [color=\"#888888\", penwidth=1.0, fontsize=8];\n\n");

    double scaleFactor = 0.08;

    for (Station s : mst.vertices()) {
        String safeName = s.getStationName().replaceAll("\"", "\\\"");

        sb.append(String.format(Locale.US, "    \"%s\" [pos=\"%.2f,%.2f!\", label=\"%s\", tooltip=\"%s\"];\n",
                s.getNodeID(),
                s.getX() * scaleFactor,
                s.getY() * scaleFactor,
                safeName,
                safeName));
    }

    sb.append("\n");

    // Add MST edges: removing labels and length hints for a minimal look
    Set<String> addedEdges = new HashSet<>();
    for (Edge<Station, ConnectionData> e : mst.edges()) {
        Station vOrig = e.getVOrigin();
        Station vDest = e.getVTarget();

        String id1 = vOrig.getNodeID();
        String id2 = vDest.getNodeID();
        String edgeKey = (id1.compareTo(id2) < 0) ? id1 + "--" + id2 : id2 + "--" + id1;

        if (addedEdges.add(edgeKey)) {
            double distance = e.getWeight().getDistance();
            sb.append(String.format(Locale.US, "    \"%s\" -- \"%s\" [label=\"%.1f\"];\n",
                    id1, id2, distance));
        }
    }

    sb.append("}\n");

    try (FileWriter fw = new FileWriter(filename)) {
        fw.write(sb.toString());
        System.out.println("Minimal Backbone Network (Clean Version) exported to " + filename);
    } catch (IOException e) {
        System.err.println("Error exporting MST: " + e.getMessage());
        e.printStackTrace();
    }
}
```

**Analysis:**
- Iterate through all vertices: $\mathcal{O}(V)$
- Iterate through all edges: $\mathcal{O}(V - 1) = \mathcal{O}(V)$
- String operations: $\mathcal{O}(1)$ per operation
- File write: $\mathcal{O}(V + (V - 1)) = \mathcal{O}(V)$ 

**Complexity:** $\mathcal{O}(V)$

---

## Final Complexity Summary

| Operation | Complexity              |
|:----------|:------------------------|
| **MST Computation** | $\mathcal{O}(E \log E)$ |
| **Statistics Calculation** | $\mathcal{O}(V)$        |
| **DOT Export** | $\mathcal{O}(V)$        |

### Overall USEI12 Complexity

**Time Complexity:**
$$\mathcal{O}(E \log E) + \mathcal{O}(V) + \mathcal{O}(V) = \mathcal{O}(E \log E)$$