package app;

import ui.NetworkUI;
import ui.SpatialQueryingUI;
import ui.WarehouseUI;
import warehouseMngt.controller.WarehouseController;
import warehouseMngt.picking.*;
import utils.LoadData;
import warehouseMngt.domain.Warehouse;

import java.util.*;

public class MenuESINF {
    private static final Scanner sc = new Scanner(System.in);

    public static void menuESINF() {
        Warehouse warehouse = new Warehouse(
                LoadData.getWagons(),
                LoadData.getItems(),
                LoadData.getOrderLines(),
                LoadData.getOrders(),
                LoadData.getReturns(),
                LoadData.getWarehouses()
        );
        WarehouseController warehouseController = new WarehouseController(warehouse);

        int op2;

        do {
            System.out.println("\n══════════════════════ ESINF MENU ══════════════════════");
            System.out.println("1. Warehouse Management");
            System.out.println("2. Spatial Querying");
            System.out.println("3. Freight Management");
            System.out.println("0. Return to Main Menu");
            System.out.println("══════════════════════════════════════════════════════════");
            System.out.print("\nSelect option: ");
            op2 = sc.nextInt();
            sc.nextLine();

            switch (op2) {
                case 1:
                    WarehouseUI warehouseUI = new WarehouseUI(warehouseController);
                    warehouseUI.run();
                    break;
                case 2:
                    SpatialQueryingUI spatialQueryingUI = new SpatialQueryingUI();
                    spatialQueryingUI.run();
                    break;
                case 3:
                    NetworkUI networkUI = new NetworkUI();
                    networkUI.run();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } while (op2 != 0);
    }
}