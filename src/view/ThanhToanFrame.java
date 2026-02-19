package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import config.DatabaseConfig;
import config.UserSession;
import dao.BanDAO;
import model.NhanVien;


public class ThanhToanFrame extends JDialog {

    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK = new Color(25, 45, 85);        
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);     
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);     
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);     
    private static final Color DANGER_RED = new Color(192, 57, 43);         
    private static final Color WARNING_AMBER = new Color(241, 196, 15);     
    
    private static final Color BG_MAIN = new Color(241, 244, 247);          
    private static final Color BG_SECONDARY = new Color(255, 255, 255);     
    private static final Color TEXT_DARK = new Color(44, 62, 80);           
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);       
    private static final Color BORDER_COLOR = new Color(189, 195, 199);     

    private static final long serialVersionUID = 1L;
    private int maBan;
    private String tenBan;
    private JFrame parentFrame;
    
    private JTable tableChiTiet;
    private JLabel lblTongTien;
    private JLabel lblGiamGia;
    private JLabel lblThanhToan;
    private JTextField txtGiamGia;
    
    private JLabel lblMaHoaDon;
    private JLabel lblTenPhong;
    private JLabel lblDonGia;
    private JLabel lblNgayThanhToan;
    private JLabel lblGioVao;
    private JLabel lblGioRa;
    private JLabel lblTenNV;
    private JLabel lblMaNV;
    private JLabel lblTenNVTinhTien;
    private JLabel lblMaNVTinhTien;
    private JTextArea txtGhiChu;
    private JLabel lblTongSoMon;
    
    private DefaultTableModel model;
    private BigDecimal tongTienGoc = BigDecimal.ZERO;
    private boolean daThanhToan = false;
    private boolean isGiamGiaApDung = false;
    private int maCTHDDaThanhToan = -1;
    
    public ThanhToanFrame(JFrame parent, int maBan, String giamGiaHienTai) {
        super(parent, "Thanh Toán - Bàn " + maBan, true);
        this.parentFrame = parent;
        this.maBan = maBan;
        this.tenBan = getTenBanFromDatabase(maBan);
        
        initComponents();
        loadChiTiet();
        
        if (giamGiaHienTai != null && !giamGiaHienTai.trim().isEmpty()) {
            txtGiamGia.setText(giamGiaHienTai);
            apDungGiamGia();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private String getTenBanFromDatabase(int maBan) {
        String query = "SELECT TenBan FROM Ban WHERE MaBan = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("TenBan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Bàn " + maBan;
    }
    
    public boolean isDaThanhToan() {
        return daThanhToan;
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1000, 900);
        
        // ========== PANEL CHÍNH ==========
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_MAIN);
        
      
        
        // ========== CONTENT ==========
        JPanel contentPanel = createContentPanel();
        
        // ========== FOOTER (BUTTONS) ==========
        JPanel footerPanel = createFooterPanel();
        
      
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    // ========== HEADER PANEL ==========
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), 0, new Color(45, 75, 125)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(0, 60));
        
        JLabel iconHeader = new JLabel("💳");
        iconHeader.setFont(new Font("Arial", Font.PLAIN, 28));
        
        JLabel titleHeader = new JLabel("HÓA ĐƠN THANH TOÁN");
        titleHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleHeader.setForeground(Color.WHITE);
        
        JLabel subtitleHeader = new JLabel(tenBan);
        subtitleHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleHeader.setForeground(new Color(200, 220, 240));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(titleHeader);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(subtitleHeader);
        
        panel.add(iconHeader, BorderLayout.WEST);
        panel.add(leftPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== CONTENT PANEL ==========
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // ========== THÔNG TIN HÓA ĐƠN ==========
        JPanel infoPanel = createInfoPanel();
        
        // ========== BẢNG CHI TIẾT ==========
        JPanel tablePanel = createTablePanel();
        
        // ========== TỔNG CỘNG ==========
        JPanel totalPanel = createTotalPanel();
        
        // ========== SPLIT PANE ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(infoPanel);
        splitPane.setBottomComponent(tablePanel);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.35);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(totalPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ========== THÔNG TIN HÓA ĐƠN ==========
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JPanel headerInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerInfo.setBackground(BG_SECONDARY);
        
       
        
        JLabel titleInfo = new JLabel("THÔNG TIN HÓA ĐƠN");
        titleInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleInfo.setForeground(PRIMARY_LIGHT);
        
       
        headerInfo.add(titleInfo);
        
        JPanel gridPanel = new JPanel(new GridLayout(4, 3, 12, 10));
        gridPanel.setBackground(BG_SECONDARY);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Hàng 1
        gridPanel.add(createInfoField("Mã bàn:", lblMaHoaDon = new JLabel(String.valueOf(maBan))));
        gridPanel.add(createInfoField("Tên bàn:", lblTenPhong = new JLabel(tenBan)));
        gridPanel.add(createInfoField("Tổng món:", lblTongSoMon = new JLabel("0")));
        
        // Hàng 2
        gridPanel.add(createInfoField("Ngày:", lblNgayThanhToan = new JLabel("---")));
        gridPanel.add(createInfoField("Giờ vào:", lblGioVao = new JLabel("---")));
        gridPanel.add(createInfoField("Giờ ra:", lblGioRa = new JLabel("---")));
        
        // Hàng 3
        gridPanel.add(createInfoField("Mã NV mở:", lblMaNV = new JLabel("---")));
        gridPanel.add(createInfoField("Tên NV mở:", lblTenNV = new JLabel("---")));
        gridPanel.add(createInfoField("Đơn giá:", lblDonGia = new JLabel("0")));
        
        // Hàng 4
        gridPanel.add(createInfoField("Mã NV tính:", lblMaNVTinhTien = new JLabel("---")));
        gridPanel.add(createInfoField("Tên NV tính:", lblTenNVTinhTien = new JLabel("---")));
        gridPanel.add(new JLabel(""));
        
        JPanel ghuChuPanel = new JPanel(new BorderLayout(8, 5));
        ghuChuPanel.setBackground(BG_SECONDARY);
        ghuChuPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel lblGhiChuTitle = new JLabel("Ghi chú:");
        lblGhiChuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblGhiChuTitle.setForeground(TEXT_DARK);
        
        txtGhiChu = new JTextArea(2, 0);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBorder(null);
        scrollGhiChu.setPreferredSize(new Dimension(0, 50));
        
        ghuChuPanel.add(lblGhiChuTitle, BorderLayout.NORTH);
        ghuChuPanel.add(scrollGhiChu, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setBackground(BG_SECONDARY);
        contentPanel.add(gridPanel, BorderLayout.NORTH);
        contentPanel.add(ghuChuPanel, BorderLayout.CENTER);
        
        panel.add(headerInfo, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInfoField(String label, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BG_SECONDARY);
        
        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(TEXT_DARK);
        
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        valueLabel.setForeground(PRIMARY_LIGHT);
        
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== BẢNG CHI TIẾT ==========
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JPanel headerTable = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerTable.setBackground(BG_SECONDARY);
        
       
        
        JLabel titleTable = new JLabel("CHI TIẾT ĐƠN HÀNG");
        titleTable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleTable.setForeground(PRIMARY_LIGHT);
        
       
        headerTable.add(titleTable);
        
        // ========== TABLE ==========
        model = new DefaultTableModel();
        model.addColumn("Mã");
        model.addColumn("Tên Món");
        model.addColumn("ĐVT");
        model.addColumn("SL");
        model.addColumn("Giá");
        model.addColumn("CK %");
        model.addColumn("Thành Tiền");
        
        tableChiTiet = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tableChiTiet.setForeground(TEXT_DARK);
        tableChiTiet.setBackground(Color.WHITE);
        tableChiTiet.setRowHeight(28);
        tableChiTiet.setShowGrid(true);
        tableChiTiet.setGridColor(new Color(230, 235, 240));
        tableChiTiet.setSelectionBackground(new Color(70, 130, 180, 30));
        tableChiTiet.setSelectionForeground(TEXT_DARK);
        
        JTableHeader header = tableChiTiet.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(45, 85, 135));
        header.setForeground(Color.BLACK);
        header.setBorder(BorderFactory.createLineBorder(PRIMARY_LIGHT));
        header.setPreferredSize(new Dimension(0, 28));
        
        JScrollPane scrollPane = new JScrollPane(tableChiTiet);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);
        
        panel.add(headerTable, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== TỔNG CỘNG PANEL ==========
    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // ========== TỔNG TIỀN & GIẢM GIÁ ==========
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        topPanel.setBackground(BG_SECONDARY);
        
        // Tổng tiền
        JPanel tongTienPanel = createAmountPanel("Tổng Tiền Hóa Đơn:", lblTongTien = new JLabel("0 VNĐ"), new Color(52, 152, 219));
        
        // Giảm giá
        JPanel giamGiaPanel = new JPanel(new BorderLayout(8, 5));
        giamGiaPanel.setBackground(BG_SECONDARY);
        
        JLabel lblGiamGiaTitle = new JLabel("Giảm Giá (%):");
        lblGiamGiaTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGiamGiaTitle.setForeground(TEXT_DARK);
        
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBackground(BG_SECONDARY);
        
        txtGiamGia = new JTextField("0", 10);
        txtGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiamGia.setBackground(Color.WHITE);
        txtGiamGia.setForeground(TEXT_DARK);
        txtGiamGia.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        
        JButton btnApDung = createSmallButton("Áp dụng", PRIMARY_LIGHT);
        btnApDung.addActionListener(e -> apDungGiamGia());
        
        inputPanel.add(txtGiamGia, BorderLayout.CENTER);
        inputPanel.add(btnApDung, BorderLayout.EAST);
        
        giamGiaPanel.add(lblGiamGiaTitle, BorderLayout.NORTH);
        giamGiaPanel.add(inputPanel, BorderLayout.CENTER);
        
        topPanel.add(tongTienPanel);
        topPanel.add(giamGiaPanel);
        
        // ========== THÀNH TIỀN & GIẢM ==========
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setBackground(BG_SECONDARY);
        
        JPanel thanhTienPanel = createAmountPanel("Phải Thu:", lblThanhToan = new JLabel("0 VNĐ"), DANGER_RED);
        JPanel soTienGiamPanel = createAmountPanel("Tiền Giảm:", lblGiamGia = new JLabel("0 VNĐ"), SUCCESS_GREEN);
        
        bottomPanel.add(thanhTienPanel);
        bottomPanel.add(soTienGiamPanel);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAmountPanel(String title, JLabel valueLabel, Color valueColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(BG_SECONDARY);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_DARK);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(JLabel.RIGHT);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== FOOTER PANEL (BUTTONS) ==========
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        panel.setPreferredSize(new Dimension(0, 70));
        
        JButton btnTienMat = createActionButton("Tiền Mặt", "", SUCCESS_GREEN);
        btnTienMat.addActionListener(e -> onThanhToan("Tiền mặt"));
        
        JButton btnChuyenKhoan = createActionButton("Chuyển Khoản", "", PRIMARY_LIGHT);
        btnChuyenKhoan.addActionListener(e -> onThanhToanQR());
        
        JButton btnXemTruoc = createActionButton("Xem Trước", "", ACCENT_ORANGE);
        btnXemTruoc.addActionListener(e -> xemTruocHoaDonTamThoi());
        
        JButton btnThoat = createActionButton("Thoát", "", new Color(120, 130, 140));
        btnThoat.addActionListener(e -> dispose());
        
        panel.add(btnTienMat);
        panel.add(btnChuyenKhoan);
        panel.add(btnXemTruoc);
        panel.add(btnThoat);
        
        return panel;
    }
    
    // ========== HELPER - TẠO NÚT ==========
    private JButton createActionButton(String text, String emoji, Color bgColor) {
        JButton btn = new JButton(emoji + " " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, 
                    getWidth(), getHeight(), 8, 8);
                g2d.setColor(getBackground());
                g2d.fill(roundRect);
                
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(140, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
                btn.repaint();
            }
        });
        
        return btn;
    }
    
    private JButton createSmallButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, 
                    getWidth(), getHeight(), 6, 6);
                g2d.setColor(getBackground());
                g2d.fill(roundRect);
                
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(80, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
                btn.repaint();
            }
        });
        
        return btn;
    }
    
    // ========== LOAD DATA ==========
    private void loadChiTiet() {
        String query = "SELECT h.MaHDBH, m.MaMon, m.TenMon, m.DonViTinh, h.SoLuong, m.GiaTien, " +
                      "(h.SoLuong * m.GiaTien) AS ThanhTien, h.NgayVao " +
                      "FROM HoaDonBanHang h " +
                      "JOIN MonAn m ON h.MaMon = m.MaMon " +
                      "WHERE h.MaBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            
            model.setRowCount(0);
            BigDecimal tong = BigDecimal.ZERO;
            int tongSoMon = 0;
            Date ngayVaoDauTien = null;
            
            while (rs.next()) {
                int maHDBH = rs.getInt("MaHDBH");
                int maMon = rs.getInt("MaMon");
                String tenMon = rs.getString("TenMon");
                String donViTinh = rs.getString("DonViTinh");
                int soLuong = rs.getInt("SoLuong");
                BigDecimal giaTien = rs.getBigDecimal("GiaTien");
                BigDecimal thanhTien = rs.getBigDecimal("ThanhTien");
                Date ngayVao = rs.getDate("NgayVao");
                
                if (ngayVaoDauTien == null) {
                    ngayVaoDauTien = ngayVao;
                }
                
                Object[] row = {
                    maHDBH,
                    tenMon,
                    donViTinh,
                    soLuong,
                    String.format("%,d", giaTien.longValue()),
                    "0.00",
                    String.format("%,d", thanhTien.longValue())
                };
                model.addRow(row);
                tong = tong.add(thanhTien);
                tongSoMon += soLuong;
            }
            
            tongTienGoc = tong;
            lblTongTien.setText(String.format("%,d VNĐ", tongTienGoc.longValue()));
            lblTongSoMon.setText(String.valueOf(tongSoMon));
            
            if (ngayVaoDauTien != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                lblNgayThanhToan.setText(sdf.format(ngayVaoDauTien));
                
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                lblGioVao.setText(timeFormat.format(ngayVaoDauTien));
                lblGioRa.setText(timeFormat.format(new java.util.Date()));
            }
            
            NhanVien nhanVienHienTai = UserSession.getInstance().getCurrentUser();
            if (nhanVienHienTai != null) {
                int maNV = nhanVienHienTai.getMaNV();
                String tenNV = nhanVienHienTai.getHoTen();
                lblMaNV.setText(String.valueOf(maNV));
                lblMaNVTinhTien.setText(String.valueOf(maNV));
                lblTenNV.setText(tenNV);
                lblTenNVTinhTien.setText(tenNV);
            }
            
            isGiamGiaApDung = false;
            txtGiamGia.setText("0");
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
    
    private void onThanhToan(String phuongThuc) {
        String thanhToanAmount = lblThanhToan.getText().replaceAll("[^0-9]", "");
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận thanh toán " + tenBan + " bằng " + phuongThuc + "?\n\n" +
            "Tổng tiền: " + lblThanhToan.getText(),
            "Xác Nhận Thanh Toán", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                
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
                
                String getItems = "SELECT MaMon, SoLuong, (SoLuong * " +
                                "(SELECT GiaTien FROM MonAn WHERE MaMon = h.MaMon)) AS GiaTien " +
                                "FROM HoaDonBanHang h WHERE MaBan = ?";
                
                PreparedStatement pstmtGet = conn.prepareStatement(getItems,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                pstmtGet.setInt(1, maBan);
                ResultSet rsItems = pstmtGet.executeQuery();
                
                String insertCTBH = "INSERT INTO ChiTietBanHang (SoLuong, MaMon) VALUES (?, ?)";
                PreparedStatement pstmtCTBH = conn.prepareStatement(insertCTBH, 
                    Statement.RETURN_GENERATED_KEYS);
                
                int maCTBH = -1;
                if (rsItems.next()) {
                    pstmtCTBH.setInt(1, rsItems.getInt("SoLuong"));
                    pstmtCTBH.setInt(2, rsItems.getInt("MaMon"));
                    pstmtCTBH.executeUpdate();
                    
                    ResultSet rsKey = pstmtCTBH.getGeneratedKeys();
                    if (rsKey.next()) {
                        maCTBH = rsKey.getInt(1);
                    }
                }
                
                int tongSoMon = 0;
                rsItems.beforeFirst();
                while (rsItems.next()) {
                    tongSoMon += rsItems.getInt("SoLuong");
                }
                
                String insertHD = "INSERT INTO HoaDonKhachHang " +
                                "(NgayThanhToan, MaBan, MaCTBH, TongTienThanhToan, " +
                                "PhanTramGiamGia, TongSoLuongMon, LoaiHoaDon, DanhSachMon) " +
                                "VALUES (CURDATE(), ?, ?, ?, ?, ?, 'Tại quán', ?)";
                
                PreparedStatement pstmtHD = conn.prepareStatement(insertHD, 
                    Statement.RETURN_GENERATED_KEYS);
                
                pstmtHD.setInt(1, maBan);
                pstmtHD.setInt(2, maCTBH);
                pstmtHD.setBigDecimal(3, thanhToanValue);
                pstmtHD.setBigDecimal(4, giamGia);
                pstmtHD.setInt(5, tongSoMon);
                pstmtHD.setString(6, txtGhiChu.getText());
                pstmtHD.executeUpdate();
                
                ResultSet rsHD = pstmtHD.getGeneratedKeys();
                int maCTHD = -1;
                if (rsHD.next()) {
                    maCTHD = rsHD.getInt(1);
                    maCTHDDaThanhToan = maCTHD;
                }
                
                String insertCTHD = "INSERT INTO ChiTietHoaDon " +
                                  "(MaCTHD, MaMon, SoLuong, GiaTien) " +
                                  "VALUES (?, ?, ?, ?)";
                PreparedStatement pstmtDetailHD = conn.prepareStatement(insertCTHD);
                
                String getItemsDetail = "SELECT MaMon, SoLuong, (SoLuong * " +
                                      "(SELECT GiaTien FROM MonAn WHERE MaMon = h.MaMon)) AS GiaTien " +
                                      "FROM HoaDonBanHang h WHERE MaBan = ?";
                PreparedStatement pstmtDetail = conn.prepareStatement(getItemsDetail);
                pstmtDetail.setInt(1, maBan);
                ResultSet rsDetail = pstmtDetail.executeQuery();
                
                while (rsDetail.next()) {
                    pstmtDetailHD.setInt(1, maCTHD);
                    pstmtDetailHD.setInt(2, rsDetail.getInt("MaMon"));
                    pstmtDetailHD.setInt(3, rsDetail.getInt("SoLuong"));
                    pstmtDetailHD.setBigDecimal(4, rsDetail.getBigDecimal("GiaTien"));
                    pstmtDetailHD.addBatch();
                }
                
                pstmtDetailHD.executeBatch();
                
                BanDAO.xoaTatCaMonTrongBan(maBan, conn);
                BanDAO.updateTrangThaiBan(maBan, "Trống", conn);
                
                conn.commit();
                
                daThanhToan = true;
                
                JOptionPane.showMessageDialog(this,
                    "✅ Thanh toán thành công!\n\n" +
                    "Phương thức: " + phuongThuc + "\n" +
                    "Tổng tiền: " + lblThanhToan.getText(),
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                
                if (maCTHD > 0) {
                    xemTruocHoaDonVoiMa(maCTHD);
                }
                
                dispose();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Lỗi thanh toán: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    private void onThanhToanQR() {
        try {
            BigDecimal thanhToanAmount = new BigDecimal(
                lblThanhToan.getText().replaceAll("[^0-9]", "")
            );
            
            // Hiển thị dialog QR Code
            ThanhToanQRFrame qrFrame = new ThanhToanQRFrame(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                -1,  // Không có maCTHD (dùng -1 để biểu thị chưa lưu)
                thanhToanAmount
            );
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
            conn.setAutoCommit(false);
            
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

            System.out.println("═══════════════════════════════════════");
            System.out.println("THANH TOÁN CHUYỂN KHOẢN QR");
            System.out.println("Bàn: " + tenBan);
            System.out.println("Tổng tiền: " + thanhToanValue);
            System.out.println("Giảm giá: " + giamGia + "%");
            System.out.println("═══════════════════════════════════════");

            // ────────────────────────────────────────────────────────
            // 1️⃣ CẬP NHẬT HÓA ĐƠN
            // ────────────────────────────────────────────────────────
            String updateHD = "UPDATE HoaDonKhachHang " +
                    "SET PhanTramGiamGia = ?, TongTienThanhToan = ?, DanhSachMon = ?, PhuongThucThanhToan = ? " +
                    "WHERE MaCTHD = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(updateHD);
            pstmt.setBigDecimal(1, giamGia);
            pstmt.setBigDecimal(2, thanhToanValue);
            pstmt.setString(3, txtGhiChu.getText());
            pstmt.setString(4, phuongThuc);
            pstmt.setInt(5, maBan);
            pstmt.executeUpdate();
            
            System.out.println("Hóa đơn đã cập nhật");
            
            // ────────────────────────────────────────────────────────
            // 2️⃣ ✅ RESET TRẠNG THÁI BÀN VỀ "TRỐNG"
            // ────────────────────────────────────────────────────────
            System.out.println("🪑 Đang reset bàn #" + maBan + "...");
            BanDAO.xoaTatCaMonTrongBan(maBan, conn);
            BanDAO.updateTrangThaiBan(maBan, "Trống", conn);
            System.out.println("Bàn #" + maBan + " → TRỐNG");
            
            // ────────────────────────────────────────────────────────
            // 3️⃣ COMMIT TRANSACTION
            // ────────────────────────────────────────────────────────
            conn.commit();
                                
            daThanhToan = true;
            
            JOptionPane.showMessageDialog(this,
                "✅ Thanh toán " + phuongThuc + " thành công!\n\n" +
                "Tổng tiền: " + lblThanhToan.getText() + "\n" +
                "Bàn " + tenBan + " đã được reset",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            xemTruocHoaDonTamThoi();
            dispose();
            
            System.out.println("═══════════════════════════════════════");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "❌ Lỗi thanh toán: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    
    private void xemTruocHoaDonTamThoi() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
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
            
            String getItems = "SELECT MaMon, SoLuong FROM HoaDonBanHang WHERE MaBan = ?";
            PreparedStatement pstmtGet = conn.prepareStatement(getItems,
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmtGet.setInt(1, maBan);
            ResultSet rsItems = pstmtGet.executeQuery();
            
            String insertCTBH = "INSERT INTO ChiTietBanHang (SoLuong, MaMon) VALUES (?, ?)";
            PreparedStatement pstmtCTBH = conn.prepareStatement(insertCTBH, 
                Statement.RETURN_GENERATED_KEYS);
            
            int maCTBH = -1;
            if (rsItems.next()) {
                pstmtCTBH.setInt(1, rsItems.getInt("SoLuong"));
                pstmtCTBH.setInt(2, rsItems.getInt("MaMon"));
                pstmtCTBH.executeUpdate();
                
                ResultSet rsKey = pstmtCTBH.getGeneratedKeys();
                if (rsKey.next()) {
                    maCTBH = rsKey.getInt(1);
                }
            }
            
            int tongSoMon = 0;
            rsItems.beforeFirst();
            while (rsItems.next()) {
                tongSoMon += rsItems.getInt("SoLuong");
            }
            
            String insertHD = "INSERT INTO HoaDonKhachHang " +
                            "(NgayThanhToan, MaBan, MaCTBH, TongTienThanhToan, " +
                            "PhanTramGiamGia, TongSoLuongMon, LoaiHoaDon, DanhSachMon) " +
                            "VALUES (CURDATE(), ?, ?, ?, ?, ?, 'Tại quán', ?)";
            
            PreparedStatement pstmtHD = conn.prepareStatement(insertHD, 
                Statement.RETURN_GENERATED_KEYS);
            
            pstmtHD.setInt(1, maBan);
            pstmtHD.setInt(2, maCTBH);
            pstmtHD.setBigDecimal(3, thanhToanValue);
            pstmtHD.setBigDecimal(4, giamGia);
            pstmtHD.setInt(5, tongSoMon);
            pstmtHD.setString(6, txtGhiChu.getText());
            pstmtHD.executeUpdate();
            
            ResultSet rsHD = pstmtHD.getGeneratedKeys();
            int maCTHD = -1;
            if (rsHD.next()) {
                maCTHD = rsHD.getInt(1);
            }
            
            String insertCTHD = "INSERT INTO ChiTietHoaDon " +
                              "(MaCTHD, MaMon, SoLuong, GiaTien) " +
                              "VALUES (?, ?, ?, ?)";
            PreparedStatement pstmtDetailHD = conn.prepareStatement(insertCTHD);
            
            String getItemsDetail = "SELECT MaMon, SoLuong, " +
                                  "(SoLuong * (SELECT GiaTien FROM MonAn WHERE MaMon = h.MaMon)) AS GiaTien " +
                                  "FROM HoaDonBanHang h WHERE MaBan = ?";
            PreparedStatement pstmtDetail = conn.prepareStatement(getItemsDetail);
            pstmtDetail.setInt(1, maBan);
            ResultSet rsDetail = pstmtDetail.executeQuery();
            
            while (rsDetail.next()) {
                pstmtDetailHD.setInt(1, maCTHD);
                pstmtDetailHD.setInt(2, rsDetail.getInt("MaMon"));
                pstmtDetailHD.setInt(3, rsDetail.getInt("SoLuong"));
                pstmtDetailHD.setBigDecimal(4, rsDetail.getBigDecimal("GiaTien"));
                pstmtDetailHD.addBatch();
            }
            
            pstmtDetailHD.executeBatch();
            conn.commit();
            
            if (maCTHD > 0) {
                HoaDonPreviewFrame preview = new HoaDonPreviewFrame(parentFrame, maCTHD, "Tại quán");
                preview.setVisible(true);
                
                conn.setAutoCommit(false);
                
                String deleteCTHD = "DELETE FROM ChiTietHoaDon WHERE MaCTHD = ?";
                PreparedStatement pstmtDelCTHD = conn.prepareStatement(deleteCTHD);
                pstmtDelCTHD.setInt(1, maCTHD);
                pstmtDelCTHD.executeUpdate();
                
                String deleteHD = "DELETE FROM HoaDonKhachHang WHERE MaCTHD = ?";
                PreparedStatement pstmtDelHD = conn.prepareStatement(deleteHD);
                pstmtDelHD.setInt(1, maCTHD);
                pstmtDelHD.executeUpdate();
                
                String deleteCTBH = "DELETE FROM ChiTietBanHang WHERE MaCTBH = ?";
                PreparedStatement pstmtDelCTBH = conn.prepareStatement(deleteCTBH);
                pstmtDelCTBH.setInt(1, maCTBH);
                pstmtDelCTBH.executeUpdate();
                
                conn.commit();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "❌ Lỗi xem trước: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void xemTruocHoaDonVoiMa(int maCTHD) {
        HoaDonPreviewFrame preview = new HoaDonPreviewFrame(parentFrame, maCTHD, "Tại quán");
        preview.setVisible(true);
    }
}