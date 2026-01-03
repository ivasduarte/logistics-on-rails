package scheduler;

import freightMngt.domain.PendingFreight;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scheduler {

    public static void main(String[] args) {
        TrainScheduler scheduler = new TrainScheduler();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        FREIGHT MANAGER SCHEDULER                           ║");
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
                        autoAssignTrainsToFreight(scheduler, scanner);
                        break;
                    case 3:
                        scheduler.generateScheduleReport();
                        break;
                    case 4:
                        viewScheduledTrains(scheduler);
                        break;
                    case 5:
                        viewCrossingOperations(scheduler);
                        break;
                    case 6:
                        listAllTrains(scheduler);
                        break;
                    case 7:
                        listAllEndpoints(scheduler);
                        break;
                    case 8:
                        listAllRailLines(scheduler);
                        break;
                    case 9:
                        System.out.println("\n✓ Closing scheduler...");
                        running = false;
                        break;
                    default:
                        System.out.println("\n✗ Invalid option! Please choose 1-9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n✗ Please enter a valid number.");
            }
        }
        scheduler.close();
    }

    private static void printMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│              MAIN MENU                         │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│ 1. Schedule Train (Manual Route)               │");
        System.out.println("│ 2. Auto-Assign Trains to Pending Freight       │");
        System.out.println("│ 3. Generate Complete Scheduling Report         │");
        System.out.println("│ 4. View All Scheduled Trains                   │");
        System.out.println("│ 5. View Crossing Operations                    │");
        System.out.println("│ 6. List All Available Trains                   │");
        System.out.println("│ 7. List All Endpoints (Stations)               │");
        System.out.println("│ 8. List All Rail Lines                         │");
        System.out.println("│ 9. Exit                                        │");
        System.out.println("└────────────────────────────────────────────────┘");
    }

    private static void autoAssignTrainsToFreight(TrainScheduler scheduler, Scanner scanner) {
        System.out.println("\n┌─ AUTO-ASSIGN TRAINS TO FREIGHT ───────────────");
        System.out.println("│ This feature automatically schedules trains");
        System.out.println("│ based on pending freight shipments.");
        System.out.println("└───────────────────────────────────────────────");

        // Get all rail lines
        List<TrainScheduler.RailLineInfo> railLines = scheduler.getAllRailLines();
        if (railLines.isEmpty()) {
            System.out.println("\n✗ No rail lines found in database.");
            return;
        }

        System.out.println("\n┌─ STEP 1: SELECT RAIL LINE ────────────────────");
        System.out.println("│ Available Rail Lines:");
        for (TrainScheduler.RailLineInfo line : railLines) {
            System.out.printf("│   [%d] %-30s | %s → %s%n",
                    line.id, line.name, line.startName, line.endName);
        }

        int railLineId = -1;
        while (railLineId == -1) {
            System.out.print("│ Enter Rail Line ID: ");
            try {
                railLineId = Integer.parseInt(scanner.nextLine());

                // Validate rail line exists
                boolean found = false;
                for (TrainScheduler.RailLineInfo line : railLines) {
                    if (line.id == railLineId) {
                        found = true;
                        System.out.println("│ ✓ Selected: " + line.name);
                        break;
                    }
                }

                if (!found) {
                    System.out.println("│ ✗ Invalid Rail Line ID. Try again.");
                    railLineId = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("│ ✗ Invalid number format.");
            }
        }
        System.out.println("└───────────────────────────────────────────────");

// Get pending freights for this rail line
        System.out.println("\n┌─ STEP 2: FETCH PENDING FREIGHTS ──────────────");
        List<PendingFreight> allFreights = scheduler.getPendingFreightsByLine(railLineId);

        if (allFreights.isEmpty()) {
            System.out.println("│ ✓ No pending freights found for this rail line.");
            System.out.println("│   There are no freight shipments assigned to this route.");
            System.out.println("└───────────────────────────────────────────────");
            return;
        }

        // Filter out freights that already have scheduled trains or invalid trains
        List<PendingFreight> pendingFreights = new ArrayList<>();
        Set<Integer> countedTrains = new HashSet<>();  // <-- ADICIONA ESTA LINHA
        int trainsUnavailable = 0;
        int trainsInvalid = 0;
        int noTrainAssigned = 0;

        for (PendingFreight pf : allFreights) {
            if (pf.getTrainID() == null || pf.getTrainID() <= 0) {
                noTrainAssigned++;
                continue;
            }
            if (!scheduler.isValidTrainId(pf.getTrainID())) {
                trainsInvalid++;
                continue;
            }
            if (scheduler.isTrainScheduled(pf.getTrainID())) {
                // Only count each train ONCE  <-- ADICIONA ESTAS LINHAS
                if (!countedTrains.contains(pf.getTrainID())) {
                    trainsUnavailable++;
                    countedTrains.add(pf.getTrainID());
                }
                continue;
            }
            pendingFreights.add(pf);
        }

        if (pendingFreights.isEmpty()) {
            System.out.println("│ ⚠ No available freights to schedule on this rail line.");
            System.out.println("│");
            if (allFreights.size() == 1) {
                System.out.println("│ The freight on this line has already been scheduled.");
            } else {
                System.out.println("│ Status breakdown:");
                if (noTrainAssigned > 0) {
                    System.out.println("│   • " + noTrainAssigned + " freight(s) without assigned train");
                }
                if (trainsUnavailable > 0) {
                    System.out.println("│   • " + trainsUnavailable + " freight(s) cannot be scheduled");
                    System.out.println("│     (train is already in use by another freight)");
                }
                if (trainsInvalid > 0) {
                    System.out.println("│   • " + trainsInvalid + " freight(s) with invalid trains");
                }
            }
            System.out.println("└───────────────────────────────────────────────");
            return;
        }

        System.out.println("│ Found " + pendingFreights.size() + " pending freight(s) ready for scheduling:");
        System.out.println("│");
        for (int i = 0; i < pendingFreights.size(); i++) {
            PendingFreight pf = pendingFreights.get(i);
            System.out.printf("│   [%d] Freight #%d | Train #%d | %s → %s%n",
                    i + 1,
                    pf.getId(),
                    pf.getTrainID(),
                    pf.getOriginName(),
                    pf.getDestName());
        }
        System.out.println("└───────────────────────────────────────────────");

        //Select which freight to schedule
        System.out.println("\n┌─ STEP 3: SELECT FREIGHT TO SCHEDULE ──────────");
        System.out.print("│ Enter freight number (1-" + pendingFreights.size() + "): ");
        int freightChoice = -1;
        List<PendingFreight> selectedFreights = new ArrayList<>();

        try {
            freightChoice = Integer.parseInt(scanner.nextLine());

            if (freightChoice > 0 && freightChoice <= pendingFreights.size()) {
                selectedFreights.add(pendingFreights.get(freightChoice - 1));
                PendingFreight selected = pendingFreights.get(freightChoice - 1);
                System.out.println("│ ✓ Selected: Freight #" + selected.getId());
            } else {
                System.out.println("│ ✗ Invalid selection. Please choose between 1 and " + pendingFreights.size());
                System.out.println("└───────────────────────────────────────────────");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("│ ✗ Invalid number format.");
            System.out.println("└───────────────────────────────────────────────");
            return;
        }
        System.out.println("└───────────────────────────────────────────────");

        // Get departure time
        System.out.println("\n┌─ STEP 4: SET DEPARTURE TIME ──────────────────");
        LocalDateTime departureTime = null;
        while (departureTime == null) {
            System.out.print("│ Enter departure time (yyyy-MM-dd HH:mm): ");
            String dateStr = scanner.nextLine();
            try {
                departureTime = LocalDateTime.parse(dateStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                System.out.println("│ ✓ Departure time set: " +
                        departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } catch (Exception e) {
                System.out.println("│ ✗ Invalid format. Use: yyyy-MM-dd HH:mm");
            }
        }
        System.out.println("└───────────────────────────────────────────────");

        System.out.println("\n┌─ SCHEDULE RESULTS ───────────────────");
        System.out.println("│ ══════════════════════════════════════════════");

        int assignmentCount = 0;

        // For each selected freight
        for (PendingFreight pf : selectedFreights) {
            int trainId = pf.getTrainID();

            // Build route from origin to destination via the selected rail line
            TrainScheduler.Route route = buildRouteForFreight(scheduler, railLineId, pf);

            if (route == null || route.getSegments().isEmpty()) {
                System.out.println("│ ✗ Could not build route for Freight #" + pf.getId());
                continue;
            }

            // Print the automatically defined route
            System.out.println("│");
            System.out.println("│ ─────────────────────────────────────────────");
            System.out.printf("│ [%d/%d] Scheduling Train #%d for Freight #%d%n",
                    assignmentCount + 1,
                    selectedFreights.size(),
                    trainId,
                    pf.getId());
            System.out.println("│   Route: " + route.getStartName() + " → " + route.getEndName());
            System.out.println("│   Total Segments: " + route.getSegments().size());
            System.out.println("│");
            System.out.println("│   Route Details:");
            for (int i = 0; i < route.getSegments().size(); i++) {
                TrainScheduler.RouteSegment seg = route.getSegments().get(i);
                System.out.printf("│     [%d] Segment #%d | Length: %.1f km | Max Weight: %.0f kg | Tracks: %d | Electric: %s%n",
                        i + 1,
                        seg.getId(),
                        seg.getLength() / 1000.0,
                        seg.getMaxWeight(),
                        seg.getNumberOfTracks(),
                        seg.isElectric ? "Yes" : "No");
            }
            System.out.println("│");
            System.out.println("│   Departure: " + departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Schedule the train
            try {
                TrainScheduler.ScheduledTrain scheduled = scheduler.scheduleManualDispatch(
                        trainId, route, departureTime);
            } catch (IllegalArgumentException e) {
                System.out.println("│   Status: ✗ " + e.getMessage());
            } catch (Exception e) {
                System.out.println("│   Status: ✗ Unexpected error: " + e.getMessage());
            }
        }

        System.out.println("│ ══════════════════════════════════════════════");
        System.out.println("│ ASSIGNMENT COMPLETE");
        System.out.println("└───────────────────────────────────────────────");
    }

    /**
     * Builds the shortest route for a freight shipment from origin to destination
     */
    private static TrainScheduler.Route buildRouteForFreight(TrainScheduler scheduler,
                                                             int railLineId,
                                                             PendingFreight freight) {
        try {
            // Get origin and destination endpoints
            TrainScheduler.Endpoint origin = scheduler.getEndpointById(freight.getOrigin());
            TrainScheduler.Endpoint destination = scheduler.getEndpointById(freight.getDestination());

            if (origin == null || destination == null) {
                System.out.println("│ ✗ Invalid origin or destination endpoint.");
                return null;
            }

            // Find shortest path using Dijkstra's algorithm
            Map<Integer, Double> distances = new HashMap<>();
            Map<Integer, Integer> previous = new HashMap<>();
            Map<Integer, Integer> railLineUsed = new HashMap<>();
            PriorityQueue<EndpointDistance> queue = new PriorityQueue<>();
            Set<Integer> visited = new HashSet<>();

            distances.put(origin.id, 0.0);
            queue.offer(new EndpointDistance(origin.id, 0.0));

            while (!queue.isEmpty()) {
                EndpointDistance current = queue.poll();
                int currentEndpoint = current.endpointId;

                if (visited.contains(currentEndpoint)) continue;
                visited.add(currentEndpoint);

                if (currentEndpoint == destination.id) break;

                // Explore all connected rail lines
                List<TrainScheduler.RailLine> lines = scheduler.getConnectedRailLines(currentEndpoint);
                for (TrainScheduler.RailLine line : lines) {
                    int nextEndpoint = (line.startId == currentEndpoint) ? line.endId : line.startId;

                    if (visited.contains(nextEndpoint)) continue;

                    // Calculate distance for this rail line
                    List<TrainScheduler.RouteSegment> segments = scheduler.getSegmentsForRailLine(line.id);
                    double lineDistance = segments.stream().mapToDouble(TrainScheduler.RouteSegment::getLength).sum();

                    double newDistance = distances.get(currentEndpoint) + lineDistance;

                    if (!distances.containsKey(nextEndpoint) || newDistance < distances.get(nextEndpoint)) {
                        distances.put(nextEndpoint, newDistance);
                        previous.put(nextEndpoint, currentEndpoint);
                        railLineUsed.put(nextEndpoint, line.id);
                        queue.offer(new EndpointDistance(nextEndpoint, newDistance));
                    }
                }
            }

            // Check if path was found
            if (!previous.containsKey(destination.id) && origin.id != destination.id) {
                System.out.println("│ ✗ No path found from " + origin.name + " to " + destination.name);
                return null;
            }

            // Reconstruct path
            List<Integer> path = new ArrayList<>();
            int current = destination.id;
            while (current != origin.id) {
                path.add(0, current);
                current = previous.get(current);
            }
            path.add(0, origin.id);

            // Create route and add segments
            TrainScheduler.Route route = new TrainScheduler.Route(
                    -1,
                    origin.id,
                    destination.id,
                    origin.name,
                    destination.name
            );

            // Add segments for each rail line in the path
            for (int i = 0; i < path.size() - 1; i++) {
                int fromEndpoint = path.get(i);
                int toEndpoint = path.get(i + 1);
                int lineId = railLineUsed.get(toEndpoint);

                List<TrainScheduler.RouteSegment> segments = scheduler.getSegmentsForRailLine(lineId);
                for (TrainScheduler.RouteSegment segment : segments) {
                    route.addSegment(segment);
                }
            }

            if (route.getSegments().isEmpty()) {
                System.out.println("│ ✗ Could not find segments for the route.");
                return null;
            }

            return route;

        } catch (Exception e) {
            System.err.println("│ ✗ Error building route: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper class for Dijkstra's algorithm
     */
    private static class EndpointDistance implements Comparable<EndpointDistance> {
        int endpointId;
        double distance;

        EndpointDistance(int endpointId, double distance) {
            this.endpointId = endpointId;
            this.distance = distance;
        }

        @Override
        public int compareTo(EndpointDistance other) {
            return Double.compare(this.distance, other.distance);
        }
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
                    System.out.println("│ ✗ Train ID " + trainId + " does not exist in database. Try again.");
                } else if (scheduler.isTrainScheduled(trainId)) {
                    System.out.println("│ ✗ Train ID " + trainId + " already has a route assigned.");
                    System.out.println("│   Please select a different train from the list above.");
                } else {
                    System.out.println("│ ✓ Train ID " + trainId + " validated.");
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("│ ✗ Invalid number format. Please enter a numeric ID.");
            }
        }

        System.out.println("└───────────────────────────────────────────────");
        System.out.println("\n┌─ STEP 2: DEFINE ROUTE MANUALLY ───────────────");
        List<TrainScheduler.Endpoint> endpoints = scheduler.getAllEndpoints();
        if (endpoints.isEmpty()) {
            System.out.println("│ ✗ No endpoints found in database.");
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
                    System.out.println("│ ✗ Station ID " + startId + " does not exist. Please select from the list above.");
                } else {
                    System.out.println("│ ✓ Starting at: " + currentEndpoint.name);
                }
            } catch (NumberFormatException e) {
                System.out.println("│ ✗ Invalid number format.");
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
                System.out.println("│ ✗ Invalid input. Please enter a number.");
                continue;
            }

            if (lineChoice == 0) {
                System.out.println("│ ✓ Route completed at: " + currentEndpoint.name);
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
                    System.out.println("│ ✗ Invalid Line ID. Please select from the available lines above.");
                    continue;
                }

                List<TrainScheduler.RouteSegment> segments = scheduler.getSegmentsForRailLine(selectedLine.id);

                if (segments.isEmpty()) {
                    System.out.println("│ ⚠ Warning: Line " + selectedLine.name + " has no segments defined.");
                }

                for (TrainScheduler.RouteSegment seg : segments) {
                    manualRoute.addSegment(seg);
                }

                int nextId = (selectedLine.startId == currentEndpoint.id) ?
                        selectedLine.endId : selectedLine.startId;
                currentEndpoint = scheduler.getEndpointById(nextId);

                System.out.println("│ ✓ Added " + segments.size() + " segment(s) from line: " + selectedLine.name);
                System.out.println("│ ✓ Moved to: " + currentEndpoint.name);
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
            System.out.println("\n✗ Cannot schedule train: Route has no segments!");
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
                System.out.println("│ ✓ Departure time set: " +
                        departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } catch (Exception e) {
                System.out.println("│ ✗ Invalid format. Use: yyyy-MM-dd HH:mm");
            }
        }

        System.out.println("│");

        TrainScheduler.ScheduledTrain scheduled = scheduler.scheduleManualDispatch(
                trainId, manualRoute, departureTime);

        if (scheduled != null) {
            System.out.println("│ ✓ Train #" + trainId + " successfully scheduled!");
            System.out.println("│   Departure: " + departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("│   Route: " + manualRoute.getStartName() + " → " + manualRoute.getEndName());
        } else {
            System.out.println("│ ✗ Failed to schedule train");
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
            System.out.println("│ ✓ No crossing conflicts detected.");
        } else {
            System.out.println("│ ⚠ " + crossings.size() + " crossing operation(s) required:");
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