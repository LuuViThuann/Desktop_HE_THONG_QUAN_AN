package view;
import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import dao.BanDAO;
import model.DatBanTruoc;

public class ThongTinDatBanFrame extends JDialog {
    private static final long serialVersionUID = 1L;
    private int maBan;
    private String tenBan;
    private DatBanTruoc datBan;
    
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color INFO_BG = new Color(250, 250, 250);
    
    public ThongTinDatBanFrame(JFrame parent, int maBan, String tenBan) {
        super(parent, "Thông Tin Đặt Bàn - " + tenBan, true);
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.datBan = BanDAO.getThongTinDatBan(maBan);
        
        if (datBan != null) {
            initComponents();
            setLocationRelativeTo(parent);
        } else {
            JOptionPane.showMessageDialog(parent, 
                "Không tìm thấy thông tin đặt bàn!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        
        // ===== HEADER PANEL =====
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(WARNING_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel iconLabel = new JLabel("📋", JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel headerLabel = new JLabel("THÔNG TIN ĐẶT BÀN");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(new Color(51, 51, 51));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subLabel = new JLabel("Chi tiết đặt bàn trước");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(new Color(80, 80, 80));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(headerLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subLabel);
        
        JPanel headerContent = new JPanel(new BorderLayout(15, 0));
        headerContent.setOpaque(false);
        headerContent.add(iconLabel, BorderLayout.WEST);
        headerContent.add(titlePanel, BorderLayout.CENTER);
        
        headerPanel.add(headerContent, BorderLayout.CENTER);
        
        // ===== INFO PANEL - BỐ CỤC 2 CỘT =====
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 20, 15));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        
        // Hàng 1: Bàn + Họ Tên
        infoPanel.add(createInfoCard("🪑", "Bàn", tenBan, new Color(41, 128, 185)));
        infoPanel.add(createInfoCard("👤", "Họ Tên Khách", datBan.getHoTenKhachHang(), new Color(52, 152, 219)));
        
        // Hàng 2: Số Điện Thoại + Số Lượng Khách
        infoPanel.add(createInfoCard("📱", "Số Điện Thoại", datBan.getSdt(), new Color(46, 204, 113)));
        infoPanel.add(createInfoCard("👥", "Số Lượng Khách", datBan.getSoLuongKhach() + " người", new Color(155, 89, 182)));
        
        // Hàng 3: Ngày Đặt + Giờ Đặt
        infoPanel.add(createInfoCard("📅", "Ngày Đặt", sdfDate.format(datBan.getNgayDat()), new Color(230, 126, 34)));
        infoPanel.add(createInfoCard("🕐", "Giờ Đặt", sdfTime.format(datBan.getGioDat()), new Color(231, 76, 60)));
        
        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        JButton btnGoiMon = createStyledButton("Gọi Món Ngay", SUCCESS_COLOR, 160, 45);
        JButton btnHuyDat = createStyledButton("Hủy Đặt Bàn", DANGER_COLOR, 160, 45);
        JButton btnDong = createStyledButton("Đóng", new Color(127, 140, 141), 120, 45);
        
        btnGoiMon.addActionListener(e -> onGoiMonNgay());
        btnHuyDat.addActionListener(e -> onHuyDatBan());
        btnDong.addActionListener(e -> dispose());
        
        buttonPanel.add(btnGoiMon);
        buttonPanel.add(btnHuyDat);
        buttonPanel.add(btnDong);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    private JPanel createInfoCard(String icon, String label, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(12, 0));
        card.setBackground(INFO_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        // Icon panel
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setPreferredSize(new Dimension(35, 35));
        lblIcon.setHorizontalAlignment(JLabel.CENTER);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLabel.setForeground(new Color(100, 100, 100));
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValue.setForeground(accentColor);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(lblLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(lblValue);
        
        card.add(lblIcon, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JButton createStyledButton(String text, Color bgColor, int width, int height) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(originalColor);
            }
        });
        
        return btn;
    }
    
    private void onGoiMonNgay() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
          
            String updateBan = "UPDATE Ban SET TrangThai = 'Đang sử dụng' WHERE MaBan = ?";
            PreparedStatement pstmtBan = conn.prepareStatement(updateBan);
            pstmtBan.setInt(1, maBan);
            pstmtBan.executeUpdate();
            
            conn.commit();
            
         
            dispose();
            
          
            GoiMonFrame formGoiMon = new GoiMonFrame(maBan, tenBan);
            formGoiMon.setVisible(true);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật trạng thái: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void onHuyDatBan() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn hủy đặt bàn này?",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (BanDAO.huyDatBan(maBan)) {
                JOptionPane.showMessageDialog(this, 
                    "Đã hủy đặt bàn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi hủy đặt bàn!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}