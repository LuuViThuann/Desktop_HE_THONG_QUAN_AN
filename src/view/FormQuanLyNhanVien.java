package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import config.DatabaseConfig;
import dao.LuongDAO;


public class FormQuanLyNhanVien extends JFrame {

    private JTextField txtHoTen;
    private JTextField txtEmail;
    private JTextField txtSDT;
    private JTextField txtDiaChi;
    private JTextField txtMatKhau;
    private JTextField txtTimKiem;
    private JComboBox<String> cboGioiTinh;
    private JComboBox<String> cboPhanQuyen;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JButton btnTimKiem;
    private JButton btnCapNhatLuong;           // ✅ NÚT MỚI
    private JButton btnCapNhatLuongAll;        // ✅ NÚT MỚI
    private JTable tableNhanVien;
    private DefaultTableModel tableModel;
    private int selectedMaNV = -1;
    
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
    
    public FormQuanLyNhanVien() {
        initComponents();
        loadPhanQuyen();
        setupLayout();
        loadNhanVienData();
        
        setTitle("Quản Lý Nhân Viên");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        txtHoTen = createModernTextField("Nhập họ tên...");
        txtEmail = createModernTextField("Nhập email...");
        txtSDT = createModernTextField("Nhập số điện thoại...");
        txtDiaChi = createModernTextField("Nhập địa chỉ...");
        txtMatKhau = createModernTextField("Nhập mật khẩu...");
        txtTimKiem = createModernTextField("Tìm kiếm nhân viên...");
        
        cboGioiTinh = createModernComboBox();
        cboGioiTinh.addItem("Nam");
        cboGioiTinh.addItem("Nữ");
        
        cboPhanQuyen = createModernComboBox();
        
        btnThem = createModernButton("Thêm NV", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa NV", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa NV", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiem = createModernButton("Tìm Kiếm", PRIMARY_LIGHT, true);
        
      
        btnCapNhatLuong = createModernButton("Cập Nhật Lương NV", ACCENT_ORANGE, true);
        btnCapNhatLuongAll = createModernButton("Cập Nhật Lương Tất Cả", ACCENT_ORANGE, true);
        
        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());
        btnXoa.addActionListener(e -> xoaNhanVien());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiem.addActionListener(e -> timKiemNhanVien());
        
      
        btnCapNhatLuong.addActionListener(e -> capNhatLuongNhanVien());
        btnCapNhatLuongAll.addActionListener(e -> capNhatLuongAll());
        
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemNhanVien();
                }
            }
        });
        
        String[] columns = {"Mã NV", "Họ Tên", "Giới Tính", "SĐT", "Email", "Địa Chỉ", "Phân Quyền", "Tổng Lương"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableNhanVien = new JTable(tableModel);
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

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

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

                if (isPrimary) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 2, 8, 8);
                }

                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

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
        tableNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableNhanVien.setRowHeight(40);
        tableNhanVien.setSelectionBackground(PRIMARY_LIGHT);
        tableNhanVien.setSelectionForeground(Color.WHITE);
        tableNhanVien.setGridColor(new Color(220, 220, 220));
        tableNhanVien.setShowGrid(true);
        tableNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tableNhanVien.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        tableNhanVien.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableNhanVien.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableNhanVien.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableNhanVien.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableNhanVien.getColumnModel().getColumn(4).setPreferredWidth(180);
        tableNhanVien.getColumnModel().getColumn(5).setPreferredWidth(200);
        tableNhanVien.getColumnModel().getColumn(6).setPreferredWidth(120);
        tableNhanVien.getColumnModel().getColumn(7).setPreferredWidth(130);
        
        tableNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiThongTinNhanVien();
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
        
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
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
        
        JLabel lblSearch = new JLabel("Tìm kiếm nhân viên:");
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
        JPanel panel = new JPanel(new GridLayout(2, 4, 20, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        panel.add(createFormField("Họ Tên *", txtHoTen));
        panel.add(createFormField("Email *", txtEmail));
        panel.add(createFormField("SĐT *", txtSDT));
        panel.add(createFormField("Giới Tính *", cboGioiTinh));
        panel.add(createFormField("Địa Chỉ", txtDiaChi));
        panel.add(createFormField("Mật Khẩu *", txtMatKhau));
        panel.add(createFormField("Phân Quyền *", cboPhanQuyen));
        
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(BG_SECONDARY);
        panel.add(emptyPanel);
        
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
        panel.add(new JSeparator(JSeparator.VERTICAL));  // Đường phân cách
        panel.add(btnCapNhatLuong);     
        panel.add(btnCapNhatLuongAll);  
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel lblTitle = new JLabel("DANH SÁCH NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tableNhanVien);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadPhanQuyen() {
        String query = "SELECT MaPQ, TenQuyen FROM PhanQuyen ORDER BY MaPQ";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            cboPhanQuyen.removeAllItems();
            
            while (rs.next()) {
                cboPhanQuyen.addItem(rs.getString("TenQuyen"));
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách phân quyền: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadNhanVienData() {
        String query = "SELECT n.MaNV, n.HoTen, n.GioiTinh, n.SDT, n.Email, n.DiaChi, " +
                      "p.TenQuyen, n.TongLuong " +
                      "FROM NhanVien n " +
                      "LEFT JOIN PhanQuyen p ON n.MaPQ = p.MaPQ " +
                      "ORDER BY n.MaNV DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaNV"),
                    rs.getString("HoTen"),
                    rs.getString("GioiTinh"),
                    rs.getString("SDT"),
                    rs.getString("Email"),
                    rs.getString("DiaChi"),
                    rs.getString("TenQuyen"),
                    String.format("%,.0f VND", rs.getDouble("TongLuong"))
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void timKiemNhanVien() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty() || keyword.equals("Tìm kiếm nhân viên...")) {
            loadNhanVienData();
            return;
        }
        
        String query = "SELECT n.MaNV, n.HoTen, n.GioiTinh, n.SDT, n.Email, n.DiaChi, " +
                      "p.TenQuyen, n.TongLuong " +
                      "FROM NhanVien n " +
                      "LEFT JOIN PhanQuyen p ON n.MaPQ = p.MaPQ " +
                      "WHERE n.HoTen LIKE ? OR n.SDT LIKE ? OR n.Email LIKE ? " +
                      "ORDER BY n.MaNV DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaNV"),
                    rs.getString("HoTen"),
                    rs.getString("GioiTinh"),
                    rs.getString("SDT"),
                    rs.getString("Email"),
                    rs.getString("DiaChi"),
                    rs.getString("TenQuyen"),
                    String.format("%,.0f VND", rs.getDouble("TongLuong"))
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "Không tìm thấy nhân viên nào!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tìm kiếm: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
   
    private void capNhatLuongNhanVien() {
        if (selectedMaNV <= 0) {
            showError("Vui lòng chọn nhân viên cần cập nhật lương!");
            return;
        }
        
        if (LuongDAO.calculateAndUpdateLuong(selectedMaNV)) {
            JOptionPane.showMessageDialog(this,
                "✓ Cập nhật lương nhân viên thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadNhanVienData();
        } else {
            showError("Lỗi khi cập nhật lương!");
        }
    }
    
  
    private void capNhatLuongAll() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn muốn cập nhật lương cho tất cả nhân viên?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Hiển thị progress dialog
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setString("Đang cập nhật lương...");
            progressBar.setStringPainted(true);
            
            JOptionPane optionPane = new JOptionPane(progressBar, JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Vui lòng chờ...");
            dialog.setModal(false);
            dialog.setVisible(true);
            
            // Chạy cập nhật trong thread riêng
            Thread updateThread = new Thread(() -> {
                int count = LuongDAO.calculateAndUpdateLuongAll();
                
                dialog.dispose();
                
                JOptionPane.showMessageDialog(this,
                    "✓ Cập nhật lương cho " + count + " nhân viên thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                loadNhanVienData();
            });
            updateThread.start();
        }
    }
    
    private void themNhanVien() {
        if (!validateInput()) {
            return;
        }
        
        String query = "INSERT INTO NhanVien (HoTen, GioiTinh, SDT, DiaChi, Email, MatKhau, TongLuong, MaPQ) " +
                      "VALUES (?, ?, ?, ?, ?, ?, 0, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtHoTen.getText().trim());
            pstmt.setString(2, (String) cboGioiTinh.getSelectedItem());
            pstmt.setString(3, txtSDT.getText().trim());
            pstmt.setString(4, txtDiaChi.getText().trim());
            pstmt.setString(5, txtEmail.getText().trim());
            pstmt.setString(6, txtMatKhau.getText().trim());
            pstmt.setInt(7, cboPhanQuyen.getSelectedIndex() + 1);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Thêm nhân viên thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadNhanVienData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi thêm nhân viên: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void suaNhanVien() {
        if (selectedMaNV <= 0) {
            showError("Vui lòng chọn nhân viên cần sửa từ bảng!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        String query = "UPDATE NhanVien SET HoTen = ?, GioiTinh = ?, SDT = ?, DiaChi = ?, " +
                      "Email = ?, MatKhau = ?, MaPQ = ? WHERE MaNV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtHoTen.getText().trim());
            pstmt.setString(2, (String) cboGioiTinh.getSelectedItem());
            pstmt.setString(3, txtSDT.getText().trim());
            pstmt.setString(4, txtDiaChi.getText().trim());
            pstmt.setString(5, txtEmail.getText().trim());
            pstmt.setString(6, txtMatKhau.getText().trim());
            pstmt.setInt(7, cboPhanQuyen.getSelectedIndex() + 1);
            pstmt.setInt(8, selectedMaNV);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Cập nhật nhân viên thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadNhanVienData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật nhân viên: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void xoaNhanVien() {
        if (selectedMaNV <= 0) {
            showError("Vui lòng chọn nhân viên cần xóa từ bảng!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa nhân viên này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM NhanVien WHERE MaNV = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaNV);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "✓ Xóa nhân viên thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
                    clearForm();
                    loadNhanVienData();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa nhân viên: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void hienThiThongTinNhanVien() {
        int selectedRow = tableNhanVien.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMaNV = (int) tableModel.getValueAt(selectedRow, 0);
            
            String query = "SELECT HoTen, GioiTinh, SDT, DiaChi, Email, MatKhau, MaPQ " +
                          "FROM NhanVien WHERE MaNV = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaNV);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    txtHoTen.setText(rs.getString("HoTen"));
                    txtHoTen.setForeground(TEXT_DARK);
                    cboGioiTinh.setSelectedItem(rs.getString("GioiTinh"));
                    txtSDT.setText(rs.getString("SDT"));
                    txtSDT.setForeground(TEXT_DARK);
                    txtDiaChi.setText(rs.getString("DiaChi"));
                    txtDiaChi.setForeground(TEXT_DARK);
                    txtEmail.setText(rs.getString("Email"));
                    txtEmail.setForeground(TEXT_DARK);
                    txtMatKhau.setText(rs.getString("MatKhau"));
                    txtMatKhau.setForeground(TEXT_DARK);
                    
                    int maPQ = rs.getInt("MaPQ");
                    cboPhanQuyen.setSelectedIndex(maPQ - 1);
                    
                    btnThem.setEnabled(false);
                    btnSua.setEnabled(true);
                    btnXoa.setEnabled(true);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải thông tin nhân viên: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateInput() {
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSDT.getText().trim();
        String matKhau = txtMatKhau.getText().trim();
        
        if (hoTen.isEmpty() || hoTen.equals("Nhập họ tên...")) {
            showError("Vui lòng nhập họ tên!");
            txtHoTen.requestFocus();
            return false;
        }
        
        if (email.isEmpty() || email.equals("Nhập email...")) {
            showError("Vui lòng nhập email!");
            txtEmail.requestFocus();
            return false;
        }
        
        if (sdt.isEmpty() || sdt.equals("Nhập số điện thoại...")) {
            showError("Vui lòng nhập số điện thoại!");
            txtSDT.requestFocus();
            return false;
        }
        
        if (matKhau.isEmpty() || matKhau.equals("Nhập mật khẩu...")) {
            showError("Vui lòng nhập mật khẩu!");
            txtMatKhau.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void clearForm() {
        txtHoTen.setText("Nhập họ tên...");
        txtHoTen.setForeground(new Color(180, 180, 180));
        txtEmail.setText("Nhập email...");
        txtEmail.setForeground(new Color(180, 180, 180));
        txtSDT.setText("Nhập số điện thoại...");
        txtSDT.setForeground(new Color(180, 180, 180));
        txtDiaChi.setText("Nhập địa chỉ...");
        txtDiaChi.setForeground(new Color(180, 180, 180));
        txtMatKhau.setText("Nhập mật khẩu...");
        txtMatKhau.setForeground(new Color(180, 180, 180));
        txtTimKiem.setText("Tìm kiếm nhân viên...");
        txtTimKiem.setForeground(new Color(180, 180, 180));
        
        cboGioiTinh.setSelectedIndex(0);
        if (cboPhanQuyen.getItemCount() > 0) {
            cboPhanQuyen.setSelectedIndex(0);
        }
        
        selectedMaNV = -1;
        txtHoTen.requestFocus();
        tableNhanVien.clearSelection();
        
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
        
        loadNhanVienData();
    }
}