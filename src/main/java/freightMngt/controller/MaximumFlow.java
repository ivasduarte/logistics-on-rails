package freightMngt.controller;

import freightMngt.graph.Edge;
import freightMngt.graph.Graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MaximumFlow {

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
}
