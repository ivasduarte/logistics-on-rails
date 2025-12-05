package freightMngt.graph;

import java.util.*;
import java.util.function.BinaryOperator;

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

            for (V vAdj : g.adjVertices(vert)) {
                if (!visited.get(vAdj)) {
                    qbfs.add(vAdj);
                    qaux.offer(vAdj);
                    visited.put(vAdj, true);
                }
            }
        }

        return qbfs;
    }

    /**
     * Performs depth-first search starting in a vertex
     *
     * @param g       Graph instance
     * @param vOrig   vertex of graph g that will be the source of the search
     * @param visited set of previously visited vertices
     * @param qdfs    return LinkedList with vertices of depth-first search
     */
    private static <V, E> void DepthFirstSearch(Graph<V, E> g, V vOrig, boolean[] visited, LinkedList<V> qdfs) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Performs depth-first search starting in a vertex
     *
     * @param g    Graph instance
     * @param vert vertex of graph g that will be the source of the search
     *
     * @return a LinkedList with the vertices of depth-first search
     */
    public static <V, E> LinkedList<V> DepthFirstSearch(Graph<V, E> g, V vert) {

        throw new UnsupportedOperationException("Not supported yet.");
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

        // initialize distances to infinity (zero) and pathKeys to null
        for (int i = 0; i < g.numVertices(); i++) {
            dist[i] = zero;
            pathKeys[i] = null;
            visited[i] = false;
        }

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

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Computes the shortest-path distance from a source vertex to all reachable vertices of a graph
     * g with no-negative-cycle edges, using Bellman-Ford algorithm.
     *
     * @param g         Graph instance
     * @param vOrig     Vertex that will be the source of the path
     * @param ce        Comparator between elements of type E
     * @param sum       Sum two elements of type E
     * @param zero      Neutral element of the sum in elements of type E
     * @param paths     ArrayList with all the paths
     * @param dists     ArrayList the corresponding minimum distances
     * @return          If vOrig exists in the graph, true, false otherwise
     */
    public static <V, E> boolean shortestPaths(Graph<V, E> g, V vOrig,
                                               Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                               ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

        if(!g.validVertex(vOrig)) return false;

        int numVerts = g.numVertices();
        int sourceKeys = g.key(vOrig);
        if(sourceKeys == -1) return false;

        // initialize data structures
        E[] dist = (E[]) new Object[numVerts];
        int[] predecessors = new int[numVerts];
        Arrays.fill(predecessors, -1);

        paths.clear(); dists.clear();

        // initialize return lists
        for(int i = 0; i < numVerts; i++) {
            dists.add(null);
            paths.add(null);
        }

        // distance from source to itself is zero
        dist[sourceKeys] = zero;
        dists.set(sourceKeys, zero);

        relaxation(g, dist, sum, ce, predecessors);
        LinkedList<V> cycle = checkNegativeCycles(g, dist, sum, ce, predecessors);

        if(cycle != null) {
            // if cycle exists -> clean everything -> return only cycle in first position
            paths.clear(); dists.clear();
            paths.add(cycle); // return cycle as error path
            return false;
        }

        // if no cycles found -> reconstruct paths
        reconstructPaths(g, sourceKeys, dist, predecessors, paths, dists);

        return true; // success
    }

    /**
     * Performs the relaxation steps of Bellman-Ford algorithm (V - 1) times.
     */
    private static <V, E> void relaxation(Graph<V, E> g, E[] dist, BinaryOperator<E> sum,
                                          Comparator<E> ce, int[] predecessors) {

        int numVerts = g.numVertices();

        for(int i = 1; i < numVerts; i++) {
            boolean changed = false;

            for(Edge<V, E> edge : g.edges()) {
                int u = g.key(edge.getVOrigin());
                int v = g.key(edge.getVTarget());
                E weight = edge.getWeight();

                if(dist[u] != null) {
                    E newDist = sum.apply(dist[u], weight);

                    if(dist[v] == null || ce.compare(newDist, dist[v]) < 0) {
                        dist[v] = newDist;
                        predecessors[v] = u;
                        changed = true;
                    }
                }
            }
            // if no distance changed, break
            if(!changed) break;
        }
    }

    /**
     * Check for negative cycles in the graph.
     *
     * @return  The cycle as a LinkedList if found, otherwise null
     */
    private static <V, E> LinkedList<V> checkNegativeCycles(Graph<V, E> g, E[] dist, BinaryOperator<E> sum,
                                                   Comparator<E> ce, int[] predecessors) {

        for(Edge<V, E> edge : g.edges()) {
            int u = g.key(edge.getVOrigin());
            int v = g.key(edge.getVTarget());
            E weight = edge.getWeight();

            if(dist[u] != null) {
                E newDist = sum.apply(dist[u], weight);
                if (dist[v] == null || ce.compare(newDist, dist[v]) < 0) {
                    // cycle detected -> reconstruction
                    return buildCyclePath(g, v, predecessors);

                }
            }
        }
        return null;
    }

    /**
     * Reconstructs the specific negative cycle previously found.
     */
    private static <V, E> LinkedList<V> buildCyclePath(Graph<V, E> g, int startKey, int[] predecessors) {
        LinkedList<V> cycle = new LinkedList<>();
        boolean[] visited = new boolean[g.numVertices()];
        int current = startKey;

        // go back in predecessors until we reach the start vertex
        while(!visited[current]) {
            visited[current] = true;
            current = predecessors[current];
        }

        // go through the cycle
        int cycleStart = current;
        cycle.addFirst(g.vertex(current));
        current = predecessors[current];

        while(current != cycleStart) {
            cycle.addFirst(g.vertex(current));
            current = predecessors[current];
        }
        cycle.addFirst(g.vertex(cycleStart)); // close cycle

        return cycle;
    }

    /**
     * Reconstructs all shortest paths from source to valid destinations.
     */
    private static <V, E> void reconstructPaths(Graph<V, E> g, int sourceKey, E[] dist, int[] predecessors,
                                                ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

        int numVerts = g.numVertices();

        for(int i = 0; i < numVerts; i++) {
            dists.set(i, dist[i]);

            if(dist[i] != null) {
                LinkedList<V> path = new LinkedList<>();
                int current = i;

                // reconstruct from back to front
                while(current != -1) {
                    path.addFirst(g.vertex(current));
                    if(current == sourceKey) break;
                    current = predecessors[current];
                }
                paths.set(i, path);
            }
        }
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

        throw new UnsupportedOperationException("Not supported yet.");
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

        throw new UnsupportedOperationException("Not supported yet.");
    }
}
