package view.NguyenLieu;

import config.UserSession;
import dao.NguyenLieuDAO;
import dao.XuatKhoDAO;
import model.LichSuXuatKho;
import model.NguyenLieu;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class FormXuatKho extends JPanel {

    // ─────────────── Màu sắc (đồng bộ hệ thống) ───────────────
    private static final Color NAV_BG       = new Color(25,  45,  85);
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

    // ─────────────── Form fields ───────────────
    private JComboBox<NguyenLieu> cboNL;
    private JTextField txtSoLuong, txtLyDo, txtGhiChu;
    private JLabel     lblTonHienTai, lblDVT, lblTrangThai;

    // ─────────────── Buttons ───────────────
    private JButton btnXuat, btnLamMoi;

    // ─────────────── Bảng lịch sử ───────────────
    private JTable            table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField        txtSearch;

    // ─────────────── Filter ───────────────
    private JTextField txtNgayLoc;
    private JButton    btnLoc;

    // ════════════════════════════════════════════
    public FormXuatKho() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        buildUI();
        loadCombo();
        loadData(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
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

        // ── Chọn nguyên liệu ──
        content.add(sectionHeader("Chon Nguyen Lieu Xuat"));
        content.add(vgap(10));

        cboNL = new JComboBox<>();
        cboNL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNL.setBackground(Color.WHITE);
        cboNL.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        cboNL.setPreferredSize(new Dimension(0, 42));
        cboNL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cboNL.addActionListener(e -> refreshTonKho());

        content.add(formRowCombo("Nguyen Lieu *", cboNL)); content.add(vgap(14));

        // ── Thông tin tồn kho ──
        content.add(sectionHeader("Thong Tin Ton Kho Hien Tai"));
        content.add(vgap(10));

        lblTonHienTai = new JLabel("—");
        lblTonHienTai.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTonHienTai.setForeground(SUCCESS);
        lblTonHienTai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        lblTonHienTai.setOpaque(true);
        lblTonHienTai.setBackground(new Color(240, 255, 245));

        lblDVT = new JLabel("—");
        lblDVT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDVT.setForeground(TEXT_DARK);
        lblDVT.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        lblDVT.setOpaque(true);
        lblDVT.setBackground(Color.WHITE);

        lblTrangThai = new JLabel("—");
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTrangThai.setForeground(SUCCESS);
        lblTrangThai.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        lblTrangThai.setOpaque(true);
        lblTrangThai.setBackground(Color.WHITE);

        content.add(formRowLabel("Ton Hien Tai", lblTonHienTai)); content.add(vgap(8));
        content.add(formRowLabel("Don Vi Tinh",  lblDVT));         content.add(vgap(8));
        content.add(formRowLabel("Trang Thai",   lblTrangThai));   content.add(vgap(14));

        // ── Cảnh báo ──
        JPanel warnBox = new JPanel(new BorderLayout());
        warnBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        warnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        warnBox.setBackground(new Color(255, 243, 205));
        warnBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(241, 196, 15), 1),
            new EmptyBorder(6, 10, 6, 10)));
        JLabel warnLbl = new JLabel("Nguyen lieu vang = sap het, do = het kho");
        warnLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        warnLbl.setForeground(new Color(100, 70, 0));
        warnBox.add(warnLbl, BorderLayout.CENTER);
        content.add(warnBox); content.add(vgap(14));

        // ── Thông tin xuất kho ──
        content.add(sectionHeader("Thong Tin Xuat Kho"));
        content.add(vgap(10));

        txtSoLuong = createField("So luong xuat...");
        txtLyDo    = createField("Ly do xuat...");
        txtGhiChu  = createField("Ghi chu them...");
        txtLyDo.setText("Su dung san xuat");
        txtLyDo.setForeground(TEXT_DARK);

        content.add(formRow("So Luong Xuat *", txtSoLuong)); content.add(vgap(8));
        content.add(formRow("Ly Do Xuat",      txtLyDo));    content.add(vgap(8));
        content.add(formRow("Ghi Chu",         txtGhiChu));  content.add(vgap(14));

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        content.add(sep); content.add(vgap(14));

        // ── Thao tác ──
        content.add(sectionHeader("Thao Tac"));
        content.add(vgap(10));

        btnXuat   = bigBtn("Xuat Kho",  DANGER);
        btnLamMoi = bigBtn("Lam Moi", WARNING);
        btnXuat  .addActionListener(e -> xuatKho());
        btnLamMoi.addActionListener(e -> clearForm());

        content.add(btnRow(btnXuat, btnLamMoi)); content.add(vgap(16));

        // ── Hướng dẫn ──
        content.add(buildHintBox());
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

    // ─────────────────────── BẢNG LỊCH SỬ (60%) ───────────────────────
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

        JLabel title = new JLabel("LICH SU XUAT KHO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAV_BG);

        // Legend + Filter ngày
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legend.setBackground(Color.WHITE);
        legend.add(legendChip(new Color(240, 248, 255), "Ban hang tu dong"));
        legend.add(legendChip(Color.WHITE,              "Xuat thu cong"));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        filterRow.setBackground(Color.WHITE);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        txtNgayLoc = miniField(today);
        btnLoc     = smallBtn("Xem Ngay", PRIMARY);
        btnLoc.addActionListener(e -> {
            try {
                String db = LocalDate.parse(txtNgayLoc.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                loadData(db);
            } catch (Exception ex) { err("Ngay khong hop le (dd/MM/yyyy)!"); }
        });
        filterRow.add(new JLabel("Ngay:")); filterRow.add(txtNgayLoc); filterRow.add(btnLoc);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 2));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(legend,    BorderLayout.NORTH);
        rightPanel.add(filterRow, BorderLayout.SOUTH);

        headerRow.add(title,      BorderLayout.WEST);
        headerRow.add(rightPanel, BorderLayout.EAST);

        // Thanh tìm kiếm
        JPanel searchBar = buildSearchBar();

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerRow, BorderLayout.NORTH);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // Table model
        String[] cols = {"Ma", "Ngay Xuat", "Nguyen Lieu", "SL Xuat", "DVT", "Ly Do", "Mon Xuat", "NV", "Ghi Chu"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        setupTable();

        // Renderer màu theo loại xuất
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int modelRow = table.convertRowIndexToModel(r);
                    String lyDo = String.valueOf(tableModel.getValueAt(modelRow, 5));
                    if (lyDo.startsWith("Ban hang"))
                        comp.setBackground(new Color(240, 248, 255));
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

        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sc.getViewport().setBackground(Color.WHITE);

        JLabel note = new JLabel("  * Dong nen xanh nhat = xuat tu dong theo hoa don ban hang");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(TEXT_MID);
        note.setBorder(new EmptyBorder(6, 0, 0, 0));

        p.add(topPanel, BorderLayout.NORTH);
        p.add(sc,       BorderLayout.CENTER);
        p.add(note,     BorderLayout.SOUTH);
        return p;
    }

    // ─────────────────────── SEARCH BAR ───────────────────────
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 0, 4, 0));

        txtSearch = createSearchField("Nhap tu khoa tim kiem (ten nguyen lieu, ly do, mon xuat...)");

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

        // Live filter theo Tên Nguyên Liệu (cột 2)
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

    /** Lọc theo Tên Nguyên Liệu (cột 2) */
    private void doFilter(JLabel lblCount) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 2));
            int matched = table.getRowCount();
            int total   = tableModel.getRowCount();
            lblCount.setText(matched + "/" + total + " dong");
        }
    }

    // ─────────────────────── LOGIC ───────────────────────
    private void loadCombo() {
        cboNL.removeAllItems();
        for (NguyenLieu nl : NguyenLieuDAO.getAll()) cboNL.addItem(nl);
        refreshTonKho();
    }

    private void refreshTonKho() {
        NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
        if (nl == null) {
            lblTonHienTai.setText("—"); lblDVT.setText("—"); lblTrangThai.setText("—");
            lblTonHienTai.setBackground(Color.WHITE);
            return;
        }
        NguyenLieu fresh = NguyenLieuDAO.getById(nl.getMaNL());
        if (fresh == null) return;

        lblDVT.setText(fresh.getDonViTinh());
        String tt = fresh.getTrangThaiTon();
        lblTonHienTai.setText(String.format("%.3f %s", fresh.getSoLuong(), fresh.getDonViTinh()));

        if ("Het hang".equals(tt) || "Hết hàng".equals(tt)) {
            lblTonHienTai.setForeground(DANGER);
            lblTonHienTai.setBackground(new Color(255, 235, 235));
            lblTrangThai.setText("Het hang");
            lblTrangThai.setForeground(DANGER);
            lblTrangThai.setBackground(new Color(255, 235, 235));
        } else if ("Sap het".equals(tt) || "Sắp hết".equals(tt)) {
            lblTonHienTai.setForeground(WARNING);
            lblTonHienTai.setBackground(new Color(255, 248, 220));
            lblTrangThai.setText("Sap het");
            lblTrangThai.setForeground(WARNING);
            lblTrangThai.setBackground(new Color(255, 248, 220));
        } else {
            lblTonHienTai.setForeground(SUCCESS);
            lblTonHienTai.setBackground(new Color(240, 255, 245));
            lblTrangThai.setText("Du ton kho");
            lblTrangThai.setForeground(SUCCESS);
            lblTrangThai.setBackground(new Color(240, 255, 245));
        }
    }

    private void loadData(String ngayDB) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmtView = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (LichSuXuatKho xk : XuatKhoDAO.getByDate(ngayDB)) {
            tableModel.addRow(new Object[]{
                xk.getMaXuat(),
                xk.getNgayXuat() != null ? xk.getNgayXuat().format(fmtView) : "",
                xk.getTenNguyenLieu(),
                String.format("%.3f", xk.getSoLuong()),
                xk.getDonViTinh(),
                xk.getLyDo(),
                xk.getTenMon()  != null ? xk.getTenMon()  : "—",
                xk.getTenNV()   != null ? xk.getTenNV()   : "—",
                xk.getGhiChu()  != null ? xk.getGhiChu()  : ""
            });
        }
    }

    private void xuatKho() {
        NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
        if (nl == null) { err("Chon nguyen lieu!"); return; }
        double sl;
        try {
            sl = Double.parseDouble(txtSoLuong.getText().trim());
            if (sl <= 0) throw new Exception();
        } catch (Exception e) { err("So luong phai > 0!"); return; }

        NguyenLieu fresh = NguyenLieuDAO.getById(nl.getMaNL());
        if (fresh != null && fresh.getSoLuong() < sl) {
            err("Khong du ton kho!\nTon hien tai: " + fresh.getSoLuong() + " " + fresh.getDonViTinh());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Xuat " + sl + " " + nl.getDonViTinh() + " [" + nl.getTenNguyenLieu() + "]?",
            "Xac Nhan Xuat Kho", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        LichSuXuatKho xk = new LichSuXuatKho();
        xk.setMaNL(nl.getMaNL());
        xk.setSoLuong(sl);
        xk.setDonViTinh(nl.getDonViTinh());
        xk.setLyDo(txtLyDo.getText().trim().isEmpty() ? "Xuat thu cong" : txtLyDo.getText().trim());
        xk.setGhiChu(txtGhiChu.getText().trim());
        int maNV = UserSession.getInstance().getCurrentUser() != null
            ? UserSession.getInstance().getCurrentUser().getMaNV() : 0;
        xk.setMaNV(maNV);

        if (XuatKhoDAO.xuatKhoThuCong(xk)) {
            JOptionPane.showMessageDialog(this, "Xuat kho thanh cong!", "OK", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            err("Khong the xuat kho! Ton kho khong du hoac co loi.");
        }
    }

    private void clearForm() {
        resetField(txtSoLuong, "So luong xuat...");
        txtLyDo.setText("Su dung san xuat"); txtLyDo.setForeground(TEXT_DARK);
        resetField(txtGhiChu, "Ghi chu them...");
        refreshTonKho();
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

    private JTextField miniField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setPreferredSize(new Dimension(110, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(4, 8, 4, 8)));
        return tf;
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

    private JButton smallBtn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(90, 30));
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

    private JPanel formRowLabel(String labelText, JLabel comp) {
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
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setBackground(NAV_BG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        });

        int[] ws = {45, 130, 160, 70, 60, 130, 120, 80, 120};
        for (int i = 0; i < ws.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
    }

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
        box.add(ht); box.add(Box.createVerticalStrut(5));

        for (String line : new String[]{
                "- Chon nguyen lieu, nhap so luong can xuat.",
                "- Kiem tra ton kho truoc khi xuat.",
                "- Tim kiem lich su theo ten nguyen lieu.",
                "- Dong xanh nhat = xuat theo hoa don ban hang."}) {
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
    private void resetField(JTextField f, String ph) { f.setText(ph); f.setForeground(TEXT_MID); }
    private void err(String msg) { JOptionPane.showMessageDialog(this, msg, "Loi", JOptionPane.ERROR_MESSAGE); }
}