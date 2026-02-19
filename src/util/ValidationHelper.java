package util;
import java.util.regex.Pattern;
public class ValidationHelper {
    
    // Kiểm tra email hợp lệ
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    // Kiểm tra số điện thoại hợp lệ
    public static boolean isValidPhone(String phone) {
        String phoneRegex = "^[0-9]{10}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }
    
    // Kiểm tra mật khẩu mạnh
    public static boolean isStrongPassword(String password) {
        // Ít nhất 6 ký tự, có chữ hoa, chữ thường, số
        String strongRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";
        Pattern pattern = Pattern.compile(strongRegex);
        return pattern.matcher(password).matches();
    }
    
    // Kiểm tra chuỗi trống
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
