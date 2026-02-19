package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.event.MouseListener;
import config.DatabaseConfig;
import model.MonAn;

import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class GoiMonFrame extends JFrame {
    
    private int maBan;
    private String tenBan;
    
    private JTabbedPane tabbedPaneNhom;
    private JTable tableGioHang;
    private DefaultTableModel modelGioHang;
    private JLabel lblTongTien;
    private JTextField txtTimKiem;
    private JButton btnTimKiem;
    private JButton btnXacNhan;
    private JButton btnHuy;
    
    private JButton btnMangVe;
    
  
    private JPanel panelThongTinMon;
    private JLabel lblMaSP;
    private JLabel lblTenSP;
    private JLabel lblDonViTinh;
    private JLabel lblGiaBan;
    private JLabel lblSoLuongTon;
    private JLabel lblMaBan;
    private JLabel lblTenBan;
    
    private List<MonAnGioHang> gioHang = new ArrayList<>();
    private JPanel selectedCard = null;
    
    private boolean daGoiMon = false;
    
    private Map<Integer, JLabel> soLuongLabelsByMaMon = new HashMap<>();
    private Map<Integer, JPanel> monAnCardPanelsByMaMon = new HashMap<>();
    
    private static final String IMAGE_FOLDER = "src/Assets/images/";
    private static final Color SELECTED_BORDER_COLOR = new Color(220, 53, 69);
    private static final Color NORMAL_BORDER_COLOR = new Color(200, 210, 220);
    
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

    private static final Color CARD_HOVER = new Color(52, 152, 219);
    private static final Color CARD_SELECTED = DANGER_RED;
    
    
    public GoiMonFrame(int maBan, String tenBan) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        
        initComponents();
        setupLayout();
        loadNhomVaMonAn();
        
        setTitle("Gọi Món - " + tenBan);
        setSize(1500, 850); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });
    }
    
    private void onWindowClosing() {
        if (!daGoiMon && !gioHang.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn chưa xác nhận gọi món!\nCác món trong giỏ hàng sẽ bị hủy.\nBạn có chắc muốn thoát?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.NO_OPTION) {
                return;
            } else {
                for (MonAnGioHang item : gioHang) {
                    capNhatSoLuongMon(item.getMaMon(), item.getSoLuong());
                }
            }
        }
    }
    
    private void initComponents() {
        // Tabbed Pane hiện đại
        tabbedPaneNhom = new JTabbedPane();
        tabbedPaneNhom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPaneNhom.setForeground(TEXT_DARK);
        tabbedPaneNhom.setBackground(BG_SECONDARY);
        
        // Search field hiện đại
        txtTimKiem = new JTextField(25);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.setForeground(TEXT_DARK);
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (txtTimKiem.getText().trim().isEmpty()) {
                    loadNhomVaMonAn();
                }
            }
        });
        
        // Button tìm kiếm
        btnTimKiem = createModernButton("Tìm Kiếm", PRIMARY_LIGHT, 140, 40);
        btnTimKiem.addActionListener(e -> timKiemMon());
        
        // Table giỏ hàng hiện đại
        String[] columns = {"Tên Món", "Đơn Giá", "SL", "Thành Tiền"};
        modelGioHang = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableGioHang = new JTable(modelGioHang);
        tableGioHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableGioHang.setForeground(TEXT_DARK);
        tableGioHang.setRowHeight(35);
        tableGioHang.setShowGrid(true);
        tableGioHang.setGridColor(new Color(230, 230, 230));
        tableGioHang.setSelectionBackground(new Color(70, 130, 180, 30));
        tableGioHang.setSelectionForeground(TEXT_DARK);
        tableGioHang.setIntercellSpacing(new Dimension(10, 5));
        
        // Header table
        JTableHeader header = tableGioHang.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(PRIMARY_LIGHT);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(0, 35));
        
        // Center align cho số lượng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableGioHang.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        tableGioHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableGioHang.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getButton() == MouseEvent.BUTTON3) {
                    xoaMonKhoiGioHang(row);
                }
            }
        });
        
        // Panel thông tin món
        initThongTinMonPanel();
        
        // Label tổng tiền
        lblTongTien = new JLabel("Tổng Tiền: 0 VND");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongTien.setForeground(DANGER_RED);
        lblTongTien.setHorizontalAlignment(JLabel.CENTER);
        lblTongTien.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DANGER_RED, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        lblTongTien.setOpaque(true);
        lblTongTien.setBackground(new Color(255, 240, 240));
        
        // Buttons
        btnXacNhan = createModernButton("Xác Nhận Gọi Món", SUCCESS_GREEN, 180, 45);
        btnXacNhan.addActionListener(e -> xacNhanGoiMon());
        
        btnMangVe = createModernButton("Tính Tiền Mang Về", ACCENT_ORANGE, 180, 45);
        btnMangVe.addActionListener(e -> xacNhanMangVe());
        
        btnHuy = createModernButton("Hủy", DANGER_RED, 180, 45);
        btnHuy.addActionListener(e -> {
            onWindowClosing();
            if (daGoiMon || gioHang.isEmpty()) {
                dispose();
            }
        });
    }

private JButton createModernButton(String text, Color bgColor, int width, int height) {
    JButton btn = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            super.paintComponent(g);
        }
    };
    
    btn.setBackground(bgColor);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(false);
    btn.setPreferredSize(new Dimension(width, height));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    btn.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            btn.setBackground(bgColor.darker());
        }
        @Override
        public void mouseExited(MouseEvent e) {
            btn.setBackground(bgColor);
        }
    });
    
    return btn;
}

private void initThongTinMonPanel() {
    panelThongTinMon = new JPanel();
    panelThongTinMon.setLayout(new GridLayout(7, 2, 12, 8));
    panelThongTinMon.setBackground(BG_SECONDARY);
    panelThongTinMon.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(PRIMARY_LIGHT, 2),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));
    
    Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
    Font valueFont = new Font("Segoe UI", Font.PLAIN, 12);
    
    // Mã SP
    panelThongTinMon.add(createInfoLabel("Mã SP:", labelFont));
    lblMaSP = createValueLabel("---", valueFont, TEXT_DARK);
    panelThongTinMon.add(lblMaSP);
    
    // Tên SP
    panelThongTinMon.add(createInfoLabel("Tên sản phẩm:", labelFont));
    lblTenSP = createValueLabel("---", valueFont, TEXT_DARK);
    panelThongTinMon.add(lblTenSP);
    
    // Đơn vị
    panelThongTinMon.add(createInfoLabel("Đơn vị tính:", labelFont));
    lblDonViTinh = createValueLabel("---", valueFont, TEXT_DARK);
    panelThongTinMon.add(lblDonViTinh);
    
    // Giá
    panelThongTinMon.add(createInfoLabel("Giá bán:", labelFont));
    lblGiaBan = createValueLabel("---", valueFont, DANGER_RED);
    lblGiaBan.setFont(new Font("Segoe UI", Font.BOLD, 13));
    panelThongTinMon.add(lblGiaBan);
    
    // Số lượng
    panelThongTinMon.add(createInfoLabel("Kho còn:", labelFont));
    lblSoLuongTon = createValueLabel("---", valueFont, SUCCESS_GREEN);
    panelThongTinMon.add(lblSoLuongTon);
    
    // Mã bàn
    panelThongTinMon.add(createInfoLabel("Mã bàn:", labelFont));
    lblMaBan = createValueLabel(String.valueOf(maBan), valueFont, TEXT_DARK);
    panelThongTinMon.add(lblMaBan);
    
    // Tên bàn
    panelThongTinMon.add(createInfoLabel("Tên bàn:", labelFont));
    lblTenBan = createValueLabel(tenBan, valueFont, PRIMARY_LIGHT);
    lblTenBan.setFont(new Font("Segoe UI", Font.BOLD, 12));
    panelThongTinMon.add(lblTenBan);
}
    
//Helper methods
private JLabel createInfoLabel(String text, Font font) {
 JLabel label = new JLabel(text);
 label.setFont(font);
 label.setForeground(TEXT_DARK);
 return label;
}

private JLabel createValueLabel(String text, Font font, Color color) {
 JLabel label = new JLabel(text);
 label.setFont(font);
 label.setForeground(color);
 return label;
}
    // ✅ CẬP NHẬT THÔNG TIN MÓN ĂN
    private void capNhatThongTinMon(MonAn monAn) {
        if (monAn != null) {
            lblMaSP.setText(String.valueOf(monAn.getMaMon()));
            lblTenSP.setText(monAn.getTenMon());
            lblDonViTinh.setText(monAn.getDonViTinh());
            lblGiaBan.setText(String.format("%,d", monAn.getGiaTien().longValue()));
            lblSoLuongTon.setText(String.valueOf(monAn.getSoLuongConLai()));
        } else {
            lblMaSP.setText("---");
            lblTenSP.setText("---");
            lblDonViTinh.setText("---");
            lblGiaBan.setText("---");
            lblSoLuongTon.setText("---");
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_MAIN);
        
        // ========== HEADER PANEL ==========
        JPanel headerPanel = createHeaderPanel();
        
        // ========== TOP SEARCH PANEL ==========
        JPanel topPanel = createSearchPanel();
        
        // ========== LEFT PANEL - Danh sách món ==========
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(BG_SECONDARY);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblTitle = new JLabel("DANH SÁCH MÓN ĂN", JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        leftPanel.add(lblTitle, BorderLayout.NORTH);
        leftPanel.add(tabbedPaneNhom, BorderLayout.CENTER);
        
        // ========== RIGHT PANEL - Giỏ hàng ==========
        JPanel rightPanel = createRightPanel();
        
        // ========== SPLIT PANE ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(980);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        
        // ========== BODY PANEL ==========
        JPanel bodyPanel = new JPanel(new BorderLayout(10, 10));
        bodyPanel.setBackground(BG_MAIN);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bodyPanel.add(topPanel, BorderLayout.NORTH);
        bodyPanel.add(splitPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), 0, PRIMARY_LIGHT
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(0, 70));
        
        JLabel lblTitle = new JLabel("GỌI MÓN - " + tenBan);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.WEST);
        
        return panel;
    }

    // ============================================
    // PART 7: METHOD createSearchPanel()
    // Thêm method mới
    // ============================================

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblIcon = new JLabel("🔍");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        
        JLabel lblSearch = new JLabel("Tìm Kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(TEXT_DARK);
        
        panel.add(lblIcon);
        panel.add(lblSearch);
        panel.add(txtTimKiem);
        panel.add(btnTimKiem);
        
        return panel;
    }

    // ============================================
    // PART 8: METHOD createRightPanel()
    // Thêm method mới
    // ============================================

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(450, 0));

        // ───────────────────────────────────────────────
        // Phần trên: Tiêu đề + Thông tin chi tiết món
        // ───────────────────────────────────────────────
        JPanel topRightPanel = new JPanel(new BorderLayout(0, 10));
        topRightPanel.setBackground(BG_SECONDARY);

        JLabel lblInfoTitle = new JLabel("THÔNG TIN CHI TIẾT MÓN ĐÃ GỌI", JLabel.LEFT);
        lblInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfoTitle.setForeground(TEXT_DARK);
        lblInfoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        topRightPanel.add(lblInfoTitle, BorderLayout.NORTH);
        topRightPanel.add(panelThongTinMon, BorderLayout.CENTER);

        // ───────────────────────────────────────────────
        // Phần giữa: Tiêu đề + Bảng giỏ hàng + Tổng tiền
        // ───────────────────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(BG_SECONDARY);

        JLabel lblCartTitle = new JLabel("GIỎ HÀNG", JLabel.LEFT);
        lblCartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCartTitle.setForeground(TEXT_DARK);
        lblCartTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 8, 0));

        JScrollPane scrollGioHang = new JScrollPane(tableGioHang);
        scrollGioHang.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollGioHang.setBackground(Color.WHITE);
        scrollGioHang.getViewport().setBackground(Color.WHITE);

        centerPanel.add(lblCartTitle, BorderLayout.NORTH);
        centerPanel.add(scrollGioHang, BorderLayout.CENTER);
        centerPanel.add(lblTongTien, BorderLayout.SOUTH);

        // ───────────────────────────────────────────────
        // JSplitPane dọc - chia thông tin món (trên) và giỏ hàng (dưới)
        // ───────────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topRightPanel);
        splitPane.setBottomComponent(centerPanel);

        // Vị trí thanh kéo ban đầu - để phần thông tin món hiển thị đầy đủ
        splitPane.setDividerLocation(260);          // ← điều chỉnh 240–300 tùy màn hình

        // Thanh kéo to, dễ kéo
        splitPane.setDividerSize(10);

        // Kéo mượt, realtime
        splitPane.setContinuousLayout(true);

        // Có nút mũi tên thu gọn/mở rộng (rất tiện)
        splitPane.setOneTouchExpandable(true);

        // Cho phép phần dưới (giỏ hàng) co lại khi kéo lên
        splitPane.setResizeWeight(0.35);            // 0.3–0.45 thường tự nhiên nhất

        // Minimum size để không bị kẹt hoàn toàn
        topRightPanel.setMinimumSize(new Dimension(200, 180));
        centerPanel.setMinimumSize(new Dimension(200, 120));

        // Tùy chỉnh giao diện thanh kéo (giống phong cách hiện đại, có dấu chấm)
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int w = getWidth();
                        int h = getHeight();

                        // Gradient nền thanh kéo
                        GradientPaint gp = new GradientPaint(
                            0, 0, new Color(210, 218, 225),
                            0, h, new Color(170, 180, 195)
                        );
                        g2.setPaint(gp);
                        g2.fillRect(0, 0, w, h);

                        // Viền sáng/tối
                        g2.setColor(new Color(255, 255, 255, 130));
                        g2.drawLine(0, 0, w, 0);
                        g2.setColor(new Color(0, 0, 0, 55));
                        g2.drawLine(0, h - 1, w, h - 1);

                        // Dấu chấm kéo (drag handle) rõ ràng
                        g2.setColor(new Color(110, 125, 140));
                        int cy = h / 2;
                        int dotW = 18;
                        int dotH = 5;
                        int gap = 7;

                        for (int i = -3; i <= 3; i++) {
                            int y = cy + i * (dotH + gap) - dotH / 2;
                            if (y < 12 || y > h - 28) continue;
                            g2.fillRoundRect((w - dotW) / 2, y, dotW, dotH, 3, 3);
                        }

                        // Highlight nhẹ
                        g2.setColor(new Color(255, 255, 255, 90));
                        for (int i = -3; i <= 3; i++) {
                            int y = cy + i * (dotH + gap) - dotH / 2;
                            if (y < 12 || y > h - 28) continue;
                            g2.fillRoundRect((w - dotW) / 2 + 1, y + 1, dotW - 2, 3, 2, 2);
                        }

                        g2.dispose();
                    }

                    
                };
            }
        });

        // ───────────────────────────────────────────────
        // Nút hành động dưới cùng
        // ───────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttonPanel.setBackground(BG_SECONDARY);
        buttonPanel.add(btnXacNhan);
        buttonPanel.add(btnMangVe);
        buttonPanel.add(btnHuy);

        // Tổng hợp layout
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void loadNhomVaMonAn() {
        tabbedPaneNhom.removeAll();
        
        String queryNhom = "SELECT MaNhom, TenNhom FROM Nhom ORDER BY TenNhom";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryNhom)) {
            
            while (rs.next()) {
                int maNhom = rs.getInt("MaNhom");
                String tenNhom = rs.getString("TenNhom");
                
                JPanel nhomPanel = createNhomPanel(maNhom);
                tabbedPaneNhom.addTab(tenNhom, nhomPanel);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách nhóm: " + e.getMessage());
        }
    }
    
    private JPanel createNhomPanel(int maNhom) {
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        gridPanel.setBackground(BG_MAIN); // ✅ Thay đổi từ new Color(243, 244, 246)

        String query = "SELECT MaMon, TenMon, GiaTien, DonViTinh, HinhAnh, SoLuongConLai " +
                       "FROM MonAn WHERE MaNhom = ? ORDER BY TenMon";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, maNhom);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));

                JPanel monPanel = createMonAnPanel(monAn);
                gridPanel.add(monPanel);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải món ăn: " + e.getMessage());
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 15));
        contentPanel.setBackground(BG_MAIN); 
        contentPanel.add(gridPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_MAIN); 

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        wrapperPanel.setBackground(BG_MAIN); 
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        return wrapperPanel;
    }
    
    private JPanel createMonAnPanel(MonAn monAn) {
        // Card wrapper với shadow và bo góc
        JPanel wrapperPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Multi-layer shadow effect
                int shadowOffset = 4;
                for (int i = shadowOffset; i > 0; i--) {
                    int alpha = 8 + (i * 3);
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 16, 16);
                }
            }
        };
        wrapperPanel.setLayout(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.setPreferredSize(new Dimension(210, 270));
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        // Main card panel với bo góc
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Bo góc nền trắng
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setOpaque(false);
        mainPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ✅ Image panel với bo góc trên
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Bo góc chỉ ở trên
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 16, 16);
            }
        };
        imagePanel.setBackground(new Color(248, 249, 250));
        imagePanel.setOpaque(false);
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(202, 135));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(JLabel.CENTER);
        lblImage.setVerticalAlignment(JLabel.CENTER);
        lblImage.setText("⏳");
        lblImage.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblImage.setForeground(new Color(173, 181, 189));

        if (monAn.getHinhAnh() != null && !monAn.getHinhAnh().isEmpty()) {
            String imagePath = IMAGE_FOLDER + monAn.getHinhAnh();
            File imgFile = new File(imagePath);

            if (imgFile.exists()) {
                new SwingWorker<Icon, Void>() {
                    @Override
                    protected Icon doInBackground() throws Exception {
                        BufferedImage img = ImageIO.read(imgFile);
                        // Bo góc cho ảnh
                        BufferedImage rounded = new BufferedImage(186, 127, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = rounded.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, 186, 127, 12, 12));
                        g2d.drawImage(img.getScaledInstance(186, 127, Image.SCALE_SMOOTH), 0, 0, null);
                        g2d.dispose();
                        return new ImageIcon(rounded);
                    }

                    @Override
                    protected void done() {
                        try {
                            lblImage.setIcon(get());
                            lblImage.setText(null);
                        } catch (Exception e) {
                            lblImage.setText("🖼️");
                            lblImage.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
                        }
                    }
                }.execute();
            } else {
                lblImage.setText("🖼️");
                lblImage.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            }
        } else {
            lblImage.setText("🖼️");
            lblImage.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        }
        
        imagePanel.add(lblImage, BorderLayout.CENTER);

        // ✅ Info panel với padding đẹp
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 14, 12));

        // Tên món
        JLabel lblTen = new JLabel(monAn.getTenMon());
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTen.setForeground(TEXT_DARK);
        lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Giá tiền với viền nhẹ
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pricePanel.setOpaque(false);
        
        JLabel lblGia = new JLabel(String.format("%,d đ", monAn.getGiaTien().longValue()));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblGia.setForeground(DANGER_RED);
        pricePanel.add(lblGia);

        // Số lượng với badge style
        String soLuongText = monAn.getSoLuongConLai() > 0 ?
            "Còn " + monAn.getSoLuongConLai() + " " + monAn.getDonViTinh() :
            "Hết hàng";
        Color soLuongBg = monAn.getSoLuongConLai() > 0 ? 
            new Color(212, 237, 218) : new Color(248, 215, 218);
        Color soLuongColor = monAn.getSoLuongConLai() > 0 ? 
            new Color(21, 87, 36) : new Color(114, 28, 36);

        JLabel lblSoLuong = new JLabel(soLuongText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(soLuongBg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        lblSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSoLuong.setForeground(soLuongColor);
        lblSoLuong.setOpaque(false);
        lblSoLuong.setHorizontalAlignment(JLabel.CENTER);
        lblSoLuong.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        lblSoLuong.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        soLuongLabelsByMaMon.put(monAn.getMaMon(), lblSoLuong);
        monAnCardPanelsByMaMon.put(monAn.getMaMon(), wrapperPanel);

        infoPanel.add(lblTen);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(pricePanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(lblSoLuong);

        mainPanel.add(imagePanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        wrapperPanel.add(mainPanel, BorderLayout.CENTER);

        if (monAn.getSoLuongConLai() > 0) {
            wrapperPanel.addMouseListener(new MouseAdapter() {
                private Color originalBorder = BORDER_COLOR;
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectCard(wrapperPanel, monAn);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (wrapperPanel != selectedCard) {
                        mainPanel.setBackground(new Color(240, 248, 255));
                        wrapperPanel.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (wrapperPanel != selectedCard) {
                        mainPanel.setBackground(Color.WHITE);
                        wrapperPanel.repaint();
                    }
                }
            });
        } else {
            wrapperPanel.setEnabled(false);
            wrapperPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mainPanel.setBackground(new Color(248, 249, 250));

            for (Component comp : mainPanel.getComponents()) {
                setComponentOpacity(comp, 0.4f);
            }
        }

        return wrapperPanel;
    }
    
    private void selectCard(JPanel cardPanel, MonAn monAn) {
        if (selectedCard != null) {
            // Reset card cũ
            Component mainPanel = selectedCard.getComponent(0);
            if (mainPanel instanceof JPanel) {
                ((JPanel) mainPanel).setBackground(Color.WHITE);
            }
            selectedCard.repaint();
        }
        
        selectedCard = cardPanel;
        
        // Highlight card mới
        Component mainPanel = cardPanel.getComponent(0);
        if (mainPanel instanceof JPanel) {
            ((JPanel) mainPanel).setBackground(new Color(255, 240, 245));
        }
        cardPanel.repaint();
        
        capNhatThongTinMon(monAn);
        themVaoGioHang(monAn);
    }
    
    private void setComponentOpacity(Component comp, float alpha) {
        if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            for (Component child : panel.getComponents()) {
                setComponentOpacity(child, alpha);
            }
        } else if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            Color c = label.getForeground();
            label.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255 * alpha)));
        }
    }
    
    private void capNhatSoLuongMon(int maMon, int thayDoi) {
        if (thayDoi == 0) return;

        String query = "UPDATE MonAn SET SoLuongConLai = SoLuongConLai + ? WHERE MaMon = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(true);

            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, thayDoi);
            pstmt.setInt(2, maMon);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                System.err.println("Không tìm thấy món ăn với MaMon = " + maMon);
                return;
            }

            int soLuongConLai = getSoLuongConLai(maMon);
            int soLuongMoi = (soLuongConLai < 0) ? 0 : soLuongConLai;

            JLabel lblSoLuong = soLuongLabelsByMaMon.get(maMon);
            JPanel cardPanel = monAnCardPanelsByMaMon.get(maMon);

            if (lblSoLuong != null) {
                String text;
                Color color;

                if (soLuongMoi > 0) {
                    text = "Còn: " + soLuongMoi + " " + getDonViTinh(maMon);
                    color = new Color(40, 167, 69);
                } else {
                    text = "Hết hàng";
                    color = new Color(220, 53, 69);
                }

                SwingUtilities.invokeLater(() -> {
                    lblSoLuong.setText(text);
                    lblSoLuong.setForeground(color);

                    if (soLuongMoi <= 0 && cardPanel != null) {
                        cardPanel.setEnabled(false);
                        cardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                        MouseListener[] listeners = cardPanel.getMouseListeners();
                        for (MouseListener l : listeners) {
                            cardPanel.removeMouseListener(l);
                        }

                        setComponentOpacity(cardPanel, 0.55f);
                    }
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this,
                    "Lỗi cập nhật số lượng món:\n" + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE)
            );
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }
    
    private int getSoLuongConLai(int maMon) {
        String sql = "SELECT SoLuongConLai FROM MonAn WHERE MaMon = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getDonViTinh(int maMon) {
        String sql = "SELECT DonViTinh FROM MonAn WHERE MaMon = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ignored) {}
        return "";
    }
    
    private void themVaoGioHang(MonAn monAn) {
        String queryCheck = "SELECT SoLuongConLai FROM MonAn WHERE MaMon = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryCheck)) {
            
            pstmt.setInt(1, monAn.getMaMon());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int soLuongConLai = rs.getInt("SoLuongConLai");
                
                if (soLuongConLai <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Món \"" + monAn.getTenMon() + "\" đã hết!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                boolean found = false;
                for (MonAnGioHang item : gioHang) {
                    if (item.getMaMon() == monAn.getMaMon()) {
                        item.setSoLuong(item.getSoLuong() + 1);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    MonAnGioHang newItem = new MonAnGioHang();
                    newItem.setMaMon(monAn.getMaMon());
                    newItem.setTenMon(monAn.getTenMon());
                    newItem.setGiaTien(monAn.getGiaTien());
                    newItem.setSoLuong(1);
                    gioHang.add(newItem);
                }
                
                capNhatSoLuongMon(monAn.getMaMon(), -1);
                updateGioHangTable();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi kiểm tra số lượng món: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateGioHangTable() {
        modelGioHang.setRowCount(0);
        BigDecimal tongTien = BigDecimal.ZERO;
        
        for (MonAnGioHang item : gioHang) {
            BigDecimal thanhTien = item.getGiaTien().multiply(new BigDecimal(item.getSoLuong()));
            tongTien = tongTien.add(thanhTien);
            
            Object[] row = {
                item.getTenMon(),
                String.format("%,d VND", item.getGiaTien().longValue()),
                item.getSoLuong(),
                String.format("%,d VND", thanhTien.longValue())
            };
            modelGioHang.addRow(row);
        }
        
        lblTongTien.setText(String.format("Tổng Tiền: %,d VND", tongTien.longValue()));
    }
    
    private void xoaMonKhoiGioHang(int row) {
        if (row < 0 || row >= gioHang.size()) {
            return;
        }
        
        MonAnGioHang item = gioHang.get(row);
        
        if (item.getSoLuong() == 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa \"" + item.getTenMon() + "\" khỏi giỏ hàng?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                capNhatSoLuongMon(item.getMaMon(), item.getSoLuong());
                gioHang.remove(row);
                updateGioHangTable();
              
                capNhatThongTinMon(null);
            }
        } else {
            String input = JOptionPane.showInputDialog(this,
                "Nhập số lượng cần xóa (Tối đa: " + item.getSoLuong() + "):",
                "Xóa món",
                JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int soLuongXoa = Integer.parseInt(input.trim());
                    
                    if (soLuongXoa <= 0) {
                        JOptionPane.showMessageDialog(this,
                            "Số lượng phải lớn hơn 0!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (soLuongXoa > item.getSoLuong()) {
                        JOptionPane.showMessageDialog(this,
                            "Số lượng xóa không được vượt quá " + item.getSoLuong() + "!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    capNhatSoLuongMon(item.getMaMon(), soLuongXoa);
                    
                    if (soLuongXoa == item.getSoLuong()) {
                        gioHang.remove(row);
                       
                        capNhatThongTinMon(null);
                    } else {
                        item.setSoLuong(item.getSoLuong() - soLuongXoa);
                    }
                    
                    updateGioHangTable();
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập số hợp lệ!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void timKiemMon() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }
        
        tabbedPaneNhom.removeAll();
        selectedCard = null;
        
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        resultPanel.setBackground(new Color(243, 244, 246));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        String query = "SELECT MaMon, TenMon, GiaTien, DonViTinh, HinhAnh, SoLuongConLai " +
                      "FROM MonAn WHERE TenMon LIKE ? ORDER BY TenMon";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));
                
                JPanel monPanel = createMonAnPanel(monAn);
                resultPanel.add(monPanel);
                count++;
            }
            
            if (count == 0) {
                JLabel lblNoResult = new JLabel("Không tìm thấy món ăn!");
                lblNoResult.setFont(new Font("Arial", Font.ITALIC, 14));
                lblNoResult.setForeground(Color.RED);
                resultPanel.add(lblNoResult);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tìm kiếm: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        
        tabbedPaneNhom.addTab("Kết quả tìm kiếm", scrollPane);
        tabbedPaneNhom.setSelectedIndex(0);
    }
    
    private void xacNhanGoiMon() {
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận gọi món cho " + tenBan + "?",
            "Xác Nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                
                String checkQuery = "SELECT SoLuong FROM HoaDonBanHang WHERE MaBan = ? AND MaMon = ?";
                String updateQuery = "UPDATE HoaDonBanHang SET SoLuong = SoLuong + ? WHERE MaBan = ? AND MaMon = ?";
                String insertQuery = "INSERT INTO HoaDonBanHang (NgayVao, MaBan, MaMon, SoLuong, MaNV) VALUES (CURDATE(), ?, ?, ?, 1)";
                
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                
                for (MonAnGioHang item : gioHang) {
                    checkStmt.setInt(1, maBan);
                    checkStmt.setInt(2, item.getMaMon());
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        updateStmt.setInt(1, item.getSoLuong());
                        updateStmt.setInt(2, maBan);
                        updateStmt.setInt(3, item.getMaMon());
                        updateStmt.addBatch();
                    } else {
                        insertStmt.setInt(1, maBan);
                        insertStmt.setInt(2, item.getMaMon());
                        insertStmt.setInt(3, item.getSoLuong());
                        insertStmt.addBatch();
                    }
                }
                
                updateStmt.executeBatch();
                insertStmt.executeBatch();
                
                String updateBan = "UPDATE Ban SET TrangThai = 'Đang sử dụng' WHERE MaBan = ?";
                PreparedStatement pstmtBan = conn.prepareStatement(updateBan);
                pstmtBan.setInt(1, maBan);
                pstmtBan.executeUpdate();
                
                conn.commit();
                daGoiMon = true;
                
                JOptionPane.showMessageDialog(this, "Gọi món thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi gọi món: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
  
    private void xacNhanMangVe() {
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Tạo ChiTietBanHang
            String insertCTBH = "INSERT INTO ChiTietBanHang (SoLuong, MaMon) VALUES (?, ?)";
            PreparedStatement pstmtCTBH = conn.prepareStatement(insertCTBH, 
                Statement.RETURN_GENERATED_KEYS);
            
            int maCTBH = -1;
            if (!gioHang.isEmpty()) {
                MonAnGioHang firstItem = gioHang.get(0);
                pstmtCTBH.setInt(1, firstItem.getSoLuong());
                pstmtCTBH.setInt(2, firstItem.getMaMon());
                pstmtCTBH.executeUpdate();
                
                ResultSet rsKey = pstmtCTBH.getGeneratedKeys();
                if (rsKey.next()) {
                    maCTBH = rsKey.getInt(1);
                }
            }
            
            // 2. Tính tổng tiền
            BigDecimal tongTien = BigDecimal.ZERO;
            int tongSoLuong = 0;
            
            for (MonAnGioHang item : gioHang) {
                BigDecimal thanhTien = item.getGiaTien().multiply(new BigDecimal(item.getSoLuong()));
                tongTien = tongTien.add(thanhTien);
                tongSoLuong += item.getSoLuong();
            }
            
            // 3. Tạo HoaDonKhachHang (MaBan = NULL cho mang về)
            String insertHD = "INSERT INTO HoaDonKhachHang " +
                            "(NgayThanhToan, MaBan, MaCTBH, TongTienThanhToan, " +
                            "PhanTramGiamGia, TongSoLuongMon, LoaiHoaDon) " +
                            "VALUES (CURDATE(), NULL, ?, ?, 0, ?, 'Mang về')";
            
            PreparedStatement pstmtHD = conn.prepareStatement(insertHD, 
                Statement.RETURN_GENERATED_KEYS);
            
            pstmtHD.setInt(1, maCTBH);
            pstmtHD.setBigDecimal(2, tongTien);
            pstmtHD.setInt(3, tongSoLuong);
            pstmtHD.executeUpdate();
            
            ResultSet rsHD = pstmtHD.getGeneratedKeys();
            int maCTHD = -1;
            if (rsHD.next()) {
                maCTHD = rsHD.getInt(1);
            }
            
            // 4. Tạo ChiTietHoaDon
            String insertCTHD = "INSERT INTO ChiTietHoaDon " +
                              "(MaCTHD, MaMon, SoLuong, GiaTien) " +
                              "VALUES (?, ?, ?, ?)";
            PreparedStatement pstmtDetailHD = conn.prepareStatement(insertCTHD);
            
            for (MonAnGioHang item : gioHang) {
                pstmtDetailHD.setInt(1, maCTHD);
                pstmtDetailHD.setInt(2, item.getMaMon());
                pstmtDetailHD.setInt(3, item.getSoLuong());
                pstmtDetailHD.setBigDecimal(4, item.getGiaTien());
                pstmtDetailHD.addBatch();
            }
            
            pstmtDetailHD.executeBatch();
            conn.commit();
            
         
            
            // 5. Mở form thanh toán mang về
            ThanhToanMangVeFrame formThanhToan = new ThanhToanMangVeFrame(
                this,  // ✅ TRUYỀN GoiMonFrame (this) THAY VÌ getWindowAncestor
                maCTHD,
                tongTien
            );
            formThanhToan.setVisible(true);
            
          
            if (formThanhToan.isDaThanhToan()) {
                daGoiMon = true;
                dispose();  // Chỉ đóng GoiMonFrame khi đã thanh toán thành công
            }
          
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tạo đơn mang về: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private class MonAnGioHang {
        private int maMon;
        private String tenMon;
        private BigDecimal giaTien;
        private int soLuong;
        
        public int getMaMon() { return maMon; }
        public void setMaMon(int maMon) { this.maMon = maMon; }
        
        public String getTenMon() { return tenMon; }
        public void setTenMon(String tenMon) { this.tenMon = tenMon; }
        
        public BigDecimal getGiaTien() { return giaTien; }
        public void setGiaTien(BigDecimal giaTien) { this.giaTien = giaTien; }
        
        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    }
}