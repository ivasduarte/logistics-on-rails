package scheduler;

import utils.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * TrainScheduler - Train scheduling and dispatch system
 * Implements USLP07 - Train dispatch with simple/complex routes
 * Modified to use database functions (USLP10)
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
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // DATABASE QUERY METHODS (Using Functions)
    // ==========================================

    /**
     * Returns a list of all trains in the DB for display
     * Uses: get_all_trains function
     */
    public List<String> getAvailableTrains() {
        List<String> list = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{? = call get_all_trains}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    list.add(String.format("[ID: %d] Operator: %s (Date: %s)",
                            rs.getInt("id"),
                            rs.getString("operator"),
                            rs.getDate("train_date")));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching trains: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns a list of trains that have NOT been scheduled yet
     * Uses: get_all_trains function
     */
    public List<String> getUnscheduledTrains() {
        List<String> list = new ArrayList<>();
        // Set of IDs to exclude from automatic scheduling
        Set<Integer> excludedIds = Set.of(5421, 5435, 5437);

        Set<Integer> alreadyScheduledIds = scheduledTrains.stream()
                .map(st -> st.getTrain().getId())
                .collect(Collectors.toSet());

        try (CallableStatement stmt = connection.prepareCall("{? = call get_all_trains}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    int trainId = rs.getInt("id");
                    String operator = rs.getString("operator");


                    if (!excludedIds.contains(trainId) && !alreadyScheduledIds.contains(trainId)) {
                        alreadyScheduledIds.add(trainId);
                    }


                    if (!alreadyScheduledIds.contains(trainId)) {
                        list.add(String.format("[ID: %d] Operator: %s (Date: %s)",
                                trainId,
                                operator == null ? "Unknown" : operator
                                ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching trains: " + e.getMessage());
        }
        return list;
    }

    /**
     * Uses: is_valid_train_id function
     */
    public boolean isValidTrainId(int id) {
        try (CallableStatement stmt = connection.prepareCall("{? = call is_valid_train_id(?)}")) {
            stmt.registerOutParameter(1, Types.NUMERIC);
            stmt.setInt(2, id);
            stmt.execute();
            return stmt.getInt(1) == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isTrainScheduled(int trainId) {
        return scheduledTrains.stream()
                .anyMatch(st -> st.getTrain().getId() == trainId);
    }

    /**
     * Uses: get_all_endpoints function
     */
    public List<Endpoint> getAllEndpoints() {
        List<Endpoint> endpoints = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{? = call get_all_endpoints}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    endpoints.add(new Endpoint(
                            rs.getInt("endpointID"),
                            rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching endpoints: " + e.getMessage());
        }
        return endpoints;
    }

    /**
     * Uses: get_endpoint_by_id function
     */
    public Endpoint getEndpointById(int id) {
        try (CallableStatement stmt = connection.prepareCall("{? = call get_endpoint_by_id(?)}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setInt(2, id);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new Endpoint(
                            rs.getInt("endpointID"),
                            rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching endpoint: " + e.getMessage());
        }
        return null;
    }

    /**
     * Uses: get_connected_rail_lines function
     */
    public List<RailLine> getConnectedRailLines(int endpointId) {
        List<RailLine> lines = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{? = call get_connected_rail_lines(?)}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setInt(2, endpointId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    lines.add(new RailLine(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("startID"),
                            rs.getInt("endID")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching rail lines: " + e.getMessage());
        }
        return lines;
    }

    /**
     * Uses: get_all_rail_lines function
     */
    public List<RailLineInfo> getAllRailLines() {
        List<RailLineInfo> lines = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{? = call get_all_rail_lines}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    lines.add(new RailLineInfo(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("startName"),
                            rs.getString("endName")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error fetching all rail lines: " + e.getMessage());
        }
        return lines;
    }

    /**
     * Uses: get_segments_for_rail_line function
     */
    public List<RouteSegment> getSegmentsForRailLine(int railLineId) {
        List<RouteSegment> segments = new ArrayList<>();
        try (CallableStatement stmt = connection.prepareCall("{? = call get_segments_for_rail_line(?)}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setInt(2, railLineId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    segments.add(new RouteSegment(
                            rs.getInt("id"),
                            rs.getDouble("length"),
                            rs.getDouble("max_weight"),
                            rs.getInt("numberTracks"),
                            rs.getString("isElectric").equals("Yes")
                    ));
                }
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

    /**
     * Uses: get_train_with_locomotives function
     */
    private Train loadTrain(int trainId) throws SQLException {
        Train train = null;
        List<Locomotive> locomotives = new ArrayList<>();

        try (CallableStatement stmt = connection.prepareCall("{? = call get_train_with_locomotives(?)}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setInt(2, trainId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    if (train == null) {
                        train = new Train(
                                rs.getInt("id"),
                                rs.getString("operator"),
                                rs.getDate("train_date").toLocalDate(),
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
            }
        }

        if (train != null) {
            train.setLocomotives(locomotives);
            loadWagons(train);
        }

        return train;
    }

    /**
     * Uses: get_wagons_for_train function
     */
    private void loadWagons(Train train) throws SQLException {
        List<Wagon> wagons = new ArrayList<>();

        try (CallableStatement stmt = connection.prepareCall("{? = call get_wagons_for_train(?)}")) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setInt(2, train.getId());
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
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
            }
        }

        train.setWagons(wagons);
    }

    /**
     * Uses: get_sidings_for_segment function
     */
    private void loadSidingsForRoute(Route route) throws SQLException {
        for (RouteSegment seg : route.getSegments()) {
            try (CallableStatement stmt = connection.prepareCall("{? = call get_sidings_for_segment(?)}")) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.setInt(2, seg.getId());
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
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
            }
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
            System.out.println("\n✓ No crossing conflicts detected.\n");
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
            System.err.println("✗ Error closing connection: " + e.getMessage());
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