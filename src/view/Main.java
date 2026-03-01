package view;
import view.FormDangNhap;
import javax.swing.*;

public class Main {
	  public static void main(String[] args) {
	        // Đảm bảo UI được tạo trên Event Dispatch Thread =========	
	        SwingUtilities.invokeLater(() -> {
	            try {
	               
	                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            
	           
	            SplashScreen splash = new SplashScreen();
	            splash.setVisible(true);
	        });
	    }
}

// yêu cầu từ Thầy : 

// số lượng bán ra hôm nay 

// chọn vào hóa đơn hiển thị ra danh sách món ăn 