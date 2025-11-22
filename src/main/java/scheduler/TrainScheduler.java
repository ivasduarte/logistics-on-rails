package scheduler;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TrainScheduler - Train scheduling and dispatch system
 * Implements USLP07 - Train dispatch with simple/complex routes
 */
public class TrainScheduler {

    private Connection connection;
    private List<ScheduledTrain> scheduledTrains;

    public TrainScheduler() {
        this.scheduledTrains = new ArrayList<>();
        connectToDatabase();
    }

    public void connectToDatabase() {
        try {
            String url = "jdbc:oracle:thin:@localhost:1521:xe";
            String user = "system";
            String pass = "ipp-isep-g091";
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            System.err.println(" Error loading JDBC driver: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println(" Error connecting to the database: " + e.getMessage());
        }
    }

    // ==========================================
    // DATABASE QUERY METHODS
    // ==========================================

    /**
     * Returns a list of all trains in the DB for display
     */
    public List<String> getAvailableTrains() {
        List<String> list = new ArrayList<>();
        String query = "SELECT id, operator, \"date\", pathID FROM Train ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(String.format("[ID: %d] Operator: %s (Date: %s)",
                        rs.getInt("id"), rs.getString("operator"), rs.getDate("date")));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching trains: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns a list of trains that have NOT been scheduled yet
     */
    public List<String> getUnscheduledTrains() {
        List<String> list = new ArrayList<>();
        Set<Integer> scheduledIds = scheduledTrains.stream()
                .map(st -> st.getTrain().getId())
                .collect(Collectors.toSet());

        String query = "SELECT id, operator, \"date\", pathID FROM Train ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int trainId = rs.getInt("id");
                if (!scheduledIds.contains(trainId)) {
                    list.add(String.format("[ID: %d] Operator: %s (Date: %s)",
                            trainId, rs.getString("operator"), rs.getDate("date")));
                }
            }
        } catch (SQLException e) {
            System.err.println(" Error fetching trains: " + e.getMessage());
        }
        return list;
    }

    public boolean isValidTrainId(int id) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM Train WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isTrainScheduled(int trainId) {
        return scheduledTrains.stream()
                .anyMatch(st -> st.getTrain().getId() == trainId);
    }

    public List<Endpoint> getAllEndpoints() {
        List<Endpoint> endpoints = new ArrayList<>();
        String query = "SELECT endpointID, name FROM Endpoint ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                endpoints.add(new Endpoint(rs.getInt("endpointID"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println(" Error fetching endpoints: " + e.getMessage());
        }
        return endpoints;
    }

    public Endpoint getEndpointById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT endpointID, name FROM Endpoint WHERE endpointID = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return new Endpoint(rs.getInt("endpointID"), rs.getString("name"));
        } catch (SQLException e) {
            System.err.println(" Error fetching endpoint: " + e.getMessage());
        }
        return null;
    }

    public List<RailLine> getConnectedRailLines(int endpointId) {
        List<RailLine> lines = new ArrayList<>();
        String query = "SELECT id, name, startID, endID FROM Rail_Line WHERE startID = ? OR endID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, endpointId);
            stmt.setInt(2, endpointId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lines.add(new RailLine(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("startID"),
                        rs.getInt("endID")
                ));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching rail lines: " + e.getMessage());
        }
        return lines;
    }

    public List<RailLineInfo> getAllRailLines() {
        List<RailLineInfo> lines = new ArrayList<>();
        String query = """
            SELECT rl.id, rl.name, 
                   e1.name as startName, e2.name as endName
            FROM Rail_Line rl
            JOIN Endpoint e1 ON rl.startID = e1.endpointID
            JOIN Endpoint e2 ON rl.endID = e2.endpointID
            ORDER BY rl.name
        """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                lines.add(new RailLineInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("startName"),
                        rs.getString("endName")
                ));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching all rail lines: " + e.getMessage());
        }
        return lines;
    }

    public List<RouteSegment> getSegmentsForRailLine(int railLineId) {
        List<RouteSegment> segments = new ArrayList<>();
        String query = """
            SELECT ls.id, ls.length, ls.max_weight, lt.numberTracks, 
                   lt.isElectric
            FROM Line_Segment ls
            JOIN Line_Type lt ON ls.Line_TypeID = lt.id
            WHERE ls.Rail_LineId = ?
            ORDER BY ls.orderPosition
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, railLineId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                segments.add(new RouteSegment(
                        rs.getInt("id"),
                        rs.getDouble("length"),
                        rs.getDouble("max_weight"),
                        rs.getInt("numberTracks"),
                        rs.getString("isElectric").equals("Yes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching segments: " + e.getMessage());
        }
        return segments;
    }

    public ScheduledTrain scheduleManualDispatch(int trainId, Route manualRoute, LocalDateTime departureTime) {
        try {
            if (isTrainScheduled(trainId)) {
                throw new IllegalArgumentException("Train #" + trainId + " is already scheduled. Cannot assign multiple routes to the same train.");
            }

            Train train = loadTrain(trainId);
            if (train == null) {
                throw new IllegalArgumentException("Train not found: " + trainId);
            }

            if (manualRoute.getSegments().isEmpty()) {
                throw new IllegalArgumentException("Route has no segments defined");
            }

            ScheduledTrain scheduledTrain = new ScheduledTrain(train, manualRoute, departureTime);

            calculateTravelSpeed(scheduledTrain);
            calculatePassageTimes(scheduledTrain);

            loadSidingsForRoute(manualRoute);

            scheduledTrains.add(scheduledTrain);

            System.out.println("✓ Train " + trainId + " scheduled with manual route.");
            return scheduledTrain;

        } catch (Exception e) {
            System.err.println("✗ Error scheduling train: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<ScheduledTrain> getScheduledTrains() {
        return new ArrayList<>(scheduledTrains);
    }

    private Train loadTrain(int trainId) throws SQLException {
        String query = """
            SELECT t.id, t.operator, t."date", t.pathID,
                   l.id as locId, l.name as locName, lm.power, lm.weight as locWeight,
                   lm.maxSpeed, lm.operationalSpeed
            FROM Train t
            LEFT JOIN Locomotive_Train lt ON t.id = lt.Trainid
            LEFT JOIN Locomotive l ON lt.Locomotiveid = l.id
            LEFT JOIN Locomotive_Model lm ON l.modelID = lm.id
            WHERE t.id = ?
        """;

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, trainId);
        ResultSet rs = stmt.executeQuery();

        Train train = null;
        List<Locomotive> locomotives = new ArrayList<>();

        while (rs.next()) {
            if (train == null) {
                train = new Train(
                        rs.getInt("id"),
                        rs.getString("operator"),
                        rs.getDate("date").toLocalDate(),
                        rs.getInt("pathID")
                );
            }

            if (rs.getInt("locId") > 0) {
                Locomotive loc = new Locomotive(
                        rs.getInt("locId"),
                        rs.getString("locName"),
                        rs.getDouble("power"),
                        rs.getDouble("locWeight"),
                        rs.getDouble("maxSpeed"),
                        rs.getDouble("operationalSpeed")
                );
                locomotives.add(loc);
            }
        }

        if (train != null) {
            train.setLocomotives(locomotives);
            loadWagons(train);
        }

        return train;
    }

    private void loadWagons(Train train) throws SQLException {
        String query = """
            SELECT w.id, wm.weight, wm.payload, wm.maxSpeed,
                   COALESCE(wb.storedGeneralGoods, 0) + 
                   COALESCE(wh.storedCoal, 0) + COALESCE(wh.storedGrains, 0) +
                   COALESCE(wt.storedLiquids, 0) + COALESCE(wt.storedChemicals, 0) +
                   COALESCE(wf.storedContainers, 0) + COALESCE(wf.storedVehicles, 0) +
                   COALESCE(wr.storedPerishableGoods, 0) as cargoLoad
            FROM Freight f
            LEFT JOIN Wagon_Freight wf2 ON f.id = wf2.Freightid
            LEFT JOIN Wagon w ON wf2.Wagonid = w.id
            LEFT JOIN Wagon_Model wm ON w.modelID = wm.id
            LEFT JOIN Wagon_Boxcar wb ON w.id = wb.id
            LEFT JOIN Wagon_Hopper_Car wh ON w.id = wh.id
            LEFT JOIN Wagon_Tank_Car wt ON w.id = wt.id
            LEFT JOIN Wagon_Flatcar wf ON w.id = wf.id
            LEFT JOIN Wagon_Refrigerated_Car wr ON w.id = wr.id
            WHERE f.trainID = ?
        """;

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, train.getId());
        ResultSet rs = stmt.executeQuery();

        List<Wagon> wagons = new ArrayList<>();
        while (rs.next()) {
            Wagon wagon = new Wagon(
                    rs.getInt("id"),
                    rs.getDouble("weight"),
                    rs.getDouble("payload"),
                    rs.getDouble("maxSpeed"),
                    rs.getDouble("cargoLoad")
            );
            wagons.add(wagon);
        }

        train.setWagons(wagons);
    }

    private void loadSidingsForRoute(Route route) throws SQLException {
        Set<Integer> segmentIds = new HashSet<>();
        for (RouteSegment seg : route.getSegments()) {
            segmentIds.add(seg.getId());
        }

        if (segmentIds.isEmpty()) return;

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < segmentIds.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }

        String query = "SELECT s.id, s.position, s.length, s.segmentID " +
                "FROM Siding s WHERE s.segmentID IN (" + inClause + ") ORDER BY s.position";

        PreparedStatement stmt = connection.prepareStatement(query);
        int paramIndex = 1;
        for (Integer segId : segmentIds) {
            stmt.setInt(paramIndex++, segId);
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Siding siding = new Siding(
                    rs.getInt("id"),
                    rs.getDouble("position"),
                    rs.getDouble("length"),
                    rs.getInt("segmentID")
            );
            route.addSiding(siding);
        }
    }

    void calculateTravelSpeed(ScheduledTrain scheduledTrain) {
        Train train = scheduledTrain.getTrain();
        Route route = scheduledTrain.getRoute();
        double totalWeight = train.getTotalWeight();
        double totalPower = train.getTotalPower();
        double locMaxSpeed = train.getMaxLocomotiveSpeed();
        double wagonMaxSpeed = train.getMaxWagonSpeed();

        for (RouteSegment segment : route.getSegments()) {
            double segmentMaxSpeed = 120.0;

            if (totalWeight > segment.getMaxWeight()) {
                System.out.println("⚠ WARNING: Train weight (" + String.format("%.0f", totalWeight) +
                        " kg) exceeds segment " + segment.getId() + " limit (" +
                        String.format("%.0f", segment.getMaxWeight()) + " kg)");
            }

            double powerWeightRatio = totalPower / totalWeight;

            double calculatedSpeed = Math.min(segmentMaxSpeed, Math.min(locMaxSpeed, wagonMaxSpeed));

            if (powerWeightRatio < 20) {
                calculatedSpeed *= 0.70;
            } else if (powerWeightRatio > 50) {
                calculatedSpeed *= 0.95;
            } else {
                calculatedSpeed *= 0.85;
            }

            segment.setPermittedSpeed(calculatedSpeed);
        }
    }

    void calculatePassageTimes(ScheduledTrain scheduledTrain) {
        Route route = scheduledTrain.getRoute();
        LocalDateTime currentTime = scheduledTrain.getDepartureTime();
        double cumulativeDistance = 0;
        List<PassageTime> passageTimes = new ArrayList<>();

        passageTimes.add(new PassageTime(
                route.getStartName(),
                currentTime,
                cumulativeDistance,
                "DEPARTURE"
        ));

        for (int i = 0; i < route.getSegments().size(); i++) {
            RouteSegment segment = route.getSegments().get(i);
            double segmentLength = segment.getLength();
            double speed = segment.getPermittedSpeed();

            double timeHours = (segmentLength / 1000.0) / speed;

            currentTime = currentTime.plusMinutes((long) (timeHours * 60));
            cumulativeDistance += segmentLength;

            passageTimes.add(new PassageTime(
                    "Segment " + segment.getId(),
                    currentTime,
                    cumulativeDistance,
                    "PASSAGE"
            ));
        }

        passageTimes.add(new PassageTime(
                route.getEndName(),
                currentTime,
                cumulativeDistance,
                "ARRIVAL"
        ));

        scheduledTrain.setPassageTimes(passageTimes);
    }


    public List<CrossingOperation> detectAndResolveCrossings() {
        List<CrossingOperation> crossings = new ArrayList<>();

        scheduledTrains.sort(Comparator.comparing(ScheduledTrain::getDepartureTime));

        for (int i = 0; i < scheduledTrains.size(); i++) {
            for (int j = i + 1; j < scheduledTrains.size(); j++) {
                ScheduledTrain train1 = scheduledTrains.get(i);
                ScheduledTrain train2 = scheduledTrains.get(j);

                CrossingOperation crossing = checkForCrossing(train1, train2);
                if (crossing != null) {
                    crossings.add(crossing);
                }
            }
        }

        return crossings;
    }


    private CrossingOperation checkForCrossing(ScheduledTrain train1, ScheduledTrain train2) {
        Route route1 = train1.getRoute();
        Route route2 = train2.getRoute();

        for (int i = 0; i < route1.getSegments().size(); i++) {
            RouteSegment seg1 = route1.getSegments().get(i);

            for (int j = 0; j < route2.getSegments().size(); j++) {
                RouteSegment seg2 = route2.getSegments().get(j);

                if (seg1.getId() == seg2.getId() && seg1.getNumberOfTracks() == 1) {

                    TimeWindow window1 = getSegmentTimeWindow(train1, i);
                    TimeWindow window2 = getSegmentTimeWindow(train2, j);

                    if (timeWindowsOverlap(window1, window2)) {
                        CrossingPoint crossingPoint = findBestCrossingPoint(
                                train1, train2, seg1, route1, route2, i, j
                        );

                        if (crossingPoint != null) {
                            return new CrossingOperation(
                                    train1.getTrain().getId(),
                                    train2.getTrain().getId(),
                                    seg1.getId(),
                                    crossingPoint
                            );
                        }
                    }
                }
            }
        }

        return null;
    }

    private TimeWindow getSegmentTimeWindow(ScheduledTrain train, int segmentIndex) {
        List<PassageTime> passages = train.getPassageTimes();

        LocalDateTime entryTime = passages.get(segmentIndex + 1).getTime();

        LocalDateTime exitTime = segmentIndex + 2 < passages.size() ?
                passages.get(segmentIndex + 2).getTime() : entryTime.plusMinutes(5);

        return new TimeWindow(entryTime, exitTime);
    }

    private boolean timeWindowsOverlap(TimeWindow w1, TimeWindow w2) {
        // w1 starts before w2 ends AND w2 starts before w1 ends
        return w1.start.isBefore(w2.end) && w2.start.isBefore(w1.end);
    }

    private CrossingPoint findBestCrossingPoint(ScheduledTrain train1, ScheduledTrain train2,
                                                RouteSegment segment, Route route1, Route route2,
                                                int segIndex1, int segIndex2) {

        for (Siding siding : route1.getSidings()) {
            if (siding.getSegmentId() == segment.getId()) {
                return new CrossingPoint(
                        siding.getId(),
                        "SIDING",
                        siding.getPosition(),
                        "Siding #" + siding.getId()
                );
            }
        }

        if (segIndex1 > 0) {
            return new CrossingPoint(
                    route1.getStartEndpointId(),
                    "STATION",
                    0,
                    route1.getStartName()
            );
        }

        return new CrossingPoint(
                route1.getEndEndpointId(),
                "STATION",
                segment.getLength(),
                route1.getEndName()
        );
    }


    public void generateScheduleReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRAIN SCHEDULING REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Generated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Total scheduled trains: " + scheduledTrains.size());
        System.out.println("=".repeat(80) + "\n");

        for (ScheduledTrain st : scheduledTrains) {
            printTrainSchedule(st);
        }

        List<CrossingOperation> crossings = detectAndResolveCrossings();
        if (!crossings.isEmpty()) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("REQUIRED CROSSING OPERATIONS");
            System.out.println("=".repeat(80) + "\n");
            for (CrossingOperation crossing : crossings) {
                printCrossingOperation(crossing);
            }
        } else {
            System.out.println("\n No crossing conflicts detected.\n");
        }
    }

    private void printTrainSchedule(ScheduledTrain st) {
        Train train = st.getTrain();
        System.out.println("┌─ TRAIN #" + train.getId() + " " + "─".repeat(60));
        System.out.println("│ Operator: " + train.getOperator());
        System.out.println("│ Route: " + st.getRoute().getStartName() + " → " + st.getRoute().getEndName());
        System.out.println("│ Departure: " + st.getDepartureTime().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        System.out.println("│ Locomotives: " + train.getLocomotives().size() +
                " (Total Power: " + String.format("%.0f kW", train.getTotalPower()) + ")");
        System.out.println("│ Wagons: " + train.getWagons().size() +
                " (Total Weight: " + String.format("%.0f tonnes", train.getTotalWeight() / 1000) + ")");
        System.out.println("│");
        System.out.println("│ PASSAGE TIMES:");
        System.out.println("│ " + "-".repeat(70));

        for (PassageTime pt : st.getPassageTimes()) {
            System.out.printf("│ %-30s %s  [%.1f km]  %s%n",
                    pt.getLocation(),
                    pt.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    pt.getDistance() / 1000,
                    pt.getType());
        }
        System.out.println("└" + "─".repeat(70) + "\n");
    }

    private void printCrossingOperation(CrossingOperation crossing) {
        System.out.println("  CROSSING DETECTED:");
        System.out.println("  Trains: #" + crossing.getTrain1Id() + " ↔ #" + crossing.getTrain2Id());
        System.out.println("  Segment: " + crossing.getSegmentId());
        System.out.println("  Crossing Point: " + crossing.getCrossingPoint().getName() +
                " (" + crossing.getCrossingPoint().getType() + ")");
        System.out.println("  Position: " + String.format("%.1f m", crossing.getCrossingPoint().getPosition()));
        System.out.println();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println(" Error closing connection: " + e.getMessage());
        }
    }

    // ==========================================
    // HELPER CLASSES
    // ==========================================

    public static class ScheduledTrain {
        private Train train;
        private Route route;
        private LocalDateTime departureTime;
        private List<PassageTime> passageTimes;

        public ScheduledTrain(Train train, Route route, LocalDateTime departureTime) {
            this.train = train;
            this.route = route;
            this.departureTime = departureTime;
            this.passageTimes = new ArrayList<>();
        }
        public Train getTrain() { return train; }
        public Route getRoute() { return route; }
        public LocalDateTime getDepartureTime() { return departureTime; }
        public List<PassageTime> getPassageTimes() { return passageTimes; }
        public void setPassageTimes(List<PassageTime> passageTimes) { this.passageTimes = passageTimes; }
    }

    public static class Train {
        private int id;
        private String operator;
        private java.time.LocalDate date;
        private int pathId;
        private List<Locomotive> locomotives;
        private List<Wagon> wagons;

        public Train(int id, String operator, java.time.LocalDate date, int pathId) {
            this.id = id;
            this.operator = operator;
            this.date = date;
            this.pathId = pathId;
            this.locomotives = new ArrayList<>();
            this.wagons = new ArrayList<>();
        }
        public int getId() { return id; }
        public String getOperator() { return operator; }
        public List<Locomotive> getLocomotives() { return locomotives; }
        public List<Wagon> getWagons() { return wagons; }
        public void setLocomotives(List<Locomotive> locomotives) { this.locomotives = locomotives; }
        public void setWagons(List<Wagon> wagons) { this.wagons = wagons; }
        public double getTotalWeight() {
            double weight = locomotives.stream().mapToDouble(Locomotive::getWeight).sum();
            weight += wagons.stream().mapToDouble(w -> w.getWeight() + w.getCurrentLoad()).sum();
            return weight;
        }
        public double getTotalPower() {
            return locomotives.stream().mapToDouble(Locomotive::getPower).sum();
        }
        public double getMaxLocomotiveSpeed() {
            return locomotives.stream().mapToDouble(Locomotive::getOperationalSpeed).min().orElse(120);
        }
        public double getMaxWagonSpeed() {
            return wagons.stream().mapToDouble(Wagon::getMaxSpeed).min().orElse(120);
        }
    }

    public static class Locomotive {
        private int id;
        private String name;
        private double power;
        private double weight;
        private double maxSpeed;
        private double operationalSpeed;

        public Locomotive(int id, String name, double power, double weight, double maxSpeed, double operationalSpeed) {
            this.id = id;
            this.name = name;
            this.power = power;
            this.weight = weight;
            this.maxSpeed = maxSpeed;
            this.operationalSpeed = operationalSpeed;
        }
        public double getPower() { return power; }
        public double getWeight() { return weight; }
        public double getOperationalSpeed() { return operationalSpeed; }
    }

    public static class Wagon {
        private int id;
        private double weight;
        private double payload;
        private double maxSpeed;
        private double currentLoad;

        public Wagon(int id, double weight, double payload, double maxSpeed, double currentLoad) {
            this.id = id;
            this.weight = weight;
            this.payload = payload;
            this.maxSpeed = maxSpeed;
            this.currentLoad = Math.min(currentLoad, payload);
        }
        public double getWeight() { return weight; }
        public double getMaxSpeed() { return maxSpeed; }
        public double getCurrentLoad() { return currentLoad; }
    }

    public static class Route {
        private int id;
        private int startEndpointId;
        private int endEndpointId;
        private String startName;
        private String endName;
        private List<RouteSegment> segments;
        private List<Siding> sidings;

        public Route(int id, int startEndpointId, int endEndpointId, String startName, String endName) {
            this.id = id;
            this.startEndpointId = startEndpointId;
            this.endEndpointId = endEndpointId;
            this.startName = startName;
            this.endName = endName;
            this.segments = new ArrayList<>();
            this.sidings = new ArrayList<>();
        }
        public int getStartEndpointId() { return startEndpointId; }
        public int getEndEndpointId() { return endEndpointId; }
        public String getStartName() { return startName; }
        public String getEndName() { return endName; }
        public List<RouteSegment> getSegments() { return segments; }
        public List<Siding> getSidings() { return sidings; }
        public void setSegments(List<RouteSegment> segments) { this.segments = segments; }
        public void addSegment(RouteSegment segment) { this.segments.add(segment); }
        public void addSiding(Siding siding) { this.sidings.add(siding); }
        public void setEndEndpointId(int id) { this.endEndpointId = id; }
        public void setEndName(String name) { this.endName = name; }
    }

    public static class RouteSegment {
        private int id;
        private double length;
        private double maxWeight;
        private int numberOfTracks;
        boolean isElectric;
        private double permittedSpeed;

        public RouteSegment(int id, double length, double maxWeight, int numberOfTracks, boolean isElectric) {
            this.id = id;
            this.length = length;
            this.maxWeight = maxWeight;
            this.numberOfTracks = numberOfTracks;
            this.isElectric = isElectric;
            this.permittedSpeed = 120;
        }
        public int getId() { return id; }
        public double getLength() { return length; }
        public double getMaxWeight() { return maxWeight; }
        public int getNumberOfTracks() { return numberOfTracks; }
        public double getPermittedSpeed() { return permittedSpeed; }
        public void setPermittedSpeed(double speed) { this.permittedSpeed = speed; }
    }

    public static class Siding {
        private int id;
        private double position;
        private double length;
        private int segmentId;

        public Siding(int id, double position, double length, int segmentId) {
            this.id = id;
            this.position = position;
            this.length = length;
            this.segmentId = segmentId;
        }
        public int getId() { return id; }
        public double getPosition() { return position; }
        public int getSegmentId() { return segmentId; }
    }

    public static class PassageTime {
        private String location;
        private LocalDateTime time;
        private double distance;
        private String type;

        public PassageTime(String location, LocalDateTime time, double distance, String type) {
            this.location = location;
            this.time = time;
            this.distance = distance;
            this.type = type;
        }
        public String getLocation() { return location; }
        public LocalDateTime getTime() { return time; }
        public double getDistance() { return distance; }
        public String getType() { return type; }
    }

    public static class CrossingOperation {
        private int train1Id;
        private int train2Id;
        private int segmentId;
        private CrossingPoint crossingPoint;

        public CrossingOperation(int train1Id, int train2Id, int segmentId, CrossingPoint crossingPoint) {
            this.train1Id = train1Id;
            this.train2Id = train2Id;
            this.segmentId = segmentId;
            this.crossingPoint = crossingPoint;
        }
        public int getTrain1Id() { return train1Id; }
        public int getTrain2Id() { return train2Id; }
        public int getSegmentId() { return segmentId; }
        public CrossingPoint getCrossingPoint() { return crossingPoint; }
    }

    public static class CrossingPoint {
        private int id;
        private String type;
        private double position;
        private String name;

        public CrossingPoint(int id, String type, double position, String name) {
            this.id = id;
            this.type = type;
            this.position = position;
            this.name = name;
        }
        public String getType() { return type; }
        public double getPosition() { return position; }
        public String getName() { return name; }
    }

    public static class Endpoint {
        public int id;
        public String name;
        public Endpoint(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class RailLine {
        public int id;
        public String name;
        public int startId;
        public int endId;
        public RailLine(int id, String name, int startId, int endId) {
            this.id = id;
            this.name = name;
            this.startId = startId;
            this.endId = endId;
        }
    }

    public static class RailLineInfo {
        public int id;
        public String name;
        public String startName;
        public String endName;
        public RailLineInfo(int id, String name, String startName, String endName) {
            this.id = id;
            this.name = name;
            this.startName = startName;
            this.endName = endName;
        }
    }

    /**
     * Helper class for time window overlap detection
     */
    private static class TimeWindow {
        LocalDateTime start;
        LocalDateTime end;

        TimeWindow(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}