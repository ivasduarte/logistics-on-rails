package freightMngt.domain;

public class RollingStockDTO {
    private String type;        // "LOCOMOTIVE" or "WAGON"
    private int id;
    private String name;
    private int locationId;
    private String locationName;
    private String status;      // "PARKED" or "IN_TRANSIT"
    private double distanceFromSource; // Campo calculado no Java

    public RollingStockDTO(String type, int id, String name, int locationId, String locationName, String status) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.locationId = locationId;
        this.locationName = locationName;
        this.status = status;
        this.distanceFromSource = -1.0; // Default
    }

    // Getters and Setters
    public String getType() { return type; }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getStatus() { return status; }

    public double getDistanceFromSource() { return distanceFromSource; }
    public void setDistanceFromSource(double distanceFromSource) { this.distanceFromSource = distanceFromSource; }

    @Override
    public String toString() {
        String distStr = (status.equals("PARKED") && distanceFromSource >= 0)
                ? String.format(" | Dist: %.2f km", distanceFromSource)
                : "";
        return String.format("[%s] %s (ID: %d) | Status: %s at %s%s",
                type, name, id, status, locationName, distStr);
    }
}