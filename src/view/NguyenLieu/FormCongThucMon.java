package view.NguyenLieu;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import config.DatabaseConfig;
import dao.MonAnDAO;


public class FormCongThucMon extends JPanel {

    // ─────────────── Màu sắc (đồng bộ hệ thống) ───────────────
    private static final Color NAV_BG       = new Color(25,  45,  85);
    private static final Color SIDEBAR_BG   = new Color(255, 255, 255);
    private static final Color CONTENT_BG   = new Color(243, 246, 250);
    private static final Color PRIMARY      = new Color(52, 130, 200);
    private static final Color SUCCESS      = new Color(39, 174,  96);
    private static final Color DANGER       = new Color(192,  57,  43);
    private static final Color WARNING      = new Color(230, 170,  20);
    private static final Color PURPLE       = new Color(142,  68, 173);
    private static final Color TEXT_DARK    = new Color(44,  62,  80);
    private static final Color TEXT_MID     = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(214, 220, 229);
    private static final Color ROW_ALT      = new Color(248, 250, 253);
    private static final Color ROW_SEL      = new Color(213, 232, 255);

    // ─────────────── Combo & Fields ───────────────
    private JComboBox<Object[]> cboMon;
    private JComboBox<Object[]> cboNL;
    private JTextField txtSoLuong, txtDVT, txtGhiChu;

    // ─────────────── Buttons ───────────────
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTinhLaiSL;

    // ─────────────── Bảng ───────────────
    private JTable            table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField        txtSearch;

    // Lưu CoTheLam theo model row index để renderer tô màu (không cần cột ẩn)
    private final Map<Integer, Integer> coTheLamMap = new HashMap<>();

    private int selectedMaCT = -1;
    private int currentMaMon = -1;

    // ════════════════════════════════════════════
    public FormCongThucMon() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        buildUI();
        loadComboMon();
        loadComboNL();
        loadData();
    }

    // ─────────────────────── BUILD UI ───────────────────────
    private void buildUI() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildSidebar(), buildTablePanel());
        split.setResizeWeight(0.40);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(CONTENT_BG);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                split.setDividerLocation(0.40);
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(CONTENT_BG);
        center.setBorder(new EmptyBorder(10, 10, 10, 10));
        center.add(split, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ─────────────────────── SIDEBAR (40%) ───────────────────────
    private JPanel buildSidebar() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SIDEBAR_BG);
        content.setBorder(new EmptyBorder(16, 18, 20, 18));

        // ── Lọc theo món ──
        content.add(sectionHeader("Lọc Theo Món Ăn"));
        content.add(vgap(10));

        cboMon = new JComboBox<>();
        styleCombo(cboMon);
        cboMon.addActionListener(e -> {
            Object[] sel = (Object[]) cboMon.getSelectedItem();
            if (sel != null) { currentMaMon = (int) sel[0]; loadData(); }
        });
        content.add(formRowCombo("Chọn Món Ăn", cboMon)); content.add(vgap(14));

        // ── Thông tin công thức ──
        content.add(sectionHeader("Thông Tin Công Thức"));
        content.add(vgap(10));

        cboNL = new JComboBox<>();
        styleCombo(cboNL);
        cboNL.addActionListener(e -> {
            Object[] sel = (Object[]) cboNL.getSelectedItem();
            if (sel != null) {
                String curDVT = txtDVT.getText().trim();
                if (curDVT.isEmpty() || curDVT.equals("Ví dụ: kg, lít, gói...")) {
                    txtDVT.setText(String.valueOf(sel[2]));
                    txtDVT.setForeground(TEXT_DARK);
                }
            }
        });

        txtSoLuong = createField("Nhập số lượng cần...");
        txtDVT     = createField("Ví dụ: kg, lít, gói...");
        txtGhiChu  = createField("Ghi chú (không bắt buộc)...");

        content.add(formRowCombo("Nguyên Liệu *",         cboNL));      content.add(vgap(8));
        content.add(formRow("Số Lượng Cần / 1 Món *",     txtSoLuong)); content.add(vgap(8));
        content.add(formRow("Đơn Vị Tính",                txtDVT));     content.add(vgap(8));
        content.add(formRow("Ghi Chú",                    txtGhiChu));  content.add(vgap(14));

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        content.add(sep); content.add(vgap(14));

        // ── Thao tác ──
        content.add(sectionHeader("Thao Tác"));
        content.add(vgap(10));

        btnThem      = bigBtn("Thêm",        SUCCESS);
        btnSua       = bigBtn("Sửa",         PRIMARY);
        btnXoa       = bigBtn("Xoá",         DANGER);
        btnLamMoi    = bigBtn("Làm Mới",     WARNING);
        btnTinhLaiSL = bigBtn("Tính Lại SL", PURPLE);

        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);

        btnThem     .addActionListener(e -> them());
        btnSua      .addActionListener(e -> sua());
        btnXoa      .addActionListener(e -> xoa());
        btnLamMoi   .addActionListener(e -> clearForm());
        btnTinhLaiSL.addActionListener(e -> tinhLaiSoLuong());

        content.add(btnRow(btnThem, btnSua));   content.add(vgap(8));
        content.add(btnRow(btnXoa, btnLamMoi)); content.add(vgap(8));

        JPanel rowTinhLai = new JPanel(new BorderLayout());
        rowTinhLai.setBackground(SIDEBAR_BG);
        rowTinhLai.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowTinhLai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnTinhLaiSL.setPreferredSize(new Dimension(0, 44));
        rowTinhLai.add(btnTinhLaiSL, BorderLayout.CENTER);
        content.add(rowTinhLai); content.add(vgap(16));

        content.add(buildInfoBox());
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(SIDEBAR_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SIDEBAR_BG);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────── BẢNG (60%) ───────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 16, 16, 16)));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("DANH SÁCH CÔNG THỨC MÓN ĂN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAV_BG);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legend.setBackground(Color.WHITE);
        legend.add(legendChip(new Color(255, 200, 200), "Hết nguyên liệu"));
        legend.add(legendChip(new Color(255, 243, 205), "Sắp hết (≤5 phần)"));
        legend.add(legendChip(Color.WHITE,              "Còn đủ"));

        headerRow.add(title,  BorderLayout.WEST);
        headerRow.add(legend, BorderLayout.EAST);

        JPanel searchBar = buildSearchBar();

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerRow, BorderLayout.NORTH);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // ── 6 cột hiển thị, KHÔNG có "Có Thể Làm" ──
        String[] cols = {"Mã CT", "Tên Món", "Nguyên Liệu", "SL Cần", "ĐVT", "Ghi Chú"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        setupTable();

        // Renderer tô màu dựa vào coTheLamMap (không đọc từ cột nào trong table)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int modelRow = table.convertRowIndexToModel(r);
                    int coTheLam = coTheLamMap.getOrDefault(modelRow, 999);
                    if (coTheLam <= 0)
                        comp.setBackground(new Color(255, 200, 200));
                    else if (coTheLam <= 5)
                        comp.setBackground(new Color(255, 243, 205));
                    else
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(ROW_SEL);
                    comp.setForeground(NAV_BG);
                }
                return comp;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadToForm();
        });

        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sc.getViewport().setBackground(Color.WHITE);

        p.add(topPanel, BorderLayout.NORTH);
        p.add(sc,       BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────── SEARCH BAR ───────────────────────
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 0, 4, 0));

        txtSearch = createSearchField("Nhập từ khoá tìm kiếm (tên món, nguyên liệu...)");

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(new Color(247, 249, 252));
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 12, 6, 12)));
        searchWrap.add(txtSearch, BorderLayout.CENTER);

        JLabel lblCount = new JLabel();
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(TEXT_MID);
        lblCount.setBorder(new EmptyBorder(0, 8, 0, 0));

        JButton btnClear = new JButton("X");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setForeground(TEXT_MID);
        btnClear.setBackground(new Color(230, 235, 242));
        btnClear.setBorder(new EmptyBorder(4, 10, 4, 10));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { doFilter(lblCount); }
            @Override public void removeUpdate(DocumentEvent e)  { doFilter(lblCount); }
            @Override public void changedUpdate(DocumentEvent e) { doFilter(lblCount); }
        });

        JPanel rightPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rightPart.setBackground(Color.WHITE);
        rightPart.add(btnClear);
        rightPart.add(lblCount);

        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(rightPart,  BorderLayout.EAST);
        return bar;
    }

    private void doFilter(JLabel lblCount) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1, 2));
            int matched = table.getRowCount();
            int total   = tableModel.getRowCount();
            lblCount.setText(matched + "/" + total + " công thức");
        }
    }

    // ─────────────────────── TABLE SETUP ───────────────────────
    private void setupTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(235, 238, 242));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setBackground(NAV_BG);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                return l;
            }
        });

        // 6 cột: Mã CT, Tên Món, Nguyên Liệu, SL Cần, ĐVT, Ghi Chú
        int[] ws = {55, 210, 200, 85, 75, 220};
        for (int i = 0; i < ws.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
    }

    // ─────────────────────── LOAD DATA ───────────────────────
    private void loadComboMon() {
        String sql = "SELECT MaMon, TenMon FROM monan ORDER BY TenMon";
        cboMon.removeAllItems();
        cboMon.addItem(new Object[]{-1, "-- Tất cả --"});
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                cboMon.addItem(new Object[]{rs.getInt("MaMon"), rs.getString("TenMon")});
        } catch (SQLException e) { e.printStackTrace(); }
        cboMon.setRenderer((list, v, idx, sel, foc) -> {
            Object[] arr = (Object[]) v;
            return new JLabel(arr != null ? String.valueOf(arr[1]) : "");
        });
    }

    private void loadComboNL() {
        String sql = "SELECT MaNL, TenNguyenLieu, DonViTinh FROM nguyenlieu WHERE TrangThai=1 ORDER BY TenNguyenLieu";
        cboNL.removeAllItems();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                cboNL.addItem(new Object[]{rs.getInt("MaNL"), rs.getString("TenNguyenLieu"), rs.getString("DonViTinh")});
        } catch (SQLException e) { e.printStackTrace(); }
        cboNL.setRenderer((list, v, idx, sel, foc) -> {
            Object[] arr = (Object[]) v;
            return new JLabel(arr != null ? arr[1] + " (" + arr[2] + ")" : "");
        });
    }

    // loadData: chỉ đưa 6 cột vào tableModel, lưu CoTheLam vào coTheLamMap
    private void loadData() {
        tableModel.setRowCount(0);
        coTheLamMap.clear();

        String where = currentMaMon > 0 ? "WHERE ct.MaMon = " + currentMaMon : "";
        String sql =
            "SELECT ct.MaCT, m.TenMon, nl.TenNguyenLieu, ct.SoLuongCan, ct.DonViTinh, " +
            "ct.GhiChu, FLOOR(nl.SoLuong / NULLIF(ct.SoLuongCan, 0)) AS CoTheLam " +
            "FROM congthucmon ct " +
            "JOIN monan m ON m.MaMon = ct.MaMon " +
            "JOIN nguyenlieu nl ON nl.MaNL = ct.MaNL " +
            where + " ORDER BY m.TenMon, nl.TenNguyenLieu";

        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int rowIdx = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("MaCT"),                                             // 0
                    rs.getString("TenMon"),                                        // 1
                    rs.getString("TenNguyenLieu"),                                 // 2
                    rs.getDouble("SoLuongCan"),                                    // 3
                    rs.getString("DonViTinh"),                                     // 4
                    rs.getString("GhiChu") != null ? rs.getString("GhiChu") : ""  // 5
                });
                // Lưu CoTheLam vào Map theo model row
                coTheLamMap.put(rowIdx, rs.getInt("CoTheLam"));
                rowIdx++;
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ─────────────────────── CRUD ───────────────────────
    private void them() {
        Object[] selMon = (Object[]) cboMon.getSelectedItem();
        Object[] selNL  = (Object[]) cboNL.getSelectedItem();
        if (selMon == null || (int) selMon[0] <= 0) { err("Vui lòng chọn món ăn!"); return; }
        if (selNL == null) { err("Vui lòng chọn nguyên liệu!"); return; }
        double sl;
        try {
            sl = Double.parseDouble(txtSoLuong.getText().trim());
            if (sl <= 0) throw new Exception();
        } catch (Exception e) { err("Số lượng phải > 0!"); return; }

        String sql = "INSERT INTO congthucmon (MaMon, MaNL, SoLuongCan, DonViTinh, GhiChu) " +
                     "VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                     "SoLuongCan=VALUES(SoLuongCan), DonViTinh=VALUES(DonViTinh), GhiChu=VALUES(GhiChu)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, (int) selMon[0]);
            ps.setInt(2, (int) selNL[0]);
            ps.setDouble(3, sl);
            ps.setString(4, getDVT());
            ps.setString(5, txtGhiChu.getText().trim());
            ps.executeUpdate();
            MonAnDAO.tinhLaiSoLuong((int) selMon[0]);
            JOptionPane.showMessageDialog(this, "Thêm công thức thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } catch (SQLException e) { err("Lỗi: " + e.getMessage()); }
    }

    private void sua() {
        if (selectedMaCT <= 0) { err("Chọn dòng cần sửa!"); return; }
        double sl;
        try {
            sl = Double.parseDouble(txtSoLuong.getText().trim());
            if (sl <= 0) throw new Exception();
        } catch (Exception e) { err("Số lượng phải > 0!"); return; }

        String sql = "UPDATE congthucmon SET SoLuongCan=?, DonViTinh=?, GhiChu=? WHERE MaCT=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, sl);
            ps.setString(2, getDVT());
            ps.setString(3, txtGhiChu.getText().trim());
            ps.setInt(4, selectedMaCT);
            ps.executeUpdate();
            if (currentMaMon > 0) MonAnDAO.tinhLaiSoLuong(currentMaMon);
            else MonAnDAO.tinhLaiTatCaMon();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } catch (SQLException e) { err("Lỗi: " + e.getMessage()); }
    }

    private void xoa() {
        if (selectedMaCT <= 0) { err("Chọn dòng cần xoá!"); return; }
        int cf = JOptionPane.showConfirmDialog(this,
            "Xoá dòng công thức này?", "Xác nhận",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf != JOptionPane.YES_OPTION) return;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM congthucmon WHERE MaCT=?")) {
            ps.setInt(1, selectedMaCT);
            ps.executeUpdate();
            if (currentMaMon > 0) MonAnDAO.tinhLaiSoLuong(currentMaMon);
            else MonAnDAO.tinhLaiTatCaMon();
            clearForm(); loadData();
        } catch (SQLException e) { err("Lỗi: " + e.getMessage()); }
    }

    private void tinhLaiSoLuong() {
        MonAnDAO.tinhLaiTatCaMon();
        loadData();
        JOptionPane.showMessageDialog(this,
            "Đã tính lại số lượng khả dụng cho tất cả món ăn!",
            "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─────────────────────── PUBLIC API ───────────────────────
    public void filterByMonNgoai(int maMon) {
        this.currentMaMon = maMon;
        for (int i = 0; i < cboMon.getItemCount(); i++) {
            Object[] item = (Object[]) cboMon.getItemAt(i);
            if (item != null && (int) item[0] == maMon) {
                cboMon.setSelectedIndex(i);
                break;
            }
        }
        loadData();
    }

    // ─────────────────────── FORM HELPERS ───────────────────────
    private void loadToForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        selectedMaCT = (int) tableModel.getValueAt(modelRow, 0);

        setField(txtSoLuong, String.valueOf(tableModel.getValueAt(modelRow, 3)));
        setField(txtDVT,     String.valueOf(tableModel.getValueAt(modelRow, 4)));
        Object ghiChu = tableModel.getValueAt(modelRow, 5); // index 5 = GhiChu
        setField(txtGhiChu, ghiChu != null ? ghiChu.toString() : "");

        btnThem.setEnabled(false);
        btnSua .setEnabled(true);
        btnXoa .setEnabled(true);
    }

    private void clearForm() {
        selectedMaCT = -1;
        resetField(txtSoLuong, "Nhập số lượng cần...");
        resetField(txtDVT,     "Ví dụ: kg, lít, gói...");
        resetField(txtGhiChu,  "Ghi chú (không bắt buộc)...");
        table.clearSelection();
        btnThem.setEnabled(true);
        btnSua .setEnabled(false);
        btnXoa .setEnabled(false);
    }

    private String getDVT() {
        String d = txtDVT.getText().trim();
        return (d.isEmpty() || d.equals("Ví dụ: kg, lít, gói...")) ? "" : d;
    }

    // ─────────────────────── UI HELPERS ───────────────────────
    private JTextField createField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(TEXT_MID);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(0, 42));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(TEXT_DARK); }
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2), new EmptyBorder(7, 11, 7, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(TEXT_MID); }
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return tf;
    }

    private JTextField createSearchField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT_MID);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, getInsets().left, y);
                }
            }
        };
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(new Color(247, 249, 252));
        tf.setCaretColor(PRIMARY);
        tf.setBorder(null);
        tf.setOpaque(false);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        cb.setPreferredSize(new Dimension(0, 42));
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton bigBtn(String txt, Color bg) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color col = !isEnabled() ? new Color(180, 180, 180)
                          : getModel().isPressed()  ? bg.darker().darker()
                          : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(col);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBorder(null);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 44));
        return b;
    }

    private JPanel btnRow(JButton b1, JButton b2) {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setBackground(SIDEBAR_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b1.setPreferredSize(new Dimension(0, 44));
        b2.setPreferredSize(new Dimension(0, 44));
        p.add(b1); p.add(b2);
        return p;
    }

    private JPanel formRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(SIDEBAR_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        p.add(l, BorderLayout.NORTH);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel formRowCombo(String labelText, JComboBox<?> comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(SIDEBAR_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        p.add(l, BorderLayout.NORTH);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel sectionHeader(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SIDEBAR_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(NAV_BG);
        p.add(l, BorderLayout.CENTER);
        JPanel ul = new JPanel();
        ul.setBackground(PRIMARY);
        ul.setPreferredSize(new Dimension(0, 2));
        p.add(ul, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildInfoBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(230, 244, 255));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(170, 210, 240), 1),
            new EmptyBorder(10, 12, 10, 12)));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel ht = new JLabel("Hướng dẫn:");
        ht.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ht.setForeground(new Color(30, 70, 130));
        ht.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(ht); box.add(Box.createVerticalStrut(5));

        for (String line : new String[]{
                "- Thiết lập nguyên liệu cần cho 1 phần món ăn.",
                "- Đỏ: hết nguyên liệu. Vàng: sắp hết (≤5 phần).",
                "- Nhấn [Tính Lại SL] để cập nhật toàn bộ.",
                "- Chọn dòng trên bảng để sửa hoặc xoá."}) {
            JLabel hl = new JLabel(line);
            hl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hl.setForeground(TEXT_DARK);
            hl.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(hl);
        }
        return box;
    }

    private JLabel legendChip(Color bg, String text) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));
        return l;
    }

    private Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
    private void setField(JTextField f, String val) { f.setText(val); f.setForeground(TEXT_DARK); }
    private void resetField(JTextField f, String ph) { f.setText(ph); f.setForeground(TEXT_MID); }
    private void err(String msg) { JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE); }
}