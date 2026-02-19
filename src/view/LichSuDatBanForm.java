package view;

import dao.DatBanTruocDAO;
import model.DatBanTruoc;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Calendar;


public class LichSuDatBanForm extends JPanel {
    
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
    
    private JTable tableLichSu;
    private DefaultTableModel tableModel;
    private JTextField txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboLocTheo;
    private JSpinner spinnerTuNgay;
    private JSpinner spinnerDenNgay;
    private JButton btnXemChiTiet, btnXacNhanDaDen, btnHuyDat, btnLamMoi;
    private JButton btnTimKiem, btnLoc;
    private JLabel lblTongSo;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public LichSuDatBanForm() {
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(BG_MAIN);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top Panel - Filters
        JPanel topPanel = createFilterPanel();
        mainContainer.add(topPanel, BorderLayout.NORTH);
        
        // Center Panel - Table
        JPanel centerPanel = createTablePanel();
        mainContainer.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Stats
        JPanel bottomPanel = createStatsPanel();
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        
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
        
        JLabel lblTitle = new JLabel("LỊCH SỬ ĐẶT BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        panel.add(lblTitle, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Row 1 - Search and Status
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row1.setBackground(BG_SECONDARY);
        
        JLabel lblTimKiem = new JLabel("Tìm kiếm:");
        lblTimKiem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTimKiem.setForeground(PRIMARY_DARK);
        
        txtTimKiem = createModernTextField("Nhập tên khách hàng hoặc SĐT...");
        txtTimKiem.setPreferredSize(new Dimension(250, 45));
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timKiem();
                }
            }
        });
        
        btnTimKiem = createModernButton("Tìm kiếm", PRIMARY_LIGHT, true);
        btnTimKiem.addActionListener(e -> timKiem());
        
        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTrangThai.setForeground(PRIMARY_DARK);
        
        cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Đã đặt", "Đã đến", "Đã hủy"
        });
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboTrangThai.setPreferredSize(new Dimension(150, 45));
        cboTrangThai.addActionListener(e -> locTheoTrangThai());
        
        row1.add(lblTimKiem);
        row1.add(txtTimKiem);
        row1.add(btnTimKiem);
        row1.add(Box.createHorizontalStrut(20));
        row1.add(lblTrangThai);
        row1.add(cboTrangThai);
        
        // Row 2 - Date Range Filter
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row2.setBackground(BG_SECONDARY);
        
        JLabel lblLocTheo = new JLabel("Lọc theo:");
        lblLocTheo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLocTheo.setForeground(PRIMARY_DARK);
        
        cboLocTheo = new JComboBox<>(new String[]{
            "Tất cả", "Hôm nay", "Tuần này", "Tháng này", "Tùy chọn"
        });
        cboLocTheo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboLocTheo.setPreferredSize(new Dimension(150, 45));
        cboLocTheo.addActionListener(e -> locTheoThoiGian());
        
        JLabel lblTuNgay = new JLabel("Từ ngày:");
        lblTuNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTuNgay.setForeground(PRIMARY_DARK);
        
        spinnerTuNgay = createDateSpinner();
        spinnerTuNgay.setEnabled(false);
        
        JLabel lblDenNgay = new JLabel("Đến ngày:");
        lblDenNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDenNgay.setForeground(PRIMARY_DARK);
        
        spinnerDenNgay = createDateSpinner();
        spinnerDenNgay.setEnabled(false);
        
        btnLoc = createModernButton("Lọc", SUCCESS_GREEN, true);
        btnLoc.addActionListener(e -> locTheoKhoangThoiGian());
        
        JButton btnReset = createModernButton("Reset", new Color(149, 165, 166), true);
        btnReset.addActionListener(e -> resetFilter());
        
        row2.add(lblLocTheo);
        row2.add(cboLocTheo);
        row2.add(Box.createHorizontalStrut(10));
        row2.add(lblTuNgay);
        row2.add(spinnerTuNgay);
        row2.add(lblDenNgay);
        row2.add(spinnerDenNgay);
        row2.add(btnLoc);
        row2.add(btnReset);
        
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 15));
        mainPanel.setBackground(BG_SECONDARY);
        mainPanel.add(row1);
        mainPanel.add(row2);
        
        panel.add(mainPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.setPreferredSize(new Dimension(120, 45));
        
        // Style spinner
        JComponent spinnerEditor = spinner.getEditor();
        JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) spinnerEditor;
        defaultEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
        defaultEditor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        return spinner;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel lblTitle = new JLabel("DANH SÁCH ĐẶT BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_DARK);
        
        // Table
        String[] columns = {
            "Mã", "Khách hàng", "SĐT", "Số khách", 
            "Ngày đặt", "Giờ đặt", "Bàn", "Khu vực", "Trạng thái"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableLichSu = new JTable(tableModel);
        setupTable();
        
        JScrollPane scrollPane = new JScrollPane(tableLichSu);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(BG_SECONDARY);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        btnXemChiTiet = createModernButton("Xem chi tiết", PRIMARY_LIGHT, true);
        btnXemChiTiet.addActionListener(e -> xemChiTiet());
        
        btnXacNhanDaDen = createModernButton("Xác nhận đã đến", SUCCESS_GREEN, true);
        btnXacNhanDaDen.addActionListener(e -> xacNhanDaDen());
        
        btnHuyDat = createModernButton("Hủy đặt bàn", DANGER_RED, true);
        btnHuyDat.addActionListener(e -> huyDatBan());
        
        btnLamMoi = createModernButton("Làm mới", WARNING_AMBER, true);
        btnLamMoi.addActionListener(e -> loadData());
        
        buttonPanel.add(btnXemChiTiet);
        buttonPanel.add(btnXacNhanDaDen);
        buttonPanel.add(btnHuyDat);
        buttonPanel.add(btnLamMoi);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupTable() {
        tableLichSu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableLichSu.setRowHeight(40);
        tableLichSu.setSelectionBackground(PRIMARY_LIGHT);
        tableLichSu.setSelectionForeground(Color.WHITE);
        tableLichSu.setGridColor(new Color(220, 220, 220));
        tableLichSu.setShowGrid(true);
        tableLichSu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = tableLichSu.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(BG_SECONDARY);
        header.setForeground(PRIMARY_DARK);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        // Set column widths
        tableLichSu.getColumnModel().getColumn(0).setPreferredWidth(50);   // Mã
        tableLichSu.getColumnModel().getColumn(1).setPreferredWidth(150);  // Tên
        tableLichSu.getColumnModel().getColumn(2).setPreferredWidth(100);  // SĐT
        tableLichSu.getColumnModel().getColumn(3).setPreferredWidth(80);   // Số khách
        tableLichSu.getColumnModel().getColumn(4).setPreferredWidth(100);  // Ngày
        tableLichSu.getColumnModel().getColumn(5).setPreferredWidth(80);   // Giờ
        tableLichSu.getColumnModel().getColumn(6).setPreferredWidth(80);   // Bàn
        tableLichSu.getColumnModel().getColumn(7).setPreferredWidth(100);  // Khu vực
        tableLichSu.getColumnModel().getColumn(8).setPreferredWidth(100);  // Trạng thái
        
        // Custom cell renderer for status colors
        tableLichSu.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String trangThai = (String) table.getModel().getValueAt(row, 8);
                    switch (trangThai) {
                        case "Đã đặt":
                            c.setBackground(new Color(255, 243, 205));
                            break;
                        case "Đã đến":
                            c.setBackground(new Color(212, 237, 218));
                            break;
                        case "Đã hủy":
                            c.setBackground(new Color(248, 215, 218));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }
                
                return c;
            }
        });
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        lblTongSo = new JLabel("Tổng số: 0 đặt bàn");
        lblTongSo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongSo.setForeground(PRIMARY_DARK);
        
        panel.add(lblTongSo);
        
        return panel;
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
        textField.setCaretColor(PRIMARY_LIGHT);
        textField.setBorder(new EmptyBorder(12, 16, 12, 16));
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
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 45));
        
        return btn;
    }
    
    // ==================== XỬ LÝ SỰ KIỆN ====================
    
    private void loadData() {
        List<DatBanTruoc> list = DatBanTruocDAO.getAllDatBan();
        displayData(list);
    }
    
    private void displayData(List<DatBanTruoc> list) {
        tableModel.setRowCount(0);
        
        for (DatBanTruoc db : list) {
            Object[] row = {
                db.getMaDatBan(),
                db.getHoTenKhachHang(),
                db.getSdt(),
                db.getSoLuongKhach() + " khách",
                db.getNgayDat().toLocalDate().format(dateFormatter),
                db.getGioDat().toString(),
                db.getTenBan(),
                db.getTenKV(),
                db.getTrangThai()
            };
            tableModel.addRow(row);
        }
        
        lblTongSo.setText("Tổng số: " + list.size() + " đặt bàn");
    }
    
    private void timKiem() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty() || keyword.equals("Nhập tên khách hàng hoặc SĐT...")) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập từ khóa tìm kiếm!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<DatBanTruoc> list = DatBanTruocDAO.searchDatBan(keyword);
        displayData(list);
    }
    
    private void locTheoTrangThai() {
        String trangThai = (String) cboTrangThai.getSelectedItem();
        
        if ("Tất cả".equals(trangThai)) {
            loadData();
        } else {
            List<DatBanTruoc> list = DatBanTruocDAO.getDatBanByTrangThai(trangThai);
            displayData(list);
        }
    }
    
    private void locTheoThoiGian() {
        String loaiLoc = (String) cboLocTheo.getSelectedItem();
        
        if ("Tùy chọn".equals(loaiLoc)) {
            spinnerTuNgay.setEnabled(true);
            spinnerDenNgay.setEnabled(true);
            return;
        } else {
            spinnerTuNgay.setEnabled(false);
            spinnerDenNgay.setEnabled(false);
        }
        
        LocalDate today = LocalDate.now();
        List<DatBanTruoc> list = null;
        
        switch (loaiLoc) {
            case "Hôm nay":
                list = DatBanTruocDAO.getDatBanByDate(today);
                break;
            case "Tuần này":
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                list = DatBanTruocDAO.getDatBanByDateRange(startOfWeek, today);
                break;
            case "Tháng này":
                LocalDate startOfMonth = today.withDayOfMonth(1);
                list = DatBanTruocDAO.getDatBanByDateRange(startOfMonth, today);
                break;
            default:
                loadData();
                return;
        }
        
        displayData(list);
    }
    
    private void locTheoKhoangThoiGian() {
        Date tuNgayDate = (Date) spinnerTuNgay.getValue();
        Date denNgayDate = (Date) spinnerDenNgay.getValue();
        
        LocalDate tuNgay = tuNgayDate.toInstant()
                                      .atZone(ZoneId.systemDefault())
                                      .toLocalDate();
        LocalDate denNgay = denNgayDate.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate();
        
        List<DatBanTruoc> list = DatBanTruocDAO.getDatBanByDateRange(tuNgay, denNgay);
        displayData(list);
    }
    
    private void resetFilter() {
        txtTimKiem.setText("Nhập tên khách hàng hoặc SĐT...");
        txtTimKiem.setForeground(new Color(180, 180, 180));
        cboTrangThai.setSelectedIndex(0);
        cboLocTheo.setSelectedIndex(0);
        spinnerTuNgay.setValue(new Date());
        spinnerDenNgay.setValue(new Date());
        spinnerTuNgay.setEnabled(false);
        spinnerDenNgay.setEnabled(false);
        loadData();
    }
    
    private void xemChiTiet() {
        int selectedRow = tableLichSu.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một đặt bàn để xem chi tiết!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int maDatBan = (int) tableModel.getValueAt(selectedRow, 0);
        DatBanTruoc datBan = DatBanTruocDAO.getDatBanById(maDatBan);
        
        if (datBan != null) {
            String info = String.format(
                "=== THÔNG TIN ĐẶT BÀN ===\n\n" +
                "Mã đặt bàn: %d\n" +
                "Khách hàng: %s\n" +
                "Số điện thoại: %s\n" +
                "Số lượng khách: %d người\n" +
                "Ngày đặt: %s\n" +
                "Giờ đặt: %s\n" +
                "Bàn: %s\n" +
                "Khu vực: %s\n" +
                "Trạng thái: %s",
                datBan.getMaDatBan(),
                datBan.getHoTenKhachHang(),
                datBan.getSdt(),
                datBan.getSoLuongKhach(),
                datBan.getNgayDat().toLocalDate().format(dateFormatter),
                datBan.getGioDat(),
                datBan.getTenBan(),
                datBan.getTenKV(),
                datBan.getTrangThai()
            );
            
            JOptionPane.showMessageDialog(this, info, "Chi tiết đặt bàn", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void xacNhanDaDen() {
        int selectedRow = tableLichSu.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một đặt bàn!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String trangThai = (String) tableModel.getValueAt(selectedRow, 8);
        if (!"Đã đặt".equals(trangThai)) {
            JOptionPane.showMessageDialog(this, 
                "Chỉ có thể xác nhận đặt bàn có trạng thái 'Đã đặt'!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận khách hàng đã đến?", 
            "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int maDatBan = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (DatBanTruocDAO.xacNhanKhachDen(maDatBan)) {
                JOptionPane.showMessageDialog(this, 
                    "✓ Xác nhận thành công! Bàn đã chuyển sang trạng thái 'Đang sử dụng'", 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Xác nhận thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void huyDatBan() {
        int selectedRow = tableLichSu.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một đặt bàn!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String trangThai = (String) tableModel.getValueAt(selectedRow, 8);
        if (!"Đã đặt".equals(trangThai)) {
            JOptionPane.showMessageDialog(this, 
                "Chỉ có thể hủy đặt bàn có trạng thái 'Đã đặt'!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận hủy đặt bàn này?", 
            "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int maDatBan = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (DatBanTruocDAO.updateTrangThai(maDatBan, "Đã hủy")) {
                JOptionPane.showMessageDialog(this, 
                    "✓ Hủy đặt bàn thành công!", 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Hủy đặt bàn thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}