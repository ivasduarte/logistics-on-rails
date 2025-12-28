package ui;

import freightMngt.controller.RouteController;
import java.util.List;
import java.util.Scanner;

public class RoutePlannerUI {
    private final RouteController controller = new RouteController();
    private final Scanner sc = new Scanner(System.in);

    public void run() {
        System.out.println("\n===== Route Planning (USLP08) =====");

        // 1. List Pending Freights
        List<String> pending = controller.getPendingFreights();
        if (pending.isEmpty()) {
            System.out.println("No pending freights found.");
            return;
        }
        System.out.println("Pending Freights List:");
        pending.forEach(System.out::println);

        // 2. Selection of Freight
        System.out.print("\nEnter Pending Freight ID: ");
        int freightId = sc.nextInt();

        // 3. List Available Rail Lines (New Step)
        // Providing a list makes it easier for the user to choose
        List<String> lines = controller.getAvailableRailLines();
        System.out.println("\nAvailable Rail Lines (Routes):");
        if (lines.isEmpty()) {
            System.out.println("No rail lines registered in the system.");
            return;
        }
        lines.forEach(System.out::println);

        // 4. Selection of Line
        System.out.print("\nEnter Rail Line ID (Route) to assign: ");
        int lineId = sc.nextInt();

        // 5. Display Detailed Operational Plan
        // Shows sequence of stations and loading/unloading actions
        System.out.println("\n--- Operational Route Plan ---");
        List<String> routeDetails = controller.getDetailedRoutePlan(lineId, freightId);

        if (routeDetails.isEmpty()) {
            System.out.println("Could not generate plan. Please check if IDs are correct.");
        } else {
            routeDetails.forEach(System.out::println);
            System.out.println("------------------------------");
        }
    }
}