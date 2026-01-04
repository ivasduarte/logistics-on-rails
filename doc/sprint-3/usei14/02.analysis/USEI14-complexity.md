# USEI14 - Maximum throughput between two Hubs

## Objective
The goal of USEI14 is to determine the maximum throughput 
between two specified hubs using the Ford-Fulkerson algorithm with Depth-First Search (DFS).

This represents the maximum number of trains that can travel between two stations per day,
considering all available routes and track capacity constraints.

---

## Algorithm Overview

The Ford-Fulkerson algorithm computes maximum flow by iteratively finding augmenting paths
from source to sink in a residual graph, 
and pushing flow along these paths until no more augmenting paths exist.

**Main Algorithm Flow:**
```java
public static <V> Integer maxFlow(Graph<V, Integer> graph, V source, V sink) {
    
    // Create residual graph (cloning of original graph)
    Graph<V, Integer> residualGraph = graph.clone();

    int totalFlow = 0;

    // Iterate until no augmenting path is found
    while (true) {
        boolean[] visited = new boolean[residualGraph.numVertices()];
        LinkedList<V> path = new LinkedList<>();

        // Phase 1: Find an augmentation path using DFS
        boolean existingPath = findAugmentationPath(residualGraph, source, sink, visited, path);

        if (!existingPath || path.isEmpty()) break; 

        // Phase 2: Find bottleneck capacity
        int minCap = getPathMinCap(residualGraph, path);
        
        // Phase 3: Update residual graph
        updateResidualGraph(residualGraph, path, minCap);

        totalFlow += minCap;
    }

    return totalFlow;
}
```

---

## Complexity Analysis
The maximum flow complexity is determined by three main phases 
executed within each iteration of finding augmenting paths.
The total number of iterations depends on the maximum flow value.

### Phase 1: Finding Augmenting Paths (DFS)
This phase is executed in `findAugmentationPath()` 
using depth-first search to locate a path from source to sink with available capacity.

```java
private static <V> boolean findAugmentationPath(Graph<V, Integer> graph, V currentVertex, V sink, boolean[] visited, LinkedList<V> path) {

        int currentKey = graph.key(currentVertex);
        visited[currentKey] = true;

        path.push(currentVertex);

        // Base case: reached sink
        if (currentVertex.equals(sink)) {
            return true;
        }

        // Explore adjacent vertices with available capacity
        for (V adjVertex : graph.adjVertices(currentVertex)) {
            int adjKey = graph.key(adjVertex);

            if (!visited[adjKey]) {
                Edge<V, Integer> edge = graph.edge(currentVertex, adjVertex);

                // Only follow edges with positive residual capacity
                if (edge != null && edge.getWeight() != null && edge.getWeight() > 0) {
                    if (findAugmentationPath(graph, adjVertex, sink, visited, path)) {
                        return true;
                    }
                }
            }
        }

        path.pop();

        return false;
    }
```

**Analysis:**

- **Vertex Exploration**: Each vertex is visited at most once per DFS call: $\mathcal{O}(V)$
- **Edge Examination**: Each edge is examined at most one per DFS call: $\mathcal{O}(E)$
- **Path construction**: Adding/removing vertices from path: $\mathcal{O}(1)$ per edge
- **Capacity Check**: Checking edge capacity: $\mathcal{O}(1)$ per edge

**Complexity of Phase 1: $\mathcal{O}(V + E)$**

---

### Phase 2: Finding Minimum Capacity (Bottleneck)

This phase is executed in `getPathMinCap()` 
to determine the maximum flow that can be pushed through the found path.

```java
private static <V> int getPathMinCap(Graph<V, Integer> graph, LinkedList<V> path) {
        int minCap = Integer.MAX_VALUE;

        ArrayList<V> pathList = new ArrayList<>(path);

        // Traverse path and find minimum capacity
        for (int i = pathList.size() - 1; i > 0; i--) {
            V from = pathList.get(i);
            V to = pathList.get(i - 1);

            Edge<V, Integer> edge = graph.edge(from, to);
            if (edge != null && edge.getWeight() != null) {
                minCap = Math.min(minCap, edge.getWeight());
            }
        }

        return minCap;
    }
```

**Analysis:**

- **Path Length**: Path contains at most $V$ vertices.
- **Edge Lookup**: $\mathcal{O}(1)$ per edge in the path.
- **Comparison**: $\mathcal{O}(1)$ per edge.

**Complexity of Phase 2: $\mathcal{O}(V)$**

---

### Phase 3: Updating Residual Graph

This phase is executed in `updateResidualGraph()` to modify edge capacities after pushing flow.

```java
private static <V> void updateResidualGraph(Graph<V, Integer> graph, LinkedList<V> path, int flow) {
        ArrayList<V> pathList = new ArrayList<>(path);

        // Traverse path and update capacities
        for (int i = pathList.size() - 1; i > 0; i--) {
            V from = pathList.get(i);
            V to = pathList.get(i - 1);

            // Decrease forward edge capacity
            Edge<V, Integer> forwardEdge = graph.edge(from, to);
            if (forwardEdge != null) {
                int newCap = forwardEdge.getWeight() - flow;
                forwardEdge.setWeight(newCap);
            }

            // Increase backward edge capacity
            Edge<V, Integer> backwardEdge = graph.edge(to, from);
            if (backwardEdge != null) {
                int newCap = backwardEdge.getWeight() + flow;
                backwardEdge.setWeight(newCap);
            } else {
                graph.addEdge(to, from, flow);
            }
        }
    }
```

**Analysis:**

- **Path Traversal**: Iterate through at most $V$ vertices: $\mathcal{O}(V)$
- **Edge Lookup**: $\mathcal{O}(1)$ per edge
- **Edge Update**: $\mathcal{O}(1)$ per edge
- **Edge Creation**: $\mathcal{O}(1)$ when creation backward edge

**Complexity of Phase 3: $\mathcal{O}(V)$**

---

### Combined Complexity per Iteration

Each iteration of the main loop consists of:

$$\text{Per iteration} = \mathcal{O}(E) + \mathcal{O}(V) + \mathcal{O}(V) = \mathcal{O}(E + V)$$

However, for connected graphs, where $E \geq V - 1$:

$$\text{Per iteration} = \mathcal{O}(E)$$

---

### Total Number of Iterations

The key question is: **How many augmenting paths will be found?**

**Theorem**: Each augmenting path increases the total flow by at least 1 unit.

**Proof**:
- The bottleneck capacity $f$ of any augmenting path satisfies $f \geq 1$.
- After pushing flow $f$ through the path, at least one edge becomes saturated.
- The total flow increases by $f \geq 1$.

**Therefore**: If the minimum flow value is $F$, the algorithm performs at most $F$ iterations.

---

### Final Temporal Complexity

Combining the cost per iteration with the number of iterations:

$$\text{Total Complexity} = \text{(Cost per Iteration)} \times \text{(Number of Iterations)}$$

$$\mathcal{O}(E) \times F = \mathcal{O}(E \times F)$$

Where:
- $E$ = number of edges in the graph
- $F$ = maximum flow value

**Temporal Complexity: $\mathcal{O}(E \times F)$**