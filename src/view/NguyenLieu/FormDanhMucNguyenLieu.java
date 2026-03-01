package view.NguyenLieu;

import dao.NguyenLieuDAO;
import model.NguyenLieu;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;


public class FormDanhMucNguyenLieu extends JPanel {

    // ─────────────── Màu sắc (đồng bộ toàn hệ thống) ───────────────
    private static final Color NAV_BG       = new Color(25,  45,  85);
    private static final Color SUBNAV_BG    = new Color(35,  58, 105);
    private static final Color SUBNAV_ACT   = new Color(55,  95, 160);
    private static final Color SIDEBAR_BG   = new Color(255, 255, 255);
    private static final Color CONTENT_BG   = new Color(243, 246, 250);
    private static final Color PRIMARY      = new Color(52, 130, 200);
    private static final Color SUCCESS      = new Color(39, 174,  96);
    private static final Color DANGER       = new Color(192,  57,  43);
    private static final Color WARNING      = new Color(230, 170,  20);
    private static final Color TEXT_DARK    = new Color(44,  62,  80);
    private static final Color TEXT_MID     = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(214, 220, 229);
    private static final Color ROW_ALT      = new Color(248, 250, 253);
    private static final Color ROW_SEL      = new Color(213, 232, 255);

    // ─────────────── Fields form ───────────────
    private JTextField txtTen, txtDVT, txtDonGia, txtSoLuong,
                       txtDVTNhap, txtHeSo, txtMin, txtMax, txtGhiChu;
    private JComboBox<String> cboTrangThai;

    // ─────────────── Buttons ───────────────
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;

    // ─────────────── Bảng ───────────────
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;

    private int selectedMaNL = -1;

    // Định dạng tiền VNĐ
    private static final DecimalFormat VND_FMT;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("vi", "VN"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        VND_FMT = new DecimalFormat("#,###", sym);
    }

    // ════════════════════════════════════════════
    public FormDanhMucNguyenLieu() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        buildUI();
        loadData();
    }

    // ─────────────────────── BUILD UI ───────────────────────
    private void buildUI() {
        // Top: nav + sub-nav
        JPanel topStack = new JPanel(new BorderLayout());
      
        
        add(topStack, BorderLayout.NORTH);

        // Center: sidebar 40% + bảng 60%
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildSidebar(), buildTablePanel());
        split.setResizeWeight(0.40);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(CONTENT_BG);

        // Giữ đúng 40% khi resize
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

    // ── Navigation bar (gradient navy) ──
   

    // ── Sub-navigation tabs ──
   

    // ─────────────────────── SIDEBAR (40%, cuộn được) ───────────────────────
    private JPanel buildSidebar() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SIDEBAR_BG);
        content.setBorder(new EmptyBorder(16, 18, 20, 18));

        // ── Thông tin cơ bản ──
        content.add(sectionHeader("Thông tin cơ bản"));
        content.add(vgap(10));

        txtTen     = createField("Nhập tên nguyên liệu...");
        txtDVT     = createField("Nhập đơn vị tính...");
        txtDonGia  = createPriceField();
        txtSoLuong = createField("0");

        content.add(formRow("Tên nguyên liệu *",  txtTen));      content.add(vgap(8));
        content.add(formRow("Đơn vị tính *",       txtDVT));     content.add(vgap(8));
        content.add(formRow("Đơn giá (VND)",       txtDonGia));  content.add(vgap(8));
        content.add(formRow("Tồn hiện tại",        txtSoLuong)); content.add(vgap(14));

        // ── Thông tin nhập kho ──
        content.add(sectionHeader("Thông tin nhập kho"));
        content.add(vgap(10));

        txtDVTNhap = createField("Đơn vị tính nhập hàng...");
        txtHeSo    = createField("1.0");
        txtMin     = createField("0");
        txtMax     = createField("9999");

        content.add(formRow("ĐVT nhập hàng",   txtDVTNhap)); content.add(vgap(8));
        content.add(formRow("Hệ số quy đổi",   txtHeSo));    content.add(vgap(8));
        content.add(formRow("Tồn tối thiểu",   txtMin));     content.add(vgap(8));
        content.add(formRow("Tồn tối đa",      txtMax));     content.add(vgap(14));

        // ── Ghi chú & trạng thái ──
        content.add(sectionHeader("Ghi chú và trạng thái"));
        content.add(vgap(10));

        txtGhiChu = createField("Ghi chú thêm...");
        cboTrangThai = new JComboBox<>(new String[]{"Dang dung", "Ngung dung"});
        styleComboBox(cboTrangThai);

        content.add(formRow("Ghi Chu",     txtGhiChu));    content.add(vgap(8));
        content.add(formRow("Trang Thai",  cboTrangThai)); content.add(vgap(16));

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        content.add(sep); content.add(vgap(14));

        // ── Thao tác ──
        content.add(sectionHeader("Thao Tác"));
        content.add(vgap(10));

        btnThem   = bigBtn("Thêm",     SUCCESS);
        btnSua    = bigBtn("Sửa",      PRIMARY);
        btnXoa    = bigBtn("Xoá",      DANGER);
        btnLamMoi = bigBtn("Làm Mới",  WARNING);

        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);

        btnThem  .addActionListener(e -> them());
        btnSua   .addActionListener(e -> sua());
        btnXoa   .addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> clearForm());

        content.add(btnRow(btnThem, btnSua));   content.add(vgap(8));
        content.add(btnRow(btnXoa, btnLamMoi)); content.add(vgap(16));

        // ── Hướng dẫn ──
        content.add(buildHintBox());
        content.add(Box.createVerticalGlue());

        // Bọc trong JScrollPane
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

    // ─────────────────────── BẢNG DANH SÁCH (60%) ───────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 16, 16, 16)));

        // Header: tiêu đề + legend
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("DANH SÁCH NGUYÊN LIỆU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAV_BG);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legend.setBackground(Color.WHITE);
        legend.add(legendChip(new Color(255, 200, 200), "Hết hàng"));
        legend.add(legendChip(new Color(255, 243, 205), "Sắp hết"));
        legend.add(legendChip(new Color(209, 231, 221), "Dự tồn kho"));

        headerRow.add(title,  BorderLayout.WEST);
        headerRow.add(legend, BorderLayout.EAST);

        // Thanh tìm kiếm
        JPanel searchBar = buildSearchBar();

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerRow,  BorderLayout.NORTH);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // Table model
        String[] cols = {"Ma","Ten Nguyen Lieu","DVT","Don Gia","Ton Kho","Ton Toi Thieu","Trang Thai Ton","Ghi Chu"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        setupTable();

        // Renderer màu theo trạng thái tồn
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int modelRow = table.convertRowIndexToModel(r);
                    String tt = String.valueOf(tableModel.getValueAt(modelRow, 6));
                    if      ("Het hang".equals(tt)) comp.setBackground(new Color(255, 200, 200));
                    else if ("Sap het".equals(tt))  comp.setBackground(new Color(255, 243, 205));
                    else                             comp.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
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

        txtSearch = createSearchField("Nhap tu khoa tim kiem nguyen lieu");

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(new Color(247, 249, 252));
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 12, 6, 12)));
        searchWrap.add(txtSearch, BorderLayout.CENTER);

        // Nút xóa + đếm kết quả
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

        // Live filter chỉ theo tên nguyên liệu (cột 1)
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

    /** Lọc bảng chỉ theo Tên Nguyen Lieu (cột 1) */
    private void doFilter(JLabel lblCount) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1));
            int matched = table.getRowCount();
            int total   = tableModel.getRowCount();
            lblCount.setText(matched + "/" + total + " nguyen lieu");
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

        int[] ws = {50, 220, 80, 120, 100, 110, 130, 200};
        for (int i = 0; i < ws.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
    }

    // ─────────────────────── LOAD DATA ───────────────────────
    private void loadData() {
        tableModel.setRowCount(0);
        for (NguyenLieu nl : NguyenLieuDAO.getAllKeTongNgung()) {
            String donGia = nl.getDonGia() != null
                ? VND_FMT.format(nl.getDonGia().longValue()) + " VND" : "—";
            tableModel.addRow(new Object[]{
                nl.getMaNL(),
                nl.getTenNguyenLieu(),
                nl.getDonViTinh(),
                donGia,
                nl.getSoLuong(),
                nl.getMucTonToiThieu(),
                nl.getTrangThaiTon(),
                nl.getGhiChu() != null ? nl.getGhiChu() : ""
            });
        }
    }

    // ─────────────────────── CRUD ───────────────────────
    private void them() {
        NguyenLieu nl = collectForm();
        if (nl == null) return;
        if (NguyenLieuDAO.add(nl)) {
            JOptionPane.showMessageDialog(this, "Them nguyen lieu thanh cong!", "OK", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } else err("Khong the them nguyen lieu!");
    }

    private void sua() {
        if (selectedMaNL < 0) { err("Chon nguyen lieu can sua!"); return; }
        NguyenLieu nl = collectForm();
        if (nl == null) return;
        nl.setMaNL(selectedMaNL);
        if (NguyenLieuDAO.update(nl)) {
            JOptionPane.showMessageDialog(this, "Cap nhat thanh cong!", "OK", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } else err("Khong the cap nhat!");
    }

    private void xoa() {
        if (selectedMaNL < 0) { err("Chon nguyen lieu can xoa!"); return; }
        int cf = JOptionPane.showConfirmDialog(this,
            "Xoa mem (ngung su dung) nguyen lieu nay?", "Xac nhan",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf == JOptionPane.YES_OPTION) {
            NguyenLieuDAO.deactivate(selectedMaNL);
            clearForm(); loadData();
        }
    }

    private NguyenLieu collectForm() {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty() || ten.equals("Nhap ten nguyen lieu...")) {
            err("Nhap ten nguyen lieu!"); return null;
        }
        String dvt = txtDVT.getText().trim();
        if (dvt.isEmpty() || dvt.equals("Nhap don vi tinh...")) {
            err("Nhap don vi tinh!"); return null;
        }
        NguyenLieu nl = new NguyenLieu();
        nl.setTenNguyenLieu(ten);
        nl.setDonViTinh(dvt);
        nl.setDonViTinhNhap(txtDVTNhap.getText().trim());
        nl.setGhiChu(txtGhiChu.getText().trim());
        nl.setTrangThai(cboTrangThai.getSelectedIndex() == 0 ? 1 : 0);

        // Đơn giá: bóc số thuần từ chuỗi đã format
        try {
            String rawPrice = txtDonGia.getText().replaceAll("[^0-9]", "");
            nl.setDonGia(rawPrice.isEmpty() ? BigDecimal.ZERO : new BigDecimal(rawPrice));
        } catch (Exception e) { nl.setDonGia(BigDecimal.ZERO); }

        try { nl.setSoLuong(Double.parseDouble(txtSoLuong.getText().trim())); } catch (Exception ignored) {}
        try { nl.setHeSoQuyDoi(Double.parseDouble(txtHeSo.getText().trim())); } catch (Exception e) { nl.setHeSoQuyDoi(1.0); }
        try { nl.setMucTonToiThieu(Double.parseDouble(txtMin.getText().trim())); } catch (Exception ignored) {}
        try { nl.setMucTonToiDa(Double.parseDouble(txtMax.getText().trim())); } catch (Exception e) { nl.setMucTonToiDa(9999); }
        return nl;
    }

    private void loadToForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        selectedMaNL = (int) tableModel.getValueAt(modelRow, 0);
        NguyenLieu nl = NguyenLieuDAO.getById(selectedMaNL);
        if (nl == null) return;

        setField(txtTen,      nl.getTenNguyenLieu());
        setField(txtDVT,      nl.getDonViTinh());
        // Hiển thị đơn giá dạng VNĐ
        if (nl.getDonGia() != null) {
            txtDonGia.setText(VND_FMT.format(nl.getDonGia().longValue()) + " VND");
            txtDonGia.setForeground(TEXT_DARK);
        } else {
            txtDonGia.setText("0 VND");
            txtDonGia.setForeground(TEXT_DARK);
        }
        setField(txtSoLuong,  String.valueOf(nl.getSoLuong()));
        setField(txtDVTNhap,  nl.getDonViTinhNhap() != null ? nl.getDonViTinhNhap() : "");
        setField(txtHeSo,     String.valueOf(nl.getHeSoQuyDoi()));
        setField(txtMin,      String.valueOf(nl.getMucTonToiThieu()));
        setField(txtMax,      String.valueOf(nl.getMucTonToiDa()));
        setField(txtGhiChu,   nl.getGhiChu() != null ? nl.getGhiChu() : "");
        cboTrangThai.setSelectedIndex(nl.getTrangThai() == 1 ? 0 : 1);

        btnThem.setEnabled(false);
        btnSua.setEnabled(true);
        btnXoa.setEnabled(true);
    }

    private void clearForm() {
        selectedMaNL = -1;
        resetField(txtTen,      "Nhap ten nguyen lieu...");
        resetField(txtDVT,      "Nhap don vi tinh...");
        resetField(txtDVTNhap,  "Don vi tinh nhap hang...");
        resetField(txtGhiChu,   "Ghi chu them...");
        txtDonGia.setText("Nhap don gia...");
        txtDonGia.setForeground(TEXT_MID);
        txtSoLuong.setText("0"); txtSoLuong.setForeground(TEXT_DARK);
        txtHeSo.setText("1.0");  txtHeSo.setForeground(TEXT_DARK);
        txtMin.setText("0");     txtMin.setForeground(TEXT_DARK);
        txtMax.setText("9999");  txtMax.setForeground(TEXT_DARK);
        cboTrangThai.setSelectedIndex(0);
        table.clearSelection();
        btnThem.setEnabled(true);
        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);
    }

    // ─────────────────────── UI HELPERS ───────────────────────

    /** TextField thường có placeholder */
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

    /** TextField giá tiền: focus vào bóc số thuần, focus ra format VNĐ */
    private JTextField createPriceField() {
        JTextField tf = new JTextField("Nhap don gia...");
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(TEXT_MID);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(0, 42));

        // Chỉ nhận ký tự số
        tf.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE)
                    e.consume();
            }
        });

        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                String cur = tf.getText().trim();
                if (cur.equals("Nhap don gia...")) tf.setText("");
                else tf.setText(cur.replaceAll("[^0-9]", ""));
                tf.setForeground(TEXT_DARK);
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2), new EmptyBorder(7, 11, 7, 11)));
                tf.setBackground(new Color(232, 244, 255));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 12, 8, 12)));
                tf.setBackground(Color.WHITE);
                String raw = tf.getText().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) {
                    tf.setText("Nhap don gia..."); tf.setForeground(TEXT_MID);
                } else {
                    try {
                        long val = Long.parseLong(raw);
                        tf.setText(VND_FMT.format(val) + " VND");
                        tf.setForeground(TEXT_DARK);
                    } catch (NumberFormatException ex) { tf.setForeground(DANGER); }
                }
            }
        });
        return tf;
    }

    /** TextField dùng cho thanh tìm kiếm — vẽ placeholder bằng paintComponent */
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
        tf.setText("");
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    /** JComboBox styling đồng bộ */
    private void styleComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        cb.setPreferredSize(new Dimension(0, 42));
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Button to, bo tròn, màu rõ ràng */
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

    /** Hàng 2 nút song song full-width, cao 44px */
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

    /** Label + field xếp dọc trong sidebar */
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

    /** Section header với đường kẻ màu PRIMARY bên dưới */
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

    /** Hộp hướng dẫn */
    private JPanel buildHintBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(230, 244, 255));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(170, 210, 240), 1),
            new EmptyBorder(10, 12, 10, 12)));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel ht = new JLabel("Huong dan:");
        ht.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ht.setForeground(new Color(30, 70, 130));
        ht.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(ht);
        box.add(Box.createVerticalStrut(5));

        for (String line : new String[]{
                "- Chon nguyen lieu tren bang de sua/xoa.",
                "- Don gia tu dong dinh dang VND khi nhap.",
                "- Xoa mem: nguyen lieu chuyen sang Ngung dung."}) {
            JLabel hl = new JLabel(line);
            hl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hl.setForeground(TEXT_DARK);
            hl.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(hl);
        }
        return box;
    }

    /** Chip legend màu */
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
    private void err(String msg) { JOptionPane.showMessageDialog(this, msg, "Loi", JOptionPane.ERROR_MESSAGE); }
}