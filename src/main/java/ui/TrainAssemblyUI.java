package ui;

import freightMngt.controller.NetworkController;
import freightMngt.controller.TrainAssemblyController;
import freightMngt.domain.RollingStockDTO;
import freightMngt.domain.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrainAssemblyUI {
    private final TrainAssemblyController controller;
    private final NetworkController networkController = new NetworkController();
    private final Scanner sc = new Scanner(System.in);

    // Construtor recebe o controller da rede
    public TrainAssemblyUI(NetworkController netController) {
        // Passa o controller recebido para o TrainAssemblyController
        this.controller = new TrainAssemblyController(netController);
    }

    public void run() {
        System.out.println("\n===== Assemble Train (USLP09) =====");

        System.out.println("--- Available Stations ---");
        List<Station> stations = controller.getAllStations();

        if (stations.isEmpty()) {
            System.out.println("No stations found in the network.");
            return;
        }

        // Formatação limpa em colunas ou lista simples
        for (Station s : stations) {
            System.out.printf("ID: %-5s | Name: %s%n", s.getNodeID(), s.getStationName());
        }
        System.out.println("--------------------------");
        // ------------------------------------

        String originId = "";
        boolean valid = false;

        while (!valid) {
            System.out.print("Enter Freight/Route Origin Station ID from the list above: ");
            originId = sc.next();

            String finalOriginId = originId;
            if (stations.stream().anyMatch(s -> s.getNodeID().equals(finalOriginId))) {
                valid = true;
            } else {
                System.out.println("Invalid ID. Please try again.");
            }
        }

        System.out.println("\nFetching available resources...");
        List<RollingStockDTO> resources = controller.getRollingStockForAssembly(originId);

        if (resources.isEmpty()) {
            System.out.println("No rolling stock available.");
            return;
        }

        // Mostrar lista formatada
        System.out.println("\n--- Available Rolling Stock ---");
        System.out.println("(Sorted: Parked items by descending distance from Origin)");
        for (int i = 0; i < resources.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, resources.get(i));
        }

        // Seleção simples
        List<Integer> selectedLocos = new ArrayList<>();
        List<Integer> selectedWagons = new ArrayList<>();

        System.out.println("\nSelect items by number (comma separated, e.g., 1,3,4) or 0 to finish:");
        sc.nextLine(); // Consumir newline
        String input = sc.nextLine();

        String[] indices = input.split(",");
        for (String indexStr : indices) {
            try {
                int idx = Integer.parseInt(indexStr.trim()) - 1;
                if (idx >= 0 && idx < resources.size()) {
                    RollingStockDTO item = resources.get(idx);
                    if ("LOCOMOTIVE".equals(item.getType())) {
                        selectedLocos.add(item.getId());
                    } else {
                        selectedWagons.add(item.getId());
                    }
                    System.out.println("Added: " + item.getName());
                }
            } catch (NumberFormatException e) {
                // ignorar
            }
        }

        System.out.print("Enter Train ID to assign these resources: ");
        int trainId = sc.nextInt();

        boolean success = controller.saveTrainAssembly(trainId, selectedLocos, selectedWagons);
        if (success) {
            System.out.println("Train assembled successfully!");
        } else {
            System.out.println("Error assembling train.");
        }
    }
}