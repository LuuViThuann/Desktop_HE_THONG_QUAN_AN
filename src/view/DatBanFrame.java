package view;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import dao.BanDAO;
import model.DatBanTruoc;

public class DatBanFrame extends JDialog {
    private static final long serialVersionUID = 1L;
    private int maBan;
    private String tenBan;
    
    private JTextField txtHoTenKhach;
    private JTextField txtSDT;
    private JSpinner spinnerSoLuongKhach;
    private JSpinner spinnerNgayDat;
    private JSpinner spinnerGioDat;
    
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    public DatBanFrame(JFrame parent, int maBan, String tenBan) {
        super(parent, "Đặt Bàn - " + tenBan, true);
        this.maBan = maBan;
        this.tenBan = tenBan;
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(550, 500);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JLabel headerLabel = new JLabel("THÔNG TIN ĐẶT BÀN", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(PRIMARY_COLOR);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        formPanel.setBackground(Color.WHITE);
        
        // Họ tên
        JLabel lblHoTen = new JLabel("Họ Tên Khách:");
        lblHoTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtHoTenKhach = new JTextField();
        txtHoTenKhach.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtHoTenKhach.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Số điện thoại
        JLabel lblSDT = new JLabel("Số Điện Thoại:");
        lblSDT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSDT = new JTextField();
        txtSDT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSDT.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Số lượng khách
        JLabel lblSoLuong = new JLabel("Số Lượng Khách:");
        lblSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spinnerSoLuongKhach = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        spinnerSoLuongKhach.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinnerSoLuongKhach.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        
        // Ngày đặt
        JLabel lblNgayDat = new JLabel("Ngày Đặt:");
        lblNgayDat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spinnerNgayDat = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerNgayDat, "dd/MM/yyyy");
        spinnerNgayDat.setEditor(dateEditor);
        spinnerNgayDat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Giờ đặt
        JLabel lblGioDat = new JLabel("Giờ Đặt:");
        lblGioDat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        SpinnerDateModel timeModel = new SpinnerDateModel();
        spinnerGioDat = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerGioDat, "HH:mm");
        spinnerGioDat.setEditor(timeEditor);
        spinnerGioDat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        formPanel.add(lblHoTen);
        formPanel.add(txtHoTenKhach);
        formPanel.add(lblSDT);
        formPanel.add(txtSDT);
        formPanel.add(lblSoLuong);
        formPanel.add(spinnerSoLuongKhach);
        formPanel.add(lblNgayDat);
        formPanel.add(spinnerNgayDat);
        formPanel.add(lblGioDat);
        formPanel.add(spinnerGioDat);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton btnDat = createStyledButton("Đặt Bàn", SUCCESS_COLOR);
        JButton btnHuy = createStyledButton("Hủy", DANGER_COLOR);
        
        btnDat.addActionListener(e -> onDatBan());
        btnHuy.addActionListener(e -> dispose());
        
        buttonPanel.add(btnDat);
        buttonPanel.add(btnHuy);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 40));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void onDatBan() {
        String hoTen = txtHoTenKhach.getText().trim();
        String sdt = txtSDT.getText().trim();
        int soLuong = (Integer) spinnerSoLuongKhach.getValue();
        
        if (hoTen.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập đầy đủ thông tin!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Chuyển đổi từ java.util.Date sang java.sql.Date và java.sql.Time
        java.util.Date ngayUtil = (java.util.Date) spinnerNgayDat.getValue();
        java.util.Date gioUtil = (java.util.Date) spinnerGioDat.getValue();
        
        Date ngayDat = new Date(ngayUtil.getTime());
        Time gioDat = new Time(gioUtil.getTime());
        
        // Tạo đối tượng đặt bàn
        DatBanTruoc datBan = new DatBanTruoc(hoTen, sdt, soLuong, ngayDat, gioDat, maBan);
        
        // Lưu vào database
        if (BanDAO.themDatBan(datBan)) {
            // Cập nhật trạng thái bàn
            BanDAO.updateTrangThaiBan(maBan, "Đã đặt");
            
            JOptionPane.showMessageDialog(this, 
                "Đặt bàn thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi đặt bàn!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}