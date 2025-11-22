package app;

import scheduler.Scheduler;
import warehouseMngt.service.LoadData;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        LoadData.loadAllData();


        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. ESINF User Stories");
            System.out.println("2. LAPR US03");
            System.out.println("3. LAPR US07");
            System.out.println("0. EXIT");
            System.out.print("Select option: ");
            int option = sc.nextInt();
            sc.nextLine();

            switch (option) {
                case 1 -> MenuESINF.menuESINF();
                case 2 -> LaprMain.main();
                case 3 -> Scheduler.main(args);
                case 0 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}