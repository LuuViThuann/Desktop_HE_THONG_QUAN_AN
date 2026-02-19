package util;
import javax.swing.*;
import java.awt.*;
public class UIHelper {
    
    // Tạo button với style
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        return btn;
    }
    
    // Tạo label với style
    public static JLabel createLabel(String text, int fontSize, boolean bold) {
        JLabel lbl = new JLabel(text);
        int fontStyle = bold ? Font.BOLD : Font.PLAIN;
        lbl.setFont(new Font("Arial", fontStyle, fontSize));
        return lbl;
    }
    
    // Tạo text field với style
    public static JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.PLAIN, 12));
        txt.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return txt;
    }
    
    // Hiển thị error dialog
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    // Hiển thị success dialog
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thành Công", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Hiển thị confirmation dialog
    public static int showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xác Nhận", JOptionPane.YES_NO_OPTION);
    }
}