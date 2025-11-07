package warehouseMngt.logistics;

import java.util.*;

/**
 * Represents a wagon identified by a unique Wagon ID.
 * Encapsulates basic information and behaviors associated with a wagon.
 */
public class Wagon {
    private String wagonID;

    public Wagon(String wagonID) {
        this.wagonID = wagonID;
    }

    public String getWagonID() {
        return wagonID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wagon)) return false;
        Wagon wagon = (Wagon) o;
        return Objects.equals(wagonID, wagon.wagonID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wagonID);
    }

    @Override
    public String toString() {
        return "Wagon{" +
                "wagonID='" + wagonID + '\'' +
                '}';
    }
}
