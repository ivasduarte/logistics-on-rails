package freightMngt.services;

import freightMngt.domain.ConnectionData;
import freightMngt.domain.Station;
import freightMngt.graph.Edge;
import freightMngt.graph.Graph;
import freightMngt.graph.MapGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class NetworkService {

    private Graph<Station, ConnectionData> graph;

    public NetworkService() {
        this.graph = new MapGraph<>(false);
    }

    public Graph<Station, ConnectionData> getGraph() { return graph; }

    public void loadNetwork(String stationsFile, String linesFile) {
        Map<String, Station> stationMap = new HashMap<>();

        // read stations from file
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
}