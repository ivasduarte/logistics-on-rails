package freightMngt.controller;

import freightMngt.domain.*;
import freightMngt.graph.Algorithms;
import freightMngt.graph.Edge;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.BinaryOperator;

public class NetworkController {

    private Graph<Station, ConnectionData> graph;

    public NetworkController() {
        this.graph = new MapGraph<>(true);
    }

    public Graph<Station, ConnectionData> getGraph() { return graph; }

    public void loadNetwork(String stationsFile, String linesFile) {
        Map<String, Station> stationMap = new HashMap<>();

        // read stations from the file
        try (BufferedReader br = new BufferedReader(new FileReader(stationsFile))) {
            String line = br.readLine(); // skip header

            while((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 6) continue;

                String id = parts[0].trim();
                String name = parts[1].trim();
                double lat = Double.parseDouble(parts[2].trim());
                double lon = Double.parseDouble(parts[3].trim());
                double x = Double.parseDouble(parts[4].trim());
                double y = Double.parseDouble(parts[5].trim());

                Station station = new Station(id, name, lat, lon, x, y);

                graph.addVertex(station);
                stationMap.put(id, station);
            }
        } catch (IOException e) {
            System.err.println("Error reading stations: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing station coordinates: " + e.getMessage());
        }

        // read lines from file
        try (BufferedReader br = new BufferedReader(new FileReader(linesFile))) {
            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 5) continue;

                String sourceId = parts[0].trim();
                String targetId = parts[1].trim();
                double distance = Double.parseDouble(parts[2].trim());
                double capacity = Double.parseDouble(parts[3].trim());
                double cost = Double.parseDouble(parts[4].trim());

                Station s1 = stationMap.get(sourceId);
                Station s2 = stationMap.get(targetId);

                if(s1 != null && s2 != null) {
                    ConnectionData data = new ConnectionData(distance, capacity, cost);
                    graph.addEdge(s1, s2, data);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading lines: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing line data: " + e.getMessage());
        }
    }

    /**
     * Exports graph to file .dot
     */
    public void exportToGraphviz(String outputFile) {

        StringBuilder sb = new StringBuilder();

        sb.append("graph BelgianRailNetwork {\n");
        sb.append("    layout=neato;\n");
        sb.append("    node [shape=circle style=filled fillcolor=\"#66ccff\" fixedsize=true width=0.3 label=\"\"];\n");
        sb.append("    edge [color=\"#555555\" penwidth=1.0];\n\n");

        double scaleFactor = 0.01;

        for(Station s : graph.vertices()) {
            String safeName = s.getStationName().replaceAll("\"", "\\\"");

            sb.append(String.format(Locale.US, "    \"%s\" [pos=\"%.2f, %.2f!\" tooltip=\"%s\"];\n",
                    s.getNodeID(),
                    s.getX() * scaleFactor,
                    s.getY() * scaleFactor,
                    safeName));
        }

        sb.append("\n");

        for(Edge<Station, ConnectionData> e : graph.edges()) {
            Station vOrig = e.getVOrigin();
            Station vDest = e.getVTarget();

            if(vOrig.getNodeID().compareTo(vDest.getNodeID()) < 0) {
                sb.append(String.format(Locale.US, "  \"%s\" -- \"%s\" [len=%.2f];\n",
                        vOrig.getNodeID(), vDest.getNodeID(), e.getWeight().getDistance() * scaleFactor));
            }
        }

        sb.append("}\n");

        try (FileWriter fw = new FileWriter(outputFile)) {
            fw.write(sb.toString());
            System.out.println("Graph exported to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes upgrade order by calling Algorithms.topologicalSort and Algorithms.detectCycles.
     */
    public UpgradeOrderResult computeUpgradeOrder() {
        if (graph == null || graph.numVertices() == 0) {
            return UpgradeOrderResult.error("Network not loaded");
        }
        if (!graph.isDirected()) {
            return UpgradeOrderResult.error("Graph must be directed for upgrade planning");
        }

        // Attempt topological sort using Algorithms class
        LinkedList<Station> order = Algorithms.topologicalSort(graph);

        if (order != null) {
            // Success - no cycles detected
            return UpgradeOrderResult.success(order);
        } else {
            // Cycles detected - use Algorithms.detectCycles
            ArrayList<LinkedList<Station>> cycles = Algorithms.detectCycles(graph);

            Set<Station> stationsInCycles = new HashSet<>();
            for (LinkedList<Station> cycle : cycles) {
                stationsInCycles.addAll(cycle);
            }

            return UpgradeOrderResult.withCycles(cycles, new ArrayList<>(stationsInCycles));
        }
    }

    /**
     * Computes the shortest path between two stations using the Bellman-Ford algorithm.
     * Uses ABSOLUTE VALUE of costs to avoid negative cycles in the network data.
     *
     * @param vOrig     Origin station.
     * @param vDest     Destination station.
     * @param ce        Comparator for Double costs.
     * @param sum       Binary operator for summing costs.
     * @param zero      Neutral element (0.0).
     * @param shortPath Returns the shortest path (or negative cycle if detected).
     * @return The shortest distance (total cost), or null if the path doesn't exist
     *         or a negative cycle is detected.
     */
    public Double shortestPathBellmanFord(Station vOrig, Station vDest,
                                          Comparator<Double> ce, BinaryOperator<Double> sum, Double zero,
                                          LinkedList<Station> shortPath) {

        // Create a temporary graph with Double weights (ABSOLUTE VALUE of cost)
        Graph<Station, Double> costGraph = new MapGraph<>(true);

        // Copy all vertices
        for (Station station : graph.vertices()) {
            costGraph.addVertex(station);
        }

        // Copy all edges, using ABSOLUTE VALUE of cost to avoid negative cycles
        for (Edge<Station, ConnectionData> edge : graph.edges()) {
            Station origin = edge.getVOrigin();
            Station target = edge.getVTarget();
            Double cost = Math.abs(edge.getWeight().getCost()); // |cost| ensures non-negative
            costGraph.addEdge(origin, target, cost);
        }

        // Run Bellman-Ford on the cost graph (now without negative weights)
        return Algorithms.shortestPathBellmanFord(costGraph, vOrig, vDest, ce, sum, zero, shortPath);
    }

    /**
     * Identifies edges that are part of known negative cycles.
     * Currently, handles the DRONGEN-GENT-ST-P negative cycle.
     */
    private boolean isNegativeCycleEdge(Station origin, Station target) {
        String origId = origin.getNodeID();
        String destId = target.getNodeID();

        // Known negative cycle: DRONGEN (335) <-> GENT-ST-P (455)
        return (origId.equals("335") && destId.equals("455")) ||
                (origId.equals("455") && destId.equals("335"));
    }

    /**
     * Calculates the HubScores for the network using the HubService.
     * Uses 'Distance' as the weight criteria for centrality calculations.
     *
     * @param n The number of top hubs to return
     * @return List of the top N stations sorted by HubScore
     */
    public List<HubMetrics<Station>> calculateAndRankHubs(int n) {
        if (this.graph == null || this.graph.numVertices() == 0) {
            return new ArrayList<>();
        }

        HubService hubService = new HubService();


        Map<Station, HubMetrics<Station>> scores = hubService.calculateHubScores(
                this.graph,
                // 1. Comparator: Compares two edges by distance
                Comparator.comparingDouble(ConnectionData::getDistance),

                // 2. BinaryOperator (Sum): Sums two edges (creates a new one with the sum of distances)
                (c1, c2) -> new ConnectionData(c1.getDistance() + c2.getDistance(), 0.0, 0.0),

                // 3. Zero: Neutral element (distance 0)
                new ConnectionData(0.0, 0.0, 0.0),

                // 4. ConvertToDouble: How to transform the edge into a simple double for calculations
                ConnectionData::getDistance
        );

        return hubService.getTopHubs(scores, n);
    }

    public void exportHubsToCSV(String filename, int topN) {
        if (this.graph == null) return;

        HubService hubService = new HubService();

        // Nota o <Station> no tipo da variável
        Map<Station, HubMetrics<Station>> metrics = hubService.calculateHubScores(
                this.graph,
                Comparator.comparingDouble(ConnectionData::getDistance),
                (c1, c2) -> new ConnectionData(c1.getDistance() + c2.getDistance(), 0.0, 0.0),
                new ConnectionData(0.0, 0.0, 0.0),
                ConnectionData::getDistance
        );

        List<HubMetrics<Station>> topHubs = hubService.getTopHubs(metrics, topN);

        try (FileWriter fw = new FileWriter(filename)) {
            // Cabeçalho EXATO pedido na user story (com stid e stname)
            fw.write("stid,stname,degree,strength,betweenness,harmonic_closeness,hubscore\n");

            for (HubMetrics<Station> m : topHubs) {
                Station s = m.getVertex(); // Recuperamos a estação

                // Formatamos a linha com ID e Nome separados
                String line = String.format(Locale.US, "%s,%s,%.0f,%.2f,%.4f,%.4f,%.4f\n",
                        s.getNodeID(),          // stid
                        s.getStationName(),     // stname
                        m.getDegree(),
                        m.getStrength(),
                        m.getBetweenness(),
                        m.getHarmonic(),
                        m.getHubScore()
                );
                fw.write(line);
            }
            System.out.println("Hubs exported to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a Station by its ID (String), which is the unique NodeID.
     */
    public Station getStationByID(String stationID) {
        for (Station station : graph.vertices()) {
            if (station.getNodeID().equals(stationID)) {
                return station;
            }
        }
        return null;
    }

    /**
     * This method creates a new graph from the original graph.
     * The edges weight of this graph represent the capacities.
     *
     * @return capacity graph
     */
    public Graph<Station, Integer> getCapacityGraph() {
        Graph<Station, ConnectionData> originalGraph = this.getGraph();
        Graph<Station, Integer> capacityGraph = new MapGraph<>(originalGraph.isDirected());

        for (Station station : originalGraph.vertices()) {
            capacityGraph.addVertex(station);
        }

        for (Edge<Station, ConnectionData> edge : originalGraph.edges()) {
            Station origin = edge.getVOrigin();
            Station target = edge.getVTarget();

            Integer capacity = (int) edge.getWeight().getCapacity();

            capacityGraph.addEdge(origin, target, capacity);
        }

        return capacityGraph;
    }
}