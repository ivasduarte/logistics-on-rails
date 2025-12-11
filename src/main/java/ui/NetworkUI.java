package ui;

import freightMngt.domain.ConnectionData;
import freightMngt.domain.UpgradeOrderResult;
import freightMngt.domain.HubMetrics;
import freightMngt.domain.Station;
import freightMngt.graph.Edge;
import freightMngt.graph.Graph;
import freightMngt.controller.NetworkController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
                    directedLineUpgradePlan();
                    break;
                case "4":
                    System.out.println("Not implemented yet!");
                    break;
                case "5":
                    railHubAnalysis();
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

    /**
     * USEI11 - Directed Line Upgrade Plan
     *
     * Computes an ordering of Belgian stations based on directed track dependencies
     * for scheduling station upgrades that respect all directional dependencies.
     * If cycles exist, displays which stations and links are involved.
     */
    private void directedLineUpgradePlan() {
        // Check if network is loaded
        if (networkController.getGraph().numVertices() == 0) {
            showError("Network not loaded! Please load the network first (option 1).");
            return;
        }

        System.out.println("     USEI11 - Directed Line Upgrade Plan                    ");

        System.out.println("\nAnalyzing directed dependencies in the railway network...");
        System.out.println("Note: Each edge A→B means 'B requires A to be upgraded first'\n");


        // Compute upgrade order using controller (returns DTO)
        UpgradeOrderResult result = networkController.computeUpgradeOrder();


        // Display results based on DTO data
        printUpgradeOrderResult(result);

        // Handle different result scenarios
        if (result.hasError()) {
            showError(result.getErrorMessage());
            return;
        }

        if (result.isSuccess()) {
            handleSuccessfulUpgradeOrder(result);
        } else if (result.hasCycles()) {
            handleCyclesDetected(result);
        }
    }

    /**
     * Prints the result summary from the DTO.
     */
    private void printUpgradeOrderResult(UpgradeOrderResult result) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          UPGRADE ORDER ANALYSIS RESULT                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        if (result.hasError()) {
            System.out.println("\nERROR: " + result.getErrorMessage());
            return;
        }

        if (result.isSuccess()) {
            LinkedList<Station> order = result.getUpgradeOrder();
            System.out.println("\nStatus: SUCCESS - Valid upgrade order found (DAG)");
            System.out.println("  Total Stations: " + order.size());

            if (!order.isEmpty()) {
                System.out.println("\n  First 5 stations in upgrade sequence:");
                int count = 0;
                for (Station station : order) {
                    if (count >= 5) break;
                    System.out.printf("    %d. %s (ID: %s)\n",
                            count + 1, station.getStationName(), station.getNodeID());
                    count++;
                }

                if (order.size() > 5) {
                    System.out.println("    ... and " + (order.size() - 5) + " more");
                }
            }
        } else if (result.hasCycles()) {
            ArrayList<LinkedList<Station>> cycles = result.getCycles();
            System.out.println("\nFAILURE - Cycles detected in dependency graph");
            System.out.println("  Total Cycles: " + cycles.size());
            System.out.println("  Stations Involved in Cycles: " + result.getStationsInCycles().size());

            if (!cycles.isEmpty()) {
                System.out.println("\n  Sample cycles detected:");
                int cycleCount = 0;
                for (LinkedList<Station> cycle : cycles) {
                    if (cycleCount >= 3) break;
                    System.out.printf("    Cycle #%d (length %d): ",
                            cycleCount + 1, cycle.size() - 1);

                    int nodeCount = 0;
                    for (Station station : cycle) {
                        if (nodeCount >= 4) {
                            System.out.print("...");
                            break;
                        }
                        System.out.print(station.getStationName());
                        if (nodeCount < cycle.size() - 1) {
                            System.out.print(" → ");
                        }
                        nodeCount++;
                    }
                    System.out.println();
                    cycleCount++;
                }

                if (cycles.size() > 3) {
                    System.out.println("    ... and " + (cycles.size() - 3) + " more cycles");
                }
            }
        }

        System.out.println("─────────────────────────────────────────────────────────────\n");
    }

    /**
     * Handles the case where topological sort succeeded (no cycles).
     */
    private void handleSuccessfulUpgradeOrder(UpgradeOrderResult result) {
        LinkedList<Station> order = result.getUpgradeOrder();

        showSuccess("Valid upgrade order computed successfully!");
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│              COMPLETE STATION UPGRADE ORDER                 │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.printf("Total stations: %d\n\n", order.size());

        // Display preview (first 20 stations)
        int previewLimit = Math.min(20, order.size());
        System.out.println("Preview of upgrade sequence (first " + previewLimit + " stations):");
        System.out.println("╔═══════╦═══════════╦══════════════════════════════════════════╗");
        System.out.println("║ Order ║ Station ID║ Station Name                             ║");
        System.out.println("╠═══════╬═══════════╬══════════════════════════════════════════╣");

        int orderNum = 1;
        for (Station station : order) {
            if (orderNum > previewLimit) break;

            String name = station.getStationName();
            if (name.length() > 38) name = name.substring(0, 35) + "...";

            System.out.printf("║ %-5d ║ %-9s ║ %-40s ║\n",
                    orderNum, station.getNodeID(), name);
            orderNum++;
        }

        System.out.println("╚═══════╩═══════════╩══════════════════════════════════════════╝");

    }

    /**
     * Handles the case where cycles were detected in the graph.
     */
    private void handleCyclesDetected(UpgradeOrderResult result) {
        ArrayList<LinkedList<Station>> cycles = result.getCycles();
        List<Station> stationsInCycles = result.getStationsInCycles();

        showError("CYCLES DETECTED! Cannot compute valid upgrade order.");
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│         DEPENDENCY CYCLES FOUND IN NETWORK                  │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.printf("Total cycles detected: %d\n", cycles.size());
        System.out.printf("Unique stations involved in cycles: %d\n\n", stationsInCycles.size());

        System.out.println("These stations have circular dependencies and must be addressed:");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              STATIONS INVOLVED IN CYCLES                   ║");
        System.out.println("╠═══════════╦════════════════════════════════════════════════╣");
        System.out.println("║ Station ID║ Station Name                                   ║");
        System.out.println("╠═══════════╬════════════════════════════════════════════════╣");

        int displayLimit = Math.min(15, stationsInCycles.size());
        for (int i = 0; i < displayLimit; i++) {
            Station station = stationsInCycles.get(i);
            String name = station.getStationName();
            if (name.length() > 48) name = name.substring(0, 45) + "...";

            System.out.printf("║ %-9s ║ %-48s ║\n", station.getNodeID(), name);
        }

        System.out.println("╚═══════════╩════════════════════════════════════════════════╝");

        if (stationsInCycles.size() > displayLimit) {
            System.out.printf("\n... and %d more stations\n", stationsInCycles.size() - displayLimit);
        }

        // Display cycle details
        System.out.println("\n─────────────────────────────────────────────────────────────");
        System.out.println("CYCLE DETAILS:");
        System.out.println("─────────────────────────────────────────────────────────────");

        int cycleNum = 1;
        int maxCyclesToShow = Math.min(5, cycles.size());

        for (LinkedList<Station> cycle : cycles) {
            if (cycleNum > maxCyclesToShow) break;

            System.out.printf("\n[Cycle #%d] Length: %d stations\n", cycleNum, cycle.size() - 1);
            System.out.print("Path: ");

            for (int i = 0; i < cycle.size(); i++) {
                Station station = cycle.get(i);
                System.out.print(station.getStationName());

                if (i < cycle.size() - 1) {
                    System.out.print(" → ");
                }
            }
            System.out.println();
            cycleNum++;
        }
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
     * Analyzes and ranks railway hubs based on various centrality metrics in the transportation network.
     *
     * The method performs the following operations:
     * 1. Validates whether the transportation network graph is loaded. If not, displays an error message
     *    and terminates the operation.
     * 2. Requests user input to determine the number of top hubs (N) to analyze and rank.
     * 3. Calculates centrality metrics for the railway hubs, including Degree, Strength, Betweenness,
     *    Harmonic Closeness, and Hub Score, using the network controller.
     * 4. Displays a summary table in the console with rankings of the top N hubs and their metrics.
     * 5. Exports detailed hub analysis results to a CSV file named "hubs_ranking.csv".
     *
     * Error handling is included for invalid user inputs, exceptions during metric calculations, and
     * graph data issues (e.g., empty or disconnected graph).
     *
     * Outputs include a console table summarizing hub rankings and a CSV file with detailed results
     * for statistical or archival purposes.
     */
    private void railHubAnalysis() {
        // 1. Check if the network is loaded
        if (networkController.getGraph().numVertices() == 0) {
            showError("Network not loaded! Please load the network first (option 1).");
            return;
        }

        System.out.println("\n--- Executing Rail Hub Centrality Analysis ---");

        // 2. Request user input (Top N)
        int n = 0;
        while (n <= 0) {
            System.out.print("Enter the number of Top Hubs to analyze/rank: ");
            try {
                String input = scanner.nextLine().trim();
                n = Integer.parseInt(input);
                if (n <= 0) System.out.println("Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }

        System.out.println("\nCalculating centrality metrics (Betweenness, Harmonic, Strength, Degree)...");

        try {
            // 3. Get data from Controller
            List<HubMetrics<Station>> topHubs = networkController.calculateAndRankHubs(n);

            if (topHubs.isEmpty()) {
                showError("No hubs calculated. Check graph connectivity.");
                return;
            }

            // 4. Display Preview Table in Console
            System.out.println("\nTop " + n + " Hubs Ranking Preview:");
            System.out.println("═════════════════════════════════════════════════════════════════════════════════════════");
            System.out.printf("| %-6s | %-25s | %-6s | %-8s | %-11s | %-10s | %-8s |\n",
                    "ID", "Station Name", "Degree", "Strength", "Between.", "Harmonic", "HubScore");
            System.out.println("─────────────────────────────────────────────────────────────────────────────────────────");

            for (HubMetrics<Station> hub : topHubs) {
                // Truncate name if too long to prevent table breakage
                String name = hub.getVertex().getStationName();
                if (name.length() > 25) name = name.substring(0, 22) + "...";

                System.out.printf("| %-6s | %-25s | %-6.0f | %-8.1f | %-11.4f | %-10.4f | %-8.4f |\n",
                        hub.getVertex().getNodeID(),
                        name,
                        hub.getDegree(),
                        hub.getStrength(),
                        hub.getBetweenness(),
                        hub.getHarmonic(),
                        hub.getHubScore());
            }
            System.out.println("═════════════════════════════════════════════════════════════════════════════════════════");

            // 5. Export CSV
            String filename = "hubs_ranking.csv";
            System.out.println("\nExporting detailed results to CSV...");

            networkController.exportHubsToCSV(filename, n);

            showSuccess("Hub analysis exported successfully to file: " + filename);
            System.out.println("Format: stid, stname, degree, strength, betweenness, harmonic_closeness, hubscore");

        } catch (Exception e) {
            showError("Error during hub analysis: " + e.getMessage());
            e.printStackTrace();
        }
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