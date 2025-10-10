package esinf.us01;

import java.util.*;

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
