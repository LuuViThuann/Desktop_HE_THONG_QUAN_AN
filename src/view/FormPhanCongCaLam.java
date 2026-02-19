package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import dao.PhanCongCaLamDAO;
import dao.CaLamDAO;
import config.DatabaseConfig;
import model.PhanCongCaLam;
import model.CaLam;


public class FormPhanCongCaLam extends JFrame {

    private JComboBox<ComboItem> cboNhanVien;
    private JComboBox<CaLam> cboCaLam;
    private JSpinner spinnerNgayLam;       
    private JTextField txtTimKiem;
    private JSpinner spinnerTimKiem;      
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTimKiemNgay, btnTimKiemNhanVien;
    private JTable tablePhanCong;
    private DefaultTableModel tableModel;
    private int selectedMaPhanCong = -1;
    
    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK = new Color(25, 45, 85);        
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);     
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);     
    private static final Color DANGER_RED = new Color(192, 57, 43);         
    private static final Color WARNING_AMBER = new Color(241, 196, 15);     
    private static final Color BG_MAIN = new Color(241, 244, 247);          
    private static final Color BG_SECONDARY = new Color(255, 255, 255);     
    private static final Color TEXT_DARK = new Color(44, 62, 80);           
    private static final Color BORDER_COLOR = new Color(189, 195, 199);
    
    // Inner class để lưu trữ ID và tên
    private class ComboItem {
        private int id;
        private String ten;
        
        public ComboItem(int id, String ten) {
            this.id = id;
            this.ten = ten;
        }
        
        public int getId() { return id; }
        
        @Override
        public String toString() { return ten; }
    }
    
    public FormPhanCongCaLam() {
        initComponents();
        setupLayout();
        loadNhanVien();
        loadCaLam();
        loadPhanCongData();
        
        setTitle("Phân Công Ca Làm");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        cboNhanVien = new JComboBox<>();
        cboCaLam = new JComboBox<>();
        
        // ✅ Thay JDateChooser bằng JSpinner
        spinnerNgayLam = createDateSpinner(new Date());
        spinnerTimKiem = createDateSpinner(new Date());
        
        txtTimKiem = createModernTextField("Tìm kiếm theo tên nhân viên...");
        
        btnThem = createModernButton("Thêm Phân Công", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Phân Công", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Phân Công", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiemNgay = createModernButton("Tìm Theo Ngày", PRIMARY_LIGHT, true);
        btnTimKiemNhanVien = createModernButton("Tìm Theo NV", PRIMARY_LIGHT, true);
        
        btnThem.addActionListener(e -> themPhanCong());
        btnSua.addActionListener(e -> suaPhanCong());
        btnXoa.addActionListener(e -> xoaPhanCong());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiemNgay.addActionListener(e -> timKiemTheoNgay());
        btnTimKiemNhanVien.addActionListener(e -> timKiemTheoNhanVien());
        
        String[] columns = {"Mã PC", "Nhân Viên", "Ca Làm", "Giờ BĐ", "Giờ KT", "Ngày Làm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablePhanCong = new JTable(tableModel);
        setupTable();
        
        styleComboBox(cboNhanVien);
        styleComboBox(cboCaLam);
    }
    
    /**
     * ✅ Helper method: Tạo JSpinner cho ngày tháng
     * Thay thế JDateChooser.getDate()
     */
    private JSpinner createDateSpinner(Date initialDate) {
        SpinnerDateModel model = new SpinnerDateModel(
            initialDate,
            null,  // Không giới hạn tối thiểu
            null,  // Không giới hạn tối đa
            java.util.Calendar.DAY_OF_MONTH
        );
        
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.setPreferredSize(new Dimension(0, 45));
        spinner.setBackground(new Color(248, 250, 252));
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        return spinner;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(248, 250, 252));
        combo.setForeground(TEXT_DARK);
        combo.setPreferredSize(new Dimension(0, 45));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    private JTextField createModernTextField(String placeholder) {
        JTextField textField = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (hasFocus()) {
                    g2d.setColor(PRIMARY_LIGHT);
                    g2d.setStroke(new BasicStroke(2.5f));
                } else {
                    g2d.setColor(BORDER_COLOR);
                    g2d.setStroke(new BasicStroke(1.5f));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g);
            }
        };
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setForeground(TEXT_DARK);
        textField.setBackground(new Color(248, 250, 252));
        textField.setBorder(new EmptyBorder(12, 16, 12, 16));
        textField.setPreferredSize(new Dimension(0, 45));
        textField.setOpaque(true);
        return textField;
    }
    
    private JButton createModernButton(String text, Color bgColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color currentColor = getModel().isPressed() ? bgColor.darker().darker() : 
                                   getModel().isRollover() ? bgColor.brighter() : bgColor;
                if (isPrimary) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 2, 8, 8);
                }
                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 45));
        return btn;
    }
    
    private void setupTable() {
        tablePhanCong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablePhanCong.setRowHeight(40);
        tablePhanCong.setSelectionBackground(PRIMARY_LIGHT);
        tablePhanCong.setSelectionForeground(Color.WHITE);
        tablePhanCong.setGridColor(new Color(220, 220, 220));
        tablePhanCong.setShowGrid(true);
        
        JTableHeader header = tablePhanCong.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        
        tablePhanCong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienThiThongTinPhanCong();
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);
        
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), getHeight(), new Color(45, 85, 145)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        JLabel lblTitle = new JLabel("PHÂN CÔNG CA LÀM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        headerPanel.add(lblTitle);
        
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(BG_MAIN);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        mainContainer.add(createTopPanel(), BorderLayout.NORTH);
        mainContainer.add(createTablePanel(), BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        panel.add(createSearchPanel(), BorderLayout.NORTH);
        panel.add(createFormPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBackground(BG_SECONDARY);
        
        JPanel row1 = new JPanel(new BorderLayout(15, 0));
        row1.setBackground(BG_SECONDARY);
        JLabel lbl1 = new JLabel("Tìm kiếm theo ngày:");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl1.setPreferredSize(new Dimension(180, 45));
        row1.add(lbl1, BorderLayout.WEST);
        row1.add(spinnerTimKiem, BorderLayout.CENTER); 
        row1.add(btnTimKiemNgay, BorderLayout.EAST);
        
        JPanel row2 = new JPanel(new BorderLayout(15, 0));
        row2.setBackground(BG_SECONDARY);
        JLabel lbl2 = new JLabel("Tìm kiếm nhân viên:");
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl2.setPreferredSize(new Dimension(180, 45));
        row2.add(lbl2, BorderLayout.WEST);
        row2.add(txtTimKiem, BorderLayout.CENTER);
        row2.add(btnTimKiemNhanVien, BorderLayout.EAST);
        
        panel.add(row1);
        panel.add(row2);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel[] labels = {
            new JLabel("Nhân Viên *"),
            new JLabel("Ca Làm *"),
            new JLabel("Ngày Làm *")
        };
        
        for (JLabel lbl : labels) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(PRIMARY_DARK);
        }
        
        panel.add(labels[0]);
        panel.add(cboNhanVien);
        panel.add(labels[1]);
        panel.add(cboCaLam);
        panel.add(labels[2]);
        panel.add(spinnerNgayLam);  
        
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
        
        JLabel lblTitle = new JLabel("DANH SÁCH PHÂN CÔNG CA LÀM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tablePhanCong);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadNhanVien() {
        cboNhanVien.removeAllItems();
        String query = "SELECT MaNV, HoTen FROM NhanVien ORDER BY HoTen";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                cboNhanVien.addItem(new ComboItem(rs.getInt("MaNV"), rs.getString("HoTen")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadCaLam() {
        cboCaLam.removeAllItems();
        for (CaLam ca : CaLamDAO.getAllCaLam()) {
            cboCaLam.addItem(ca);
        }
    }
    
    private void loadPhanCongData() {
        tableModel.setRowCount(0);
        for (PhanCongCaLam pc : PhanCongCaLamDAO.getAllPhanCong()) {
            tableModel.addRow(new Object[]{
                pc.getMaPhanCong(),
                pc.getHoTenNV(),
                pc.getTenCa(),
                pc.getGioBatDau(),
                pc.getGioKetThuc(),
                pc.getNgayLam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            });
        }
    }
    
    private void timKiemTheoNgay() {
        Date selectedDate = (Date) spinnerTimKiem.getValue(); 
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày!");
            return;
        }
        
        LocalDate ngay = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        tableModel.setRowCount(0);
        
        for (PhanCongCaLam pc : PhanCongCaLamDAO.searchByDate(ngay)) {
            tableModel.addRow(new Object[]{
                pc.getMaPhanCong(), pc.getHoTenNV(), pc.getTenCa(),
                pc.getGioBatDau(), pc.getGioKetThuc(),
                pc.getNgayLam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            });
        }
    }
    
    private void timKiemTheoNhanVien() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            loadPhanCongData();
            return;
        }
        
        tableModel.setRowCount(0);
        for (PhanCongCaLam pc : PhanCongCaLamDAO.searchByNhanVien(keyword)) {
            tableModel.addRow(new Object[]{
                pc.getMaPhanCong(), pc.getHoTenNV(), pc.getTenCa(),
                pc.getGioBatDau(), pc.getGioKetThuc(),
                pc.getNgayLam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            });
        }
    }
    
    private void themPhanCong() {
        if (!validateInput()) return;
        
        PhanCongCaLam pc = new PhanCongCaLam();
        pc.setMaNV(((ComboItem)cboNhanVien.getSelectedItem()).getId());
        pc.setMaCa(((CaLam)cboCaLam.getSelectedItem()).getMaCa());
        pc.setNgayLam(((Date)spinnerNgayLam.getValue()).toInstant() 
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        
        if (PhanCongCaLamDAO.insertPhanCong(pc)) {
            JOptionPane.showMessageDialog(this, "✓ Thêm phân công thành công!");
            clearForm();
            loadPhanCongData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi! Nhân viên đã được phân công ca này trong ngày!");
        }
    }
    
    private void suaPhanCong() {
        if (selectedMaPhanCong <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phân công cần sửa!");
            return;
        }
        if (!validateInput()) return;
        
        PhanCongCaLam pc = new PhanCongCaLam();
        pc.setMaPhanCong(selectedMaPhanCong);
        pc.setMaNV(((ComboItem)cboNhanVien.getSelectedItem()).getId());
        pc.setMaCa(((CaLam)cboCaLam.getSelectedItem()).getMaCa());
        pc.setNgayLam(((Date)spinnerNgayLam.getValue()).toInstant() 
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        
        if (PhanCongCaLamDAO.updatePhanCong(pc)) {
            JOptionPane.showMessageDialog(this, "✓ Cập nhật phân công thành công!");
            clearForm();
            loadPhanCongData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
        }
    }
    
    private void xoaPhanCong() {
        if (selectedMaPhanCong <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phân công cần xóa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa phân công này?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION && PhanCongCaLamDAO.deletePhanCong(selectedMaPhanCong)) {
            JOptionPane.showMessageDialog(this, "✓ Xóa phân công thành công!");
            clearForm();
            loadPhanCongData();
        }
    }
    
    private void hienThiThongTinPhanCong() {
        int row = tablePhanCong.getSelectedRow();
        if (row >= 0) {
            selectedMaPhanCong = (int) tableModel.getValueAt(row, 0);
            btnThem.setEnabled(false);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
        }
    }
    
    private boolean validateInput() {
        if (cboNhanVien.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!");
            return false;
        }
        if (cboCaLam.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca làm!");
            return false;
        }
        if (spinnerNgayLam.getValue() == null) {  
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày làm!");
            return false;
        }
        return true;
    }
    
    private void clearForm() {
        if (cboNhanVien.getItemCount() > 0) cboNhanVien.setSelectedIndex(0);
        if (cboCaLam.getItemCount() > 0) cboCaLam.setSelectedIndex(0);
        spinnerNgayLam.setValue(new Date());  
        spinnerTimKiem.setValue(new Date());  
        txtTimKiem.setText("");
        selectedMaPhanCong = -1;
        tablePhanCong.clearSelection();
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
        loadPhanCongData();
    }
}