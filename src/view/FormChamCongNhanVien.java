package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import dao.ChamCongNhanVienDAO;
import config.DatabaseConfig;
import model.ChamCongNhanVien;

public class FormChamCongNhanVien extends JFrame {

    private JComboBox<ComboItem> cboNhanVien;
    private JComboBox<String> cboTrangThai;
    private JSpinner spinnerNgayCong;
    private JSpinner spinnerTimKiem;
    private JTextField txtTimKiem;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboNam;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JButton btnTimKiemNgay, btnTimKiemNhanVien, btnTimKiemThang;
    private JTable tableChamCong;
    private DefaultTableModel tableModel;
    private int selectedMaCong = -1;
    
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
    
    public FormChamCongNhanVien() {
        initComponents();
        setupLayout();
        loadNhanVien();
        loadChamCongData();
        
        setTitle("Chấm Công Nhân Viên");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        cboNhanVien = new JComboBox<>();
        cboTrangThai = new JComboBox<>(new String[]{"Đi làm", "Nghỉ phép", "Nghỉ không phép", "Trễ"});
        
        // Thay thế JDateChooser bằng JSpinner
        spinnerNgayCong = createDateSpinner(new Date());
        spinnerTimKiem = createDateSpinner(new Date());
        
        txtTimKiem = createModernTextField("Tìm kiếm theo tên nhân viên...");
        
        // ComboBox tháng năm
        cboThang = new JComboBox<>();
        cboNam = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cboThang.addItem(String.valueOf(i));
        }
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            cboNam.addItem(String.valueOf(i));
        }
        cboNam.setSelectedItem(String.valueOf(currentYear));
        
        btnThem = createModernButton("Thêm Chấm Công", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Chấm Công", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Chấm Công", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiemNgay = createModernButton("Tìm Theo Ngày", PRIMARY_LIGHT, true);
        btnTimKiemNhanVien = createModernButton("Tìm Theo NV", PRIMARY_LIGHT, true);
        btnTimKiemThang = createModernButton("Tìm Theo Tháng", PRIMARY_LIGHT, true);
        
        btnThem.addActionListener(e -> themChamCong());
        btnSua.addActionListener(e -> suaChamCong());
        btnXoa.addActionListener(e -> xoaChamCong());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiemNgay.addActionListener(e -> timKiemTheoNgay());
        btnTimKiemNhanVien.addActionListener(e -> timKiemTheoNhanVien());
        btnTimKiemThang.addActionListener(e -> timKiemTheoThang());
        
        String[] columns = {"Mã Công", "Nhân Viên", "Ngày Công", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableChamCong = new JTable(tableModel);
        setupTable();
        
        styleComboBox(cboNhanVien);
        styleComboBox(cboTrangThai);
        styleComboBox(cboThang);
        styleComboBox(cboNam);
    }
    
    /**
     * Tạo JSpinner để chọn ngày thay thế JDateChooser
     */
    private JSpinner createDateSpinner(Date initialDate) {
        SpinnerDateModel model = new SpinnerDateModel(initialDate, null, null, java.util.Calendar.DAY_OF_MONTH);
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
        btn.setPreferredSize(new Dimension(170, 45));
        return btn;
    }
    
    private void setupTable() {
        tableChamCong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableChamCong.setRowHeight(40);
        tableChamCong.setSelectionBackground(PRIMARY_LIGHT);
        tableChamCong.setSelectionForeground(Color.WHITE);
        tableChamCong.setGridColor(new Color(220, 220, 220));
        tableChamCong.setShowGrid(true);
        
        JTableHeader header = tableChamCong.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        
        tableChamCong.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableChamCong.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableChamCong.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableChamCong.getColumnModel().getColumn(3).setPreferredWidth(180);
        
        tableChamCong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienThiThongTinChamCong();
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
        JLabel lblTitle = new JLabel("CHẤM CÔNG NHÂN VIÊN");
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
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBackground(BG_SECONDARY);
        
        // Row 1: Tìm theo ngày
        JPanel row1 = new JPanel(new BorderLayout(15, 0));
        row1.setBackground(BG_SECONDARY);
        JLabel lbl1 = new JLabel("Tìm kiếm theo ngày:");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl1.setPreferredSize(new Dimension(180, 45));
        row1.add(lbl1, BorderLayout.WEST);
        row1.add(spinnerTimKiem, BorderLayout.CENTER);
        row1.add(btnTimKiemNgay, BorderLayout.EAST);
        
        // Row 2: Tìm theo nhân viên
        JPanel row2 = new JPanel(new BorderLayout(15, 0));
        row2.setBackground(BG_SECONDARY);
        JLabel lbl2 = new JLabel("Tìm kiếm nhân viên:");
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl2.setPreferredSize(new Dimension(180, 45));
        row2.add(lbl2, BorderLayout.WEST);
        row2.add(txtTimKiem, BorderLayout.CENTER);
        row2.add(btnTimKiemNhanVien, BorderLayout.EAST);
        
        // Row 3: Tìm theo tháng/năm
        JPanel row3 = new JPanel(new BorderLayout(15, 0));
        row3.setBackground(BG_SECONDARY);
        JLabel lbl3 = new JLabel("Tìm kiếm theo tháng:");
        lbl3.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl3.setPreferredSize(new Dimension(180, 45));
        
        JPanel monthYearPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        monthYearPanel.setBackground(BG_SECONDARY);
        monthYearPanel.add(cboThang);
        monthYearPanel.add(cboNam);
        
        row3.add(lbl3, BorderLayout.WEST);
        row3.add(monthYearPanel, BorderLayout.CENTER);
        row3.add(btnTimKiemThang, BorderLayout.EAST);
        
        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel[] labels = {
            new JLabel("Nhân Viên *"),
            new JLabel("Ngày Công *"),
            new JLabel("Trạng Thái *")
        };
        
        for (JLabel lbl : labels) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(PRIMARY_DARK);
        }
        
        panel.add(labels[0]);
        panel.add(cboNhanVien);
        panel.add(labels[1]);
        panel.add(spinnerNgayCong);
        panel.add(labels[2]);
        panel.add(cboTrangThai);
        
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
        
        JLabel lblTitle = new JLabel("DANH SÁCH CHẤM CÔNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tableChamCong);
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
    
    private void loadChamCongData() {
        tableModel.setRowCount(0);
        for (ChamCongNhanVien cc : ChamCongNhanVienDAO.getAllChamCong()) {
            tableModel.addRow(new Object[]{
                cc.getMaCong(),
                cc.getHoTenNV(),
                cc.getNgayCong().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                cc.getTrangThai()
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
        
        for (ChamCongNhanVien cc : ChamCongNhanVienDAO.searchByDate(ngay)) {
            tableModel.addRow(new Object[]{
                cc.getMaCong(), cc.getHoTenNV(),
                cc.getNgayCong().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                cc.getTrangThai()
            });
        }
    }
    
    private void timKiemTheoNhanVien() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            loadChamCongData();
            return;
        }
        
        tableModel.setRowCount(0);
        for (ChamCongNhanVien cc : ChamCongNhanVienDAO.searchByNhanVien(keyword)) {
            tableModel.addRow(new Object[]{
                cc.getMaCong(), cc.getHoTenNV(),
                cc.getNgayCong().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                cc.getTrangThai()
            });
        }
    }
    
    private void timKiemTheoThang() {
        int thang = Integer.parseInt((String) cboThang.getSelectedItem());
        int nam = Integer.parseInt((String) cboNam.getSelectedItem());
        
        tableModel.setRowCount(0);
        for (ChamCongNhanVien cc : ChamCongNhanVienDAO.searchByMonthYear(thang, nam)) {
            tableModel.addRow(new Object[]{
                cc.getMaCong(), cc.getHoTenNV(),
                cc.getNgayCong().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                cc.getTrangThai()
            });
        }
    }
    
    private void themChamCong() {
        if (!validateInput()) return;
        
        ChamCongNhanVien cc = new ChamCongNhanVien();
        cc.setMaNV(((ComboItem)cboNhanVien.getSelectedItem()).getId());
        cc.setNgayCong(((Date)spinnerNgayCong.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        cc.setTrangThai((String) cboTrangThai.getSelectedItem());
        
        if (ChamCongNhanVienDAO.insertChamCong(cc)) {
            ChamCongNhanVienDAO.updateTongNgayCong(cc.getMaNV());
            
            JOptionPane.showMessageDialog(this, "✓ Thêm chấm công thành công!");
            clearForm();
            loadChamCongData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi! Nhân viên đã được chấm công trong ngày này!");
        }
    }
    
    private void suaChamCong() {
        if (selectedMaCong <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chấm công cần sửa!");
            return;
        }
        if (!validateInput()) return;
        
        ChamCongNhanVien cc = new ChamCongNhanVien();
        cc.setMaCong(selectedMaCong);
        cc.setMaNV(((ComboItem)cboNhanVien.getSelectedItem()).getId());
        cc.setNgayCong(((Date)spinnerNgayCong.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        cc.setTrangThai((String) cboTrangThai.getSelectedItem());
        
        if (ChamCongNhanVienDAO.updateChamCong(cc)) {
            ChamCongNhanVienDAO.updateTongNgayCong(cc.getMaNV());
            
            JOptionPane.showMessageDialog(this, "✓ Cập nhật chấm công thành công!");
            clearForm();
            loadChamCongData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
        }
    }
    
    private void xoaChamCong() {
        if (selectedMaCong <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chấm công cần xóa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa chấm công này?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int maNV = ((ComboItem)cboNhanVien.getSelectedItem()).getId();
            
            if (ChamCongNhanVienDAO.deleteChamCong(selectedMaCong)) {
                ChamCongNhanVienDAO.updateTongNgayCong(maNV);
                
                JOptionPane.showMessageDialog(this, "✓ Xóa chấm công thành công!");
                clearForm();
                loadChamCongData();
            }
        }
    }
    
    private void hienThiThongTinChamCong() {
        int row = tableChamCong.getSelectedRow();
        if (row >= 0) {
            selectedMaCong = (int) tableModel.getValueAt(row, 0);
            String trangThai = (String) tableModel.getValueAt(row, 3);
            cboTrangThai.setSelectedItem(trangThai);
            
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
        if (spinnerNgayCong.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày công!");
            return false;
        }
        if (cboTrangThai.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái!");
            return false;
        }
        return true;
    }
    
    private void clearForm() {
        if (cboNhanVien.getItemCount() > 0) cboNhanVien.setSelectedIndex(0);
        if (cboTrangThai.getItemCount() > 0) cboTrangThai.setSelectedIndex(0);
        spinnerNgayCong.setValue(new Date());
        spinnerTimKiem.setValue(new Date());
        txtTimKiem.setText("");
        selectedMaCong = -1;
        tableChamCong.clearSelection();
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
        loadChamCongData();
    }
}