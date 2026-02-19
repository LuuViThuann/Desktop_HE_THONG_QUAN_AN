package view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import dao.HoaDonDAO;
import model.HoaDon;


public class FormQuanLyHoaDon extends JFrame {

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

    // ============ UI Components ============
    private JTable tableHoaDon;
    private DefaultTableModel modelHoaDon;
    
    // Filter Components
    private JSpinner spinnerDateFrom;
    private JSpinner spinnerDateTo;
    private JComboBox<String> cboLoaiHoaDon;
    private JTextField txtSearch;
    
    // Statistics Labels
    private JLabel lblTongHoaDon;
    private JLabel lblTongDoanhThu;
    private JLabel lblHoaDonHomNay;
    
    // Buttons
    private JButton btnLocNgay;
    private JButton btnHomNay;
    private JButton btnReset;
    private JButton btnXemChiTiet;
    private JButton btnXuatExcel;
    private JButton btnXoa;
    private JButton btnRefresh;

    public FormQuanLyHoaDon() {
        initComponents();
        loadAllHoaDon();
        updateStatistics();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("QUẢN LÝ HÓA ĐƠN - ADMIN");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_MAIN);

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Body (Split: Filter Panel + Table Panel)
        JPanel bodyPanel = createBodyPanel();
        add(bodyPanel, BorderLayout.CENTER);

        // Footer (Statistics)
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    // ========== HEADER PANEL ==========
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), PRIMARY_LIGHT
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setPreferredSize(new Dimension(0, 80));

        // Left: Title + Icon
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(loadIcon("src/Assets/images/list.png", 40, 40));
        if (iconLabel.getIcon() == null) {
            iconLabel.setText("📋");
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 36));
            iconLabel.setForeground(ACCENT_ORANGE);
        }
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("QUẢN LÝ HÓA ĐƠN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Xem, lọc và quản lý hóa đơn khách hàng");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(220, 230, 240));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        
        leftPanel.add(iconLabel);
        leftPanel.add(titlePanel);

        // Right: Action Buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
       
       

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JButton createHeaderButton(String iconPath, String tooltip, Color bgColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        
        ImageIcon icon = loadIcon(iconPath, 24, 24);
        if (icon != null) {
            btn.setIcon(icon);
        } else {
            btn.setText("?");
        }
        
        btn.setPreferredSize(new Dimension(45, 45));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    // ========== BODY PANEL ==========
    private JPanel createBodyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Top: Filter Panel
        JPanel filterPanel = createFilterPanel();
        
        // Center: Table Panel
        JPanel tablePanel = createTablePanel();

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    // ========== FILTER PANEL ==========
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Top Row: Date Filter + Quick Actions
        JPanel topRow = new JPanel(new GridLayout(1, 2, 15, 0));
        topRow.setBackground(BG_SECONDARY);

        // Left: Date Range Filter
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        datePanel.setBackground(BG_SECONDARY);
        
        JLabel lblTuNgay = createLabel("Từ ngày:");
        
     
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        spinnerDateFrom = createStyledDateSpinner(cal.getTime());
        
        JLabel lblDenNgay = createLabel("Đến ngày:");
        
     
        spinnerDateTo = createStyledDateSpinner(new java.util.Date());
        
        btnLocNgay = createFilterButton("Lọc", PRIMARY_LIGHT);
        btnLocNgay.addActionListener(e -> filterByDateRange());
        
        btnHomNay = createFilterButton("Hôm nay", SUCCESS_GREEN);
        btnHomNay.addActionListener(e -> filterToday());
        
        datePanel.add(lblTuNgay);
        datePanel.add(spinnerDateFrom);
        datePanel.add(lblDenNgay);
        datePanel.add(spinnerDateTo);
        datePanel.add(btnLocNgay);
        datePanel.add(btnHomNay);

        // Right: Type Filter + Reset
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        typePanel.setBackground(BG_SECONDARY);
        
        JLabel lblLoai = createLabel("Loại hóa đơn:");
        cboLoaiHoaDon = new JComboBox<>(new String[]{
            "Tất cả", "Tại quán", "Mang về"
        });
        cboLoaiHoaDon.setPreferredSize(new Dimension(140, 32));
        cboLoaiHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboLoaiHoaDon.addActionListener(e -> filterByType());
        
        btnReset = createFilterButton("Reset", WARNING_AMBER);
        btnReset.addActionListener(e -> resetFilters());
        
        typePanel.add(lblLoai);
        typePanel.add(cboLoaiHoaDon);
        typePanel.add(btnReset);

        topRow.add(datePanel);
        topRow.add(typePanel);

        // Bottom Row: Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(BG_SECONDARY);
        
        JLabel lblSearch = createLabel("Tìm kiếm:");
        txtSearch = new JTextField(30);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(300, 32));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton btnSearch = createFilterButton("Tìm", PRIMARY_DARK);
        btnSearch.addActionListener(e -> searchHoaDon());
        
        // Real-time search
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchHoaDon();
                }
            }
        });
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }
    
   
    private JSpinner createStyledDateSpinner(java.util.Date defaultDate) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        
        // Date Editor với format dd/MM/yyyy
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        
        // Styling
        spinner.setPreferredSize(new Dimension(140, 32));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Set border đẹp hơn
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        
        // Set value mặc định
        if (defaultDate != null) {
            spinner.setValue(defaultDate);
        }
        
        // Styling cho text field bên trong
        JComponent editorComponent = spinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defEditor = (JSpinner.DefaultEditor) editorComponent;
            defEditor.getTextField().setBackground(Color.WHITE);
            defEditor.getTextField().setForeground(TEXT_DARK);
            defEditor.getTextField().setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        }
        
        return spinner;
    }

    private JButton createFilterButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        
        btn.setPreferredSize(new Dimension(90, 32));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
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

    // ========== TABLE PANEL ==========
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Header
        JPanel headerTable = new JPanel(new BorderLayout());
        headerTable.setBackground(BG_SECONDARY);
        
        JLabel titleTable = new JLabel("DANH SÁCH HÓA ĐƠN");
        titleTable.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleTable.setForeground(TEXT_DARK);
        
        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBackground(BG_SECONDARY);
        
        btnXemChiTiet = createActionButton("Xem chi tiết", PRIMARY_LIGHT);
        btnXemChiTiet.addActionListener(e -> viewDetail());
        
        btnXoa = createActionButton("Xóa", DANGER_RED);
        btnXoa.addActionListener(e -> deleteHoaDon());
        
        actionPanel.add(btnXemChiTiet);
        actionPanel.add(btnXoa);
        
        headerTable.add(titleTable, BorderLayout.WEST);
        headerTable.add(actionPanel, BorderLayout.EAST);

        // Table
        String[] columns = {
            "Mã HĐ", "Ngày", "Mã Bàn", "Số Món", "Tổng Tiền", "Giảm (%)", "Thanh Toán", "Loại"
        };
        
        modelHoaDon = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableHoaDon = new JTable(modelHoaDon);
        tableHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableHoaDon.setForeground(TEXT_DARK);
        tableHoaDon.setBackground(Color.WHITE);
        tableHoaDon.setRowHeight(35);
        tableHoaDon.setShowGrid(true);
        tableHoaDon.setGridColor(new Color(230, 230, 230));
        tableHoaDon.setSelectionBackground(new Color(70, 130, 180, 30));
        tableHoaDon.setSelectionForeground(TEXT_DARK);
        tableHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Header styling
        JTableHeader header = tableHoaDon.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(240, 245, 250));
        header.setForeground(TEXT_DARK);
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 35));
        
        // Column widths
        tableHoaDon.getColumnModel().getColumn(0).setPreferredWidth(80);  // Mã HĐ
        tableHoaDon.getColumnModel().getColumn(1).setPreferredWidth(100); // Ngày
        tableHoaDon.getColumnModel().getColumn(2).setPreferredWidth(80);  // Mã Bàn
        tableHoaDon.getColumnModel().getColumn(3).setPreferredWidth(80);  // Số Món
        tableHoaDon.getColumnModel().getColumn(4).setPreferredWidth(120); // Tổng Tiền
        tableHoaDon.getColumnModel().getColumn(5).setPreferredWidth(80);  // Giảm
        tableHoaDon.getColumnModel().getColumn(6).setPreferredWidth(120); // Thanh Toán
        tableHoaDon.getColumnModel().getColumn(7).setPreferredWidth(100); // Loại
        
        // Center alignment for numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableHoaDon.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableHoaDon.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableHoaDon.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableHoaDon.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        // Right alignment for money columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableHoaDon.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableHoaDon.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        
        // Double-click to view detail
        tableHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewDetail();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);

        panel.add(headerTable, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        
        btn.setPreferredSize(new Dimension(110, 32));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
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

    // ========== FOOTER PANEL (Statistics) ==========
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(new Color(60, 70, 85));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        panel.setPreferredSize(new Dimension(0, 70));

        // Tổng hóa đơn
        lblTongHoaDon = createStatLabel("0", "Tổng hóa đơn", PRIMARY_LIGHT);
        
        // Tổng doanh thu
        lblTongDoanhThu = createStatLabel("0 VND", "Tổng doanh thu", SUCCESS_GREEN);
        
        // Hóa đơn hôm nay
        lblHoaDonHomNay = createStatLabel("0", "Hóa đơn hôm nay", WARNING_AMBER);

        panel.add(lblTongHoaDon);
        panel.add(lblTongDoanhThu);
        panel.add(lblHoaDonHomNay);

        return panel;
    }

    private JLabel createStatLabel(String value, String description, Color color) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(60, 70, 85));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(color);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(new Color(180, 180, 180));
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        container.add(lblValue);
        container.add(Box.createVerticalStrut(5));
        container.add(lblDesc);
        
        JLabel wrapper = new JLabel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(container, BorderLayout.CENTER);
        
        return lblValue; // Return the value label for updates
    }

    // ========== DATA LOADING ==========
    private void loadAllHoaDon() {
        List<HoaDon> list = HoaDonDAO.getAllHoaDon();
        displayHoaDonList(list);
    }

    private void displayHoaDonList(List<HoaDon> list) {
        modelHoaDon.setRowCount(0);
        
        for (HoaDon hd : list) {
            Object[] row = {
                hd.getMaCTHD(),
                hd.getNgayThanhToan() != null ? 
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(hd.getNgayThanhToan()) : "",
                hd.getMaBan(),
                hd.getTongSoLuongMon(),
                formatMoney(hd.getTongTienThanhToan()),
                hd.getPhanTramGiamGia() != null ? hd.getPhanTramGiamGia() + "%" : "0%",
                formatMoney(hd.getTongTienThanhToan()),
                hd.getLoaiHoaDon() != null ? hd.getLoaiHoaDon() : "Tại quán"
            };
            modelHoaDon.addRow(row);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 VND";
        return String.format("%,d VND", amount.longValue());
    }

    // ========== FILTER ACTIONS ==========
    private void filterByDateRange() {
        java.util.Date fromDateUtil = (java.util.Date) spinnerDateFrom.getValue();
        java.util.Date toDateUtil = (java.util.Date) spinnerDateTo.getValue();
        
        if (fromDateUtil == null || toDateUtil == null) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Kiểm tra logic ngày
        if (fromDateUtil.after(toDateUtil)) {
            JOptionPane.showMessageDialog(this,
                "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Date fromDate = new Date(fromDateUtil.getTime());
        Date toDate = new Date(toDateUtil.getTime());
        
        List<HoaDon> list = HoaDonDAO.getHoaDonByDateRange(fromDate, toDate);
        displayHoaDonList(list);
        updateStatistics();
    }

    private void filterToday() {
        List<HoaDon> list = HoaDonDAO.getHoaDonToday();
        displayHoaDonList(list);
        updateStatistics();
    }

    private void filterByType() {
        String loai = (String) cboLoaiHoaDon.getSelectedItem();
        
        if ("Tất cả".equals(loai)) {
            loadAllHoaDon();
        } else {
            List<HoaDon> list = HoaDonDAO.getHoaDonByLoai(loai);
            displayHoaDonList(list);
        }
        
        updateStatistics();
    }

    private void searchHoaDon() {
        String keyword = txtSearch.getText().trim();
        
        if (keyword.isEmpty()) {
            loadAllHoaDon();
            return;
        }
        
        List<HoaDon> list = HoaDonDAO.searchHoaDon(keyword);
        displayHoaDonList(list);
        updateStatistics();
    }

    private void resetFilters() {
        // Reset date range: 7 ngày trước đến hôm nay
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        spinnerDateFrom.setValue(cal.getTime());
        spinnerDateTo.setValue(new java.util.Date());
        
        cboLoaiHoaDon.setSelectedIndex(0);
        txtSearch.setText("");
        loadAllHoaDon();
        updateStatistics();
    }

    // ========== ACTIONS ==========
    private void viewDetail() {
        int selectedRow = tableHoaDon.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn hóa đơn cần xem!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int maCTHD = (Integer) modelHoaDon.getValueAt(selectedRow, 0);
        HoaDon hoaDon = HoaDonDAO.getHoaDonById(maCTHD);
        
        if (hoaDon != null) {
            showDetailDialog(hoaDon);
        }
    }

    private void showDetailDialog(HoaDon hoaDon) {
        JDialog dialog = new JDialog(this, "CHI TIẾT HÓA ĐƠN #" + hoaDon.getMaCTHD(), true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(BG_SECONDARY);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setBackground(PRIMARY_DARK);
        
        JLabel titleLabel = new JLabel("THÔNG TIN HÓA ĐƠN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_SECONDARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Info fields
        contentPanel.add(createInfoRow("Mã hóa đơn:", "#" + hoaDon.getMaCTHD()));
        contentPanel.add(createInfoRow("Ngày thanh toán:", 
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hoaDon.getNgayThanhToan())));
        contentPanel.add(createInfoRow("Mã bàn:", "Bàn " + hoaDon.getMaBan()));
        contentPanel.add(createInfoRow("Loại hóa đơn:", hoaDon.getLoaiHoaDon()));
        contentPanel.add(createInfoRow("Tổng số món:", String.valueOf(hoaDon.getTongSoLuongMon())));
        contentPanel.add(createInfoRow("Giảm giá:", hoaDon.getPhanTramGiamGia() + "%"));
        contentPanel.add(createInfoRow("Tổng tiền:", formatMoney(hoaDon.getTongTienThanhToan())));
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Chi tiết món
        JLabel lblMon = new JLabel("CHI TIẾT MÓN ĂN:");
        lblMon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMon.setForeground(TEXT_DARK);
        lblMon.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblMon);
        contentPanel.add(Box.createVerticalStrut(5));
        
        List<String> danhSachMon = HoaDonDAO.getChiTietMonAnByHoaDon(hoaDon.getMaCTHD());
        
        JTextArea txtMon = new JTextArea(10, 40);
        txtMon.setEditable(false);
        txtMon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMon.setBackground(new Color(248, 250, 252));
        txtMon.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        StringBuilder sb = new StringBuilder();
        for (String mon : danhSachMon) {
            sb.append("• ").append(mon).append("\n");
        }
        txtMon.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(txtMon);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(scrollPane);

        // Footer buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footerPanel.setBackground(BG_SECONDARY);
        
        JButton btnClose = createActionButton("Đóng", PRIMARY_LIGHT);
        btnClose.addActionListener(e -> dialog.dispose());
        footerPanel.add(btnClose);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(BG_SECONDARY);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLabel.setForeground(TEXT_DARK);
        lblLabel.setPreferredSize(new Dimension(150, 20));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblValue.setForeground(TEXT_LIGHT);
        
        panel.add(lblLabel);
        panel.add(lblValue);
        
        return panel;
    }

    private void deleteHoaDon() {
        int selectedRow = tableHoaDon.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn hóa đơn cần xóa!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int maCTHD = (Integer) modelHoaDon.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hóa đơn #" + maCTHD + "?\n" +
            "Hành động này không thể hoàn tác!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (HoaDonDAO.deleteHoaDon(maCTHD)) {
                JOptionPane.showMessageDialog(this,
                    "Xóa hóa đơn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa hóa đơn!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToExcel() {
        JOptionPane.showMessageDialog(this,
            "Chức năng xuất Excel đang được phát triển!\n" +
            "Sẽ sớm có trong phiên bản tiếp theo.",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadAllHoaDon();
        updateStatistics();
        JOptionPane.showMessageDialog(this,
            "Đã làm mới dữ liệu!",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== STATISTICS ==========
    private void updateStatistics() {
        int rowCount = tableHoaDon.getRowCount();
        lblTongHoaDon.setText(String.valueOf(rowCount));
        
        // Tính tổng doanh thu từ table hiện tại
        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        for (int i = 0; i < rowCount; i++) {
            String moneyStr = (String) modelHoaDon.getValueAt(i, 6);
            String cleanStr = moneyStr.replaceAll("[^0-9]", "");
            if (!cleanStr.isEmpty()) {
                tongDoanhThu = tongDoanhThu.add(new BigDecimal(cleanStr));
            }
        }
        lblTongDoanhThu.setText(formatMoney(tongDoanhThu));
        
        // Hóa đơn hôm nay
        int hoaDonHomNay = HoaDonDAO.countHoaDonToday();
        lblHoaDonHomNay.setText(String.valueOf(hoaDonHomNay));
    }

    // ========== HELPER ==========
    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(path));
            java.awt.Image scaledImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            return null;
        }
    }

    // ========== MAIN (For Testing) ==========
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new FormQuanLyHoaDon().setVisible(true);
        });
    }
}