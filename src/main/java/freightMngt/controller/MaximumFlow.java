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
}
