package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import config.DatabaseConfig;

public class FormQuanLyPhanQuyen extends JFrame {

    private JTextField txtTenQuyen;
    private JTextField txtMucLuong;
    private JTextField txtTimKiem;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JButton btnTimKiem;
    private JTable tablePhanQuyen;
    private DefaultTableModel tableModel;
    private int selectedMaPQ = -1;
    
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
    
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    
    public FormQuanLyPhanQuyen() {
        initComponents();
        setupLayout();
        loadPhanQuyenData();
        
        setTitle("Quản Lý Phân Quyền");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        txtTenQuyen = createModernTextField("Nhập tên quyền...");
        txtMucLuong = createModernTextField("Nhập mức lương...");
        txtTimKiem = createModernTextField("Tìm kiếm phân quyền...");
        
        btnThem = createModernButton("Thêm Quyền", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Quyền", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Quyền", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiem = createModernButton("Tìm Kiếm", PRIMARY_LIGHT, true);
        
        btnThem.addActionListener(e -> themPhanQuyen());
        btnSua.addActionListener(e -> suaPhanQuyen());
        btnXoa.addActionListener(e -> xoaPhanQuyen());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiem.addActionListener(e -> timKiemPhanQuyen());
        
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemPhanQuyen();
                }
            }
        });
        
        String[] columns = {"Mã Quyền", "Tên Quyền", "Mức Lương"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablePhanQuyen = new JTable(tableModel);
        setupTable();
    }
    
  
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
        tablePhanQuyen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablePhanQuyen.setRowHeight(40);
        tablePhanQuyen.setSelectionBackground(PRIMARY_LIGHT);
        tablePhanQuyen.setSelectionForeground(Color.WHITE);
        tablePhanQuyen.setGridColor(new Color(220, 220, 220));
        tablePhanQuyen.setShowGrid(true);
        tablePhanQuyen.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tablePhanQuyen.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        tablePhanQuyen.getColumnModel().getColumn(0).setPreferredWidth(150);
        tablePhanQuyen.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablePhanQuyen.getColumnModel().getColumn(2).setPreferredWidth(200);
        
        tablePhanQuyen.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiThongTinPhanQuyen();
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
        
        JLabel lblTitle = new JLabel("QUẢN LÝ PHÂN QUYỀN");
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
        
        JLabel lblSearch = new JLabel("Tìm kiếm phân quyền:");
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
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        panel.add(createFormField("Tên Quyền *", txtTenQuyen));
        panel.add(createFormField("Mức Lương *", txtMucLuong));
        
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
        
        JLabel lblTitle = new JLabel("DANH SÁCH PHÂN QUYỀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tablePhanQuyen);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadPhanQuyenData() {
        String query = "SELECT MaPQ, TenQuyen, MucLuong FROM PhanQuyen ORDER BY MaPQ DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaPQ"),
                    rs.getString("TenQuyen"),
                    currencyFormat.format(rs.getDouble("MucLuong")) + " VNĐ"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void timKiemPhanQuyen() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty() || keyword.equals("Tìm kiếm phân quyền...")) {
            loadPhanQuyenData();
            return;
        }
        
        String query = "SELECT MaPQ, TenQuyen, MucLuong FROM PhanQuyen WHERE TenQuyen LIKE ? ORDER BY MaPQ DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaPQ"),
                    rs.getString("TenQuyen"),
                    currencyFormat.format(rs.getDouble("MucLuong")) + " VNĐ"
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy phân quyền nào!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tìm kiếm: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void themPhanQuyen() {
        if (!validateInput()) {
            return;
        }
        
        String query = "INSERT INTO PhanQuyen (TenQuyen, MucLuong) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenQuyen.getText().trim());
            pstmt.setDouble(2, Double.parseDouble(txtMucLuong.getText().trim().replace(",", "")));
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Thêm phân quyền thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadPhanQuyenData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi thêm phân quyền: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void suaPhanQuyen() {
        if (selectedMaPQ <= 0) {
            showError("Vui lòng chọn phân quyền cần sửa từ bảng!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        String query = "UPDATE PhanQuyen SET TenQuyen = ?, MucLuong = ? WHERE MaPQ = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenQuyen.getText().trim());
            pstmt.setDouble(2, Double.parseDouble(txtMucLuong.getText().trim().replace(",", "")));
            pstmt.setInt(3, selectedMaPQ);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Cập nhật phân quyền thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadPhanQuyenData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật phân quyền: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void xoaPhanQuyen() {
        if (selectedMaPQ <= 0) {
            showError("Vui lòng chọn phân quyền cần xóa từ bảng!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa phân quyền này?\n⚠️ Lưu ý: Nhân viên có quyền này cũng sẽ bị ảnh hưởng!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM PhanQuyen WHERE MaPQ = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaPQ);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "✓ Xóa phân quyền thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
                    clearForm();
                    loadPhanQuyenData();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa phân quyền: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void hienThiThongTinPhanQuyen() {
        int selectedRow = tablePhanQuyen.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMaPQ = (int) tableModel.getValueAt(selectedRow, 0);
            
            String query = "SELECT TenQuyen, MucLuong FROM PhanQuyen WHERE MaPQ = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaPQ);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    txtTenQuyen.setText(rs.getString("TenQuyen"));
                    txtTenQuyen.setForeground(TEXT_DARK);
                    txtMucLuong.setText(String.valueOf(rs.getDouble("MucLuong")));
                    txtMucLuong.setForeground(TEXT_DARK);
                    
                    btnThem.setEnabled(false);
                    btnSua.setEnabled(true);
                    btnXoa.setEnabled(true);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải thông tin phân quyền: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateInput() {
        String tenQuyen = txtTenQuyen.getText().trim();
        String mucLuong = txtMucLuong.getText().trim();
        
        if (tenQuyen.isEmpty() || tenQuyen.equals("Nhập tên quyền...")) {
            showError("Vui lòng nhập tên quyền!");
            txtTenQuyen.requestFocus();
            return false;
        }
        
        if (mucLuong.isEmpty() || mucLuong.equals("Nhập mức lương...")) {
            showError("Vui lòng nhập mức lương!");
            txtMucLuong.requestFocus();
            return false;
        }
        
        try {
            double luong = Double.parseDouble(mucLuong.replace(",", ""));
            if (luong < 0) {
                showError("Mức lương phải lớn hơn hoặc bằng 0!");
                txtMucLuong.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Mức lương phải là số hợp lệ!");
            txtMucLuong.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void clearForm() {
        txtTenQuyen.setText("Nhập tên quyền...");
        txtTenQuyen.setForeground(new Color(180, 180, 180));
        txtMucLuong.setText("Nhập mức lương...");
        txtMucLuong.setForeground(new Color(180, 180, 180));
        txtTimKiem.setText("Tìm kiếm phân quyền...");
        txtTimKiem.setForeground(new Color(180, 180, 180));
        
        selectedMaPQ = -1;
        txtTenQuyen.requestFocus();
        tablePhanQuyen.clearSelection();
        
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
        
        loadPhanQuyenData();
    }
}