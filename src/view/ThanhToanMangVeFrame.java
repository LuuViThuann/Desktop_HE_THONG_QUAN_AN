package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import config.DatabaseConfig;
import config.UserSession;
import model.NhanVien;
import util.HoaDonPrinter;
import javax.swing.SwingUtilities;

public class ThanhToanMangVeFrame extends JDialog {

    private static final long serialVersionUID = 1L;
    private int maCTHD;
    private BigDecimal tongTienGoc;
    private JFrame parentFrame;
    
    private JTable tableChiTiet;
    private JLabel lblTongTien;
    private JLabel lblGiamGia;
    private JLabel lblThanhToan;
    private JTextField txtGiamGia;
    
   
    private JLabel lblMaHoaDon;
    private JLabel lblNgayThanhToan;
    private JLabel lblGioVao;
    private JLabel lblGioRa;
    private JLabel lblTenNV;
    private JLabel lblMaNV;
    private JTextArea txtGhiChu;
    private JLabel lblTongSoMon;
    private JLabel lblLoaiHoaDon;
    
    private DefaultTableModel model;
    private boolean daThanhToan = false;
    
    private boolean isGiamGiaApDung = false;
    
    public ThanhToanMangVeFrame(JFrame parent, int maCTHD, BigDecimal tongTien) {
        super(parent, "Thanh Toán Mang Về", true);
        this.parentFrame = parent;
        this.maCTHD = maCTHD;
        this.tongTienGoc = tongTien;
        initComponents();
        loadChiTiet();
        loadThongTinHoaDon(); 
        pack();
        setLocationRelativeTo(parent);
    }
    
    public boolean isDaThanhToan() {
        return daThanhToan;
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 800);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Header
   
       
        
     
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(243, 156, 18), 2),
                "Thông tin hóa đơn mua mang về",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(243, 156, 18)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        Font labelFont = new Font("Arial", Font.BOLD, 13);
        Font valueFont = new Font("Arial", Font.PLAIN, 13);
        Color valueColor = new Color(220, 53, 69);
        
        // Dòng 1: Mã hóa đơn, Ngày, Loại hóa đơn
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblMaHDTitle = new JLabel("Mã hóa đơn:");
        lblMaHDTitle.setFont(labelFont);
        infoPanel.add(lblMaHDTitle, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.3;
        lblMaHoaDon = new JLabel("---");
        lblMaHoaDon.setFont(new Font("Arial", Font.BOLD, 13));
        lblMaHoaDon.setForeground(valueColor);
        infoPanel.add(lblMaHoaDon, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblNgayTitle = new JLabel("Ngày:");
        lblNgayTitle.setFont(labelFont);
        infoPanel.add(lblNgayTitle, gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.3;
        lblNgayThanhToan = new JLabel("---");
        lblNgayThanhToan.setFont(valueFont);
        lblNgayThanhToan.setForeground(valueColor);
        infoPanel.add(lblNgayThanhToan, gbc);
        
        gbc.gridx = 4; gbc.weightx = 0;
        JLabel lblLoaiTitle = new JLabel("Loại:");
        lblLoaiTitle.setFont(labelFont);
        infoPanel.add(lblLoaiTitle, gbc);
        
        gbc.gridx = 5; gbc.weightx = 0.3;
        lblLoaiHoaDon = new JLabel("Mang về");
        lblLoaiHoaDon.setFont(new Font("Arial", Font.BOLD, 13));
        lblLoaiHoaDon.setForeground(new Color(243, 156, 18));
        infoPanel.add(lblLoaiHoaDon, gbc);
        
        // Dòng 2: Giờ vào, Giờ ra, Tổng số món
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblGioVaoTitle = new JLabel("Giờ vào:");
        lblGioVaoTitle.setFont(labelFont);
        infoPanel.add(lblGioVaoTitle, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.3;
        lblGioVao = new JLabel("---");
        lblGioVao.setFont(valueFont);
        infoPanel.add(lblGioVao, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblGioRaTitle = new JLabel("Giờ ra:");
        lblGioRaTitle.setFont(labelFont);
        infoPanel.add(lblGioRaTitle, gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.3;
        lblGioRa = new JLabel("---");
        lblGioRa.setFont(valueFont);
        infoPanel.add(lblGioRa, gbc);
        
        gbc.gridx = 4; gbc.weightx = 0;
        JLabel lblTongSoMonTitle = new JLabel("Tổng số món:");
        lblTongSoMonTitle.setFont(labelFont);
        infoPanel.add(lblTongSoMonTitle, gbc);
        
        gbc.gridx = 5; gbc.weightx = 0.3;
        lblTongSoMon = new JLabel("0");
        lblTongSoMon.setFont(new Font("Arial", Font.BOLD, 13));
        lblTongSoMon.setForeground(new Color(52, 152, 219));
        infoPanel.add(lblTongSoMon, gbc);
        
        // Dòng 3: Mã NV, Tên NV
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblMaNVTitle = new JLabel("Mã NV:");
        lblMaNVTitle.setFont(labelFont);
        infoPanel.add(lblMaNVTitle, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.3;
        lblMaNV = new JLabel("---");
        lblMaNV.setFont(valueFont);
        infoPanel.add(lblMaNV, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblTenNVTitle = new JLabel("Tên NV:");
        lblTenNVTitle.setFont(labelFont);
        infoPanel.add(lblTenNVTitle, gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.3; gbc.gridwidth = 3;
        lblTenNV = new JLabel("---");
        lblTenNV.setFont(valueFont);
        infoPanel.add(lblTenNV, gbc);
        
        // Dòng 4: Ghi chú
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lblGhiChuTitle = new JLabel("Ghi chú:");
        lblGhiChuTitle.setFont(labelFont);
        infoPanel.add(lblGhiChuTitle, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        txtGhiChu = new JTextArea(2, 20);
        txtGhiChu.setFont(valueFont);
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setPreferredSize(new Dimension(0, 50));
        infoPanel.add(scrollGhiChu, gbc);
        
        // Bảng chi tiết
        model = new DefaultTableModel();
        model.addColumn("Mã món");
        model.addColumn("Mã SP");
        model.addColumn("Tên Món");
        model.addColumn("ĐVT");
        model.addColumn("Số Lượng");
        model.addColumn("Giá");
        model.addColumn("% CK");
        model.addColumn("Thành Tiền");
        model.addColumn("Ghi chú");
        
        tableChiTiet = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableChiTiet.setFont(new Font("Arial", Font.PLAIN, 12));
        tableChiTiet.setRowHeight(30);
        tableChiTiet.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableChiTiet.getTableHeader().setBackground(new Color(52, 152, 219));
        tableChiTiet.getTableHeader().setForeground(Color.BLACK);
        tableChiTiet.setSelectionBackground(new Color(189, 195, 199));
        tableChiTiet.setGridColor(new Color(189, 195, 199));
        
        JScrollPane scrollPane = new JScrollPane(tableChiTiet);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Panel tổng cộng
        JPanel totalPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(243, 156, 18), 2),
                "Thanh Toán",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 15),
                new Color(243, 156, 18)
            ),
            BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        totalPanel.setBackground(Color.WHITE);
        
        // Dòng 1: Tổng tiền hóa đơn
        JLabel lblTongTienHDLabel = new JLabel("Tổng tiền hóa đơn:");
        lblTongTienHDLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPanel.add(lblTongTienHDLabel);
        
        lblTongTien = new JLabel("0 VNĐ");
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 15));
        lblTongTien.setForeground(new Color(52, 73, 94));
        totalPanel.add(lblTongTien);
        
        JLabel lblTongTienThuLabel = new JLabel("Tổng tiền phải thu:");
        lblTongTienThuLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPanel.add(lblTongTienThuLabel);
        
        lblThanhToan = new JLabel("0 VNĐ");
        lblThanhToan.setFont(new Font("Arial", Font.BOLD, 18));
        lblThanhToan.setForeground(new Color(231, 76, 60));
        totalPanel.add(lblThanhToan);
        
        // Dòng 2: Giảm giá
        JLabel lblGiamGiaLabel = new JLabel("Giảm Giá (%):");
        lblGiamGiaLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblGiamGiaLabel);
        
        JPanel giamGiaPanel = new JPanel(new BorderLayout(8, 0));
        giamGiaPanel.setBackground(Color.WHITE);

        txtGiamGia = new JTextField("0");
        txtGiamGia.setFont(new Font("Arial", Font.PLAIN, 14));
        txtGiamGia.setBackground(Color.WHITE);
        txtGiamGia.setForeground(Color.BLACK);
        txtGiamGia.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JButton btnApDung = new JButton("Áp dụng");
        btnApDung.setFont(new Font("Arial", Font.BOLD, 12));
        btnApDung.setBackground(new Color(52, 152, 219));
        btnApDung.setForeground(Color.WHITE);
        btnApDung.setFocusPainted(false);
        btnApDung.setBorderPainted(false);
        btnApDung.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApDung.setPreferredSize(new Dimension(80, 30));
        btnApDung.addActionListener(e -> apDungGiamGia());
        
        btnApDung.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnApDung.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(MouseEvent e) {
                btnApDung.setBackground(new Color(52, 152, 219));
            }
        });

        giamGiaPanel.add(txtGiamGia, BorderLayout.CENTER);
        giamGiaPanel.add(btnApDung, BorderLayout.EAST);
        totalPanel.add(giamGiaPanel);
        
        JLabel lblTongTienGiaoLabel = new JLabel("Tổng Tiền Giao:");
        lblTongTienGiaoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblTongTienGiaoLabel);
        
        JLabel lblTongTienGiao = new JLabel("0 VNĐ");
        lblTongTienGiao.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblTongTienGiao);
        
        // Dòng 3: Số tiền giảm
        JLabel lblGiamLabel = new JLabel("Giảm:");
        lblGiamLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblGiamLabel);
        
        lblGiamGia = new JLabel("0 VNĐ");
        lblGiamGia.setFont(new Font("Arial", Font.BOLD, 14));
        lblGiamGia.setForeground(new Color(39, 174, 96));
        totalPanel.add(lblGiamGia);
        
        JLabel lblCongNoLabel = new JLabel("Công nợ:");
        lblCongNoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblCongNoLabel);
        
        JLabel lblCongNo = new JLabel("0");
        lblCongNo.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblCongNo);
        
        // Dòng 4: Đã thanh toán và Tổng lời nhuận
        JLabel lblDaThanhToanLabel = new JLabel("Đã thanh toán:");
        lblDaThanhToanLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblDaThanhToanLabel);
        
        JLabel lblDaThanhToan = new JLabel("0 VNĐ");
        lblDaThanhToan.setFont(new Font("Arial", Font.PLAIN, 14));
        totalPanel.add(lblDaThanhToan);
        
        JLabel lblLoiNhuanLabel = new JLabel("Tổng lợi nhuận:");
        lblLoiNhuanLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPanel.add(lblLoiNhuanLabel);
        
        JLabel lblLoiNhuan = new JLabel("0 VNĐ");
        lblLoiNhuan.setFont(new Font("Arial", Font.BOLD, 14));
        lblLoiNhuan.setForeground(new Color(40, 167, 69));
        totalPanel.add(lblLoiNhuan);
        
        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);

        // Nút Tiền Mặt
        JButton btnTienMat = new JButton("Tiền Mặt");
        btnTienMat.setFont(new Font("Arial", Font.BOLD, 14));
        btnTienMat.setBackground(new Color(34, 139, 34));
        btnTienMat.setForeground(Color.WHITE);
        btnTienMat.setFocusPainted(false);
        btnTienMat.setBorderPainted(false);
        btnTienMat.setPreferredSize(new Dimension(150, 45));
        btnTienMat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTienMat.addActionListener(e -> onThanhToan("Tiền mặt"));
        
        btnTienMat.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnTienMat.setBackground(new Color(27, 111, 27));
            }
            public void mouseExited(MouseEvent e) {
                btnTienMat.setBackground(new Color(34, 139, 34));
            }
        });

        // Nút Chuyển Khoản
     // Nút Chuyển Khoản QR (Thêm mới)
        JButton btnChuyenKhoanQR = new JButton("Chuyển Khoản QR");
        btnChuyenKhoanQR.setFont(new Font("Arial", Font.BOLD, 14));
        btnChuyenKhoanQR.setBackground(new Color(155, 89, 182));
        btnChuyenKhoanQR.setForeground(Color.WHITE);
        btnChuyenKhoanQR.setFocusPainted(false);
        btnChuyenKhoanQR.setBorderPainted(false);
        btnChuyenKhoanQR.setPreferredSize(new Dimension(150, 45));
        btnChuyenKhoanQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChuyenKhoanQR.addActionListener(e -> onThanhToanQR());

        btnChuyenKhoanQR.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnChuyenKhoanQR.setBackground(new Color(142, 68, 173));
            }
            public void mouseExited(MouseEvent e) {
                btnChuyenKhoanQR.setBackground(new Color(155, 89, 182));
            }
        });

        // Nút Xem Trước Hóa Đơn -
        JButton btnInHoaDon = new JButton("Xem Trước Hóa Đơn");
        btnInHoaDon.setFont(new Font("Arial", Font.BOLD, 14));
        btnInHoaDon.setBackground(new Color(243, 156, 18));
        btnInHoaDon.setForeground(Color.WHITE);
        btnInHoaDon.setFocusPainted(false);
        btnInHoaDon.setBorderPainted(false);
        btnInHoaDon.setPreferredSize(new Dimension(180, 45));
        btnInHoaDon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnInHoaDon.addActionListener(e -> xemTruocHoaDonTamThoi());  
        
        btnInHoaDon.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnInHoaDon.setBackground(new Color(211, 136, 16));
            }
            public void mouseExited(MouseEvent e) {
                btnInHoaDon.setBackground(new Color(243, 156, 18));
            }
        });

        // Nút Thoát
        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Arial", Font.BOLD, 14));
        btnThoat.setBackground(new Color(231, 76, 60));
        btnThoat.setForeground(Color.WHITE);
        btnThoat.setFocusPainted(false);
        btnThoat.setBorderPainted(false);
        btnThoat.setPreferredSize(new Dimension(150, 45));
        btnThoat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThoat.addActionListener(e -> dispose());
        
        btnThoat.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnThoat.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(MouseEvent e) {
                btnThoat.setBackground(new Color(231, 76, 60));
            }
        });

        buttonPanel.add(btnTienMat);
        buttonPanel.add(btnChuyenKhoanQR); 
        buttonPanel.add(btnInHoaDon);
        buttonPanel.add(btnThoat);

      //  mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(236, 240, 241));
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setBackground(new Color(236, 240, 241));
        southPanel.add(totalPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
   
    private void loadThongTinHoaDon() {
        String query = "SELECT h.MaCTHD, h.NgayThanhToan, h.TongSoLuongMon, h.LoaiHoaDon, " +
                      "h.PhanTramGiamGia, h.TongTienThanhToan " +
                      "FROM HoaDonKhachHang h " +
                      "WHERE h.MaCTHD = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCTHD);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Mã hóa đơn
                lblMaHoaDon.setText(String.valueOf(rs.getInt("MaCTHD")));
                
                // Ngày thanh toán
                Date ngayTT = rs.getDate("NgayThanhToan");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                lblNgayThanhToan.setText(sdf.format(ngayTT));
                
                // Giờ vào - hiển thị thời gian hiện tại
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                lblGioVao.setText(timeFormat.format(ngayTT));
                lblGioRa.setText(timeFormat.format(new java.util.Date()));
                
                // Tổng số món
                lblTongSoMon.setText(String.valueOf(rs.getInt("TongSoLuongMon")));
                
                // Loại hóa đơn
                String loai = rs.getString("LoaiHoaDon");
                lblLoaiHoaDon.setText(loai != null ? loai : "Mang về");
                
                // Giảm giá
                BigDecimal giamGia = rs.getBigDecimal("PhanTramGiamGia");
                if (giamGia != null && giamGia.compareTo(BigDecimal.ZERO) > 0) {
                    txtGiamGia.setText(giamGia.toString());
                }
            }
            
            // Load thông tin nhân viên (mặc định MaNV = 1)
            NhanVien nhanVienHienTai = UserSession.getInstance().getCurrentUser();
            if (nhanVienHienTai != null) {
                lblMaNV.setText(String.valueOf(nhanVienHienTai.getMaNV()));
                lblTenNV.setText(nhanVienHienTai.getHoTen());
            } else {
                lblMaNV.setText("---");
                lblTenNV.setText("---");
            }
            
            isGiamGiaApDung = false;
            txtGiamGia.setText("0");
            
            apDungGiamGia();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải thông tin hóa đơn: " + e.getMessage());
        }
    }
    
  
    private void loadThongTinNhanVien(int maNV) {
        String query = "SELECT HoTen FROM NhanVien WHERE MaNV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                lblTenNV.setText(rs.getString("HoTen"));
            }
            
        } catch (SQLException e) {
            lblTenNV.setText("---");
        }
    }
    
    private void loadChiTiet() {
        String query = "SELECT c.MaCT, m.MaMon, m.TenMon, m.DonViTinh, c.SoLuong, c.GiaTien, " +
                      "(c.SoLuong * c.GiaTien) AS ThanhTien " +
                      "FROM ChiTietHoaDon c " +
                      "JOIN MonAn m ON c.MaMon = m.MaMon " +
                      "WHERE c.MaCTHD = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCTHD);
            ResultSet rs = pstmt.executeQuery();
            
            model.setRowCount(0);
            BigDecimal tong = BigDecimal.ZERO;
            
            while (rs.next()) {
                int maCT = rs.getInt("MaCT");
                int maMon = rs.getInt("MaMon");
                String tenMon = rs.getString("TenMon");
                String donViTinh = rs.getString("DonViTinh");
                int soLuong = rs.getInt("SoLuong");
                BigDecimal giaTien = rs.getBigDecimal("GiaTien");
                BigDecimal thanhTien = rs.getBigDecimal("ThanhTien");
                
                Object[] row = {
                    maCT,                                           // Mã món (MaCT)
                    String.format("%010d", maMon),                 // Mã SP (format 10 số)
                    tenMon,                                        // Tên món
                    donViTinh,                                     // ĐVT
                    soLuong,                                       // Số lượng
                    String.format("%,d", giaTien.longValue()),    // Giá
                    "0.00",                                        // % CK
                    String.format("%,d", thanhTien.longValue()),  // Thành tiền
                    ""                                             // Ghi chú
                };
                model.addRow(row);
                tong = tong.add(thanhTien);
            }
            
            tongTienGoc = tong;
            lblTongTien.setText(String.format("%,d VNĐ", tongTienGoc.longValue()));
            apDungGiamGia();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết: " + e.getMessage());
        }
    }
    
    private void apDungGiamGia() {
        try {
            BigDecimal phanTramGiam = new BigDecimal(txtGiamGia.getText().trim());
            
            if (phanTramGiam.compareTo(BigDecimal.ZERO) < 0 || 
                phanTramGiam.compareTo(new BigDecimal(100)) > 0) {
                JOptionPane.showMessageDialog(this, "Giảm giá phải từ 0-100%!");
                txtGiamGia.setText("0");
                return;
            }
            
          
            BigDecimal tienGiam = tongTienGoc.multiply(phanTramGiam)
                                           .divide(new BigDecimal(100));
            BigDecimal thanhToan = tongTienGoc.subtract(tienGiam);
            
          
            lblGiamGia.setText(String.format("%,d VNĐ", tienGiam.longValue()));
            lblThanhToan.setText(String.format("%,d VNĐ", thanhToan.longValue()));
            
         
            isGiamGiaApDung = true;
            
        } catch (NumberFormatException e) {
            txtGiamGia.setText("0");
            lblGiamGia.setText("0 VNĐ");
            lblThanhToan.setText(String.format("%,d VNĐ", tongTienGoc.longValue()));
            isGiamGiaApDung = false;
        }
    }
    
    private void onThanhToanQR() {
        try {
            BigDecimal thanhToanAmount = new BigDecimal(
                lblThanhToan.getText().replaceAll("[^0-9]", "")
            );
            
            ThanhToanQRFrame qrFrame = new ThanhToanQRFrame(parentFrame, maCTHD, thanhToanAmount);
            qrFrame.setVisible(true);
            
            // Kiểm tra xem user có xác nhận thanh toán hay không
            if (qrFrame.isDaThanhToan()) {
                // Cập nhật vào database
                capNhatThanhToan("Chuyển khoản QR");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi xử lý QR: " + e.getMessage());
        }
    }

    private void capNhatThanhToan(String phuongThuc) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String phanTramGiam = txtGiamGia.getText().trim();
            BigDecimal giamGia = BigDecimal.ZERO;

            if (!phanTramGiam.isEmpty()) {
                try {
                    giamGia = new BigDecimal(phanTramGiam);
                    if (giamGia.compareTo(BigDecimal.ZERO) < 0 || 
                        giamGia.compareTo(new BigDecimal(100)) > 0) {
                        giamGia = BigDecimal.ZERO;
                    }
                } catch (NumberFormatException e) {
                    giamGia = BigDecimal.ZERO;
                }
            }

            BigDecimal tienGiam = tongTienGoc.multiply(giamGia)
                                             .divide(new BigDecimal(100), 0, java.math.RoundingMode.HALF_UP);
            BigDecimal thanhToanValue = tongTienGoc.subtract(tienGiam);

            String updateHD = "UPDATE HoaDonKhachHang " +
                    "SET PhanTramGiamGia = ?, TongTienThanhToan = ?, DanhSachMon = ?, PhuongThucThanhToan = ? " +
                    "WHERE MaCTHD = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(updateHD);
            pstmt.setBigDecimal(1, giamGia);
            pstmt.setBigDecimal(2, thanhToanValue);
            pstmt.setString(3, txtGhiChu.getText());
            pstmt.setString(4, phuongThuc);
            pstmt.setInt(5, maCTHD);
            pstmt.executeUpdate();
                                
            daThanhToan = true;
            
            JOptionPane.showMessageDialog(this,
                "Thanh toán " + phuongThuc + " thành công!\n" +
                "Tổng tiền: " + lblThanhToan.getText(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            xemTruocHoaDon();
            dispose();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi thanh toán: " + e.getMessage());
        }
    }
    
    private void onThanhToan(String phuongThuc) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận thanh toán mang về bằng " + phuongThuc + "?\n" + lblThanhToan.getText(),
            "Xác Nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String phanTramGiam = txtGiamGia.getText().trim();
                BigDecimal giamGia = BigDecimal.ZERO;

                if (!phanTramGiam.isEmpty()) {
                    try {
                        giamGia = new BigDecimal(phanTramGiam);
                        
                      
                        if (giamGia.compareTo(BigDecimal.ZERO) < 0 || 
                            giamGia.compareTo(new BigDecimal(100)) > 0) {
                            giamGia = BigDecimal.ZERO;
                        }
                    } catch (NumberFormatException e) {
                        giamGia = BigDecimal.ZERO;
                    }
                }

              
                BigDecimal tienGiam = tongTienGoc.multiply(giamGia)
                                                 .divide(new BigDecimal(100), 0, java.math.RoundingMode.HALF_UP);
                BigDecimal thanhToanValue = tongTienGoc.subtract(tienGiam);
                String thanhToanStr = String.valueOf(thanhToanValue.longValue());
                
                // Cập nhật hóa đơn
                String updateHD = "UPDATE HoaDonKhachHang " +
                        "SET PhanTramGiamGia = ?, TongTienThanhToan = ?, DanhSachMon = ? " +
                        "WHERE MaCTHD = ?";
                
                PreparedStatement pstmt = conn.prepareStatement(updateHD);
                pstmt.setBigDecimal(1, giamGia);
                pstmt.setBigDecimal(2, new BigDecimal(thanhToanStr));
                pstmt.setString(3, txtGhiChu.getText());  
                pstmt.setInt(4, maCTHD);
                pstmt.executeUpdate();
                                
                daThanhToan = true;
                
                JOptionPane.showMessageDialog(this,
                    "Thanh toán mang về thành công bằng " + phuongThuc + "!\n" +
                    "Tổng tiền: " + lblThanhToan.getText(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
              
                xemTruocHoaDon();
                
                dispose();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi thanh toán: " + e.getMessage());
            }
        }
    }
    
   
    private void xemTruocHoaDonTamThoi() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // Tính toán giảm giá
            String phanTramGiam = txtGiamGia.getText().trim();
            BigDecimal giamGia = BigDecimal.ZERO;
            
            if (!phanTramGiam.isEmpty()) {
                try {
                    giamGia = new BigDecimal(phanTramGiam);
                    if (giamGia.compareTo(BigDecimal.ZERO) < 0 || 
                        giamGia.compareTo(new BigDecimal(100)) > 0) {
                        giamGia = BigDecimal.ZERO;
                    }
                } catch (NumberFormatException e) {
                    giamGia = BigDecimal.ZERO;
                }
            }
            
            BigDecimal tienGiam = tongTienGoc.multiply(giamGia)
                                             .divide(new BigDecimal(100), 0, java.math.RoundingMode.HALF_UP);
            BigDecimal thanhToanValue = tongTienGoc.subtract(tienGiam);
            
            // 1. Lấy dữ liệu chi tiết hiện tại
            String getDetail = "SELECT MaMon, SoLuong, GiaTien FROM ChiTietHoaDon WHERE MaCTHD = ?";
            PreparedStatement pstmtGet = conn.prepareStatement(getDetail,
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmtGet.setInt(1, maCTHD);
            ResultSet rsDetail = pstmtGet.executeQuery();
            
            // 2. Tạo ChiTietBanHang TẠM
            String insertCTBH = "INSERT INTO ChiTietBanHang (SoLuong, MaMon) VALUES (?, ?)";
            PreparedStatement pstmtCTBH = conn.prepareStatement(insertCTBH, 
                Statement.RETURN_GENERATED_KEYS);
            
            int maCTBH = -1;
            if (rsDetail.next()) {
                pstmtCTBH.setInt(1, rsDetail.getInt("SoLuong"));
                pstmtCTBH.setInt(2, rsDetail.getInt("MaMon"));
                pstmtCTBH.executeUpdate();
                
                ResultSet rsKey = pstmtCTBH.getGeneratedKeys();
                if (rsKey.next()) {
                    maCTBH = rsKey.getInt(1);
                }
            }
            
            // 3. Tính tổng số món
            int tongSoMon = 0;
            rsDetail.beforeFirst();
            while (rsDetail.next()) {
                tongSoMon += rsDetail.getInt("SoLuong");
            }
            
            // 4. Tạo HoaDonKhachHang TẠM
            String insertHD = "INSERT INTO HoaDonKhachHang " +
                            "(NgayThanhToan, MaBan, MaCTBH, TongTienThanhToan, " +
                            "PhanTramGiamGia, TongSoLuongMon, LoaiHoaDon, DanhSachMon) " +
                            "VALUES (CURDATE(), NULL, ?, ?, ?, ?, 'Mang về', ?)";
            
            PreparedStatement pstmtHD = conn.prepareStatement(insertHD, 
                Statement.RETURN_GENERATED_KEYS);
            
            pstmtHD.setInt(1, maCTBH);
            pstmtHD.setBigDecimal(2, thanhToanValue);
            pstmtHD.setBigDecimal(3, giamGia);
            pstmtHD.setInt(4, tongSoMon);
            pstmtHD.setString(5, txtGhiChu.getText());  // Lưu ghi chú
            pstmtHD.executeUpdate();
            
            ResultSet rsHD = pstmtHD.getGeneratedKeys();
            int maCTHDTam = -1;
            if (rsHD.next()) {
                maCTHDTam = rsHD.getInt(1);
            }
            
            // 5. Tạo ChiTietHoaDon TẠM
            String insertCTHD = "INSERT INTO ChiTietHoaDon " +
                              "(MaCTHD, MaMon, SoLuong, GiaTien) " +
                              "VALUES (?, ?, ?, ?)";
            PreparedStatement pstmtDetailHD = conn.prepareStatement(insertCTHD);
            
            rsDetail.beforeFirst();
            while (rsDetail.next()) {
                pstmtDetailHD.setInt(1, maCTHDTam);
                pstmtDetailHD.setInt(2, rsDetail.getInt("MaMon"));
                pstmtDetailHD.setInt(3, rsDetail.getInt("SoLuong"));
                pstmtDetailHD.setBigDecimal(4, rsDetail.getBigDecimal("GiaTien"));
                pstmtDetailHD.addBatch();
            }
            
            pstmtDetailHD.executeBatch();
            
            // 6. COMMIT để lưu vào database
            conn.commit();
            
            // 7. XEM TRƯỚC HÓA ĐƠN
            if (maCTHDTam > 0) {
                HoaDonPreviewFrame preview = new HoaDonPreviewFrame(
                    parentFrame,
                    maCTHDTam,
                    "Mang về"
                );
                preview.setVisible(true);
                
                // 8. SAU KHI ĐÓNG PREVIEW - XÓA DỮ LIỆU TẠM
                conn.setAutoCommit(false);
                
                String deleteCTHD = "DELETE FROM ChiTietHoaDon WHERE MaCTHD = ?";
                PreparedStatement pstmtDelCTHD = conn.prepareStatement(deleteCTHD);
                pstmtDelCTHD.setInt(1, maCTHDTam);
                pstmtDelCTHD.executeUpdate();
                
                String deleteHD = "DELETE FROM HoaDonKhachHang WHERE MaCTHD = ?";
                PreparedStatement pstmtDelHD = conn.prepareStatement(deleteHD);
                pstmtDelHD.setInt(1, maCTHDTam);
                pstmtDelHD.executeUpdate();
                
                String deleteCTBH = "DELETE FROM ChiTietBanHang WHERE MaCTBH = ?";
                PreparedStatement pstmtDelCTBH = conn.prepareStatement(deleteCTBH);
                pstmtDelCTBH.setInt(1, maCTBH);
                pstmtDelCTBH.executeUpdate();
                
                conn.commit();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi xem trước hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
  
    private void xemTruocHoaDon() {
        HoaDonPreviewFrame preview = new HoaDonPreviewFrame(
            parentFrame,
            maCTHD,
            "Mang về"
        );
        preview.setVisible(true);
    }
}