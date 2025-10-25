# USLP03 - Estimate Travel Time

## 4. Tests

**Test 1:** Tests the TravelTime constructor and getters to ensure the locomotive and stations are properly initialized.

```java
@Test
void testGetters() {
    assertEquals(locomotive, travelTime.getLocomotive());
    assertEquals(List.of(startStation, endStation), travelTime.getStations());
}
```

**Test 2:** Tests the functionality to calculate the total travel distance by summing all segment lengths in the connecting line.

```java
@Test
void testCalculateTravelDistance() {
    int expectedDistance = 3000; // 1000 + 2000
    assertEquals(expectedDistance, travelTime.calculateTravelDistance(lines));
}
```

**Test 3:** Tests the functionality to calculate travel time based on distance and locomotive's maximum speed.

```java
@Test
void testCalculateTravelTime() {
    double expectedTime = 25.0; // 3000/120
    assertEquals(expectedTime, travelTime.calculateTravelTime(3000));
}
```

**Test 4:** Tests the Locomotive constructor and all getter methods.

```java
@Test
public void testLocomotiveConstructorAndGetters() {
    Locomotive locomotive = new Locomotive(1, "Test Loco", "TestMake", "TestModel",
            2020, 4, "TestBogies", 5000, 20.5, 3.2, 4.1,
            80000, 160, 120, 1, "Electric", 25000, 50,
            "TestOperator", 1435, 0);

    assertEquals(1, locomotive.getLocomotiveID());
    assertEquals("Test Loco", locomotive.getLocomotiveName());
    assertEquals("TestMake", locomotive.getMake());
    assertEquals("TestModel", locomotive.getModel());
    assertEquals(2020, locomotive.getServiceYear());
    assertEquals(4, locomotive.getNumberBogies());
    assertEquals("TestBogies", locomotive.getBogies());
    assertEquals(5000, locomotive.getPower());
    assertEquals(20.5, locomotive.getLength(), 0.001);
    assertEquals(3.2, locomotive.getWidth(), 0.001);
    assertEquals(4.1, locomotive.getHeight(), 0.001);
    assertEquals(80000, locomotive.getWeight());
    assertEquals(160, locomotive.getMaxSpeed());
    assertEquals(120, locomotive.getOperationalSpeed());
    assertEquals(1, locomotive.getTraction());
    assertEquals("Electric", locomotive.getType());
    assertEquals(25000, locomotive.getVoltage());
    assertEquals(50, locomotive.getFrequency());
    assertEquals("TestOperator", locomotive.getOperator());
    assertEquals(1435, locomotive.getGauge());
    assertEquals(0, locomotive.getFuel());
}
```

**Test 5:** Tests the Station constructor and getter methods.

```java
@Test
void testStationConstructor() {
    Station station = new Station(1, "Test Station");
    assertNotNull(station);
}

@Test
void testGetStationID() {
    Station station = new Station(1, "Test Station");
    assertEquals(1, station.getStationID());
}

@Test
void testGetStationName() {
    Station station = new Station(1, "Test Station");
    assertEquals("Test Station", station.getStationName());
}
```

**Test 6:** Tests the Line constructor and the ability to set and retrieve segments.

```java
@Test
void testLineConstructor() {
    assertNotNull(line);
    assertEquals(LINE_ID, line.getLineID());
    assertEquals(LINE_NAME, line.getLineName());
    assertEquals(OWNER_NAME, line.getOwnerName());
    assertEquals(START_STATION, line.getStartStationID());
    assertEquals(END_STATION, line.getEndStationID());
    assertEquals(GAUGE, line.getGauge());
}

@Test
void testSetAndGetSegments() {
    List<Segment> segments = new ArrayList<>();
    segments.add(new Segment(1, LINE_ID, 1, true, 1000, 500, 2));
    segments.add(new Segment(2, LINE_ID, 2, false, 800, 300, 1));

    line.setSegments(segments);

    assertEquals(segments, line.getSegments());
    assertEquals(2, line.getSegments().size());
}
```

**Test 7:** Tests the Segment constructor and all getter methods.

```java
@Test
void testSegmentConstructorAndGetters() {
    Segment segment = new Segment(1, 100, 1, true, 1000, 500, 2);

    assertEquals(1, segment.getSegmentID());
    assertEquals(100, segment.getLineID());
    assertEquals(1, segment.getOrder());
    assertTrue(segment.isElectrified());
    assertEquals(1000, segment.getMaxWeight());
    assertEquals(500, segment.getLength());
    assertEquals(2, segment.getNumberTracks());
}

@Test
void testSegmentWithDifferentValues() {
    Segment segment = new Segment(2, 101, 2, false, 2000, 750, 1);

    assertEquals(2, segment.getSegmentID());
    assertEquals(101, segment.getLineID());
    assertEquals(2, segment.getOrder());
    assertFalse(segment.isElectrified());
    assertEquals(2000, segment.getMaxWeight());
    assertEquals(750, segment.getLength());
    assertEquals(1, segment.getNumberTracks());
}
```

## 5. Construction (Implementation)

### Class TravelTime.java

```java
package lapr3.uslp03;

import java.util.List;

public class TravelTime {

    private final Locomotive locomotive;
    private final List<Station> stations;

    /**
     * Constructs a TravelTime object, initializing the locomotive to be used
     * and the start and end stations for the travel.
     *
     * @param locomotive the locomotive used for travel calculations, which
     *                   provides characteristics such as speed and performance.
     * @param startStation the station where the travel begins.
     * @param endStation the station where the travel ends.
     */
    public TravelTime(Locomotive locomotive, Station startStation, Station endStation) {
        this.locomotive = locomotive;
        this.stations = List.of(startStation, endStation);
    }

    /**
     * Retrieves the locomotive associated with this travel time calculation.
     *
     * @return the locomotive used in travel calculations, providing its characteristics
     *         and capabilities such as speed and performance.
     */
    public Locomotive getLocomotive() {
        return locomotive;
    }

    /**
     * Retrieves the list of stations associated with this travel.
     *
     * @return a list of Station objects representing the stations involved in this travel
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * Finds a railway line that directly connects the start station and the end station
     * from the provided list of lines.
     *
     * @param lines a list of Line objects, representing available railway lines
     * @return a Line object representing the direct connection between the start
     *         and end station
     * @throws IllegalArgumentException if no direct line connects the start and end stations
     */
    private Line findConnectingLine(List<Line> lines) {
        int startStationId = stations.get(0).getStationID();
        int endStationId = stations.get(1).getStationID();

        return lines.stream()
                .filter(l -> (l.getStartStationID() == startStationId && l.getEndStationID() == endStationId) ||
                        (l.getStartStationID() == endStationId && l.getEndStationID() == startStationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No direct line connects these stations"));
    }

    /**
     * Calculates the total travel distance of the railway line by summing the lengths
     * of all segments that compose the line.
     *
     * @return the total travel distance in meters
     */
    public int calculateTravelDistance(List<Line> lines) {
        Line line = findConnectingLine(lines);

        List<Segment> segments = line.getSegments();
        int travelDistance = 0;

        for (Segment segment : segments) {
            travelDistance += segment.getLength();
        }

        return travelDistance;
    }

    /**
     * Calculates the travel time based on the provided distance and the locomotive's maximum speed.
     *
     * @param distance the distance to travel in meters
     * @return the travel time in hours as a double
     */
    public double calculateTravelTime(int distance) {
        return (double) distance / locomotive.getMaxSpeed();
    }
}
```

### Class Locomotive.java

```java
package lapr3.uslp03;

public class Locomotive {

    private final int locomotiveID;
    private final String locomotiveName;
    private final String make;
    private final String model;
    private final int serviceYear;
    private final int numberBogies;
    private final String bogies;
    private final int power;
    private final double length;
    private final double width;
    private final double height;
    private final int weight;
    private final int maxSpeed;
    private final int operationalSpeed;
    private final int traction;
    private final String type;
    private final int voltage;
    private final int frequency;
    private final String operator;
    private final int gauge;
    private final int fuel;

    /**
     * Constructs a new Locomotive object with the specified parameters.
     *
     * @param locomotiveID    the unique identifier for the locomotive
     * @param locomotiveName  the name of the locomotive
     * @param make            the manufacturer of the locomotive
     * @param model           the model designation of the locomotive
     * @param serviceYear     the year the locomotive entered into service
     * @param numberBogies    the number of bogies in the locomotive
     * @param bogies          the description of the bogies used
     * @param power           the power output of the locomotive, typically measured in horsepower or kilowatts
     * @param length          the length of the locomotive in meters
     * @param width           the width of the locomotive in meters
     * @param height          the height of the locomotive in meters
     * @param weight          the weight of the locomotive in tons
     * @param maxSpeed        the maximum achievable speed of the locomotive in kilometers per hour
     * @param operationalSpeed the normal operating speed of the locomotive in kilometers per hour
     * @param traction        the traction capability of the locomotive, typically measured in kilonewtons
     * @param type            the type of locomotive (e.g., diesel, electric, hybrid)
     * @param voltage         the operating voltage of the locomotive for electrified systems
     * @param frequency       the frequency of the electrical power supply used by the locomotive
     * @param operator        the name of the company or organization operating the locomotive
     * @param gauge           the track gauge the locomotive is designed for, typically in millimeters
     * @param fuel            the fuel capacity of the locomotive, typically in liters
     */
    public Locomotive(int locomotiveID, String locomotiveName, String make, String model, int serviceYear, int numberBogies, String bogies, int power, double length, double width, double height, int weight, int maxSpeed, int operationalSpeed, int traction, String type, int voltage, int frequency, String operator, int gauge, int fuel) {
        this.locomotiveID = locomotiveID;
        this.locomotiveName = locomotiveName;
        this.make = make;
        this.model = model;
        this.serviceYear = serviceYear;
        this.numberBogies = numberBogies;
        this.bogies = bogies;
        this.power = power;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.operationalSpeed = operationalSpeed;
        this.traction = traction;
        this.type = type;
        this.voltage = voltage;
        this.frequency = frequency;
        this.operator = operator;
        this.gauge = gauge;
        this.fuel = fuel;
    }

    // Getters for all fields
    public int getLocomotiveID() { return locomotiveID; }
    public String getLocomotiveName() { return locomotiveName; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getServiceYear() { return serviceYear; }
    public int getNumberBogies() { return numberBogies; }
    public String getBogies() { return bogies; }
    public int getPower() { return power; }
    public double getLength() { return length; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getWeight() { return weight; }
    public int getMaxSpeed() { return maxSpeed; }
    public int getOperationalSpeed() { return operationalSpeed; }
    public int getTraction() { return traction; }
    public String getType() { return type; }
    public int getVoltage() { return voltage; }
    public int getFrequency() { return frequency; }
    public String getOperator() { return operator; }
    public int getGauge() { return gauge; }
    public int getFuel() { return fuel; }
}
```

### Class Station.java

```java
package lapr3.uslp03;

public class Station {

    private final int stationID;
    private final String stationName;

    /**
     * Constructs a new Station object with the specified ID and name.
     *
     * @param stationID   the unique identifier for the station
     * @param stationName the name of the station
     */
    public Station(int stationID, String stationName) {
        this.stationID = stationID;
        this.stationName = stationName;
    }

    public int getStationID() {
        return stationID;
    }

    public String getStationName() {
        return stationName;
    }
}
```

### Class Line.java

```java
package lapr3.uslp03;

import java.util.List;

public class Line {

    private final int lineID;
    private final String lineName;
    private final String ownerName;
    private final int startStationID;
    private final int endStationID;
    private final int gauge;
    private List<Segment> segments;

    /**
     * Constructs a new Line object with the specified parameters.
     *
     * @param lineID         the unique identifier for the railway line
     * @param lineName       the name of the railway line
     * @param ownerName      the name of the owner of the railway line
     * @param startStationID the ID of the starting station of the railway line
     * @param endStationID   the ID of the ending station of the railway line
     * @param gauge          the track gauge of the railway line (in millimeters)
     */
    public Line(int lineID, String lineName, String ownerName, int startStationID, int endStationID, int gauge) {
        this.lineID = lineID;
        this.lineName = lineName;
        this.ownerName = ownerName;
        this.startStationID = startStationID;
        this.endStationID = endStationID;
        this.gauge = gauge;
    }

    public int getLineID() { return lineID; }
    public String getLineName() { return lineName; }
    public String getOwnerName() { return ownerName; }
    public int getStartStationID() { return startStationID; }
    public int getEndStationID() { return endStationID; }
    public int getGauge() { return gauge; }
    public List<Segment> getSegments() { return segments; }
    
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
```

### Class Segment.java

```java
package lapr3.uslp03;

public class Segment {

    private final int segmentID;
    private final int lineID;
    private final int order;
    private final boolean electrified;
    private final int maxWeight;
    private final int length;
    private final int numberTracks;

    /**
     * Constructs a new Segment object with the specified parameters.
     *
     * @param segmentID   the unique identifier for the segment within the railway system
     * @param lineID      the unique identifier for the associated railway line
     * @param order       the sequential order of the segment within its associated line
     * @param electrified whether the segment is electrified
     * @param maxWeight   the maximum allowable weight on the segment, typically in tons
     * @param length      the length of the segment, typically in meters
     * @param numberTracks the number of tracks within the segment
     */
    public Segment(int segmentID, int lineID, int order, boolean electrified, int maxWeight, int length, int numberTracks) {
        this.segmentID = segmentID;
        this.lineID = lineID;
        this.order = order;
        this.electrified = electrified;
        this.maxWeight = maxWeight;
        this.length = length;
        this.numberTracks = numberTracks;
    }

    public int getSegmentID() { return segmentID; }
    public int getLineID() { return lineID; }
    public int getOrder() { return order; }
    public boolean isElectrified() { return electrified; }
    public int getMaxWeight() { return maxWeight; }
    public int getLength() { return length; }
    public int getNumberTracks() { return numberTracks; }
}
```