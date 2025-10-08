import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Login login = new Login();
        Logout logout = new Logout();
        int choice;

        System.out.println("USER LOGIN");

        do{
            System.out.println("\nUser Menu:");
            System.out.println("1. Login");
            System.out.println("2. Logout");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch(choice){
                case 1:
                    if(login.isLoggedIn()) {
                        System.out.println("Already logged in.");
                    } else {
                        login.login();
                    }
                    break;

                case 2:
                    logout.logout(login);
                    break;

                case 0:
                    System.out.println("Shutting down...");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice.");
            }
        }while(choice != 0);
        sc.close();
    }
}
