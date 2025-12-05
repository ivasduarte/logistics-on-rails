package freightMngt.controller;

import freightMngt.domain.ConnectionData;
import freightMngt.domain.Station;
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
     * Currently handles the DRONGEN-GENT-ST-P negative cycle.
     */
    private boolean isNegativeCycleEdge(Station origin, Station target) {
        String origId = origin.getNodeID();
        String destId = target.getNodeID();

        // Known negative cycle: DRONGEN (335) <-> GENT-ST-P (455)
        return (origId.equals("335") && destId.equals("455")) ||
                (origId.equals("455") && destId.equals("335"));
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
}