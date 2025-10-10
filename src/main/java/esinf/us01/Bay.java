package esinf.us01;


import java.util.Objects;

public class Bay {
    private int bayID;
    private int aisle;
    private int boxCapacity;

    public Bay(int bayID, int aisle, int boxCapacity) {
        this.bayID = bayID;
        this.aisle = aisle;
        this.boxCapacity = boxCapacity;
    }

    public int getBayID() {
        return bayID;
    }

    public void setBayID(int bayID) {
        this.bayID = bayID;
    }

    public int getAisle() {
        return aisle;
    }

    public void setAisle(int aisle) {
        this.aisle = aisle;
    }

    public int getBoxCapacity() {
        return boxCapacity;
    }

    public void setBoxCapacity(int boxCapacity) {
        this.boxCapacity = boxCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bay)) return false;
        Bay other = (Bay) o;
        return aisle == other.aisle && bayID == other.bayID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bayID, aisle, boxCapacity);
    }

    @Override
    public String toString() {
        return "Bay{" +
                "bayID=" + bayID +
                ", aisle=" + aisle +
                ", boxCapacity=" + boxCapacity +
                '}';
    }
}