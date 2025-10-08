import java.util.Scanner;

public class Login {
    private User user;
    private boolean loggedIn;
    private String loggedUser;
    static Scanner sc = new Scanner(System.in);

    public Login() {
        this.user = new User("username", "12345678");
        this.loggedIn = false;
        this.loggedUser = null;
    }

    public boolean login() {
        System.out.println("\n=== User Login ===");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        if (!user.validatePassword(password)) {
            System.out.println("Invalid Password");
            return false;
        }

        if (user.authenticate(username, password)) {
            loggedIn = true;
            System.out.println("User successfully logged in.");
        }
        return false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLogin(boolean login) {
        this.loggedIn = login;
    }

    public String getCurrentUser() {
        return user.getUser();
    }
}
