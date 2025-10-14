package esinf.us01;

import java.util.List;

public class InventoryLoader {
    public void verifyExpirationDate(List<Box> boxes){
        for(Box box : boxes){
            if(box.hasExpirationDate()) System.out.printf("Box %s has an expiration date: %s%n", box.getBoxID(), box.getExpiryDate());
            else System.out.printf("Box %s has no expiration date%n", box.getBoxID());
        }
    }
}
