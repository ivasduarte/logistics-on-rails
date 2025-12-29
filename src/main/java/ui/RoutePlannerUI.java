package ui;

import freightMngt.controller.RouteController;
import freightMngt.dao.FreightDAO;
import freightMngt.domain.PendingFreight;

import java.util.List;
import java.util.Scanner;

public class RoutePlannerUI {
    private final RouteController controller = new RouteController();
    private final FreightDAO freightDAO = FreightDAO.getInstance();
    private final Scanner sc = new Scanner(System.in);

    public void run() {
        System.out.println("\n===== Route Planning (USLP08) =====");

        List<String> lines = controller.getAvailableRailLines();
        if (lines.isEmpty()) {
            System.out.println("No rail lines registered.");
            return;
        }

        System.out.println("\nAvailable Rail Lines:");
        lines.forEach(System.out::println);

        System.out.print("\nEnter the Line ID to view pending freights: ");
        int lineId = sc.nextInt();

        List<PendingFreight> pending = freightDAO.getPendingFreightsByLine(lineId);

        if (pending.isEmpty()) {
            System.out.println("No pending freights found for this line.");
            return;
        }

        System.out.println("\nList of Pending Freights:");
        for (PendingFreight pf : pending) {
            System.out.printf("ID: %d | Origem: %s | Destino: %s%n",
                    pf.getId(), pf.getOriginName(), pf.getDestName());
        }

        System.out.print("\nEnter the Freight ID to assign: ");
        int freightId = sc.nextInt();

        boolean success = freightDAO.assignRouteToFreight(freightId, lineId);

        if (success) {
            System.out.println("\nSuccess: Route and train assigned to freight.");

            System.out.println("\n--- Operational Route Plan ---");
            List<String> routeDetails = controller.getDetailedRoutePlan(lineId, freightId);

            if (routeDetails.isEmpty()) {
                System.out.println("[WARNING] Route assigned, but could not generate visual details.");
            } else {
                routeDetails.forEach(System.out::println);
                System.out.println("------------------------------");
            }
        } else {
            System.out.println("\n[ERROR] Failed to communicate with the database to assign the route.");
        }
    }
}