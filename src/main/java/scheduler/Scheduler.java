package scheduler;

import scheduler.TrainScheduler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Scheduler {

    public static void main(String[] args) {
        TrainScheduler scheduler = new TrainScheduler();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        FREIGHT MANAGER SCHEDULER (MANUAL DISPATCH)        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        while (running) {
            printMenu();
            System.out.print("\nChoose an Option: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        scheduleTrainWizard(scheduler, scanner);
                        break;
                    case 2:
                        scheduler.generateScheduleReport();
                        break;
                    case 3:
                        viewScheduledTrains(scheduler);
                        break;
                    case 4:
                        viewCrossingOperations(scheduler);
                        break;
                    case 5:
                        listAllTrains(scheduler);
                        break;
                    case 6:
                        listAllEndpoints(scheduler);
                        break;
                    case 7:
                        listAllRailLines(scheduler);
                        break;
                    case 8:
                        System.out.println("\n Closing scheduler...");
                        running = false;
                        break;
                    default:
                        System.out.println("\n Invalid option! Please choose 1-8.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n Please enter a valid number.");
            }
        }
        scheduler.close();
    }

    private static void printMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│              MAIN MENU                         │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│ 1. Schedule Train (Manual Route)               │");
        System.out.println("│ 2. Generate Complete Scheduling Report         │");
        System.out.println("│ 3. View All Scheduled Trains                   │");
        System.out.println("│ 4. View Crossing Operations                    │");
        System.out.println("│ 5. List All Available Trains                   │");
        System.out.println("│ 6. List All Endpoints (Stations)               │");
        System.out.println("│ 7. List All Rail Lines                         │");
        System.out.println("│ 8. Exit                                        │");
        System.out.println("└────────────────────────────────────────────────┘");
    }

    private static void scheduleTrainWizard(TrainScheduler scheduler, Scanner scanner) {
        System.out.println("\n┌─ STEP 1: SELECT TRAIN ────────────────────────");

        List<String> trains = scheduler.getUnscheduledTrains();

        if (trains.isEmpty()) {
            System.out.println("│   No unscheduled trains available.");
            System.out.println("│   All trains have already been assigned routes.");
            System.out.println("└───────────────────────────────────────────────");
            return;
        }

        System.out.println("│ Available Unscheduled Trains:");
        for (String t : trains) {
            System.out.println("│ " + t);
        }

        int trainId = -1;
        while (true) {
            System.out.print("│ Enter Train ID: ");
            try {
                String input = scanner.nextLine();
                trainId = Integer.parseInt(input);

                if (!scheduler.isValidTrainId(trainId)) {
                    System.out.println("│  Train ID " + trainId + " does not exist in database. Try again.");
                } else if (scheduler.isTrainScheduled(trainId)) {
                    System.out.println("│  Train ID " + trainId + " already has a route assigned.");
                    System.out.println("│   Please select a different train from the list above.");
                } else {
                    System.out.println("│  Train ID " + trainId + " validated.");
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("│  Invalid number format. Please enter a numeric ID.");
            }
        }

        System.out.println("└───────────────────────────────────────────────");
        System.out.println("\n┌─ STEP 2: DEFINE ROUTE MANUALLY ───────────────");
        List<TrainScheduler.Endpoint> endpoints = scheduler.getAllEndpoints();
        if (endpoints.isEmpty()) {
            System.out.println("│  No endpoints found in database.");
            System.out.println("└───────────────────────────────────────────────");
            return;
        }

        System.out.println("│ Available Stations:");
        for (TrainScheduler.Endpoint ep : endpoints) {
            System.out.printf("│   [%d] %s%n", ep.id, ep.name);
        }

        TrainScheduler.Endpoint currentEndpoint = null;
        while (currentEndpoint == null) {
            System.out.print("│ Select Start Station ID: ");
            try {
                int startId = Integer.parseInt(scanner.nextLine());
                currentEndpoint = scheduler.getEndpointById(startId);
                if (currentEndpoint == null) {
                    System.out.println("│  Station ID " + startId + " does not exist. Please select from the list above.");
                } else {
                    System.out.println("│  Starting at: " + currentEndpoint.name);
                }
            } catch (NumberFormatException e) {
                System.out.println("│  Invalid number format.");
            }
        }

        TrainScheduler.Route manualRoute = new TrainScheduler.Route(
                -1,
                currentEndpoint.id,
                -1,
                currentEndpoint.name,
                "TBD"
        );

        boolean buildingRoute = true;
        int stepCount = 0;

        while (buildingRoute) {
            stepCount++;
            System.out.println("│");
            System.out.println("│ ══════════════════════════════════════════════");
            System.out.println("│ ROUTE STEP " + stepCount);
            System.out.println("│ Current Location: " + currentEndpoint.name + " (ID: " + currentEndpoint.id + ")");
            System.out.println("│ ──────────────────────────────────────────────");
            System.out.println("│ Available Rail Lines from this location:");

            List<TrainScheduler.RailLine> lines = scheduler.getConnectedRailLines(currentEndpoint.id);

            if (lines.isEmpty()) {
                System.out.println("│ No rail lines connect from " + currentEndpoint.name);
                System.out.println("│ Route must end here.");
                buildingRoute = false;
                break;
            }

            for (TrainScheduler.RailLine line : lines) {
                int nextEndpointId = (line.startId == currentEndpoint.id) ? line.endId : line.startId;
                TrainScheduler.Endpoint nextEp = scheduler.getEndpointById(nextEndpointId);
                System.out.printf("│   [%d] Line: %-30s → Destination: %s%n",
                        line.id, line.name, nextEp.name);
            }
            System.out.println("│   [0]  Finish Route at Current Location");

            System.out.print("│ Enter Rail Line ID to continue (or 0 to finish): ");
            int lineChoice = -1;
            try {
                lineChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("│  Invalid input. Please enter a number.");
                continue;
            }

            if (lineChoice == 0) {
                System.out.println("│  Route completed at: " + currentEndpoint.name);
                buildingRoute = false;
            } else {
                TrainScheduler.RailLine selectedLine = null;
                for (TrainScheduler.RailLine l : lines) {
                    if (l.id == lineChoice) {
                        selectedLine = l;
                        break;
                    }
                }

                if (selectedLine == null) {
                    System.out.println("│  Invalid Line ID. Please select from the available lines above.");
                    continue;
                }

                List<TrainScheduler.RouteSegment> segments = scheduler.getSegmentsForRailLine(selectedLine.id);

                if (segments.isEmpty()) {
                    System.out.println("│  Warning: Line " + selectedLine.name + " has no segments defined.");
                }

                for (TrainScheduler.RouteSegment seg : segments) {
                    manualRoute.addSegment(seg);
                }

                int nextId = (selectedLine.startId == currentEndpoint.id) ?
                        selectedLine.endId : selectedLine.startId;
                currentEndpoint = scheduler.getEndpointById(nextId);

                System.out.println("│  Added " + segments.size() + " segment(s) from line: " + selectedLine.name);
                System.out.println("│  Moved to: " + currentEndpoint.name);
            }
        }

        manualRoute.setEndEndpointId(currentEndpoint.id);
        manualRoute.setEndName(currentEndpoint.name);

        System.out.println("│");
        System.out.println("│ ══════════════════════════════════════════════");
        System.out.println("│ ROUTE SUMMARY:");
        System.out.println("│   Start:    " + manualRoute.getStartName());
        System.out.println("│   End:      " + manualRoute.getEndName());
        System.out.println("│   Segments: " + manualRoute.getSegments().size());
        System.out.println("└───────────────────────────────────────────────");

        if (manualRoute.getSegments().isEmpty()) {
            System.out.println("\n Cannot schedule train: Route has no segments!");
            return;
        }

        System.out.println("\n┌─ STEP 3: DEPARTURE TIME ──────────────────────");

        LocalDateTime departureTime = null;
        while (departureTime == null) {
            System.out.print("│ Enter Departure (yyyy-MM-dd HH:mm): ");
            String dateStr = scanner.nextLine();
            try {
                departureTime = LocalDateTime.parse(dateStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                System.out.println("│  Departure time set: " +
                        departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } catch (Exception e) {
                System.out.println("│  Invalid format. Use: yyyy-MM-dd HH:mm");
            }
        }

        System.out.println("│");

        TrainScheduler.ScheduledTrain scheduled = scheduler.scheduleManualDispatch(
                trainId, manualRoute, departureTime);

        if (scheduled != null) {
            System.out.println("│   Train #" + trainId + " successfully scheduled!");
            System.out.println("│   Departure: " + departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("│   Route: " + manualRoute.getStartName() + " → " + manualRoute.getEndName());
        } else {
            System.out.println("│  Failed to schedule train");
        }

        System.out.println("└───────────────────────────────────────────────");
    }

    private static void viewScheduledTrains(TrainScheduler scheduler) {
        List<TrainScheduler.ScheduledTrain> trains = scheduler.getScheduledTrains();

        System.out.println("\n┌─ SCHEDULED TRAINS ────────────────────────────");
        if (trains.isEmpty()) {
            System.out.println("│ No trains currently scheduled.");
        } else {
            System.out.println("│ Total scheduled trains: " + trains.size());
            System.out.println("│");
            for (TrainScheduler.ScheduledTrain st : trains) {
                System.out.printf("│ Train #%-5d | %-20s | %s → %s%n",
                        st.getTrain().getId(),
                        st.getTrain().getOperator(),
                        st.getRoute().getStartName(),
                        st.getRoute().getEndName());
                System.out.printf("│             Departs: %s%n",
                        st.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                System.out.println("│ ─────────────────────────────────────────────");
            }
        }
        System.out.println("└───────────────────────────────────────────────");
    }

    private static void viewCrossingOperations(TrainScheduler scheduler) {
        System.out.println("\n┌─ CROSSING OPERATIONS ─────────────────────────");
        List<TrainScheduler.CrossingOperation> crossings = scheduler.detectAndResolveCrossings();

        if (crossings.isEmpty()) {
            System.out.println("│  No crossing conflicts detected.");
        } else {
            System.out.println("│  " + crossings.size() + " crossing operation(s) required:");
            System.out.println("│");
            for (TrainScheduler.CrossingOperation crossing : crossings) {
                System.out.printf("│ Trains #%d ↔ #%d%n",
                        crossing.getTrain1Id(), crossing.getTrain2Id());
                System.out.printf("│   Segment ID: %d%n", crossing.getSegmentId());
                System.out.printf("│   Location: %s (%s)%n",
                        crossing.getCrossingPoint().getName(),
                        crossing.getCrossingPoint().getType());
                System.out.printf("│   Position: %.1f m%n",
                        crossing.getCrossingPoint().getPosition());
                System.out.println("│ ─────────────────────────────────────────────");
            }
        }
        System.out.println("└───────────────────────────────────────────────");
    }

    private static void listAllTrains(TrainScheduler scheduler) {
        System.out.println("\n┌─ ALL TRAINS IN DATABASE ──────────────────────");
        List<String> trains = scheduler.getAvailableTrains();
        if (trains.isEmpty()) {
            System.out.println("│ No trains found.");
        } else {
            for (String train : trains) {
                System.out.println("│ " + train);
            }
        }
        System.out.println("└───────────────────────────────────────────────");
    }

    private static void listAllEndpoints(TrainScheduler scheduler) {
        System.out.println("\n┌─ ALL ENDPOINTS (STATIONS) ────────────────────");
        List<TrainScheduler.Endpoint> endpoints = scheduler.getAllEndpoints();
        if (endpoints.isEmpty()) {
            System.out.println("│ No endpoints found.");
        } else {
            for (TrainScheduler.Endpoint ep : endpoints) {
                System.out.printf("│ [%-5d] %s%n", ep.id, ep.name);
            }
        }
        System.out.println("└───────────────────────────────────────────────");
    }

    private static void listAllRailLines(TrainScheduler scheduler) {
        System.out.println("\n┌─ ALL RAIL LINES ──────────────────────────────");
        List<TrainScheduler.RailLineInfo> lines = scheduler.getAllRailLines();
        if (lines.isEmpty()) {
            System.out.println("│ No rail lines found.");
        } else {
            for (TrainScheduler.RailLineInfo line : lines) {
                System.out.printf("│ [%-5d] %-30s | %s → %s%n",
                        line.id, line.name, line.startName, line.endName);
            }
        }
        System.out.println("└───────────────────────────────────────────────");
    }
}