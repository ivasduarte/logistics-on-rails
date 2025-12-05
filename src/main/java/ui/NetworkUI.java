package ui;

import freightMngt.domain.ConnectionData;
import freightMngt.domain.Station;
import freightMngt.graph.Edge;
import freightMngt.graph.Graph;
import freightMngt.controller.NetworkController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class NetworkUI implements Runnable {
    private final Scanner scanner;
    private static final String STATIONS_FILE = "res/stations.csv";
    private static final int MAX_LINES = 560;

    private NetworkController networkController;

    public NetworkUI() {
        this.scanner = new Scanner(System.in);
        this.networkController = new NetworkController();
    }

    @Override
    public void run() {
        boolean exit = false;

        while (!exit) {
            showMainMenu();
            System.out.print("Enter your option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    loadNetwork();
                    break;
                case "2":
                    exportNetworkToSVG();
                    break;
                case "3":
                    System.out.println("Not implemented yet!");
                    break;
                case "4":
                    System.out.println("Not implemented yet!");
                    break;
                case "5":
                    System.out.println("Not implemented yet!");
                    break;
                case "6":
                    System.out.println("Not implemented yet!");
                    break;
                case "7":
                    riskAware();
                    break;
                case "0":
                    exit = true;
                    System.out.println("Exiting application...");
                    break;
                default:
                    showError("Invalid option.");
                    break;
            }

            if (!exit) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n══════════════════════ NETWORK MENU ══════════════════════");

        Graph<Station, ConnectionData> graph = networkController.getGraph();
        String status = (graph.numVertices() == 0) ? "[X] Not Loaded" : "[V] Loaded (" + graph.numVertices() + " stations)";

        System.out.println("Network Status: " + status);
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.println("01. Load Railway Network");
        System.out.println("02. Export Network to Graphviz (.dot)");
        System.out.println("03. Directed Line Upgrade Plan");
        System.out.println("04. Minimal Backbone Network");
        System.out.println("05. Rail Hub Centrality Analysis");
        System.out.println("06. Maximum Throughput Between Two Hubs");
        System.out.println("07. Risk-Aware Shortest Path");
        System.out.println(" 0. Exit");
        System.out.println("════════════════════════════════════════════════════════════");
    }

    private void loadNetwork() {
        System.out.println("\n--- Loading Network ---");

        String stationsFile = "res/stations.csv";
        String linesFile = "res/lines.csv";

        System.out.println("Reading files...");
        System.out.println(" > Stations: " + stationsFile);
        System.out.println(" > Lines:    " + linesFile);

        try {
            networkController.loadNetwork(stationsFile, linesFile);

            Graph<Station, ConnectionData> graph = networkController.getGraph();

            if (graph.numVertices() > 0) {
                showSuccess("\nNetwork loaded successfully!");
                System.out.println(" > Stations loaded: " + graph.numVertices());
                System.out.println(" > Connections established: " + graph.numEdges());
            } else {
                showError("Network loaded but is empty. Check file content/paths.");
            }
        } catch (Exception e) {
            showError("Critical error loading files: " + e.getMessage());
        }
    }

    private void exportNetworkToSVG() {
        if (networkController.getGraph().numVertices() == 0) {
            showError("No network loaded to export.");
            return;
        }

        System.out.println("\n--- Exporting Network ---");
        String filename = "belgian_railway.dot";

        try {
            networkController.exportToGraphviz(filename);
            showSuccess("File generated: " + filename);
            System.out.println("To convert to SVG, run in terminal:");
            System.out.println("neato -Tsvg " + filename + " -o belgian_railway.svg");
        } catch (Exception e) {
            showError("Error exporting file: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        System.out.println("[V] " + message);
    }

    private void showError(String message) {
        System.out.println("[X] " + message);
    }

    private void riskAware() {
        // check if the network is loaded first
        if (networkController.getGraph().numVertices() == 0) {
            showError("Network not loaded! Please load the network first (option 1).");
            return;
        }

        System.out.println("\nExecuting USEI15 - Risk-Aware Shortest Path...");

        int startLineNumber = getValidStationNumber("initial");
        if (startLineNumber == -1) return;

        int endLineNumber = getValidStationNumber("final");
        if (endLineNumber == -1) return;

        printStationsList(startLineNumber, endLineNumber);

        Station vOrig = getStationFromLineNumber(startLineNumber);
        Station vDest = getStationFromLineNumber(endLineNumber);

        if (vOrig == null || vDest == null) {
            showError("Could not find stations in the loaded network. Check if stations.csv IDs match the graph.");
            return;
        }

        processShortestPathBellmanFord(vOrig, vDest);
    }



    /**
     * Busca o Station ID no arquivo CSV correspondente ao número da linha de entrada
     * e retorna o objeto Station correspondente usando o NetworkController.
     */
    private Station getStationFromLineNumber(int inputLineNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(STATIONS_FILE))) {
            String line;
            int currentLine = 1;
            br.readLine(); // skip header
            currentLine++;

            int desiredFileLine = inputLineNumber + 1;

            while ((line = br.readLine()) != null) {
                if (currentLine == desiredFileLine) {
                    String[] cols = line.split(",");
                    if (cols.length < 1) {
                        showError("Invalid line format at line " + currentLine);
                        return null;
                    }
                    String stationId = cols[0].trim();
                    return networkController.getStationByID(stationId);
                }
                currentLine++;
            }
        } catch (IOException e) {
            showError("Error reading stations file during lookup: " + e.getMessage());
        }
        return null;
    }

    private int getValidStationNumber(String stationType) {
        int lineNumber = -1;
        while (lineNumber < 1 || lineNumber > MAX_LINES) {
            System.out.printf("\nEnter line number for the %s station (1 to %d): ", stationType, MAX_LINES);

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                lineNumber = Integer.parseInt(input);

                if (lineNumber < 1 || lineNumber > MAX_LINES) {
                    System.out.println("Invalid number. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer number.");
            }
        }
        return lineNumber;
    }

    private void printStationsList(int startLineNumber, int endLineNumber) {
        java.util.Map<Integer, String> foundLines = new java.util.HashMap<>();

        int desiredStartLine = startLineNumber + 1;
        int desiredEndLine = endLineNumber + 1;

        try (BufferedReader br = new BufferedReader(new FileReader(STATIONS_FILE))) {
            String line;
            int currentLine = 1;

            br.readLine(); // skip header
            currentLine++;

            while ((line = br.readLine()) != null) {
                if (currentLine == desiredStartLine) {
                    foundLines.put(desiredStartLine, line);
                } else if (currentLine == desiredEndLine) {
                    foundLines.put(desiredEndLine, line);
                }

                if (foundLines.size() == 2) {
                    break;
                }
                currentLine++;
            }
        } catch (IOException e) {
            showError("Error reading stations file: " + e.getMessage());
            return;
        }

        printStationDetails("INITIAL", startLineNumber, desiredStartLine, foundLines.get(desiredStartLine));
        printStationDetails("FINAL", endLineNumber, desiredEndLine, foundLines.get(desiredEndLine));
    }


    private void printStationDetails(String type, int inputLine, int fileLine, String foundLine) {
        if (foundLine != null) {
            String[] cols = foundLine.split(",");
            String id = cols[0];
            String name = cols[1];

            System.out.println("\n FOUND " + type + " STATION (Line " + inputLine + "): ");
            System.out.println("---------------------");
            System.out.println("Station ID: " + id);
            System.out.println("Station Name: " + name);
            System.out.printf("Complete file line: %s\n", foundLine);
            System.out.println("----------------------");
        } else {
            showError("Station " + type + " (line " + inputLine + ") not found in file. Check file integrity/size.");
        }
    }

    private void processShortestPathBellmanFord(Station vOrig, Station vDest) {
        System.out.println("\n--- Calculating Risk-Aware Shortest Path (Bellman-Ford) ---");
        System.out.println("Origin: " + vOrig.getStationName() + " (" + vOrig.getNodeID() + ")");
        System.out.println("Destination: " + vDest.getStationName() + " (" + vDest.getNodeID() + ")");
        System.out.println("Note: Known negative cycles have been neutralized.\n");

        java.util.Comparator<Double> costComparator = Double::compare;
        java.util.function.BinaryOperator<Double> costSum = Double::sum;
        Double zeroCost = 0.0;

        java.util.LinkedList<Station> shortPath = new java.util.LinkedList<>();

        Double totalCost = networkController.shortestPathBellmanFord(
                vOrig, vDest, costComparator, costSum, zeroCost, shortPath);

        if (totalCost == null) {
            if (!shortPath.isEmpty()) {
                showError("NEGATIVE CYCLE DETECTED! The shortest path is undefined.");
                System.out.println("Cycle involved stations (in order):");
                printNegativeCycle(shortPath);
                System.out.println("\nThis indicates an additional negative cycle in the data.");
                System.out.println("Please report this to the data administrator.");
            } else {
                showError("Path not found between " + vOrig.getStationName() + " and " + vDest.getStationName() + ".");
                System.out.println("These stations may be in disconnected components of the network.");
            }
        } else {
            showSuccess("\nShortest Path Found!");
            printShortestPath(vOrig, totalCost, shortPath);
        }
    }

    private void printShortestPath(Station vOrig, Double totalCost, java.util.LinkedList<Station> shortPath) {
        System.out.printf("Total Accumulated Cost: %.2f\n", totalCost);
        System.out.println("Path (Station Name / Accumulated Cost):");
        System.out.println("------------------------------------------------------------------");

        Graph<Station, ConnectionData> graph = networkController.getGraph();

        if (shortPath.isEmpty()) {
            System.out.println("Path is empty.");
            return;
        }

        Station current = shortPath.getFirst();
        Double accumulatedCost = 0.0;

        System.out.printf("   > %s (ID: %s)\n", current.getStationName(), current.getNodeID());

        for (int i = 1; i < shortPath.size(); i++) {
            Station next = shortPath.get(i);

            Edge<Station, ConnectionData> edge = graph.edge(current, next);
            if (edge != null) {
                Double edgeCost = edge.getWeight().getCost(); // Valor ORIGINAL

                // Adjust if this is a negative cycle edge that was fixed
                if (isNegativeCycleEdge(current, next)) {
                    edgeCost = Math.abs(edgeCost);
                }

                accumulatedCost += edgeCost;

                System.out.printf("     [Cost: %.2f] -> %s (ID: %s) [Acc. Cost: %.2f]\n",
                        edgeCost, next.getStationName(), next.getNodeID(), accumulatedCost);
            }

            current = next;
        }

        System.out.println("------------------------------------------------------------------");
    }

    private boolean isNegativeCycleEdge(Station origin, Station target) {
        String origId = origin.getNodeID();
        String destId = target.getNodeID();

        return (origId.equals("335") && destId.equals("455")) ||
                (origId.equals("455") && destId.equals("335"));
    }

    private void printNegativeCycle(java.util.LinkedList<Station> cyclePath) {
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < cyclePath.size(); i++) {
            Station station = cyclePath.get(i);
            String arrow = (i == cyclePath.size() - 1) ? "" : " -> ";
            System.out.printf("%s%s", station.getStationName(), arrow);
        }
        System.out.println("\n------------------------------------------------------------------");
    }
}