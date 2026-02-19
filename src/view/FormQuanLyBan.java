package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import config.DatabaseConfig;
import model.KhuVucQuan;

public class FormQuanLyBan extends JFrame {

    private JTextField txtTenBan;
    private JTextField txtTimKiem;
    private JComboBox<KhuVucQuan> cboKhuVuc;
    private JComboBox<String> cboTrangThai;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JButton btnTimKiem;
    private JTable tableBan;
    private DefaultTableModel tableModel;
    private int selectedMaBan = -1;
    
    // ============ COLOR PALETTE (từ FormDangNhap) ============
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
    
    public FormQuanLyBan() {
        initComponents();
        loadKhuVuc();
        setupLayout();
        loadBanData();
        
        setTitle("Quản Lý Bàn");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        txtTenBan = createModernTextField("Nhập tên bàn...");
        txtTimKiem = createModernTextField("Tìm kiếm bàn...");
        
        cboKhuVuc = createModernComboBox();
        cboTrangThai = createModernComboBox();
        cboTrangThai.addItem("Trống");
        cboTrangThai.addItem("Đang sử dụng");
        
        btnThem = createModernButton("Thêm Bàn", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Bàn", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Bàn", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiem = createModernButton("Tìm Kiếm", PRIMARY_LIGHT, true);
        
        btnThem.addActionListener(e -> themBan());
        btnSua.addActionListener(e -> suaBan());
        btnXoa.addActionListener(e -> xoaBan());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiem.addActionListener(e -> timKiemBan());
        
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemBan();
                }
            }
        });
        
        String[] columns = {"Mã Bàn", "Tên Bàn", "Khu Vực", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableBan = new JTable(tableModel);
        setupTable();
    }
    
    // ==================== CREATE MODERN TEXTFIELD (giống FormDangNhap) ====================
    private JTextField createModernTextField(String placeholder) {
        JTextField textField = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int height = getHeight();
                int cornerRadius = 8;

                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                // Border
                if (hasFocus()) {
                    g2d.setColor(PRIMARY_LIGHT);
                    g2d.setStroke(new BasicStroke(2.5f));
                } else {
                    g2d.setColor(BORDER_COLOR);
                    g2d.setStroke(new BasicStroke(1.5f));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                super.paintComponent(g);
            }
        };

        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setForeground(TEXT_DARK);
        textField.setBackground(new Color(248, 250, 252));
        textField.setCaretColor(PRIMARY_LIGHT);
        textField.setBorder(new EmptyBorder(12, 16, 12, 16));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        textField.setPreferredSize(new Dimension(0, 45));
        textField.setOpaque(true);

        // Placeholder effect
        textField.addFocusListener(new FocusAdapter() {
            private String placeholderText = placeholder;

            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholderText)) {
                    textField.setText("");
                    textField.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholderText);
                    textField.setForeground(new Color(180, 180, 180));
                }
            }
        });

        return textField;
    }
    
    // ==================== CREATE MODERN COMBOBOX ====================
    @SuppressWarnings("unchecked")
    private <T> JComboBox<T> createModernComboBox() {
        JComboBox<T> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(new Color(248, 250, 252));
        comboBox.setForeground(TEXT_DARK);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        comboBox.setPreferredSize(new Dimension(0, 45));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return comboBox;
    }
    
    // ==================== CREATE MODERN BUTTON (giống FormDangNhap) ====================
    private JButton createModernButton(String text, Color bgColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentColor = bgColor;
                
                if (getModel().isPressed()) {
                    currentColor = bgColor.darker().darker();
                } else if (getModel().isRollover()) {
                    currentColor = bgColor.brighter();
                }

                // Shadow effect
                if (isPrimary) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 2, 8, 8);
                }

                // Button background
                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Highlight effect
                if (isPrimary) {
                    g2d.setColor(new Color(255, 255, 255, 40));
                    g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() / 2, 8, 8);
                }

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 45));
        
        return btn;
    }
    
    private void setupTable() {
        tableBan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableBan.setRowHeight(40);
        tableBan.setSelectionBackground(PRIMARY_LIGHT);
        tableBan.setSelectionForeground(Color.WHITE);
        tableBan.setGridColor(new Color(220, 220, 220));
        tableBan.setShowGrid(true);
        tableBan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tableBan.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        tableBan.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableBan.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableBan.getColumnModel().getColumn(2).setPreferredWidth(200);
        tableBan.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        tableBan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiThongTinBan();
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_MAIN);
        
        JPanel headerPanel = createHeaderPanel();
        
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(BG_MAIN);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel topPanel = createTopPanel();
        JPanel bottomPanel = createTablePanel();
        
        mainContainer.add(topPanel, BorderLayout.NORTH);
        mainContainer.add(bottomPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), new Color(45, 85, 145)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("QUẢN LÝ BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        panel.add(lblTitle, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JPanel searchPanel = createSearchPanel();
        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel lblSearch = new JLabel("Tìm kiếm bàn:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(PRIMARY_DARK);
        lblSearch.setPreferredSize(new Dimension(180, 45));
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(15, 0));
        searchInputPanel.setBackground(BG_SECONDARY);
        searchInputPanel.add(txtTimKiem, BorderLayout.CENTER);
        searchInputPanel.add(btnTimKiem, BorderLayout.EAST);
        
        panel.add(lblSearch, BorderLayout.WEST);
        panel.add(searchInputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        panel.add(createFormField("Tên Bàn *", txtTenBan));
        panel.add(createFormField("Khu Vực *", cboKhuVuc));
        panel.add(createFormField("Trạng Thái *", cboTrangThai));
        
        return panel;
    }
    
    private JPanel createFormField(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setBackground(BG_SECONDARY);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLabel.setForeground(PRIMARY_DARK);
        
        panel.add(lblLabel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);
        panel.add(btnLamMoi);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel lblTitle = new JLabel("DANH SÁCH BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tableBan);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadKhuVuc() {
        String query = "SELECT MaKV, TenKV FROM KhuVucQuan ORDER BY TenKV";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            cboKhuVuc.removeAllItems();
            
            while (rs.next()) {
                KhuVucQuan kv = new KhuVucQuan();
                kv.setMaKV(rs.getInt("MaKV"));
                kv.setTenKV(rs.getString("TenKV"));
                cboKhuVuc.addItem(kv);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách khu vực: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadBanData() {
        String query = "SELECT b.MaBan, b.TenBan, k.TenKV, b.TrangThai " +
                      "FROM Ban b " +
                      "LEFT JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "ORDER BY b.MaBan DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaBan"),
                    rs.getString("TenBan"),
                    rs.getString("TenKV"),
                    rs.getString("TrangThai")
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void timKiemBan() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty() || keyword.equals("Tìm kiếm bàn...")) {
            loadBanData();
            return;
        }
        
        String query = "SELECT b.MaBan, b.TenBan, k.TenKV, b.TrangThai " +
                      "FROM Ban b " +
                      "LEFT JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "WHERE b.TenBan LIKE ? OR k.TenKV LIKE ? " +
                      "ORDER BY b.MaBan DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaBan"),
                    rs.getString("TenBan"),
                    rs.getString("TenKV"),
                    rs.getString("TrangThai")
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy bàn nào!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tìm kiếm: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void themBan() {
        if (!validateInput()) {
            return;
        }
        
        // Trạng thái bàn khi thêm mới luôn là "Trống"
        String query = "INSERT INTO Ban (TenBan, TrangThai, MaKV) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenBan.getText().trim());
            pstmt.setString(2, "Trống"); // Cố định trạng thái là "Trống"
            
            KhuVucQuan selectedKV = (KhuVucQuan) cboKhuVuc.getSelectedItem();
            pstmt.setInt(3, selectedKV.getMaKV());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Thêm bàn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadBanData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi thêm bàn: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void suaBan() {
        if (selectedMaBan <= 0) {
            showError("Vui lòng chọn bàn cần sửa từ bảng!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        String query = "UPDATE Ban SET TenBan = ?, TrangThai = ?, MaKV = ? WHERE MaBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenBan.getText().trim());
            pstmt.setString(2, (String) cboTrangThai.getSelectedItem());
            
            KhuVucQuan selectedKV = (KhuVucQuan) cboKhuVuc.getSelectedItem();
            pstmt.setInt(3, selectedKV.getMaKV());
            pstmt.setInt(4, selectedMaBan);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Cập nhật bàn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadBanData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật bàn: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void xoaBan() {
        if (selectedMaBan <= 0) {
            showError("Vui lòng chọn bàn cần xóa từ bảng!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa bàn này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM Ban WHERE MaBan = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaBan);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "✓ Xóa bàn thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
                    clearForm();
                    loadBanData();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa bàn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void hienThiThongTinBan() {
        int selectedRow = tableBan.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMaBan = (int) tableModel.getValueAt(selectedRow, 0);
            
            String query = "SELECT b.TenBan, b.TrangThai, b.MaKV " +
                          "FROM Ban b WHERE b.MaBan = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaBan);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    txtTenBan.setText(rs.getString("TenBan"));
                    txtTenBan.setForeground(TEXT_DARK);
                    cboTrangThai.setSelectedItem(rs.getString("TrangThai"));
                    
                    int maKV = rs.getInt("MaKV");
                    for (int i = 0; i < cboKhuVuc.getItemCount(); i++) {
                        if (((KhuVucQuan) cboKhuVuc.getItemAt(i)).getMaKV() == maKV) {
                            cboKhuVuc.setSelectedIndex(i);
                            break;
                        }
                    }
                    
                    btnThem.setEnabled(false);
                    btnSua.setEnabled(true);
                    btnXoa.setEnabled(true);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải thông tin bàn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateInput() {
        String tenBan = txtTenBan.getText().trim();
        
        if (tenBan.isEmpty() || tenBan.equals("Nhập tên bàn...")) {
            showError("Vui lòng nhập tên bàn!");
            txtTenBan.requestFocus();
            return false;
        }
        
        if (cboKhuVuc.getSelectedItem() == null) {
            showError("Vui lòng chọn khu vực!");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void clearForm() {
        txtTenBan.setText("Nhập tên bàn...");
        txtTenBan.setForeground(new Color(180, 180, 180));
        txtTimKiem.setText("Tìm kiếm bàn...");
        txtTimKiem.setForeground(new Color(180, 180, 180));
        
        if (cboKhuVuc.getItemCount() > 0) {
            cboKhuVuc.setSelectedIndex(0);
        }
        cboTrangThai.setSelectedIndex(0); // Mặc định là "Trống"
        
        selectedMaBan = -1;
        txtTenBan.requestFocus();
        tableBan.clearSelection();
        
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
    }
}