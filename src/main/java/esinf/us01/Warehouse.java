package esinf.us01;

import java.util.*;

public class Warehouse {
    private Map<Wagon, Set<Box>> wagons = new HashMap<>();
    private Map<String, Set<Bay>> warehouses = new HashMap<>();

    public Map<Wagon, Set<Box>> getWagonsMap() {
        return wagons;
    }

    public Map<String, Set<Bay>> getWarehouses() {
        return warehouses;
    }

    public void getWagons(List<String> lines){
        Wagon currentWagon = null;
        Set<Box> currentBox = null;

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            String[] parts = line.split(",");

            String wagonID = parts[0];
            String boxID = parts[1];
            String sku = parts[2];
            int quantity = Integer.parseInt(parts[3]);
            String expiryDate = parts[4].isEmpty() ? null : parts[4];
            String receivedDate = parts[5];

            if(currentWagon == null || !currentWagon.getWagonID().equals(wagonID)){
                if(currentWagon != null && currentBox !=null) wagons.put(currentWagon, currentBox);
                currentWagon = new Wagon(wagonID);
                currentBox = new HashSet<>();
            }

            Box box = new Box(boxID, sku, quantity, expiryDate, receivedDate);
            currentBox.add(box);
        }

        if(currentWagon != null && currentBox !=null) wagons.put(currentWagon, currentBox);
    }

    public void getBays(List<String> lines){
        String currentWarehouseID = null;
        Set<Bay> currentBays = null;

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            String[] parts = line.split(",");

            String warehouseID = parts[0];
            int aisle = Integer.parseInt(parts[1]);
            int bay = Integer.parseInt(parts[2]);
            int boxCapacity = Integer.parseInt(parts[3]);

            // If we change warehouse, its interior gets saved
            if(currentWarehouseID != null || !currentWarehouseID.equals(warehouseID)){
                if(currentWarehouseID != null && currentBays !=null) warehouses.put(currentWarehouseID, currentBays);
                currentWarehouseID = warehouseID;
                currentBays = new HashSet<>();
            }

            Bay bays = new Bay(bay, aisle, boxCapacity);
            currentBays.add(bays);
        }
        // Save the last warehouse
        if(currentWarehouseID != null && currentBays !=null) warehouses.put(currentWarehouseID, currentBays);
    }
}
