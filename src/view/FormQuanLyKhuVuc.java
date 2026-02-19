package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import config.DatabaseConfig;

public class FormQuanLyKhuVuc extends JFrame {

    private JTextField txtTenKhuVuc;
    private JTextField txtTimKiem;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JButton btnTimKiem;
    private JTable tableKhuVuc;
    private DefaultTableModel tableModel;
    private int selectedMaKV = -1;
    
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
    
    public FormQuanLyKhuVuc() {
        initComponents();
        setupLayout();
        loadKhuVucData();
        
        setTitle("Quản Lý Khu Vực");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        txtTenKhuVuc = createModernTextField("Nhập tên khu vực...");
        txtTimKiem = createModernTextField("Tìm kiếm khu vực...");
        
        btnThem = createModernButton("Thêm Khu Vực", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Khu Vực", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Khu Vực", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiem = createModernButton("Tìm Kiếm", PRIMARY_LIGHT, true);
        
        btnThem.addActionListener(e -> themKhuVuc());
        btnSua.addActionListener(e -> suaKhuVuc());
        btnXoa.addActionListener(e -> xoaKhuVuc());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiem.addActionListener(e -> timKiemKhuVuc());
        
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemKhuVuc();
                }
            }
        });
        
        String[] columns = {"Mã Khu Vực", "Tên Khu Vực", "Số Bàn"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableKhuVuc = new JTable(tableModel);
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
        tableKhuVuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableKhuVuc.setRowHeight(40);
        tableKhuVuc.setSelectionBackground(PRIMARY_LIGHT);
        tableKhuVuc.setSelectionForeground(Color.WHITE);
        tableKhuVuc.setGridColor(new Color(220, 220, 220));
        tableKhuVuc.setShowGrid(true);
        tableKhuVuc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tableKhuVuc.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        tableKhuVuc.getColumnModel().getColumn(0).setPreferredWidth(150);
        tableKhuVuc.getColumnModel().getColumn(1).setPreferredWidth(300);
        tableKhuVuc.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        tableKhuVuc.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiThongTinKhuVuc();
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
        
        JLabel lblTitle = new JLabel("QUẢN LÝ KHU VỰC");
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
        
        JLabel lblSearch = new JLabel("Tìm kiếm khu vực:");
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lblLabel = new JLabel("Tên Khu Vực *");
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLabel.setForeground(PRIMARY_DARK);
        
        panel.add(lblLabel, BorderLayout.NORTH);
        panel.add(txtTenKhuVuc, BorderLayout.CENTER);
        
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
        
        JLabel lblTitle = new JLabel("DANH SÁCH KHU VỰC");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tableKhuVuc);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadKhuVucData() {
        String query = "SELECT k.MaKV, k.TenKV, COUNT(b.MaBan) AS SoBan " +
                      "FROM KhuVucQuan k " +
                      "LEFT JOIN Ban b ON k.MaKV = b.MaKV " +
                      "GROUP BY k.MaKV, k.TenKV " +
                      "ORDER BY k.MaKV DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaKV"),
                    rs.getString("TenKV"),
                    rs.getInt("SoBan")
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void timKiemKhuVuc() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty() || keyword.equals("Tìm kiếm khu vực...")) {
            loadKhuVucData();
            return;
        }
        
        String query = "SELECT k.MaKV, k.TenKV, COUNT(b.MaBan) AS SoBan " +
                      "FROM KhuVucQuan k " +
                      "LEFT JOIN Ban b ON k.MaKV = b.MaKV " +
                      "WHERE k.TenKV LIKE ? " +
                      "GROUP BY k.MaKV, k.TenKV " +
                      "ORDER BY k.MaKV DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaKV"),
                    rs.getString("TenKV"),
                    rs.getInt("SoBan")
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy khu vực nào!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tìm kiếm: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void themKhuVuc() {
        if (!validateInput()) {
            return;
        }
        
        String query = "INSERT INTO KhuVucQuan (TenKV) VALUES (?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenKhuVuc.getText().trim());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Thêm khu vực thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadKhuVucData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi thêm khu vực: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void suaKhuVuc() {
        if (selectedMaKV <= 0) {
            showError("Vui lòng chọn khu vực cần sửa từ bảng!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        String query = "UPDATE KhuVucQuan SET TenKV = ? WHERE MaKV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenKhuVuc.getText().trim());
            pstmt.setInt(2, selectedMaKV);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Cập nhật khu vực thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadKhuVucData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật khu vực: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void xoaKhuVuc() {
        if (selectedMaKV <= 0) {
            showError("Vui lòng chọn khu vực cần xóa từ bảng!");
            return;
        }
        
        // Kiểm tra xem khu vực có bàn không
        int soBan = checkSoBanTrongKhuVuc();
        if (soBan > 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Khu vực này có " + soBan + " bàn.\n⚠️ Bạn có chắc chắn muốn xóa khu vực này?\nTất cả bàn trong khu vực sẽ bị xóa!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khu vực này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        String query = "DELETE FROM KhuVucQuan WHERE MaKV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, selectedMaKV);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Xóa khu vực thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadKhuVucData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xóa khu vực: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int checkSoBanTrongKhuVuc() {
        String query = "SELECT COUNT(*) AS SoBan FROM Ban WHERE MaKV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, selectedMaKV);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("SoBan");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    private void hienThiThongTinKhuVuc() {
        int selectedRow = tableKhuVuc.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMaKV = (int) tableModel.getValueAt(selectedRow, 0);
            String tenKV = (String) tableModel.getValueAt(selectedRow, 1);
            
            txtTenKhuVuc.setText(tenKV);
            txtTenKhuVuc.setForeground(TEXT_DARK);
            
            btnThem.setEnabled(false);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
        }
    }
    
    private boolean validateInput() {
        String tenKV = txtTenKhuVuc.getText().trim();
        
        if (tenKV.isEmpty() || tenKV.equals("Nhập tên khu vực...")) {
            showError("Vui lòng nhập tên khu vực!");
            txtTenKhuVuc.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void clearForm() {
        txtTenKhuVuc.setText("Nhập tên khu vực...");
        txtTenKhuVuc.setForeground(new Color(180, 180, 180));
        txtTimKiem.setText("Tìm kiếm khu vực...");
        txtTimKiem.setForeground(new Color(180, 180, 180));
        
        selectedMaKV = -1;
        txtTenKhuVuc.requestFocus();
        tableKhuVuc.clearSelection();
        
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
        
        loadKhuVucData();
    }
}