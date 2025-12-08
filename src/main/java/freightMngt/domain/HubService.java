package freightMngt.domain;

import freightMngt.graph.Algorithms;
import freightMngt.graph.Graph;
import java.util.*;
import java.util.function.Function;

public class HubService {

    public <V, E> Map<V, HubMetrics<V>> calculateHubScores(Graph<V, E> graph,
                                                         Comparator<E> ce,
                                                         java.util.function.BinaryOperator<E> sum,
                                                         E zero,
                                                         Function<E, Double> convertToDouble) {

        // 1. Calculate base metrics
        Map<V, Double> betweennessRaw = Algorithms.betweennessCentrality(graph, ce, sum, zero);
        Map<V, Double> betweennessNorm = Algorithms.normalizeCentrality(betweennessRaw, graph.numVertices(), graph.isDirected());
        Map<V, Double> harmonicNorm = Algorithms.harmonicCentrality(graph, ce, sum, zero, convertToDouble);
        Map<V, Double> strengthRaw = Algorithms.calculateStrength(graph, convertToDouble);
        Map<V, Double> strengthNorm = normalizeMap(strengthRaw); // Usado apenas para o score

        Map<V, HubMetrics<V>> results = new HashMap<>();

        for (V vertex : graph.vertices()) {
            double betw = betweennessNorm.getOrDefault(vertex, 0.0);
            double harm = harmonicNorm.getOrDefault(vertex, 0.0);
            double strRaw = strengthRaw.getOrDefault(vertex, 0.0);
            double strNorm = strengthNorm.getOrDefault(vertex, 0.0);

            // Degree = number of connected edges
            double degree = graph.adjVertices(vertex).size();

            // Final Formula
            double score = (0.35 * betw) + (0.35 * harm) + (0.30 * strNorm);

            results.put(vertex, new HubMetrics<>(vertex, degree, strRaw, betw, harm, score));
        }

        return results;
    }

    public <V> List<HubMetrics<V>> getTopHubs(Map<V, HubMetrics<V>> metrics, int n) {
        List<HubMetrics<V>> list = new ArrayList<>(metrics.values());
        list.sort((m1, m2) -> Double.compare(m2.getHubScore(), m1.getHubScore()));

        if (n >= list.size()) return list;
        return list.subList(0, n);
    }

    /**
     * Helper method to normalize a map of values to [0, 1] range.
     * Used specifically for Strength in this context.
     */
    private <V> Map<V, Double> normalizeMap(Map<V, Double> rawMap) {
        Map<V, Double> normalized = new HashMap<>();

        if (rawMap.isEmpty()) return normalized;

        // Find maximum value
        double maxVal = 0.0;
        for (Double val : rawMap.values()) {
            if (val > maxVal) maxVal = val;
        }

        // Divide everything by the maximum
        for (Map.Entry<V, Double> entry : rawMap.entrySet()) {
            if (maxVal == 0) {
                normalized.put(entry.getKey(), 0.0);
            } else {
                normalized.put(entry.getKey(), entry.getValue() / maxVal);
            }
        }
        return normalized;
    }
}
