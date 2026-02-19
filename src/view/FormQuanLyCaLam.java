package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import dao.CaLamDAO;
import model.CaLam;

public class FormQuanLyCaLam extends JFrame {

    private JTextField txtTenCa;
    private JTextField txtGioBatDau;
    private JTextField txtGioKetThuc;
    private JTextField txtTimKiem;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JButton btnTimKiem;
    private JTable tableCaLam;
    private DefaultTableModel tableModel;
    private int selectedMaCa = -1;
    
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
    
    public FormQuanLyCaLam() {
        initComponents();
        setupLayout();
        loadCaLamData();
        
        setTitle("Quản Lý Ca Làm");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        txtTenCa = createModernTextField("Nhập tên ca làm...");
        txtGioBatDau = createModernTextField("VD: 08:00:00");
        txtGioKetThuc = createModernTextField("VD: 16:00:00");
        txtTimKiem = createModernTextField("Tìm kiếm ca làm...");
        
        btnThem = createModernButton("Thêm Ca", SUCCESS_GREEN, true);
        btnSua = createModernButton("Sửa Ca", PRIMARY_LIGHT, true);
        btnXoa = createModernButton("Xóa Ca", DANGER_RED, true);
        btnLamMoi = createModernButton("Làm Mới", WARNING_AMBER, true);
        btnTimKiem = createModernButton("Tìm Kiếm", PRIMARY_LIGHT, true);
        
        btnThem.addActionListener(e -> themCaLam());
        btnSua.addActionListener(e -> suaCaLam());
        btnXoa.addActionListener(e -> xoaCaLam());
        btnLamMoi.addActionListener(e -> clearForm());
        btnTimKiem.addActionListener(e -> timKiemCaLam());
        
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiemCaLam();
                }
            }
        });
        
        String[] columns = {"Mã Ca", "Tên Ca", "Giờ Bắt Đầu", "Giờ Kết Thúc"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableCaLam = new JTable(tableModel);
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
        tableCaLam.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCaLam.setRowHeight(40);
        tableCaLam.setSelectionBackground(PRIMARY_LIGHT);
        tableCaLam.setSelectionForeground(Color.WHITE);
        tableCaLam.setGridColor(new Color(220, 220, 220));
        tableCaLam.setShowGrid(true);
        tableCaLam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tableCaLam.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        tableCaLam.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableCaLam.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableCaLam.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableCaLam.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        tableCaLam.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiThongTinCaLam();
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
        
        JLabel lblTitle = new JLabel("QUẢN LÝ CA LÀM");
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
        
        JLabel lblSearch = new JLabel("Tìm kiếm ca làm:");
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
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lblTenCa = new JLabel("Tên Ca *");
        lblTenCa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTenCa.setForeground(PRIMARY_DARK);
        
        JLabel lblGioBatDau = new JLabel("Giờ Bắt Đầu *");
        lblGioBatDau.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGioBatDau.setForeground(PRIMARY_DARK);
        
        JLabel lblGioKetThuc = new JLabel("Giờ Kết Thúc *");
        lblGioKetThuc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGioKetThuc.setForeground(PRIMARY_DARK);
        
        panel.add(lblTenCa);
        panel.add(txtTenCa);
        panel.add(lblGioBatDau);
        panel.add(txtGioBatDau);
        panel.add(lblGioKetThuc);
        panel.add(txtGioKetThuc);
        
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
        
        JLabel lblTitle = new JLabel("DANH SÁCH CA LÀM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        JScrollPane scrollPane = new JScrollPane(tableCaLam);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadCaLamData() {
        tableModel.setRowCount(0);
        
        for (CaLam ca : CaLamDAO.getAllCaLam()) {
            Object[] row = {
                ca.getMaCa(),
                ca.getTenCa(),
                ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            };
            tableModel.addRow(row);
        }
    }
    
    private void timKiemCaLam() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty() || keyword.equals("Tìm kiếm ca làm...")) {
            loadCaLamData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        for (CaLam ca : CaLamDAO.searchCaLam(keyword)) {
            Object[] row = {
                ca.getMaCa(),
                ca.getTenCa(),
                ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            };
            tableModel.addRow(row);
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy ca làm nào!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void themCaLam() {
        if (!validateInput()) {
            return;
        }
        
        CaLam ca = new CaLam();
        ca.setTenCa(txtTenCa.getText().trim());
        ca.setGioBatDau(LocalTime.parse(txtGioBatDau.getText().trim()));
        ca.setGioKetThuc(LocalTime.parse(txtGioKetThuc.getText().trim()));
        
        if (CaLamDAO.insertCaLam(ca)) {
            JOptionPane.showMessageDialog(this,
                "✓ Thêm ca làm thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadCaLamData();
        } else {
            showError("Lỗi khi thêm ca làm!");
        }
    }
    
    private void suaCaLam() {
        if (selectedMaCa <= 0) {
            showError("Vui lòng chọn ca làm cần sửa từ bảng!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        CaLam ca = new CaLam();
        ca.setMaCa(selectedMaCa);
        ca.setTenCa(txtTenCa.getText().trim());
        ca.setGioBatDau(LocalTime.parse(txtGioBatDau.getText().trim()));
        ca.setGioKetThuc(LocalTime.parse(txtGioKetThuc.getText().trim()));
        
        if (CaLamDAO.updateCaLam(ca)) {
            JOptionPane.showMessageDialog(this,
                "✓ Cập nhật ca làm thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            loadCaLamData();
        } else {
            showError("Lỗi khi cập nhật ca làm!");
        }
    }
    
    private void xoaCaLam() {
        if (selectedMaCa <= 0) {
            showError("Vui lòng chọn ca làm cần xóa từ bảng!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa ca làm này?\n⚠️ Lưu ý: Các phân công ca làm liên quan cũng sẽ bị xóa!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (CaLamDAO.deleteCaLam(selectedMaCa)) {
                JOptionPane.showMessageDialog(this,
                    "✓ Xóa ca làm thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                loadCaLamData();
            } else {
                showError("Lỗi khi xóa ca làm!");
            }
        }
    }
    
    private void hienThiThongTinCaLam() {
        int selectedRow = tableCaLam.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMaCa = (int) tableModel.getValueAt(selectedRow, 0);
            String tenCa = (String) tableModel.getValueAt(selectedRow, 1);
            String gioBatDau = (String) tableModel.getValueAt(selectedRow, 2);
            String gioKetThuc = (String) tableModel.getValueAt(selectedRow, 3);
            
            txtTenCa.setText(tenCa);
            txtGioBatDau.setText(gioBatDau);
            txtGioKetThuc.setText(gioKetThuc);
            
            txtTenCa.setForeground(TEXT_DARK);
            txtGioBatDau.setForeground(TEXT_DARK);
            txtGioKetThuc.setForeground(TEXT_DARK);
            
            btnThem.setEnabled(false);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
        }
    }
    
    private boolean validateInput() {
        String tenCa = txtTenCa.getText().trim();
        String gioBatDau = txtGioBatDau.getText().trim();
        String gioKetThuc = txtGioKetThuc.getText().trim();
        
        if (tenCa.isEmpty() || tenCa.equals("Nhập tên ca làm...")) {
            showError("Vui lòng nhập tên ca làm!");
            txtTenCa.requestFocus();
            return false;
        }
        
        if (gioBatDau.isEmpty() || gioBatDau.equals("VD: 08:00:00")) {
            showError("Vui lòng nhập giờ bắt đầu!");
            txtGioBatDau.requestFocus();
            return false;
        }
        
        if (gioKetThuc.isEmpty() || gioKetThuc.equals("VD: 16:00:00")) {
            showError("Vui lòng nhập giờ kết thúc!");
            txtGioKetThuc.requestFocus();
            return false;
        }
        
        try {
            LocalTime.parse(gioBatDau);
        } catch (Exception e) {
            showError("Giờ bắt đầu không đúng định dạng! (VD: 08:00:00)");
            txtGioBatDau.requestFocus();
            return false;
        }
        
        try {
            LocalTime.parse(gioKetThuc);
        } catch (Exception e) {
            showError("Giờ kết thúc không đúng định dạng! (VD: 16:00:00)");
            txtGioKetThuc.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void clearForm() {
        txtTenCa.setText("Nhập tên ca làm...");
        txtGioBatDau.setText("VD: 08:00:00");
        txtGioKetThuc.setText("VD: 16:00:00");
        txtTimKiem.setText("Tìm kiếm ca làm...");
        
        txtTenCa.setForeground(new Color(180, 180, 180));
        txtGioBatDau.setForeground(new Color(180, 180, 180));
        txtGioKetThuc.setForeground(new Color(180, 180, 180));
        txtTimKiem.setForeground(new Color(180, 180, 180));
        
        selectedMaCa = -1;
        txtTenCa.requestFocus();
        tableCaLam.clearSelection();
        
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
        
        loadCaLamData();
    }
}