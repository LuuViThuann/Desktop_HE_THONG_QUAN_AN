package config;
import model.NhanVien;
public class UserSession {
    private static UserSession instance;
    private NhanVien currentUser;
    
    private UserSession() {}
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setCurrentUser(NhanVien user) {
        this.currentUser = user;
    }
    
    public NhanVien getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getHoTen() : "Guest";
    }
}
