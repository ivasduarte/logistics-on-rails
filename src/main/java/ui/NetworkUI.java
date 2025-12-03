package ui;

import freightMngt.domain.ConnectionData;
import freightMngt.domain.Station;
import freightMngt.graph.Graph;
import freightMngt.services.NetworkService;

import java.util.Scanner;

public class NetworkUI implements Runnable {

    private final Scanner scanner;
    private NetworkService networkService;

    public NetworkUI() {
        this.scanner = new Scanner(System.in);
        this.networkService = new NetworkService();
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
                    System.out.println("Not implemented yet!");
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
                    exportNetworkToSVG();
                    break;
                case "0":
                    exit = true;
                    System.out.println("Exiting application...");
                    break;
                default:
                    showError("Invalid option.");
                    break;
            }

            if(!exit) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n══════════════════════ NETWORK MENU ══════════════════════");

        Graph<Station, ConnectionData> graph = networkService.getGraph();
        String status = (graph.numVertices() == 0) ? "[X] Not Loaded" : "[V] Loaded (" + graph.numVertices() + " stations)";

        System.out.println("Network Status: " + status);
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.println("01. Load Railway Network");
        System.out.println("02. Upgrade Picking Plan");
        System.out.println("03. Build Backbone Network");
        System.out.println("04. Centrality Analysis");
        System.out.println("05. Max Throughput");
        System.out.println("06. Shortest Path (Route Planner)");
        System.out.println("07. Export Network to Graphviz (.dot)");
        System.out.println(" 0. Exit");
        System.out.println("══════════════════════════════════════════════════════════");
    }

    private void loadNetwork() {
        System.out.println("\n--- Loading Network ---");

        String stationsFile = "res/stations.csv";
        String linesFile = "res/lines.csv";

        System.out.println("Reading files...");
        System.out.println(" > Stations: " + stationsFile);
        System.out.println(" > Lines:    " + linesFile);

        try {
            networkService.loadNetwork(stationsFile, linesFile);

            Graph<Station, ConnectionData> graph = networkService.getGraph();

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
        if (networkService.getGraph().numVertices() == 0) {
            showError("No network loaded to export.");
            return;
        }

        System.out.println("\n--- Exporting Network ---");
        String filename = "belgian_railway.dot";

        try {
            networkService.exportToGraphviz(filename);
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

}