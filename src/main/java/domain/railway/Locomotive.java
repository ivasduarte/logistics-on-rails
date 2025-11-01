package domain.railway;

public class Locomotive {

    /**
     * The unique identifier for a locomotive.
     * Represents the ID assigned to a specific locomotive instance, providing
     * a way to uniquely identify and distinguish it within the railway system.
     */
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

    /**
     * Retrieves the unique identifier for the locomotive.
     *
     * @return the locomotive ID
     */
    public int getLocomotiveID() {
        return locomotiveID;
    }

    /**
     * Retrieves the name of the locomotive.
     *
     * @return the name of the locomotive
     */
    public String getLocomotiveName() {
        return locomotiveName;
    }

    /**
     * Retrieves the manufacturer of the locomotive.
     *
     * @return the manufacturer of the locomotive
     */
    public String getMake() {
        return make;
    }

    /**
     * Retrieves the model designation of the locomotive.
     *
     * @return the model of the locomotive
     */
    public String getModel() {
        return model;
    }

    /**
     * Retrieves the year the locomotive entered into service.
     *
     * @return the service year of the locomotive
     */
    public int getServiceYear() {
        return serviceYear;
    }

    /**
     * Retrieves the number of bogies associated with the locomotive.
     *
     * @return the number of bogies
     */
    public int getNumberBogies() {
        return numberBogies;
    }

    /**
     * Retrieves the description of the bogies used in the locomotive.
     *
     * @return the description of the bogies
     */
    public String getBogies() {
        return bogies;
    }

    /**
     * Retrieves the power output of the locomotive.
     *
     * @return the power output of the locomotive, typically measured in horsepower or kilowatts
     */
    public int getPower() {
        return power;
    }

    /**
     * Retrieves the length of the locomotive.
     *
     * @return the length of the locomotive in meters
     */
    public double getLength() {
        return length;
    }

    /**
     * Retrieves the width of the locomotive.
     *
     * @return the width of the locomotive in meters
     */
    public double getWidth() {
        return width;
    }

    /**
     * Retrieves the height of the locomotive.
     *
     * @return the height of the locomotive in meters
     */
    public double getHeight() {
        return height;
    }

    /**
     * Retrieves the weight of the locomotive.
     *
     * @return the weight of the locomotive in tons
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Retrieves the maximum achievable speed of the locomotive.
     *
     * @return the maximum speed of the locomotive in kilometers per hour
     */
    public int getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Retrieves the normal operating speed of the locomotive.
     *
     * @return the operational speed of the locomotive in kilometers per hour
     */
    public int getOperationalSpeed() {
        return operationalSpeed;
    }

    /**
     * Retrieves the traction capability of the locomotive.
     *
     * @return the traction of the locomotive, typically measured in kilonewtons
     */
    public int getTraction() {
        return traction;
    }

    /**
     * Retrieves the type of the locomotive.
     *
     * @return the type of locomotive (e.g., diesel, electric, hybrid)
     */
    public String getType() {
        return type;
    }

    /**
     * Retrieves the operating voltage of the locomotive for electrified systems.
     *
     * @return the operating voltage of the locomotive
     */
    public int getVoltage() {
        return voltage;
    }

    /**
     * Retrieves the frequency of the electrical power supply used by the locomotive.
     *
     * @return the frequency of the electrical power supply
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Retrieves the name of the company or organization operating the locomotive.
     *
     * @return the operator of the locomotive
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Retrieves the track gauge the locomotive is designed for.
     *
     * @return the track gauge of the locomotive, typically in millimeters
     */
    public int getGauge() {
        return gauge;
    }

    /**
     * Retrieves the fuel capacity of the locomotive.
     *
     * @return the fuel capacity of the locomotive, typically in liters
     */
    public int getFuel() {
        return fuel;
    }
}
