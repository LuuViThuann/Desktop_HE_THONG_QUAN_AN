package view;

import dao.ThongKeDAO;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;


public class ThongKeTongQuanForm extends JPanel {

    // ============ MODERN MINIMALIST COLOR PALETTE ============
    private static final Color PRIMARY_BG       = new Color(250, 251, 252);    
    private static final Color CARD_BG          = new Color(255, 255, 255);    
    private static final Color TEXT_PRIMARY     = new Color(31, 41, 55);      
    private static final Color TEXT_SECONDARY   = new Color(107, 114, 128);    
    private static final Color TEXT_LIGHT       = new Color(156, 163, 175);    
    
    private static final Color ACCENT_BLUE      = new Color(59, 130, 246);      
    private static final Color ACCENT_GREEN     = new Color(16, 185, 129);     
    private static final Color ACCENT_ORANGE    = new Color(251, 146, 60);     
    private static final Color ACCENT_RED       = new Color(239, 68, 68);       
    private static final Color ACCENT_PURPLE    = new Color(139, 92, 246);     
    
    private static final Color BORDER_LIGHT     = new Color(229, 231, 235);   
    private static final Color SHADOW           = new Color(0, 0, 0, 8);        

    // Stats Labels
    private JLabel lblDoanhThuHomNay, lblDoanhThuHomQua, lblTangGiam;
    private JLabel lblSoHoaDonHomNay, lblSoHoaDonDangMo, lblGiaTriTrungBinh;
    private JLabel lblTongBan, lblDangSuDung, lblTrong, lblDaDat;

    // Chart Panels
    private JPanel panelThongKeDoanhThu;
    private JPanel panelThongKeMonAn;
    private JPanel panelThongKeBan;
    private JPanel panelThongKeKhuVuc;

    // Formatter
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ThongKeTongQuanForm() {
        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(PRIMARY_BG);

        // Header - MINIMALIST
        add(createMinimalistHeader(), BorderLayout.NORTH);

        // Content
        JPanel panelContent = createMainContentPanel();
        JScrollPane scrollPane = new JScrollPane(panelContent);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setBackground(PRIMARY_BG);
        scrollPane.getViewport().setBackground(PRIMARY_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Footer - MINIMALIST
        add(createMinimalistFooter(), BorderLayout.SOUTH);
    }

    // ==================== MINIMALIST HEADER ====================
    private JPanel createMinimalistHeader() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        // Left - Title
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Thống kê tổng quan");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 24));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblSubtitle = new JLabel("Xem báo cáo kinh doanh hôm nay • " + 
            LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblSubtitle.setFont(new Font("Inter", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftPanel.add(lblTitle);
        leftPanel.add(lblSubtitle);

        // Right - Actions (chỉ có nút Làm mới)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);
        
        JButton btnRefresh = createMinimalistButton("Làm mới", true);
        btnRefresh.addActionListener(e -> loadDashboardData());
        
        rightPanel.add(btnRefresh);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }

    // ==================== MAIN CONTENT ====================
    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PRIMARY_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Section 1: Key Metrics (4 cards)
        JPanel section1 = createSectionWithTitle("Hiệu suất hôm nay", createMetricsRow1());
        section1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.add(section1);
        panel.add(Box.createVerticalStrut(20));
        
        // Section 2: Additional Metrics (4 cards)
        JPanel section2 = createSectionWithTitle("Chi tiết hoạt động", createMetricsRow2());
        section2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panel.add(section2);
        panel.add(Box.createVerticalStrut(20));
        
        // Section 3: Table Stats (2 cards)
        JPanel section3 = createSectionWithTitle("Quản lý bàn", createMetricsRow3());
        section3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        panel.add(section3);
        panel.add(Box.createVerticalStrut(24));

        // Section 4: Charts
        JPanel section4 = createSectionWithTitle("Phân tích dữ liệu", createChartsPanel());
        panel.add(section4);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }

    private JPanel createSectionWithTitle(String title, JPanel content) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }

    // ==================== METRICS ROW 1 (4 CARDS) ====================
    private JPanel createMetricsRow1() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 16, 0));
        panel.setOpaque(false);

        // Card 1: Doanh thu hôm nay
        JPanel card1 = createModernMetricCard("Doanh thu hôm nay", "0đ", ACCENT_BLUE);
        lblDoanhThuHomNay = extractValueLabel(card1);

        // Card 2: Doanh thu hôm qua
        JPanel card2 = createModernMetricCard("Doanh thu hôm qua", "0đ", TEXT_SECONDARY);
        lblDoanhThuHomQua = extractValueLabel(card2);

        // Card 3: Tăng giảm
        JPanel card3 = createModernMetricCard("Tăng/Giảm", "0%", ACCENT_GREEN);
        lblTangGiam = extractValueLabel(card3);

        // Card 4: Số hóa đơn
        JPanel card4 = createModernMetricCard("Hóa đơn hôm nay", "0", ACCENT_ORANGE);
        lblSoHoaDonHomNay = extractValueLabel(card4);

        panel.add(card1);
        panel.add(card2);
        panel.add(card3);
        panel.add(card4);

        return panel;
    }

    // ==================== METRICS ROW 2 (4 CARDS) ====================
    private JPanel createMetricsRow2() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 16, 0));
        panel.setOpaque(false);

        JPanel card1 = createModernMetricCard("Hóa đơn đang mở", "0", ACCENT_ORANGE);
        lblSoHoaDonDangMo = extractValueLabel(card1);

        JPanel card2 = createModernMetricCard("Giá trị trung bình", "0đ", ACCENT_PURPLE);
        lblGiaTriTrungBinh = extractValueLabel(card2);

        JPanel card3 = createModernMetricCard("Bàn trống", "0", ACCENT_GREEN);
        lblTrong = extractValueLabel(card3);

        JPanel card4 = createModernMetricCard("Bàn đang dùng", "0", ACCENT_RED);
        lblDangSuDung = extractValueLabel(card4);

        panel.add(card1);
        panel.add(card2);
        panel.add(card3);
        panel.add(card4);

        return panel;
    }

    // ==================== METRICS ROW 3 (2 CARDS) ====================
    private JPanel createMetricsRow3() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 16, 0));
        panel.setOpaque(false);

        JPanel card1 = createModernMetricCard("Tổng số bàn", "0", ACCENT_BLUE);
        lblTongBan = extractValueLabel(card1);

        JPanel card2 = createModernMetricCard("Bàn đã đặt", "0", ACCENT_ORANGE);
        lblDaDat = extractValueLabel(card2);

        panel.add(card1);
        panel.add(card2);
        
        // Empty panels
        JPanel empty1 = new JPanel();
        empty1.setOpaque(false);
        JPanel empty2 = new JPanel();
        empty2.setOpaque(false);
        
        panel.add(empty1);
        panel.add(empty2);

        return panel;
    }

    /**
     * ✅ MODERN MINIMALIST METRIC CARD - NO ICONS
     */
    private JPanel createModernMetricCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Subtle shadow
                g2d.setColor(SHADOW);
                g2d.fillRoundRect(0, 2, getWidth(), getHeight() - 2, 12, 12);
                
                // Clean white background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Minimal border
                g2d.setColor(BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                // Accent line at top
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawLine(12, 0, getWidth() - 12, 0);
            }
        };

        card.setLayout(new BorderLayout(0, 12));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top: Title only (no icon)
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_SECONDARY);

        // Bottom: Value
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Inter", Font.BOLD, 28));
        lblValue.setForeground(TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private JLabel extractValueLabel(JPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont() != null && label.getFont().getSize() == 28) {
                    return label;
                }
            }
        }
        return new JLabel("0");
    }

    // ==================== CHARTS PANEL ====================
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 16, 16));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 640));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 640));

        panelThongKeDoanhThu = createModernChartCard("Doanh thu tháng này");
        panel.add(panelThongKeDoanhThu);

        panelThongKeMonAn = createModernChartCard("Top món bán chạy");
        panel.add(panelThongKeMonAn);

        panelThongKeBan = createModernChartCard("Tình trạng bàn");
        panel.add(panelThongKeBan);

        panelThongKeKhuVuc = createModernChartCard("Công suất khu vực");
        panel.add(panelThongKeKhuVuc);

        return panel;
    }

    private JPanel createModernChartCard(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(SHADOW);
                g2d.fillRoundRect(0, 2, getWidth(), getHeight() - 2, 12, 12);
                
                // Background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Border
                g2d.setColor(BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };

        panel.setLayout(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY);

        panel.add(lblTitle, BorderLayout.NORTH);

        return panel;
    }

    // ==================== MINIMALIST FOOTER ====================
    private JPanel createMinimalistFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LIGHT));
        panel.setPreferredSize(new Dimension(0, 48));

        JLabel lblFooter = new JLabel("© 2026 Hệ thống quán lẩu - TiTi");
        lblFooter.setFont(new Font("Inter", Font.PLAIN, 11));
        lblFooter.setForeground(TEXT_LIGHT);

        panel.add(lblFooter);
        return panel;
    }

    // ==================== MINIMALIST BUTTON ====================
    private JButton createMinimalistButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text) {
            private boolean isHovered = false;
            
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor;
                Color textColor;
                
                if (isPrimary) {
                    bgColor = isHovered ? new Color(37, 99, 235) : ACCENT_BLUE;
                    textColor = Color.WHITE;
                } else {
                    bgColor = isHovered ? new Color(243, 244, 246) : CARD_BG;
                    textColor = TEXT_PRIMARY;
                    
                    // Border for secondary button
                    g2d.setColor(BORDER_LIGHT);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }

                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setColor(textColor);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };

        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setForeground(isPrimary ? Color.WHITE : TEXT_PRIMARY);
        btn.setBackground(isPrimary ? ACCENT_BLUE : CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        
        return btn;
    }

    // ==================== LOAD DỮ LIỆU ====================
    private void loadDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Object> data = ThongKeDAO.getDashboardData();

                // Doanh thu
                BigDecimal doanhThuHomNay = (BigDecimal) data.get("doanhThuHomNay");
                BigDecimal doanhThuHomQua = (BigDecimal) data.get("doanhThuHomQua");
                BigDecimal tangGiam = (BigDecimal) data.get("tangGiamDoanhThu");

                lblDoanhThuHomNay.setText(currencyFormat.format(doanhThuHomNay));
                lblDoanhThuHomQua.setText(currencyFormat.format(doanhThuHomQua));

                double percent = tangGiam.doubleValue();
                if (percent > 0) {
                    lblTangGiam.setText("↑ " + String.format("%.1f%%", percent));
                    lblTangGiam.setForeground(ACCENT_GREEN);
                } else if (percent < 0) {
                    lblTangGiam.setText("↓ " + String.format("%.1f%%", percent));
                    lblTangGiam.setForeground(ACCENT_RED);
                } else {
                    lblTangGiam.setText("→ 0%");
                    lblTangGiam.setForeground(TEXT_SECONDARY);
                }

                // Hóa đơn
                lblSoHoaDonHomNay.setText(String.valueOf(data.get("soHoaDonHomNay")));
                lblSoHoaDonDangMo.setText(String.valueOf(data.get("soHoaDonDangMo")));
                lblGiaTriTrungBinh.setText(currencyFormat.format(data.get("giaTriTrungBinh")));

                // Bàn
                @SuppressWarnings("unchecked")
                Map<String, Integer> thongKeBan = (Map<String, Integer>) data.get("thongKeBan");
                lblTongBan.setText(String.valueOf(thongKeBan.get("TongBan")));
                lblDangSuDung.setText(String.valueOf(thongKeBan.get("DangSuDung")));
                lblTrong.setText(String.valueOf(thongKeBan.get("Trong")));
                lblDaDat.setText(String.valueOf(thongKeBan.get("DaDat")));

                // Biểu đồ
                loadDoanhThuChart();
                loadTopMonAnChart();
                loadTinhTrangBanTheoKhuVucChart();
                loadCongSuatKhuVucChart();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi tải dữ liệu thống kê: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    // ==================== BIỂU ĐỒ - MINIMALIST STYLE ====================
    private void loadDoanhThuChart() {
        try {
            LocalDate today = LocalDate.now();
            int thang = today.getMonthValue();
            int nam = today.getYear();

            List<Object[]> data = ThongKeDAO.getDoanhThuTheoNgayTrongThang(thang, nam);

            if (data == null || data.isEmpty()) {
                showNoDataMessage(panelThongKeDoanhThu, "Chưa có dữ liệu");
                return;
            }

            List<Integer> days = new ArrayList<>();
            List<Double> revenues = new ArrayList<>();

            for (Object[] row : data) {
                days.add((Integer) row[0]);
                BigDecimal dt = (BigDecimal) row[1];
                revenues.add(dt.doubleValue());
            }

            XYChart chart = new XYChartBuilder()
                    .width(500).height(260)
                    .theme(Styler.ChartTheme.Matlab)
                    .build();

            chart.addSeries("Doanh thu", days, revenues)
                    .setMarker(SeriesMarkers.CIRCLE)
                    .setLineColor(ACCENT_BLUE)
                    .setLineColor(ACCENT_BLUE)
                    .setLineWidth(2.5f);

            // Minimalist styling
            chart.getStyler().setChartBackgroundColor(CARD_BG);
            chart.getStyler().setPlotBackgroundColor(CARD_BG);
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setChartTitleVisible(false);
            chart.getStyler().setAxisTitlesVisible(false);
            chart.getStyler().setPlotGridLinesColor(BORDER_LIGHT);
            chart.getStyler().setDecimalPattern("#,##0 ₫");

            XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
            chartPanel.setBackground(CARD_BG);

            panelThongKeDoanhThu.removeAll();
            panelThongKeDoanhThu.setLayout(new BorderLayout());
            panelThongKeDoanhThu.add(chartPanel, BorderLayout.CENTER);
            panelThongKeDoanhThu.revalidate();
            panelThongKeDoanhThu.repaint();
        } catch (Exception e) {
            showErrorMessage(panelThongKeDoanhThu, "Lỗi tải dữ liệu");
            e.printStackTrace();
        }
    }

    private void loadTopMonAnChart() {
        try {
            LocalDate today = LocalDate.now();
            List<Object[]> data = ThongKeDAO.getTopMonBanChay(5, today, today);

            if (data == null || data.isEmpty()) {
                showNoDataMessage(panelThongKeMonAn, "Chưa có dữ liệu");
                return;
            }

            List<String> monAn = new ArrayList<>();
            List<Double> soLuong = new ArrayList<>();

            for (Object[] row : data) {
                monAn.add((String) row[0]);
                soLuong.add(((Number) row[1]).doubleValue());
            }

            CategoryChart chart = new CategoryChartBuilder()
                    .width(500).height(260)
                    .theme(Styler.ChartTheme.Matlab)
                    .build();

            chart.addSeries("Số lượng", monAn, soLuong)
                    .setFillColor(ACCENT_ORANGE);

            // Minimalist styling
            chart.getStyler().setChartBackgroundColor(CARD_BG);
            chart.getStyler().setPlotBackgroundColor(CARD_BG);
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setChartTitleVisible(false);
            chart.getStyler().setAxisTitlesVisible(false);
            chart.getStyler().setPlotGridLinesColor(BORDER_LIGHT);
            chart.getStyler().setXAxisLabelRotation(45);

            XChartPanel<CategoryChart> chartPanel = new XChartPanel<>(chart);
            chartPanel.setBackground(CARD_BG);

            panelThongKeMonAn.removeAll();
            panelThongKeMonAn.setLayout(new BorderLayout());
            panelThongKeMonAn.add(chartPanel, BorderLayout.CENTER);
            panelThongKeMonAn.revalidate();
            panelThongKeMonAn.repaint();
        } catch (Exception e) {
            showErrorMessage(panelThongKeMonAn, "Lỗi tải dữ liệu");
            e.printStackTrace();
        }
    }

    private void loadTinhTrangBanTheoKhuVucChart() {
        try {
            List<Object[]> data = ThongKeDAO.getThongKeBanTheoKhuVuc();

            if (data == null || data.isEmpty()) {
                showNoDataMessage(panelThongKeBan, "Chưa có dữ liệu");
                return;
            }

            List<String> khuVuc = new ArrayList<>();
            List<Double> dangDung = new ArrayList<>();
            List<Double> trong = new ArrayList<>();

            for (Object[] row : data) {
                khuVuc.add((String) row[0]);
                dangDung.add((double) (int) row[2]);
                trong.add((double) (int) row[3]);
            }

            CategoryChart chart = new CategoryChartBuilder()
                    .width(500).height(260)
                    .theme(Styler.ChartTheme.Matlab)
                    .build();

            chart.getStyler().setStacked(true);

            chart.addSeries("Đang dùng", khuVuc, dangDung)
                    .setFillColor(ACCENT_RED);

            chart.addSeries("Trống", khuVuc, trong)
                    .setFillColor(ACCENT_GREEN);

            // Minimalist styling
            chart.getStyler().setChartBackgroundColor(CARD_BG);
            chart.getStyler().setPlotBackgroundColor(CARD_BG);
            chart.getStyler().setLegendVisible(true);
            chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
            chart.getStyler().setChartTitleVisible(false);
            chart.getStyler().setAxisTitlesVisible(false);
            chart.getStyler().setPlotGridLinesColor(BORDER_LIGHT);
            chart.getStyler().setXAxisLabelRotation(45);

            XChartPanel<CategoryChart> chartPanel = new XChartPanel<>(chart);
            chartPanel.setBackground(CARD_BG);

            panelThongKeBan.removeAll();
            panelThongKeBan.setLayout(new BorderLayout());
            panelThongKeBan.add(chartPanel, BorderLayout.CENTER);
            panelThongKeBan.revalidate();
            panelThongKeBan.repaint();
        } catch (Exception e) {
            showErrorMessage(panelThongKeBan, "Lỗi tải dữ liệu");
            e.printStackTrace();
        }
    }

    private void loadCongSuatKhuVucChart() {
        try {
            List<Object[]> data = ThongKeDAO.getThongKeBanTheoKhuVuc();

            if (data == null || data.isEmpty()) {
                showNoDataMessage(panelThongKeKhuVuc, "Chưa có dữ liệu");
                return;
            }

            List<String> khuVuc = new ArrayList<>();
            List<Double> congSuat = new ArrayList<>();

            for (Object[] row : data) {
                khuVuc.add((String) row[0]);
                congSuat.add((double) row[4]);
            }

            PieChart chart = new PieChartBuilder()
                    .width(500).height(260)
                    .theme(Styler.ChartTheme.Matlab)
                    .build();

            for (int i = 0; i < khuVuc.size(); i++) {
                chart.addSeries(khuVuc.get(i), congSuat.get(i));
            }

            // Minimalist styling
            chart.getStyler().setChartBackgroundColor(CARD_BG);
            chart.getStyler().setPlotBackgroundColor(CARD_BG);
            chart.getStyler().setChartTitleVisible(false);
            chart.getStyler().setLegendVisible(true);
            chart.getStyler().setDecimalPattern("#0.0'%'");
            chart.getStyler().setLabelsVisible(true);

            XChartPanel<PieChart> chartPanel = new XChartPanel<>(chart);
            chartPanel.setBackground(CARD_BG);

            panelThongKeKhuVuc.removeAll();
            panelThongKeKhuVuc.setLayout(new BorderLayout());
            panelThongKeKhuVuc.add(chartPanel, BorderLayout.CENTER);
            panelThongKeKhuVuc.revalidate();
            panelThongKeKhuVuc.repaint();
        } catch (Exception e) {
            showErrorMessage(panelThongKeKhuVuc, "Lỗi tải dữ liệu");
            e.printStackTrace();
        }
    }

    // ==================== HELPER METHODS ====================
    private void showNoDataMessage(JPanel panel, String message) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());

        JLabel lblNoData = new JLabel(message);
        lblNoData.setFont(new Font("Inter", Font.PLAIN, 12));
        lblNoData.setForeground(TEXT_LIGHT);
        lblNoData.setHorizontalAlignment(JLabel.CENTER);
        lblNoData.setVerticalAlignment(JLabel.CENTER);

        panel.add(lblNoData, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void showErrorMessage(JPanel panel, String message) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());

        JLabel lblError = new JLabel(message);
        lblError.setFont(new Font("Inter", Font.PLAIN, 12));
        lblError.setForeground(ACCENT_RED);
        lblError.setHorizontalAlignment(JLabel.CENTER);
        lblError.setVerticalAlignment(JLabel.CENTER);

        panel.add(lblError, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
}