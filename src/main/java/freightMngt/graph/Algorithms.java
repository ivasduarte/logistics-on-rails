package freightMngt.graph;

import freightMngt.domain.ConnectionData;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * @author DEI-ISEP
 */
public class Algorithms {

    /**
     * Performs breadth-first search of a Graph starting in a vertex
     *
     * @param g    Graph instance
     * @param vert vertex that will be the source of the search
     *
     * @return a LinkedList with the vertices of breadth-first search
     */
    public static <V, E> LinkedList<V> BreadthFirstSearch(Graph<V, E> g, V vert) {
        // Validate input
        if (g == null) {
            throw new IllegalArgumentException("Graph is null.");
        }
        if (!g.validVertex(vert)) {
            return null;
        }

        // Initialize data structures
        LinkedList<V> qbfs = new LinkedList<>();
        Queue<V> qaux = new LinkedList<>();
        Map<V, Boolean> visited = new HashMap<>();

        for (V vertex : g.vertices()) {
            visited.put(vertex, false);
        }

        qbfs.add(vert);
        qaux.offer(vert);
        visited.put(vert, true);

        while (!qaux.isEmpty()) {
            V vOrig = qaux.poll();

            for (V vAdj : g.adjVertices(vOrig)) {
                if (!visited.get(vAdj)) {
                    qbfs.add(vAdj);
                    qaux.offer(vAdj);
                    visited.put(vAdj, true);
                }
            }
        }

        return qbfs;
    }

    /** Performs depth-first search starting in a vertex
     *
     * @param g Graph instance
     * @param vOrig vertex of graph g that will be the source of the search
     * @param visited set of previously visited vertices
     * @param qdfs return LinkedList with vertices of depth-first search
     */
    private static <V, E> void DepthFirstSearch(Graph<V, E> g, V vOrig, boolean[] visited, LinkedList<V> qdfs) {
        int vOrigKey = g.key(vOrig);
        // If already visited, return
        if (visited[vOrigKey]) {
            return;
        }
        // Add current vertex to result list
        qdfs.add(vOrig);
        // Mark as visited
        visited[vOrigKey] = true;
        // Recursively visit all adjacent vertices
        for (V vAdj : g.adjVertices(vOrig)) {
            DepthFirstSearch(g, vAdj, visited, qdfs);
        }
    }

    /** Performs depth-first search starting in a vertex
     *
     * @param g Graph instance
     * @param vert vertex of graph g that will be the source of the search
     * @return a LinkedList with the vertices of depth-first search
     */
    public static <V, E> LinkedList<V> DepthFirstSearch(Graph<V, E> g, V vert) {
        // Validate input
        if (g == null) {
            throw new IllegalArgumentException("Graph is null.");
        }
        if (!g.validVertex(vert)) {
            return null;
        }
        // Initialize data structures
        LinkedList<V> qdfs = new LinkedList<>();
        boolean[] visited = new boolean[g.numVertices()];
        // Perform DFS
        DepthFirstSearch(g, vert, visited, qdfs);
        return qdfs;
    }

    /**
     * Returns all paths from vOrig to vDest
     *
     * @param g       Graph instance
     * @param vOrig   Vertex that will be the source of the path
     * @param vDest   Vertex that will be the end of the path
     * @param visited set of discovered vertices
     * @param path    stack with vertices of the current path (the path is in reverse order)
     * @param paths   ArrayList with all the paths (in correct order)
     */
    private static <V, E> void allPaths(Graph<V, E> g, V vOrig, V vDest, boolean[] visited, LinkedList<V> path, ArrayList<LinkedList<V>> paths) {
        if (g == null) {
            throw new IllegalArgumentException("Graph is null.");
        }

        path.push(vOrig);

        int keyVOrig = g.key(vOrig);
        visited[keyVOrig] = true;

        for (V vAdj : g.adjVertices(vOrig)) {
            if (vAdj.equals(vDest)) {
                path.push(vDest);
                paths.add(new LinkedList<>(path));
                path.pop();
            } else {
                int keyVAdj = g.key(vAdj);
                if (!visited[keyVAdj]) {
                    allPaths(g, vAdj, vDest, visited, path, paths);
                }
            }
        }

        path.pop();
        visited[keyVOrig] = false;
    }

    /**
     * Returns all paths from vOrig to vDest
     *
     * @param g     Graph instance
     * @param vOrig information of the Vertex origin
     * @param vDest information of the Vertex destination
     *
     * @return paths ArrayList with all paths from vOrig to vDest
     */
    public static <V, E> ArrayList<LinkedList<V>> allPaths(Graph<V, E> g, V vOrig, V vDest) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

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

    /**
     * Computes the shortest-path distance from a source vertex to all reachable vertices of a graph
     * g with potentially negative edge weights, using Bellman-Ford algorithm.
     *
     * @param g         Graph instance
     * @param vOrig     Vertex that will be the source of the path
     * @param ce        Comparator between elements of type E
     * @param sum       Sum two elements of type E
     * @param zero      Neutral element of the sum in elements of type E
     * @param paths     ArrayList with all the paths
     * @param dists     ArrayList the corresponding minimum distances
     * @return          If vOrig exists and no negative cycle is detected, true. False otherwise.
     *                  If a negative cycle is found, paths.get(0) contains the cycle vertices.
     */
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

    /**
     * Extracts from pathKeys the minimum path between voInf and vdInf
     * The path is constructed from the end to the beginning
     *
     * @param g        Graph instance
     * @param vOrig    information of the Vertex origin
     * @param vDest    information of the Vertex destination
     * @param pathKeys minimum path vertices keys
     * @param path     stack with the minimum path (correct order)
     */
    private static <V, E> void getPath(Graph<V, E> g, V vOrig, V vDest,
                                       V[] pathKeys, LinkedList<V> path) {

        // return if origin and/or destination isn't valid
        if (!g.validVertex(vOrig) || !g.validVertex(vDest)) return;

        // start path from destination and work backwards
        V current = vDest;

        // build path from destination to origin
        while (current != null && !current.equals(vOrig)) {
            path.addFirst(current);
            int currentI = g.key(current);
            current = pathKeys[currentI];
        }

        if (current != null) {
            path.addFirst(vOrig); // add origin if path was found
        } else {
            path.clear(); // clear path list if no path was found
        }
    }

    /**
     * Calculates the minimum distance graph using Floyd-Warshall
     *
     * @param g   initial graph
     * @param ce  comparator between elements of type E
     * @param sum sum two elements of type E
     * @return the minimum distance graph
     */
    public static <V, E> MatrixGraph<V, E> minDistGraph(Graph<V, E> g, Comparator<E> ce, BinaryOperator<E> sum) {
        MatrixGraph<V, E> minDistGraph = new MatrixGraph<>(g.isDirected());

        ArrayList<V> vertices = g.vertices();

        for (V vertex : vertices) {
            minDistGraph.addVertex(vertex);
        }

        for (V vi : vertices) {
            for (V vj : vertices) {
                Edge<V, E> edge = g.edge(vi, vj);
                if (edge != null) {
                    minDistGraph.addEdge(vi, vj, edge.getWeight());
                }
            }
        }

        int n = minDistGraph.numVertices();
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (i != k && minDistGraph.edge(vertices.get(i), vertices.get(k)) != null) {
                    for (int j = 0; j < n; j++) {
                        if (i != j && k != j && minDistGraph.edge(vertices.get(k), vertices.get(j)) != null) {
                            E edgeIK = minDistGraph.edge(vertices.get(i), vertices.get(k)).getWeight();
                            E edgeKJ = minDistGraph.edge(vertices.get(k), vertices.get(j)).getWeight();
                            E pathDistance = sum.apply(edgeIK, edgeKJ);
                            Edge<V, E> currentEdge = minDistGraph.edge(vertices.get(i), vertices.get(j));

                            if (currentEdge == null || ce.compare(pathDistance, currentEdge.getWeight()) < 0) {
                                minDistGraph.addEdge(vertices.get(i), vertices.get(j), pathDistance);
                            }
                        }
                    }
                }
            }
        }

        return minDistGraph;
    }
}
