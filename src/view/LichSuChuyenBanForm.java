package view;

import config.DatabaseConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import config.UserSession;
import model.NhanVien;
/**
 * LichSuChuyenBanForm — Panel hiển thị lịch sử chuyển bàn & gộp bàn.
 * Nhúng vào MainFrame qua switchToPanel() hoặc mở độc lập như JFrame con.
 */
public class LichSuChuyenBanForm extends JPanel {

    // ── Màu sắc đồng nhất với hệ thống ────────────────────────────────────────
    private static final Color PRIMARY_DARK   = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT  = new Color(70, 130, 180);
    private static final Color ACCENT_ORANGE  = new Color(230, 126, 34);
    private static final Color SUCCESS_GREEN  = new Color(46, 152, 102);
    private static final Color DANGER_RED     = new Color(192, 57, 43);
    private static final Color WARNING_AMBER  = new Color(241, 196, 15);
    private static final Color BG_MAIN        = new Color(241, 244, 247);
    private static final Color BG_SECONDARY   = new Color(255, 255, 255);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color BORDER_COLOR   = new Color(189, 195, 199);

    // ── Bảng dữ liệu ──────────────────────────────────────────────────────────
    private JTable              table;
    private DefaultTableModel   tableModel;

    // ── Bộ lọc ────────────────────────────────────────────────────────────────
    private JTextField          txtSearch;
    private JComboBox<String>   cboLoaiThaoTac;   // Tất cả / Chuyển bàn / Gộp bàn
    private JSpinner            spnTuNgay;
    private JSpinner            spnDenNgay;
    private JComboBox<String>   cboNhanVien;

    // ── Thống kê nhanh ────────────────────────────────────────────────────────
    private JLabel lblTongLuot;
    private JLabel lblChuyenBan;
    private JLabel lblGopBan;
    private JLabel lblHomNay;

    // ── Formatter ─────────────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Cột bảng ──────────────────────────────────────────────────────────────
    private static final String[] COLUMNS = {
        "Mã", "Bàn Cũ", "Bàn Mới", "Khu Vực Cũ", "Khu Vực Mới",
        "Loại Thao Tác", "NV Thực Hiện", "Ngày", "Ghi Chú"
    };

    // ======================================================================
    public LichSuChuyenBanForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildCenter(),     BorderLayout.CENTER);

        loadData();
    }

    // ── HEADER ─────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, new Color(35, 70, 130));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        hdr.setOpaque(false);
        hdr.setPreferredSize(new Dimension(0, 68));
        hdr.setLayout(new BorderLayout(0, 0));
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Bên trái: icon + tiêu đề
        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        leftTitle.setOpaque(false);

        JLabel lblIcon = new JLabel("📋");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblIcon.setForeground(Color.WHITE);

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 1));
        titleStack.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH SỬ CHUYỂN BÀN & GỘP BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Theo dõi toàn bộ lịch sử di chuyển và gộp bàn trong hệ thống");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(190, 215, 245));

        titleStack.add(lblTitle);
        titleStack.add(lblSub);

        leftTitle.add(lblIcon);
        leftTitle.add(titleStack);

        // Bên phải: nút làm mới
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 18));
        rightBtns.setOpaque(false);

     // Kiểm tra quyền Admin (MaPQ == 1)
        NhanVien currentUser = UserSession.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getMaPQ() == 1;

        if (isAdmin) {
            JButton btnXoa = buildHdrBtn("Xóa Dòng", DANGER_RED);
            btnXoa.addActionListener(e -> xoaDongDaChon());
            rightBtns.add(btnXoa);
        }
        
        JButton btnRefresh = buildHdrBtn("Làm Mới", new Color(70, 130, 180));
        JButton btnExport  = buildHdrBtn("Xuất CSV", new Color(46, 152, 102));

        btnRefresh.addActionListener(e -> loadData());
        btnExport.addActionListener(e -> exportCSV());

        rightBtns.add(btnExport);
        rightBtns.add(btnRefresh);

        hdr.add(leftTitle,  BorderLayout.WEST);
        hdr.add(rightBtns,  BorderLayout.EAST);
        return hdr;
    }

    // ── CENTER (filter + stats + table) ────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(BG_MAIN);
        center.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        center.add(buildStatsRow(),  BorderLayout.NORTH);
        center.add(buildFilterBar(), BorderLayout.CENTER);   // filter nằm giữa
        center.add(buildTablePane(), BorderLayout.SOUTH);    // table chiếm phần còn lại

        // Dùng BorderLayout thứ 2 để table chiếm phần lớn
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(BG_MAIN);
        wrapper.add(buildStatsRow(),  BorderLayout.NORTH);

        JPanel midBottom = new JPanel(new BorderLayout(0, 8));
        midBottom.setBackground(BG_MAIN);
        midBottom.add(buildFilterBar(), BorderLayout.NORTH);
        midBottom.add(buildTablePane(), BorderLayout.CENTER);

        wrapper.add(midBottom, BorderLayout.CENTER);
        return wrapper;
    }

    // ── STATS ROW ──────────────────────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setBackground(BG_MAIN);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setPreferredSize(new Dimension(0, 85));

        lblTongLuot  = new JLabel("0");
        lblChuyenBan = new JLabel("0");
        lblGopBan    = new JLabel("0");
        lblHomNay    = new JLabel("0");

        row.add(buildStatCard("Tổng Lượt",    lblTongLuot,  PRIMARY_LIGHT));
        row.add(buildStatCard("Chuyển Bàn",   lblChuyenBan, SUCCESS_GREEN));
        row.add(buildStatCard("Gộp Bàn",      lblGopBan,    ACCENT_ORANGE));
        row.add(buildStatCard("Hôm Nay",      lblHomNay,    new Color(142, 68, 173)));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight() - 3, getWidth(), getHeight() - 3);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 228, 238), 1),
            BorderFactory.createEmptyBorder(10, 14, 8, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_LIGHT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        valueLabel.setHorizontalAlignment(JLabel.RIGHT);

        card.add(lblTitle,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── FILTER BAR ─────────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(BG_SECONDARY);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        // Tìm kiếm
        JLabel lblSearch = new JLabel("Tìm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearch = new JTextField(16);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(180, 30));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(3, 7, 3, 7)
        ));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilter(); }
        });

        // Loại thao tác
        JLabel lblLoai = new JLabel("Loại:");
        lblLoai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboLoaiThaoTac = new JComboBox<>(new String[]{"Tất cả", "Chuyển bàn", "Gộp bàn"});
        cboLoaiThaoTac.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboLoaiThaoTac.setPreferredSize(new Dimension(130, 30));
        cboLoaiThaoTac.addActionListener(e -> applyFilter());

        // Nhân viên
        JLabel lblNV = new JLabel("NV:");
        lblNV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboNhanVien.setPreferredSize(new Dimension(155, 30));
        loadNhanVienCombo();
        cboNhanVien.addActionListener(e -> applyFilter());

        // Từ ngày
        JLabel lblTu = new JLabel("Từ ngày:");
        lblTu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        SpinnerDateModel tuModel = new SpinnerDateModel();
        spnTuNgay = new JSpinner(tuModel);
        spnTuNgay.setEditor(new JSpinner.DateEditor(spnTuNgay, "dd/MM/yyyy"));
        spnTuNgay.setPreferredSize(new Dimension(115, 30));
        // Default: 30 ngày trước
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        tuModel.setValue(cal.getTime());

        // Đến ngày
        JLabel lblDen = new JLabel("→");
        lblDen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        SpinnerDateModel denModel = new SpinnerDateModel();
        spnDenNgay = new JSpinner(denModel);
        spnDenNgay.setEditor(new JSpinner.DateEditor(spnDenNgay, "dd/MM/yyyy"));
        spnDenNgay.setPreferredSize(new Dimension(115, 30));

        // Nút Lọc + Reset
        JButton btnLoc = buildFilterBtn("Lọc", PRIMARY_LIGHT);
        JButton btnReset = buildFilterBtn("Xóa lọc", new Color(149, 165, 166));
        btnLoc.addActionListener(e -> applyFilter());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cboLoaiThaoTac.setSelectedIndex(0);
            cboNhanVien.setSelectedIndex(0);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.add(java.util.Calendar.DAY_OF_MONTH, -30);
            spnTuNgay.setValue(c.getTime());
            spnDenNgay.setValue(new java.util.Date());
            loadData();
        });

        bar.add(lblSearch); bar.add(txtSearch);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(lblLoai); bar.add(cboLoaiThaoTac);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(lblNV); bar.add(cboNhanVien);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(lblTu); bar.add(spnTuNgay);
        bar.add(lblDen); bar.add(spnDenNgay);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(btnLoc); bar.add(btnReset);

        return bar;
    }

    // ── TABLE PANE ─────────────────────────────────────────────────────────────
    private JScrollPane buildTablePane() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 235, 240));
        table.setSelectionBackground(new Color(70, 130, 180, 40));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);   // click header để sort

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBackground(PRIMARY_DARK);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                lbl.setHorizontalAlignment(JLabel.LEFT);
                return lbl;
            }
        });

        // Renderer alternate row + badge Loại thao tác
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 250, 253));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lbl.setForeground(TEXT_DARK);

                // Cột Loại Thao Tác (index 5): tô màu badge
                if (col == 5 && v != null) {
                    String val = v.toString();
                    if (val.contains("Gộp")) {
                        lbl.setForeground(ACCENT_ORANGE);
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        lbl.setForeground(PRIMARY_LIGHT);
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                return lbl;
            }
        });

        // Độ rộng cột
        int[] colWidths = {50, 100, 100, 110, 110, 120, 150, 100, 180};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        // Double-click → chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showDetail(table.convertRowIndexToModel(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG_SECONDARY);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD DATA
    // ─────────────────────────────────────────────────────────────────────────
    private void loadData() {
        loadData(null, null, null, null, null);
    }

    private void loadData(String keyword, String loai, String maNV,
                          java.util.Date tuNgay, java.util.Date denNgay) {
        tableModel.setRowCount(0);

        StringBuilder sql = new StringBuilder(
            "SELECT ls.MaChuyenBan, " +
            "       bc.TenBan AS BanCu, bm.TenBan AS BanMoi, " +
            "       kvc.TenKV AS KVCu, kvm.TenKV AS KVMoi, " +
            "       ls.GhiChu, n.HoTen AS NhanVien, ls.NgayChuyen " +
            "FROM LichSuChuyenBan ls " +
            "JOIN Ban        bc  ON ls.MaBanCu  = bc.MaBan " +
            "JOIN Ban        bm  ON ls.MaBanMoi = bm.MaBan " +
            "JOIN KhuVucQuan kvc ON bc.MaKV     = kvc.MaKV " +
            "JOIN KhuVucQuan kvm ON bm.MaKV     = kvm.MaKV " +
            "JOIN NhanVien   n   ON ls.MaNV     = n.MaNV " +
            "WHERE 1=1 "
        );

        if (keyword != null && !keyword.isEmpty())
            sql.append("AND (bc.TenBan LIKE ? OR bm.TenBan LIKE ? OR n.HoTen LIKE ? OR ls.GhiChu LIKE ?) ");
        if (loai != null && loai.equals("Gộp bàn"))
            sql.append("AND ls.GhiChu = 'Gộp bàn' ");
        else if (loai != null && loai.equals("Chuyển bàn"))
            sql.append("AND (ls.GhiChu IS NULL OR ls.GhiChu != 'Gộp bàn') ");
        if (maNV != null && !maNV.isEmpty() && !maNV.startsWith("—"))
            sql.append("AND ls.MaNV = ? ");
        if (tuNgay != null)
            sql.append("AND ls.NgayChuyen >= ? ");
        if (denNgay != null)
            sql.append("AND ls.NgayChuyen <= ? ");

        sql.append("ORDER BY ls.MaChuyenBan DESC");

        int tongLuot = 0, soGop = 0, soHomNay = 0;
        String today = LocalDate.now().toString();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                ps.setString(idx++, kw); ps.setString(idx++, kw);
                ps.setString(idx++, kw); ps.setString(idx++, kw);
            }
            if (maNV != null && !maNV.isEmpty() && !maNV.startsWith("—")) {
                try { ps.setInt(idx++, Integer.parseInt(maNV.split("\\|")[0].trim())); }
                catch (NumberFormatException ignored) { idx++; }
            }
            if (tuNgay != null) { ps.setDate(idx++, new java.sql.Date(tuNgay.getTime())); }
            if (denNgay != null) { ps.setDate(idx++, new java.sql.Date(denNgay.getTime())); }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ghiChu     = rs.getString("GhiChu");
                String loaiPhep   = (ghiChu != null && ghiChu.equals("Gộp bàn")) ? "Gộp bàn" : "Chuyển bàn";
                String ngay       = rs.getString("NgayChuyen");

                tableModel.addRow(new Object[]{
                    rs.getInt("MaChuyenBan"),
                    rs.getString("BanCu"),
                    rs.getString("BanMoi"),
                    rs.getString("KVCu"),
                    rs.getString("KVMoi"),
                    loaiPhep,
                    rs.getString("NhanVien"),
                    ngay,
                    ghiChu != null ? ghiChu : ""
                });

                tongLuot++;
                if ("Gộp bàn".equals(ghiChu)) soGop++;
                if (ngay != null && ngay.equals(today)) soHomNay++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi tải dữ liệu:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Cập nhật thẻ thống kê
        lblTongLuot.setText(String.valueOf(tongLuot));
        lblChuyenBan.setText(String.valueOf(tongLuot - soGop));
        lblGopBan.setText(String.valueOf(soGop));
        lblHomNay.setText(String.valueOf(soHomNay));
    }

    private void loadNhanVienCombo() {
        cboNhanVien.addItem("— Tất cả nhân viên —");
        String q = "SELECT MaNV, HoTen FROM NhanVien ORDER BY HoTen";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                cboNhanVien.addItem(rs.getInt("MaNV") + " | " + rs.getString("HoTen"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Áp dụng bộ lọc ────────────────────────────────────────────────────────
    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        String loai    = (String) cboLoaiThaoTac.getSelectedItem();
        String nv      = (String) cboNhanVien.getSelectedItem();
        java.util.Date tu  = (java.util.Date) spnTuNgay.getValue();
        java.util.Date den = (java.util.Date) spnDenNgay.getValue();

        String loaiFilter = "Tất cả".equals(loai) ? null : loai;
        String nvFilter   = (nv != null && nv.startsWith("—")) ? null : nv;

        loadData(keyword.isEmpty() ? null : keyword, loaiFilter, nvFilter, tu, den);
    }

    // ── Hiển thị chi tiết khi double-click ────────────────────────────────────
    private void showDetail(int modelRow) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-14s: %s\n", "Mã lịch sử",    tableModel.getValueAt(modelRow, 0)));
        sb.append(String.format("%-14s: %s  →  %s\n", "Bàn",   tableModel.getValueAt(modelRow, 1),
                                                                  tableModel.getValueAt(modelRow, 2)));
        sb.append(String.format("%-14s: %s  →  %s\n", "Khu vực", tableModel.getValueAt(modelRow, 3),
                                                                    tableModel.getValueAt(modelRow, 4)));
        sb.append(String.format("%-14s: %s\n", "Loại",           tableModel.getValueAt(modelRow, 5)));
        sb.append(String.format("%-14s: %s\n", "Nhân viên",      tableModel.getValueAt(modelRow, 6)));
        sb.append(String.format("%-14s: %s\n", "Ngày",           tableModel.getValueAt(modelRow, 7)));
        Object ghiChu = tableModel.getValueAt(modelRow, 8);
        sb.append(String.format("%-14s: %s\n", "Ghi chú",
            (ghiChu != null && !ghiChu.toString().isEmpty()) ? ghiChu : "(không có)"));

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setBackground(new Color(248, 250, 252));
        ta.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JOptionPane.showMessageDialog(this, ta,
            "Chi Tiết Lịch Sử #" + tableModel.getValueAt(modelRow, 0),
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Xuất CSV ──────────────────────────────────────────────────────────────
    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file CSV");
        fc.setSelectedFile(new java.io.File("LichSuChuyenBan_"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {
            // BOM cho Excel đọc được tiếng Việt
            pw.print('\uFEFF');
            // Header
            pw.println(String.join(",", COLUMNS));
            // Data
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    if (c > 0) row.append(",");
                    Object val = tableModel.getValueAt(r, c);
                    String cell = val != null ? val.toString().replace(",", "،") : "";
                    row.append("\"").append(cell).append("\"");
                }
                pw.println(row);
            }
            JOptionPane.showMessageDialog(this,
                "Xuất CSV thành công!\n" + fc.getSelectedFile().getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất CSV:\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Util ──────────────────────────────────────────────────────────────────
    private JButton buildHdrBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(140, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);          btn.repaint(); }
        });
        return btn;
    }

    private JButton buildFilterBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);          btn.repaint(); }
        });
        return btn;
    }
    private void xoaDongDaChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn dòng cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int maChuyenBan = (int) tableModel.getValueAt(modelRow, 0);
        String banCu = (String) tableModel.getValueAt(modelRow, 1);
        String banMoi = (String) tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Xóa lịch sử #%d (%s → %s)?", maChuyenBan, banCu, banMoi),
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM LichSuChuyenBan WHERE MaChuyenBan = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maChuyenBan);
            ps.executeUpdate();
            tableModel.removeRow(modelRow);
            // Cập nhật lại thẻ thống kê
            int tong = Integer.parseInt(lblTongLuot.getText()) - 1;
            lblTongLuot.setText(String.valueOf(tong));
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xóa:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}