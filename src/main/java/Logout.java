public class Logout {
    public void logout(Login login){
        if(login.isLoggedIn()){
            System.out.println("\n=== User Logout ===");
            login.setLogin(false);
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("Failed logging out.");
        }
    }
}
