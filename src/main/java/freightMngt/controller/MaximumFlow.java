package freightMngt.controller;

import freightMngt.graph.Edge;
import freightMngt.graph.Graph;

import java.util.ArrayList;
import java.util.LinkedList;

public class MaximumFlow {
    /**
     * Recursive search for a path from source station to sink station in the capacity graph.
     *
     * @param graph capacity graph
     * @param currentVertex current vertex of DFS
     * @param sink destination vertex
     * @param visited flag for visited vertices
     * @param path found path updated on every recursive call
     *
     * @return {@code true} if a path from source to sink is found, {@code false} otherwise
     * @param <V> vertex type
     */
    private static <V> boolean findAugmentationPath(Graph<V, Integer> graph, V currentVertex, V sink,
                                                    boolean[] visited, LinkedList<V> path) {

        int currentKey = graph.key(currentVertex);
        visited[currentKey] = true;

        path.push(currentVertex);

        if (currentVertex.equals(sink)) {
            return true;
        }

        for (V adjVertex : graph.adjVertices(currentVertex)) {
            int adjKey = graph.key(adjVertex);

            if (!visited[adjKey]) {
                Edge<V, Integer> edge = graph.edge(currentVertex, adjVertex);

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

    /**
     * This method returns the minimum capacity of the edges in the given path.
     *
     * @param graph updated residual graph
     * @param path found augmentation path
     *
     * @return minimum capacity found in the path
     * @param <V> vertex type
     */
    private static <V> int getPathMinCap(Graph<V, Integer> graph, LinkedList<V> path) {
        int minCap = Integer.MAX_VALUE;

        ArrayList<V> pathList = new ArrayList<>(path);

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

    /**
     * This method updates the residual graph.
     * It reduces the capacity of the forward edges by the flow value,
     * and increases the capacity of the backward edges by the same flow value.
     *
     * @param graph updated residual graph
     * @param path augmentation path found
     * @param flow flow value to update the capacities
     *
     * @param <V> vertex type
     */
    private static <V> void updateResidualGraph(Graph<V, Integer> graph, LinkedList<V> path, int flow) {
        ArrayList<V> pathList = new ArrayList<>(path);

        // traversing backwards cause path is in reverse order
        for (int i = pathList.size() - 1; i > 0; i--) {
            V from = pathList.get(i);
            V to = pathList.get(i - 1);

            Edge<V, Integer> forwardEdge = graph.edge(from, to);
            if (forwardEdge != null) {
                int newCap = forwardEdge.getWeight() - flow;
                forwardEdge.setWeight(newCap);
            }

            Edge<V, Integer> backwardEdge = graph.edge(to, from);
            if (backwardEdge != null) {
                int newCap = backwardEdge.getWeight() + flow;
                backwardEdge.setWeight(newCap);
            } else {
                graph.addEdge(to, from, flow);
            }
        }
    }

    /**
     * Computes the maximum flow between two vertices.
     * Uses the Ford-Fulkerson algorithm with DFS for finding augmentation paths.
     *
     * @param graph capacity graph
     * @param source source vertex
     * @param sink sink vertex
     *
     * @return maximum flow value, or {@code null} if one of the vertices is invalid
     * @param <V> vertex type
     */
    public static <V> Integer maxFlow(Graph<V, Integer> graph, V source, V sink) {

        if (!graph.validVertex(source) || !graph.validVertex(sink)) return null;
        if (source.equals(sink)) return 0;

        // residual graph creation
        Graph<V, Integer> residualGraph = graph.clone();

        int totalFlow = 0;

        while (true) {
            boolean[] visited = new boolean[residualGraph.numVertices()];
            LinkedList<V> path = new LinkedList<>();

            boolean existingPath = findAugmentationPath(residualGraph, source, sink, visited, path);

            if (!existingPath || path.isEmpty()) break; // There are no more augmentation paths

            int minCap = getPathMinCap(residualGraph, path);
            updateResidualGraph(residualGraph, path, minCap);

            totalFlow += minCap;
        }

        return totalFlow;
    }
}
