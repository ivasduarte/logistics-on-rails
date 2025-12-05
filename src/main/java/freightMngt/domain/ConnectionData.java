package freightMngt.domain;

public class ConnectionData {

    private double distance;
    private double capacity;
    private double cost;

    public ConnectionData(double distance, double capacity, double cost) {
        this.distance = distance;
        this.capacity = capacity;
        this.cost = cost;
    }

    public double getDistance() { return distance; }
    public double getCapacity() { return capacity; }
    public double getCost() { return cost; }

    /**
     * Extracts the cost from a ConnectionData.
     * * Helper method for use with BinaryOperator.
     */
    public static Double extractCost(ConnectionData data) {
        return data.getCost();
    }

    @Override
    public String toString() {
        return String.format("Dist: %.2f | Cap: %.0f | Cost: %.2f", distance, capacity, cost);
    }
}
