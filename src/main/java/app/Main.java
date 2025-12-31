package app;

import freightMngt.controller.NetworkController;
import freightMngt.controller.RouteController;
import scheduler.Scheduler;
import ui.RoutePlannerUI;
import ui.TrainAssemblyUI;
import utils.LoadData;
import freightMngt.domain.Station;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);

        // 1. Centralized initial data loading
        LoadData.loadAllData();

        // 2. Network Controller Initialization and data loading
        NetworkController networkController = new NetworkController();
        networkController.loadNetwork("res/stations.csv", "res/lines.csv");

        // 3. Initialization of USLP08 components with a correct graph type
        RouteController routeController = new RouteController();
        RoutePlannerUI routePlannerUI = new RoutePlannerUI();

        // 4. Initialization of USLP09
        TrainAssemblyUI trainAssemblyUI = new TrainAssemblyUI(networkController);

        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. ESINF User Stories");
            System.out.println("2. LAPR US03 (Travel Time)");
            System.out.println("3. LAPR US07 (Scheduler)");
            System.out.println("4. LAPR US08 (Route Planning)");
            System.out.println("5. LAPR US09 (Train Assembly)");
            System.out.println("0. EXIT");
            System.out.print("Select option: ");

            String input = sc.nextLine();
            int option;
            try {
                option = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (option) {
                case 1 -> MenuESINF.menuESINF();
                case 2 -> LaprMain.main();
                case 3 -> Scheduler.main(args);
                case 4 -> handleRoutePlanning(routePlannerUI);
                case 5 -> trainAssemblyUI.run();
                case 0 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void handleRoutePlanning(RoutePlannerUI ui) {
        ui.run();
    }
}