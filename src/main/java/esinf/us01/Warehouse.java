package esinf.us01;

import java.util.*;

public class Warehouse {
    private Map<Wagon, Set<Box>> wagons = new HashMap<>();

    public Map<Wagon, Set<Box>> getWagonsMap() {
        return wagons;
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
}
