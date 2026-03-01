package view.NguyenLieu;

import config.UserSession;
import dao.NguyenLieuDAO;
import dao.PhieuNhapHangDAO;
import model.ChiTietPhieuNhap;
import model.NguyenLieu;
import model.PhieuNhapHang;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class FormNhapHang extends JPanel {

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
    private JTextField txtNhaCungCap, txtGhiChuPhieu;
    private JLabel     lblTongTien;

    // ─────────────── Thêm dòng ───────────────
    private JComboBox<NguyenLieu> cboNL;
    private JTextField txtSoLuong, txtDonGiaNhap, txtDVTNhap, txtHanSD;

    // ─────────────── Buttons ───────────────
    private JButton btnThemDong, btnXoaDong;
    private JButton btnLuuPhieu, btnHuyPhieu;

    // ─────────────── Bảng chi tiết đang soạn ───────────────
    private JTable           tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private List<ChiTietPhieuNhap> danhSachCT = new ArrayList<>();

    // ─────────────── Bảng lịch sử phiếu ───────────────
    private JTable            tblPhieu;
    private DefaultTableModel modelPhieu;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField        txtSearch;

    // ─────────────── Filter lịch sử ───────────────
    private JTextField txtTuNgay, txtDenNgay;
    private JButton    btnLoc, btnXemChiTiet;

    // ════════════════════════════════════════════
    public FormNhapHang() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        buildUI();
        loadComboNL();
        loadLichSuPhieu();
    }

    // ─────────────────────── BUILD UI ───────────────────────
    private void buildUI() {
        // Center: sidebar 40% + bảng 60%
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

        // ── Thông tin phiếu nhập ──
        content.add(sectionHeader("Thong Tin Phieu Nhap"));
        content.add(vgap(10));

        txtNhaCungCap  = createField("Ten nha cung cap...");
        txtGhiChuPhieu = createField("Ghi chu phieu...");

        content.add(formRow("Nha Cung Cap", txtNhaCungCap));  content.add(vgap(8));
        content.add(formRow("Ghi Chu Phieu", txtGhiChuPhieu)); content.add(vgap(8));

        // Tổng tiền hiển thị
        lblTongTien = new JLabel("0 VND");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTien.setForeground(DANGER);
        lblTongTien.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        lblTongTien.setOpaque(true);
        lblTongTien.setBackground(new Color(255, 245, 245));
        content.add(formRowLabel("Tong Tien Phieu", lblTongTien)); content.add(vgap(14));

        // ── Thêm dòng nguyên liệu ──
        content.add(sectionHeader("Them Dong Nguyen Lieu"));
        content.add(vgap(10));

        cboNL = new JComboBox<>();
        cboNL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNL.setBackground(Color.WHITE);
        cboNL.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        cboNL.setPreferredSize(new Dimension(0, 42));
        cboNL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        txtSoLuong    = createField("So luong nhap...");
        txtDonGiaNhap = createField("Don gia nhap...");
        txtDVTNhap    = createField("DVT nhap hang...");
        txtHanSD      = createField("Han SD (dd/MM/yyyy)...");

        content.add(formRowCombo("Nguyen Lieu *", cboNL));  content.add(vgap(8));
        content.add(formRow("So Luong *",      txtSoLuong));    content.add(vgap(8));
        content.add(formRow("Don Gia Nhap *",  txtDonGiaNhap)); content.add(vgap(8));
        content.add(formRow("DVT Nhap",        txtDVTNhap));    content.add(vgap(8));
        content.add(formRow("Han Su Dung",     txtHanSD));      content.add(vgap(14));

        // Buttons thêm/xóa dòng
        btnThemDong = bigBtn("Them Dong", SUCCESS);
        btnXoaDong  = bigBtn("Xoa Dong",  DANGER);
        btnThemDong.addActionListener(e -> themDong());
        btnXoaDong .addActionListener(e -> xoaDong());
        content.add(btnRow(btnThemDong, btnXoaDong)); content.add(vgap(16));

        // ── Bảng chi tiết mini ──
        content.add(sectionHeader("Chi Tiet Phieu Dang Soan"));
        content.add(vgap(8));

        String[] colsCT = {"STT", "Nguyen Lieu", "SL", "Don Gia", "Thanh Tien"};
        modelChiTiet = new DefaultTableModel(colsCT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        setupMiniTable(tblChiTiet);
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(50);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(90);

        JScrollPane scCT = new JScrollPane(tblChiTiet);
        scCT.setAlignmentX(Component.LEFT_ALIGNMENT);
        scCT.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        scCT.setPreferredSize(new Dimension(0, 160));
        scCT.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        content.add(scCT); content.add(vgap(14));

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        content.add(sep); content.add(vgap(14));

        // ── Lưu / Hủy phiếu ──
        content.add(sectionHeader("Thao Tac Phieu"));
        content.add(vgap(10));

        btnLuuPhieu = bigBtn("Luu Phieu", SUCCESS);
        btnHuyPhieu = bigBtn("Huy Phieu", DANGER);
        btnLuuPhieu.addActionListener(e -> luuPhieu());
        btnHuyPhieu.addActionListener(e -> huyPhieu());
        content.add(btnRow(btnLuuPhieu, btnHuyPhieu)); content.add(vgap(16));

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

        JLabel title = new JLabel("LICH SU PHIEU NHAP HANG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAV_BG);

        // Filter ngày
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setBackground(Color.WHITE);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        txtTuNgay  = miniField(today);
        txtDenNgay = miniField(today);
        btnLoc         = smallBtn("Loc",      PRIMARY);
        btnXemChiTiet  = smallBtn("Xem CT",   WARNING);
        btnLoc.addActionListener(e -> loadLichSuPhieu());
        btnXemChiTiet.addActionListener(e -> xemChiTietPhieu());

        filterRow.add(new JLabel("Tu:")); filterRow.add(txtTuNgay);
        filterRow.add(new JLabel("Den:")); filterRow.add(txtDenNgay);
        filterRow.add(btnLoc);
        filterRow.add(btnXemChiTiet);

        headerRow.add(title,     BorderLayout.WEST);
        headerRow.add(filterRow, BorderLayout.EAST);

        // Thanh tìm kiếm
        JPanel searchBar = buildSearchBar();

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerRow,  BorderLayout.NORTH);
        topPanel.add(searchBar,  BorderLayout.SOUTH);

        // Table model
        String[] cols = {"Ma Phieu", "Ngay Nhap", "Nha Cung Cap", "NV Nhap", "Tong Tien", "Trang Thai"};
        modelPhieu = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhieu = new JTable(modelPhieu);
        rowSorter = new TableRowSorter<>(modelPhieu);
        tblPhieu.setRowSorter(rowSorter);
        setupTable(tblPhieu);

        // Renderer
        tblPhieu.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(ROW_SEL);
                    comp.setForeground(NAV_BG);
                }
                return comp;
            }
        });

        JScrollPane sc = new JScrollPane(tblPhieu);
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

        txtSearch = createSearchField("Nhap tu khoa tim kiem phieu nhap (nha cung cap, ma phieu...)");

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

        // Live filter theo nhà cung cấp (cột 2)
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

    /** Lọc theo Nhà Cung Cấp (cột 2) */
    private void doFilter(JLabel lblCount) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 2));
            int matched = tblPhieu.getRowCount();
            int total   = modelPhieu.getRowCount();
            lblCount.setText(matched + "/" + total + " phieu");
        }
    }

    // ─────────────────────── LOGIC ───────────────────────
    private void loadComboNL() {
        cboNL.removeAllItems();
        for (NguyenLieu nl : NguyenLieuDAO.getAll()) cboNL.addItem(nl);
    }

    private void loadLichSuPhieu() {
        modelPhieu.setRowCount(0);
        DateTimeFormatter fmtIn = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtDB = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            String tu  = LocalDate.parse(txtTuNgay.getText().trim(), fmtIn).format(fmtDB);
            String den = LocalDate.parse(txtDenNgay.getText().trim(), fmtIn).format(fmtDB);
            for (PhieuNhapHang p : PhieuNhapHangDAO.getByDateRange(tu, den)) addPhieuRow(p);
        } catch (Exception e) {
            for (PhieuNhapHang p : PhieuNhapHangDAO.getAll()) addPhieuRow(p);
        }
    }

    private void addPhieuRow(PhieuNhapHang p) {
        modelPhieu.addRow(new Object[]{
            p.getMaPhieu(),
            p.getNgayNhap() != null
                ? p.getNgayNhap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "",
            p.getNhaCungCap(),
            p.getTenNV(),
            String.format("%,.0f VND", p.getTongTien()),
            p.getTrangThai()
        });
    }

    private void themDong() {
        NguyenLieu nl = (NguyenLieu) cboNL.getSelectedItem();
        if (nl == null) { err("Chon nguyen lieu!"); return; }
        double sl;
        BigDecimal dg;
        try {
            sl = Double.parseDouble(txtSoLuong.getText().trim());
            if (sl <= 0) throw new Exception();
        } catch (Exception e) { err("So luong phai > 0!"); return; }
        try {
            dg = new BigDecimal(txtDonGiaNhap.getText().replaceAll("[^0-9.]", ""));
            if (dg.compareTo(BigDecimal.ZERO) < 0) throw new Exception();
        } catch (Exception e) { err("Don gia khong hop le!"); return; }

        ChiTietPhieuNhap ct = new ChiTietPhieuNhap(nl.getMaNL(), sl, txtDVTNhap.getText().trim(), dg);
        ct.setTenNguyenLieu(nl.getTenNguyenLieu());
        String hanStr = txtHanSD.getText().trim();
        if (!hanStr.isEmpty()) {
            try { ct.setHanSuDung(LocalDate.parse(hanStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))); }
            catch (Exception ignored) {}
        }
        danhSachCT.add(ct);
        refreshChiTietTable();
        txtSoLuong.setText(""); txtDonGiaNhap.setText("");
        txtDVTNhap.setText("");  txtHanSD.setText("");
    }

    private void xoaDong() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) { err("Chon dong can xoa!"); return; }
        danhSachCT.remove(row);
        refreshChiTietTable();
    }

    private void refreshChiTietTable() {
        modelChiTiet.setRowCount(0);
        BigDecimal tong = BigDecimal.ZERO;
        for (int i = 0; i < danhSachCT.size(); i++) {
            ChiTietPhieuNhap ct = danhSachCT.get(i);
            BigDecimal tt = ct.tinhThanhTien();
            tong = tong.add(tt);
            modelChiTiet.addRow(new Object[]{
                i + 1,
                ct.getTenNguyenLieu(),
                ct.getSoLuong(),
                String.format("%,.0f", ct.getDonGiaNhap()),
                String.format("%,.0f", tt)
            });
        }
        lblTongTien.setText(String.format("%,.0f VND", tong));
    }

    private void luuPhieu() {
        if (danhSachCT.isEmpty()) { err("Chua co dong nao trong phieu!"); return; }
        PhieuNhapHang phieu = new PhieuNhapHang();
        phieu.setNhaCungCap(txtNhaCungCap.getText().trim());
        phieu.setGhiChu(txtGhiChuPhieu.getText().trim());
        phieu.setDanhSachChiTiet(new ArrayList<>(danhSachCT));
        phieu.tinhLaiTongTien();
        int maNV = UserSession.getInstance().getCurrentUser() != null
            ? UserSession.getInstance().getCurrentUser().getMaNV() : 0;
        phieu.setMaNV(maNV);

        int maPhieu = PhieuNhapHangDAO.addPhieu(phieu);
        if (maPhieu > 0) {
            JOptionPane.showMessageDialog(this,
                "Luu phieu nhap #" + maPhieu + " thanh cong!\nKho da duoc cap nhat.",
                "OK", JOptionPane.INFORMATION_MESSAGE);
            huyPhieu();
            loadLichSuPhieu();
        } else err("Khong the luu phieu! Kiem tra lai du lieu.");
    }

    private void huyPhieu() {
        danhSachCT.clear();
        refreshChiTietTable();
        resetField(txtNhaCungCap,  "Ten nha cung cap...");
        resetField(txtGhiChuPhieu, "Ghi chu phieu...");
    }

    private void xemChiTietPhieu() {
        int row = tblPhieu.getSelectedRow();
        if (row < 0) { err("Chon phieu can xem!"); return; }
        int modelRow = tblPhieu.convertRowIndexToModel(row);
        int maPhieu  = (int) modelPhieu.getValueAt(modelRow, 0);
        List<ChiTietPhieuNhap> list = PhieuNhapHangDAO.getChiTiet(maPhieu);

        String[] cols = {"Nguyen Lieu", "So Luong", "DVT", "Don Gia", "Thanh Tien", "Han SD"};
        DefaultTableModel m = new DefaultTableModel(cols, 0);
        for (ChiTietPhieuNhap ct : list) {
            m.addRow(new Object[]{
                ct.getTenNguyenLieu(), ct.getSoLuong(), ct.getDonViTinh(),
                String.format("%,.0f VND", ct.getDonGiaNhap()),
                String.format("%,.0f VND", ct.tinhThanhTien()),
                ct.getHanSuDung() != null
                    ? ct.getHanSuDung().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            });
        }
        JTable t = new JTable(m);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(30);
        JOptionPane.showMessageDialog(this, new JScrollPane(t),
            "Chi Tiet Phieu Nhap #" + maPhieu, JOptionPane.PLAIN_MESSAGE);
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
        tf.setPreferredSize(new Dimension(95, 30));
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
        b.setPreferredSize(new Dimension(80, 30));
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

    private void setupTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(38);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setGridColor(new Color(235, 238, 242));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                lbl.setBackground(NAV_BG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        });
    }

    private void setupMiniTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(28);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setGridColor(new Color(235, 238, 242));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setPreferredSize(new Dimension(0, 32));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                lbl.setBackground(NAV_BG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setBorder(new EmptyBorder(0, 6, 0, 6));
                return lbl;
            }
        });
    }

    private JPanel buildHintBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(230, 244, 255));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(170, 210, 240), 1),
            new EmptyBorder(10, 12, 10, 12)));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel ht = new JLabel("Huong dan:");
        ht.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ht.setForeground(new Color(30, 70, 130));
        ht.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(ht); box.add(Box.createVerticalStrut(5));

        for (String line : new String[]{
                "- Chon nguyen lieu va dien so luong, don gia.",
                "- Nhan 'Them Dong' de them vao phieu.",
                "- Nhan 'Luu Phieu' de luu va cap nhat kho.",
                "- Tim kiem phieu theo ten nha cung cap."}) {
            JLabel hl = new JLabel(line);
            hl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hl.setForeground(TEXT_DARK);
            hl.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(hl);
        }
        return box;
    }

    private Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
    private void resetField(JTextField f, String ph) { f.setText(ph); f.setForeground(TEXT_MID); }
    private void err(String msg) { JOptionPane.showMessageDialog(this, msg, "Loi", JOptionPane.ERROR_MESSAGE); }
}