package view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import config.DatabaseConfig;
import model.Nhom;

public class FormThemMonAn extends JFrame {

    private JTextField txtTenMon;
    private JTextField txtGiaTien;
    private JTextField txtDonViTinh;
    private JTextField txtSoLuong;
    private JComboBox<Nhom> cboNhom;
    private JLabel lblImagePreview;
    private JButton btnChonAnh;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JTable tableMonAn;
    private DefaultTableModel tableModel;
    private int selectedMaMon = -1;
    
    private String selectedImagePath = null;
    private String savedImageName = null;
    
    private static final String IMAGE_FOLDER = "src/Assets/images/";
    
  
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
    
    public FormThemMonAn() {
        initComponents();
        loadNhomMonAn();
        setupLayout();
        loadMonAnData();
        
        setTitle("Quản Lý Món Ăn - Thêm Món Mới");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        createImageFolderIfNotExists();
    }
    
    private void initComponents() {
        // Text Fields với style hiện đại
        txtTenMon = createModernTextField("Nhập tên món ăn...");
        txtGiaTien = createModernTextField("Nhập giá tiền...");
        txtDonViTinh = createModernTextField("Nhập đơn vị tính...");
        txtSoLuong = createModernTextField("Nhập số lượng...");
        
        // ComboBox với style hiện đại
        cboNhom = createModernComboBox();
        
        // Image Preview
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(200, 200));
        lblImagePreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setVerticalAlignment(JLabel.CENTER);
        lblImagePreview.setText("Chưa có ảnh");
        lblImagePreview.setBackground(new Color(248, 250, 252));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImagePreview.setForeground(TEXT_LIGHT);
        
        // Buttons với style hiện đại
        btnChonAnh = createModernButton("Chọn Ảnh", PRIMARY_LIGHT, true);
        btnThem = createModernButton("Thêm Món", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Món", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Món", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        
        btnChonAnh.addActionListener(e -> chonAnhMonAn());
        btnThem.addActionListener(e -> themMonAn());
        btnSua.addActionListener(e -> suaMonAn());
        btnXoa.addActionListener(e -> xoaMonAn());
        btnLamMoi.addActionListener(e -> clearForm());
        
        // Table setup
        String[] columns = {"Mã", "Tên Món", "Giá Tiền", "ĐVT", "SL Còn", "Nhóm", "Hình Ảnh"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableMonAn = new JTable(tableModel);
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
        textField.setForeground(new Color(180, 180, 180));
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
    private JComboBox<Nhom> createModernComboBox() {
        JComboBox<Nhom> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(248, 250, 252));
        combo.setForeground(TEXT_DARK);
        combo.setPreferredSize(new Dimension(0, 45));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        combo.setFocusable(true);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return combo;
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
        tableMonAn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableMonAn.setRowHeight(40);
        tableMonAn.setSelectionBackground(PRIMARY_LIGHT);
        tableMonAn.setSelectionForeground(Color.WHITE);
        tableMonAn.setGridColor(new Color(220, 220, 220));
        tableMonAn.setShowGrid(true);
        tableMonAn.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tableMonAn.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        // Cột width
        tableMonAn.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableMonAn.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableMonAn.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableMonAn.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableMonAn.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableMonAn.getColumnModel().getColumn(5).setPreferredWidth(150);
        tableMonAn.getColumnModel().getColumn(6).setPreferredWidth(200);
        
        // Add selection listener
        tableMonAn.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiThongTinMonAn();
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_MAIN);
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        
        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(BG_MAIN);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top Panel - Form nhập liệu
        JPanel topPanel = createTopPanel();
        
        // Bottom Panel - Bảng danh sách
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
        
        JLabel lblTitle = new JLabel("QUẢN LÝ MÓN ĂN - THÊM MÓN MỚI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        panel.add(lblTitle, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Left - Image
        JPanel imagePanel = createImagePanel();
        
        // Center - Form fields
        JPanel formPanel = createFormPanel();
        
        // Right - Buttons
        JPanel buttonPanel = createButtonPanel();
        
        panel.add(imagePanel, BorderLayout.WEST);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_SECONDARY);
        panel.setPreferredSize(new Dimension(230, 0));
        
        JLabel lblTitle = new JLabel("Hình Ảnh Món Ăn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(PRIMARY_DARK);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(lblImagePreview, BorderLayout.CENTER);
        panel.add(btnChonAnh, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 20, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        
        panel.add(createFormField("Tên Món Ăn *", txtTenMon));
        panel.add(createFormField("Giá Tiền (VND) *", txtGiaTien));
        panel.add(createFormField("Đơn Vị Tính *", txtDonViTinh));
        panel.add(createFormField("Số Lượng *", txtSoLuong));
        panel.add(createFormField("Nhóm Món *", cboNhom));
        
        // Empty panel for layout balance
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(BG_SECONDARY);
        panel.add(emptyPanel);
        
        return panel;
    }
    
    private JPanel createFormField(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_SECONDARY);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLabel.setForeground(PRIMARY_DARK);
        
        panel.add(lblLabel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 15, 25, 0));
        panel.setPreferredSize(new Dimension(160, 0));
        
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
        
        JLabel lblTitle = new JLabel("DANH SÁCH MÓN ĂN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tableMonAn);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadNhomMonAn() {
        String query = "SELECT MaNhom, TenNhom FROM Nhom ORDER BY TenNhom";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            cboNhom.removeAllItems();
            
            while (rs.next()) {
                Nhom nhom = new Nhom();
                nhom.setMaNhom(rs.getInt("MaNhom"));
                nhom.setTenNhom(rs.getString("TenNhom"));
                cboNhom.addItem(nhom);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách nhóm: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadMonAnData() {
        String query = "SELECT m.MaMon, m.TenMon, m.GiaTien, m.DonViTinh, " +
                      "m.SoLuongConLai, n.TenNhom, m.HinhAnh " +
                      "FROM MonAn m " +
                      "LEFT JOIN Nhom n ON m.MaNhom = n.MaNhom " +
                      "ORDER BY m.MaMon DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableModel.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaMon"),
                    rs.getString("TenMon"),
                    String.format("%,.0f VND", rs.getDouble("GiaTien")),
                    rs.getString("DonViTinh"),
                    rs.getInt("SoLuongConLai"),
                    rs.getString("TenNhom"),
                    rs.getString("HinhAnh") != null ? rs.getString("HinhAnh") : "Không có"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createImageFolderIfNotExists() {
        try {
            Path path = Paths.get(IMAGE_FOLDER);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Không thể tạo thư mục lưu ảnh: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void chonAnhMonAn() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh món ăn");
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            displayImagePreview(selectedFile);
        }
    }
    
    private void displayImagePreview(File imageFile) {
        try {
            BufferedImage originalImage = ImageIO.read(imageFile);
            
            Image scaledImage = originalImage.getScaledInstance(
                200, 200, Image.SCALE_SMOOTH);
            
            ImageIcon icon = new ImageIcon(scaledImage);
            lblImagePreview.setIcon(icon);
            lblImagePreview.setText("");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Không thể đọc file ảnh: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String copyImageToAssets(File sourceFile) {
        try {
            String extension = getFileExtension(sourceFile);
            String newFileName = System.currentTimeMillis() + extension;
            
            Path sourcePath = sourceFile.toPath();
            Path targetPath = Paths.get(IMAGE_FOLDER + newFileName);
            
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return newFileName;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi copy ảnh: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }
    
    private void themMonAn() {
        if (!validateInput()) {
            return;
        }
        
        if (selectedImagePath != null) {
            savedImageName = copyImageToAssets(new File(selectedImagePath));
            if (savedImageName == null) {
                return;
            }
        }
        
        String query = "INSERT INTO MonAn (TenMon, GiaTien, DonViTinh, SoLuongConLai, MaNhom, HinhAnh) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenMon.getText().trim());
            pstmt.setBigDecimal(2, new java.math.BigDecimal(txtGiaTien.getText().trim()));
            pstmt.setString(3, txtDonViTinh.getText().trim());
            pstmt.setInt(4, Integer.parseInt(txtSoLuong.getText().trim()));
            
            Nhom selectedNhom = (Nhom) cboNhom.getSelectedItem();
            pstmt.setInt(5, selectedNhom.getMaNhom());
            pstmt.setString(6, savedImageName);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Thêm món ăn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadMonAnData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi thêm món ăn: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateInput() {
        String tenMon = txtTenMon.getText().trim();
        if (tenMon.isEmpty() || tenMon.equals("Nhập tên món ăn...")) {
            showError("Vui lòng nhập tên món!");
            txtTenMon.requestFocus();
            return false;
        }
        
        String giaTien = txtGiaTien.getText().trim();
        if (giaTien.isEmpty() || giaTien.equals("Nhập giá tiền...")) {
            showError("Vui lòng nhập giá tiền!");
            txtGiaTien.requestFocus();
            return false;
        }
        
        try {
            double gia = Double.parseDouble(giaTien);
            if (gia <= 0) {
                showError("Giá tiền phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Giá tiền không hợp lệ!");
            txtGiaTien.requestFocus();
            return false;
        }
        
        String donViTinh = txtDonViTinh.getText().trim();
        if (donViTinh.isEmpty() || donViTinh.equals("Nhập đơn vị tính...")) {
            showError("Vui lòng nhập đơn vị tính!");
            txtDonViTinh.requestFocus();
            return false;
        }
        
        String soLuong = txtSoLuong.getText().trim();
        if (soLuong.isEmpty() || soLuong.equals("Nhập số lượng...")) {
            showError("Vui lòng nhập số lượng!");
            txtSoLuong.requestFocus();
            return false;
        }
        
        try {
            int sl = Integer.parseInt(soLuong);
            if (sl < 0) {
                showError("Số lượng không được âm!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng không hợp lệ!");
            txtSoLuong.requestFocus();
            return false;
        }
        
        if (cboNhom.getSelectedItem() == null) {
            showError("Vui lòng chọn nhóm món!");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void clearForm() {
        txtTenMon.setText("Nhập tên món ăn...");
        txtTenMon.setForeground(new Color(180, 180, 180));
        
        txtGiaTien.setText("Nhập giá tiền...");
        txtGiaTien.setForeground(new Color(180, 180, 180));
        
        txtDonViTinh.setText("Nhập đơn vị tính...");
        txtDonViTinh.setForeground(new Color(180, 180, 180));
        
        txtSoLuong.setText("Nhập số lượng...");
        txtSoLuong.setForeground(new Color(180, 180, 180));
        
        if (cboNhom.getItemCount() > 0) {
            cboNhom.setSelectedIndex(0);
        }
        
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("Chưa có ảnh");
        selectedImagePath = null;
        savedImageName = null;
        selectedMaMon = -1;
        txtTenMon.requestFocus();
        tableMonAn.clearSelection();
        
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
    }
    
    private void hienThiThongTinMonAn() {
        int selectedRow = tableMonAn.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMaMon = (int) tableModel.getValueAt(selectedRow, 0);
            
            String query = "SELECT m.MaMon, m.TenMon, m.GiaTien, m.DonViTinh, " +
                          "m.SoLuongConLai, m.MaNhom, m.HinhAnh " +
                          "FROM MonAn m WHERE m.MaMon = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaMon);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    txtTenMon.setText(rs.getString("TenMon"));
                    txtTenMon.setForeground(TEXT_DARK);
                    
                    txtGiaTien.setText(String.valueOf(rs.getDouble("GiaTien")));
                    txtGiaTien.setForeground(TEXT_DARK);
                    
                    txtDonViTinh.setText(rs.getString("DonViTinh"));
                    txtDonViTinh.setForeground(TEXT_DARK);
                    
                    txtSoLuong.setText(String.valueOf(rs.getInt("SoLuongConLai")));
                    txtSoLuong.setForeground(TEXT_DARK);
                    
                    int maNhom = rs.getInt("MaNhom");
                    for (int i = 0; i < cboNhom.getItemCount(); i++) {
                        if (((Nhom) cboNhom.getItemAt(i)).getMaNhom() == maNhom) {
                            cboNhom.setSelectedIndex(i);
                            break;
                        }
                    }
                    
                    String hinhAnh = rs.getString("HinhAnh");
                    if (hinhAnh != null && !hinhAnh.isEmpty()) {
                        savedImageName = hinhAnh;
                        displayImageFromPath(IMAGE_FOLDER + hinhAnh);
                    } else {
                        lblImagePreview.setIcon(null);
                        lblImagePreview.setText("Chưa có ảnh");
                    }
                    
                    btnThem.setEnabled(false);
                    btnSua.setEnabled(true);
                    btnXoa.setEnabled(true);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải thông tin món ăn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void displayImageFromPath(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                BufferedImage originalImage = ImageIO.read(imageFile);
                Image scaledImage = originalImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                lblImagePreview.setIcon(icon);
                lblImagePreview.setText("");
            }
        } catch (IOException e) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Lỗi load ảnh");
        }
    }
    
    private void suaMonAn() {
        if (selectedMaMon <= 0) {
            showError("Vui lòng chọn món ăn cần sửa từ bảng!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        if (selectedImagePath != null) {
            String newImageName = copyImageToAssets(new File(selectedImagePath));
            if (newImageName != null) {
                savedImageName = newImageName;
            }
        }
        
        String query = "UPDATE MonAn SET TenMon = ?, GiaTien = ?, DonViTinh = ?, " +
                      "SoLuongConLai = ?, MaNhom = ?, HinhAnh = ? WHERE MaMon = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, txtTenMon.getText().trim());
            pstmt.setBigDecimal(2, new java.math.BigDecimal(txtGiaTien.getText().trim()));
            pstmt.setString(3, txtDonViTinh.getText().trim());
            pstmt.setInt(4, Integer.parseInt(txtSoLuong.getText().trim()));
            
            Nhom selectedNhom = (Nhom) cboNhom.getSelectedItem();
            pstmt.setInt(5, selectedNhom.getMaNhom());
            pstmt.setString(6, savedImageName);
            pstmt.setInt(7, selectedMaMon);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Cập nhật món ăn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadMonAnData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật món ăn: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void xoaMonAn() {
        if (selectedMaMon <= 0) {
            showError("Vui lòng chọn món ăn cần xóa từ bảng!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa món ăn này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM MonAn WHERE MaMon = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selectedMaMon);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "✓ Xóa món ăn thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
                    clearForm();
                    loadMonAnData();
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa món ăn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}