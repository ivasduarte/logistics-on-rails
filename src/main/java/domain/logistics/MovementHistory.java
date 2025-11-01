package domain.logistics;

import domain.warehouse.Movement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovementHistory {
    private final List<Movement> movements = new ArrayList<>();

    public void add(Movement movement) {
        movements.add(movement);
    }

    public List<Movement> getAll() {
        return Collections.unmodifiableList(movements);
    }

    public void print() {
        System.out.println("\n=== MOVEMENT HISTORY ===");
        for (Movement m : movements) {
            System.out.println(m);
        }
    }

    public String exportToCSV() {
        StringBuilder csv = new StringBuilder("MovementID,Type,Source,Destination,BoxID,SKU,Timestamp\n");
        int id = 1;
        for (Movement m : movements) {
            csv.append(id++).append(",")
                    .append(m.getType()).append(",")
                    .append(m.fromLocation).append(",")
                    .append(m.toLocation).append(",")
                    .append(m.boxId).append(",")
                    .append(m.sku).append(",")
                    .append(m.timestamp).append("\n");
        }
        return csv.toString();
    }
}
