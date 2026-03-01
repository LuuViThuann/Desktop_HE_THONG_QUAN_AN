package view;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

import config.DatabaseConfig;
import dao.MonAnDAO;
import model.Nhom;
import view.NguyenLieu.FormCongThucMon;

/**
 * FormThemMonAn - Cập nhật:
 *  - Sidebar 40%, cuộn được
 *  - Buttons to 44px rõ ràng
 *  - Ô Giá Tiền định dạng VNĐ tự động (123.000 VNĐ)
 *  - Thanh tìm kiếm trên bảng, lọc live theo tên/nhóm
 *  - Bảng công thức NL đầy đủ cột, to rõ
 */
public class FormThemMonAn extends JFrame {

    // ─────────────── Fields ───────────────
    private JTextField txtTenMon, txtGiaTien, txtDonViTinh, txtSoLuong;
    private JComboBox<Nhom> cboNhom;
    private JLabel lblImagePreview;
    private JButton btnChonAnh, btnThem, btnSua, btnXoa, btnLamMoi, btnTinhLaiSL, btnCongThuc;

    private JTable tableMonAn;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;   // để lọc
    private JTextField txtSearch;                          // thanh tìm kiếm

    private int selectedMaMon = -1;

    private String selectedImagePath = null;
    private String savedImageName    = null;

    // Định dạng tiền VNĐ (dấu phẩy ngăn cách hàng nghìn)
    private static final DecimalFormat VND_FMT;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("vi","VN"));
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        VND_FMT = new DecimalFormat("#,###", sym);
    }

    private boolean isFormattingPrice = false; // cờ tránh vòng lặp khi format

    private static final String IMAGE_FOLDER = "src/Assets/images/";

    // ─────────────── Màu sắc ───────────────
    private static final Color NAV_BG       = new Color(25,  45,  85);
    private static final Color SUBNAV_BG    = new Color(35,  58, 105);
    private static final Color SUBNAV_ACT   = new Color(55,  95, 160);
    private static final Color SIDEBAR_BG   = new Color(255, 255, 255);
    private static final Color CONTENT_BG   = new Color(243, 246, 250);
    private static final Color PRIMARY      = new Color(52, 130, 200);
    private static final Color SUCCESS      = new Color(39, 174,  96);
    private static final Color DANGER       = new Color(192,  57,  43);
    private static final Color WARNING      = new Color(230, 170,  20);
    private static final Color PURPLE       = new Color(142,  68, 173);
    private static final Color NAVY_BTN     = new Color(41,  82, 163);
    private static final Color TEXT_DARK    = new Color(44,  62,  80);
    private static final Color TEXT_MID     = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(214, 220, 229);
    private static final Color INFO_BLUE    = new Color(52, 152, 219);
    private static final Color ROW_ALT      = new Color(248, 250, 253);
    private static final Color ROW_SEL      = new Color(213, 232, 255);
    private static final Color SEARCH_FOCUS = new Color(232, 244, 255);

    // ─────────── Panel NL ──────────
    private JPanel pnlNLInfo;

    // ════════════════════════════════════════
    public FormThemMonAn() {
        initComponents();
        loadNhomMonAn();
        setupLayout();
        loadMonAnData();
        setTitle("RESTAURANT MANAGER PRO - Quản Lý Món Ăn");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createImageFolderIfNotExists();
    }

    // ─────────────────────── INIT ───────────────────────
    private void initComponents() {
        txtTenMon    = createField("Nhập tên món ăn...");
        txtDonViTinh = createField("Nhập đơn vị tính...");
        txtSoLuong   = createField("0");

        // Ô giá tiền có định dạng VNĐ
        txtGiaTien = createPriceField();

        cboNhom = new JComboBox<>();
        cboNhom.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboNhom.setBackground(Color.WHITE);
        cboNhom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        cboNhom.setPreferredSize(new Dimension(0, 42));
        cboNhom.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblImagePreview = new JLabel("Chưa có ảnh", JLabel.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(240, 185));
        lblImagePreview.setMinimumSize(new Dimension(100, 140));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        lblImagePreview.setBackground(new Color(245, 248, 252));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblImagePreview.setForeground(TEXT_MID);

        btnChonAnh   = bigBtn("Chọn Ảnh",     new Color(100,116,139));
        btnThem      = bigBtn("Thêm",          SUCCESS);
        btnSua       = bigBtn("Sửa",           PRIMARY);
        btnXoa       = bigBtn("Xóa",           DANGER);
        btnLamMoi    = bigBtn("Làm Mới",       WARNING);
        btnTinhLaiSL = bigBtn("Tính Lại SL",  PURPLE);
        btnCongThuc  = bigBtn("Công Thức NL", NAVY_BTN);

        btnChonAnh  .addActionListener(e -> chonAnhMonAn());
        btnThem     .addActionListener(e -> themMonAn());
        btnSua      .addActionListener(e -> suaMonAn());
        btnXoa      .addActionListener(e -> xoaMonAn());
        btnLamMoi   .addActionListener(e -> clearForm());
        btnTinhLaiSL.addActionListener(e -> onTinhLaiSL());
        btnCongThuc .addActionListener(e -> moFormCongThuc());

        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);

        // Table + RowSorter để filter live
        String[] cols = {"Mã","Tên Món","Giá Tiền","ĐVT","SL Khả Dụng","SL Thủ Công","Nhóm","Hình Ảnh"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMonAn = new JTable(tableModel);
        rowSorter  = new TableRowSorter<>(tableModel);
        tableMonAn.setRowSorter(rowSorter);
        setupTable();

        // Thanh tìm kiếm
        txtSearch = createSearchField();

        pnlNLInfo = new JPanel();
        pnlNLInfo.setLayout(new BoxLayout(pnlNLInfo, BoxLayout.Y_AXIS));
        pnlNLInfo.setBackground(new Color(235, 246, 255));
        pnlNLInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INFO_BLUE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        pnlNLInfo.setVisible(false);
    }

    // ─────────────── TẠO Ô GIÁ TIỀN VNĐ ───────────────
    /**
     * Tạo JTextField tự động định dạng VNĐ:
     * - Khi focus mất → format "123.000 VNĐ"
     * - Khi focus vào → hiển thị số thuần để chỉnh sửa
     * - Khi gõ ký tự không phải số → bỏ qua
     */
    private JTextField createPriceField() {
        JTextField tf = new JTextField("Nhập giá tiền...") {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(TEXT_MID);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(0, 42));

        // Chỉ cho nhập số và dấu chấm/phẩy
        tf.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Cho phép số, backspace, delete, dấu phẩy (thập phân)
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE
                        && c != KeyEvent.VK_DELETE && c != ',' && c != '.') {
                    e.consume();
                }
            }
        });

        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                String cur = tf.getText().trim();
                // Xóa placeholder
                if (cur.equals("Nhập giá tiền...")) {
                    tf.setText("");
                    tf.setForeground(TEXT_DARK);
                } else {
                    // Bóc định dạng ra số thuần để edit
                    tf.setText(getRawNumber(cur));
                    tf.setForeground(TEXT_DARK);
                }
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2),
                    new EmptyBorder(7, 11, 7, 11)));
                tf.setBackground(SEARCH_FOCUS);
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(8, 12, 8, 12)));
                tf.setBackground(Color.WHITE);
                String raw = tf.getText().trim();
                if (raw.isEmpty()) {
                    tf.setText("Nhập giá tiền...");
                    tf.setForeground(TEXT_MID);
                } else {
                    // Parse và format
                    try {
                        long val = Long.parseLong(raw.replaceAll("[^0-9]", ""));
                        tf.setText(VND_FMT.format(val) + " VNĐ");
                        tf.setForeground(TEXT_DARK);
                    } catch (NumberFormatException ex) {
                        tf.setForeground(DANGER);
                    }
                }
            }
        });

        return tf;
    }

    /** Tách số thuần từ chuỗi đã định dạng, vd "123.000 VNĐ" → "123000" */
    private String getRawNumber(String formatted) {
        return formatted.replaceAll("[^0-9]", "");
    }

    /** Lấy giá trị số từ txtGiaTien để lưu DB */
    private double getPriceValue() {
        String raw = getRawNumber(txtGiaTien.getText().trim());
        if (raw.isEmpty()) return 0;
        try { return Double.parseDouble(raw); } catch (NumberFormatException e) { return 0; }
    }

    /** Set giá tiền vào ô txtGiaTien với định dạng VNĐ */
    private void setPriceField(double value) {
        txtGiaTien.setText(VND_FMT.format((long) value) + " VNĐ");
        txtGiaTien.setForeground(TEXT_DARK);
    }

    // ─────────────────────── LAYOUT CHÍNH ───────────────────────
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(CONTENT_BG);

        JPanel topStack = new JPanel(new BorderLayout());
        topStack.add(createNavBar(),  BorderLayout.NORTH);
        topStack.add(createSubNav(), BorderLayout.SOUTH);
        add(topStack, BorderLayout.NORTH);

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

    private JPanel createNavBar() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,NAV_BG,getWidth(),0,new Color(40,70,130)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        p.setPreferredSize(new Dimension(0, 50));

        JLabel appTitle = new JLabel("VUA LẨU - TITI - CHI NHÁNH CẦN THƠ");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appTitle.setForeground(Color.WHITE);
        appTitle.setBorder(new EmptyBorder(0, 20, 0, 30));

        JPanel navItems = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navItems.setOpaque(false);
       
        JLabel adminLbl = new JLabel("Admin  v1.0.0");
        adminLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminLbl.setForeground(new Color(170,195,230));
        adminLbl.setBorder(new EmptyBorder(0,0,0,20));

        p.add(appTitle, BorderLayout.WEST);
        p.add(navItems, BorderLayout.CENTER);
        p.add(adminLbl, BorderLayout.EAST);
        return p;
    }

    private JPanel createSubNav() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(SUBNAV_BG);
        p.setPreferredSize(new Dimension(0, 38));
        String[] tabs = {"Danh Mục Món Ăn"};
        for (int i=0; i<tabs.length; i++) {
            boolean active = i == 0;
            JLabel tab = new JLabel(tabs[i]);
            tab.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            tab.setForeground(active ? Color.WHITE : new Color(165,195,230));
            tab.setOpaque(active); tab.setBackground(SUBNAV_ACT);
            tab.setBorder(new EmptyBorder(10,18,10,18));
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ if(!active) tab.setForeground(Color.WHITE); }
                @Override public void mouseExited(MouseEvent e) { if(!active) tab.setForeground(new Color(165,195,230)); }
            });
            p.add(tab);
        }
        return p;
    }

    // ─────────────────────── SIDEBAR (40%) ───────────────────────
    private JPanel buildSidebar() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SIDEBAR_BG);
        content.setBorder(new EmptyBorder(16, 18, 20, 18));

        content.add(sectionHeader("Hình Ảnh"));
        content.add(vgap(8));
        JPanel imgWrap = new JPanel(new BorderLayout(0, 8));
        imgWrap.setBackground(SIDEBAR_BG);
        imgWrap.setAlignmentX(LEFT_ALIGNMENT);
        imgWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        imgWrap.add(lblImagePreview, BorderLayout.CENTER);
        btnChonAnh.setPreferredSize(new Dimension(0, 40));
        imgWrap.add(btnChonAnh, BorderLayout.SOUTH);
        content.add(imgWrap);
        content.add(vgap(16));

        content.add(sectionHeader("Thông Tin Món Ăn"));
        content.add(vgap(10));
        content.add(formRow("Tên Món Ăn *",    txtTenMon));    content.add(vgap(8));
        content.add(formRow("Giá Tiền (VNĐ) *", txtGiaTien)); content.add(vgap(8));
        content.add(formRow("Đơn Vị Tính *",   txtDonViTinh)); content.add(vgap(8));
        content.add(formRow("Nhóm Món *",       cboNhom));     content.add(vgap(14));

        content.add(sectionHeader("Số Lượng Khả Dụng"));
        content.add(vgap(8));
        txtSoLuong.setAlignmentX(LEFT_ALIGNMENT);
        txtSoLuong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        content.add(txtSoLuong);
        content.add(vgap(8));

        pnlNLInfo.setAlignmentX(LEFT_ALIGNMENT);
        pnlNLInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));
        content.add(pnlNLInfo);
        content.add(vgap(16));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        content.add(sep); content.add(vgap(14));

        content.add(sectionHeader("Thao Tác"));
        content.add(vgap(10));
        content.add(btnRow(btnThem,      btnSua));      content.add(vgap(8));
        content.add(btnRow(btnXoa,       btnLamMoi));   content.add(vgap(8));
        content.add(btnRow(btnTinhLaiSL, btnCongThuc)); content.add(vgap(16));

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

    // ─────────────────────── BẢNG DANH SÁCH (60%) ───────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 16, 16, 16)));

        // ── Header: tiêu đề + legend ──
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("DANH SÁCH MÓN ĂN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAV_BG);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legend.setBackground(Color.WHITE);
        legend.add(legendChip(new Color(255,200,200), "Hết nguyên liệu"));
        legend.add(legendChip(new Color(255,243,205), "Sắp hết"));
        legend.add(legendChip(new Color(209,231,221), "Đủ nguyên liệu"));

        headerRow.add(title,  BorderLayout.WEST);
        headerRow.add(legend, BorderLayout.EAST);

        // ── Thanh tìm kiếm ──
        JPanel searchBar = buildSearchBar();

        // ── Top panel gồm header + search ──
        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerRow,  BorderLayout.NORTH);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // ── ScrollPane bảng ──
        JScrollPane sc = new JScrollPane(tableMonAn);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sc.getViewport().setBackground(Color.WHITE);

        // Renderer màu theo SL
        tableMonAn.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c){
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                ((JLabel)comp).setBorder(new EmptyBorder(0,10,0,10));
                if (!sel) {
                    // lấy SL khả dụng qua model (cột 4)
                    int modelRow = tableMonAn.convertRowIndexToModel(r);
                    int sl = 0;
                    try { sl = Integer.parseInt(String.valueOf(tableModel.getValueAt(modelRow,4))); }
                    catch(Exception ignored){}
                    if      (sl<=0) comp.setBackground(new Color(255,200,200));
                    else if (sl<=5) comp.setBackground(new Color(255,243,205));
                    else            comp.setBackground(r%2==0?Color.WHITE:ROW_ALT);
                    comp.setForeground(TEXT_DARK);
                } else { comp.setBackground(ROW_SEL); comp.setForeground(NAV_BG); }
                return comp;
            }
        });

        p.add(topPanel, BorderLayout.NORTH);
        p.add(sc,       BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────── SEARCH BAR ───────────────────────
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 0, 4, 0));

        // Ô search (không icon)
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(new Color(247, 249, 252));
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 12, 6, 12)));

        searchWrap.add(txtSearch, BorderLayout.CENTER);

        // Nút xóa tìm kiếm
       
        // Label đếm kết quả
        JLabel lblCount = new JLabel();
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(TEXT_MID);
        lblCount.setBorder(new EmptyBorder(0, 8, 0, 0));

        // Lắng nghe thay đổi để filter live (chỉ tên món)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { doFilter(lblCount); }
            @Override public void removeUpdate(DocumentEvent e)  { doFilter(lblCount); }
            @Override public void changedUpdate(DocumentEvent e) { doFilter(lblCount); }
        });

        JPanel rightPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rightPart.setBackground(Color.WHITE);
      
        rightPart.add(lblCount);

        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(rightPart,  BorderLayout.EAST);
        return bar;
    }

    /** Lọc bảng chỉ theo Tên Món (cột 1) */
    private void doFilter(JLabel lblCount) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        } else {
            // Chỉ lọc cột 1 = Tên Món
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1));
            int matched = tableMonAn.getRowCount();
            int total   = tableModel.getRowCount();
            lblCount.setText(matched + "/" + total + " món");
        }
    }

    /** Tạo JTextField cho thanh tìm kiếm với placeholder "Nhập từ khóa tìm kiếm món ăn" */
    private JTextField createSearchField() {
        final String PLACEHOLDER = "Nhập từ khóa tìm kiếm món ăn";
        JTextField tf = new JTextField(PLACEHOLDER) {
            // Vẽ placeholder màu mờ khi chưa có nội dung và mất focus
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
                    g2.drawString(PLACEHOLDER, getInsets().left, y);
                }
            }
        };
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(new Color(247, 249, 252));
        tf.setCaretColor(PRIMARY);
        tf.setBorder(null);
        tf.setOpaque(false);
        // Placeholder text ban đầu là rỗng, tự vẽ bằng paintComponent
        tf.setText("");
        tf.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e) { tf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    // ─────────────────────── TABLE SETUP ───────────────────────
    private void setupTable() {
        tableMonAn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableMonAn.setRowHeight(38);
        tableMonAn.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMonAn.setGridColor(new Color(235,238,242));
        tableMonAn.setShowHorizontalLines(true);
        tableMonAn.setShowVerticalLines(false);
        tableMonAn.setIntercellSpacing(new Dimension(0,1));

        JTableHeader h = tableMonAn.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable t,Object v,boolean sel,boolean foc,int r,int c){
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                l.setBackground(NAV_BG); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI",Font.BOLD,13));
                l.setBorder(new EmptyBorder(0,10,0,10));
                return l;
            }
        });

        int[] ws = {50,210,130,70,110,110,140,170};
        for (int i=0; i<ws.length && i<tableMonAn.getColumnCount(); i++)
            tableMonAn.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        tableMonAn.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienThiThongTinMonAn();
        });
    }

    // ─────────────────────── LOAD DATA ───────────────────────
    private void loadNhomMonAn() {
        String sql = "SELECT MaNhom, TenNhom FROM Nhom ORDER BY TenNhom";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            cboNhom.removeAllItems();
            while (rs.next()) {
                Nhom n = new Nhom();
                n.setMaNhom(rs.getInt("MaNhom"));
                n.setTenNhom(rs.getString("TenNhom"));
                cboNhom.addItem(n);
            }
        } catch (SQLException e) { err("Lỗi tải nhóm: " + e.getMessage()); }
    }

    private void loadMonAnData() {
        String sql =
            "SELECT m.MaMon, m.TenMon, m.GiaTien, m.DonViTinh, m.SoLuongConLai, " +
            "n.TenNhom, m.HinhAnh, " +
            "COALESCE(FLOOR(MIN(nl.SoLuong/NULLIF(ct.SoLuongCan,0))),m.SoLuongConLai) AS SLKhaDung " +
            "FROM monan m " +
            "LEFT JOIN nhom n ON n.MaNhom=m.MaNhom " +
            "LEFT JOIN congthucmon ct ON ct.MaMon=m.MaMon " +
            "LEFT JOIN nguyenlieu nl ON nl.MaNL=ct.MaNL " +
            "GROUP BY m.MaMon,m.TenMon,m.GiaTien,m.DonViTinh,m.SoLuongConLai,n.TenNhom,m.HinhAnh " +
            "ORDER BY m.MaMon DESC";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("MaMon"),
                    rs.getString("TenMon"),
                    // Giá tiền hiển thị dạng "165.000 VNĐ"
                    VND_FMT.format(rs.getLong("GiaTien")) + " VNĐ",
                    rs.getString("DonViTinh"),
                    rs.getInt("SLKhaDung"),
                    rs.getInt("SoLuongConLai"),
                    rs.getString("TenNhom"),
                    rs.getString("HinhAnh")!=null ? rs.getString("HinhAnh") : "Không có"
                });
            }
        } catch (SQLException e) { err("Lỗi tải dữ liệu: " + e.getMessage()); }
    }

    // ─────────────────────── HIỂN THỊ THÔNG TIN ───────────────────────
    private void hienThiThongTinMonAn() {
        int row = tableMonAn.getSelectedRow();
        if (row < 0) return;
        // Chuyển view row → model row (vì có filter)
        int modelRow = tableMonAn.convertRowIndexToModel(row);
        selectedMaMon = (int) tableModel.getValueAt(modelRow, 0);

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM monan WHERE MaMon=?")) {
            ps.setInt(1, selectedMaMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                setField(txtTenMon,    rs.getString("TenMon"));
                setPriceField(rs.getDouble("GiaTien")); // format VNĐ
                setField(txtDonViTinh, rs.getString("DonViTinh"));

                int maNhom = rs.getInt("MaNhom");
                for (int i=0; i<cboNhom.getItemCount(); i++) {
                    if (((Nhom)cboNhom.getItemAt(i)).getMaNhom()==maNhom) {
                        cboNhom.setSelectedIndex(i); break;
                    }
                }

                String img = rs.getString("HinhAnh");
                if (img!=null&&!img.isEmpty()) { savedImageName=img; displayImageFromPath(IMAGE_FOLDER+img); }
                else { lblImagePreview.setIcon(null); lblImagePreview.setText("Chưa có ảnh"); }

                refreshSoLuongPanel(selectedMaMon);
                btnThem.setEnabled(false); btnSua.setEnabled(true); btnXoa.setEnabled(true);
            }
        } catch (SQLException e) { err("Lỗi tải thông tin: " + e.getMessage()); }
    }

    // ─────────────────────── REFRESH SL + BẢNG NL ───────────────────────
 // ─────────────────────── REFRESH SL + BẢNG NL ───────────────────────
    private void refreshSoLuongPanel(int maMon) {
        List<Object[]> ctNL = MonAnDAO.getCongThucNguyenLieu(maMon);
        pnlNLInfo.removeAll();

        if (!ctNL.isEmpty()) {
            int slKhaDung = MonAnDAO.tinhSoLuongKhaDung(maMon);
            setField(txtSoLuong, String.valueOf(slKhaDung));
            txtSoLuong.setEditable(false);
            txtSoLuong.setBackground(new Color(237, 246, 255));
            txtSoLuong.setToolTipText("Tự động tính từ tồn kho nguyên liệu");

            JLabel titleLbl = new JLabel("Công thức NL (tự động tính):");
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            titleLbl.setForeground(NAV_BG);
            titleLbl.setAlignmentX(LEFT_ALIGNMENT);
            pnlNLInfo.add(titleLbl);
            pnlNLInfo.add(Box.createVerticalStrut(8));

            // Bỏ cột "Có Thể Làm" — chỉ giữ 4 cột cần thiết
            String[] cols = {"Nguyên Liệu", "Cần / 1 món", "ĐVT", "Tồn Kho"};
            DefaultTableModel ctModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Object[] rd : ctNL) {
                ctModel.addRow(new Object[]{
                    rd[0],                          // Tên nguyên liệu
                    String.format("%.2f", rd[1]),   // Số lượng cần / 1 món
                    rd[2],                          // ĐVT
                    String.format("%.2f", rd[3])    // Tồn kho hiện tại
                });
            }

            JTable tblNL = new JTable(ctModel);
            tblNL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tblNL.setRowHeight(32);
            tblNL.setGridColor(new Color(220, 230, 242));
            tblNL.setShowHorizontalLines(true);
            tblNL.setShowVerticalLines(false);
            tblNL.setIntercellSpacing(new Dimension(0, 1));

            JTableHeader th = tblNL.getTableHeader();
            th.setFont(new Font("Segoe UI", Font.BOLD, 13));
            th.setPreferredSize(new Dimension(0, 36));
            th.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    l.setBackground(NAV_BG);
                    l.setForeground(Color.WHITE);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    l.setBorder(new EmptyBorder(0, 8, 0, 8));
                    return l;
                }
            });

            // Độ rộng 4 cột: Nguyên Liệu, Cần/1món, ĐVT, Tồn Kho
            int[] colW = {170, 110, 70, 110};
            for (int i = 0; i < colW.length; i++)
                tblNL.getColumnModel().getColumn(i).setPreferredWidth(colW[i]);

            // Renderer màu theo tồn kho (cột 3): đỏ nếu <= 0, vàng nếu thấp
            tblNL.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    ((JLabel) comp).setBorder(new EmptyBorder(0, 8, 0, 8));
                    if (!sel) {
                        try {
                            double ton = Double.parseDouble(
                                String.valueOf(ctModel.getValueAt(r, 3)));
                            double canDung = Double.parseDouble(
                                String.valueOf(ctModel.getValueAt(r, 1)));
                            // Tính số phần có thể làm với NL này để tô màu
                            int coTheLam = canDung > 0 ? (int)(ton / canDung) : 0;
                            if (coTheLam <= 0)
                                comp.setBackground(new Color(255, 200, 200));
                            else if (coTheLam <= 5)
                                comp.setBackground(new Color(255, 243, 205));
                            else
                                comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(240, 248, 255));
                        } catch (Exception ex) {
                            comp.setBackground(Color.WHITE);
                        }
                        comp.setForeground(TEXT_DARK);
                    } else {
                        comp.setBackground(ROW_SEL);
                        comp.setForeground(NAV_BG);
                    }
                    return comp;
                }
            });

            int tableH = 36 + Math.min(ctNL.size(), 5) * 32 + 4;
            JScrollPane scNL = new JScrollPane(tblNL);
            scNL.setAlignmentX(LEFT_ALIGNMENT);
            scNL.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            scNL.setPreferredSize(new Dimension(0, tableH));
            scNL.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableH + 20));
            scNL.setMinimumSize(new Dimension(0, 100));
            pnlNLInfo.add(scNL);
            pnlNLInfo.add(Box.createVerticalStrut(8));

            JLabel summary = new JLabel(String.format("→ Có thể phục vụ: %d phần", slKhaDung));
            summary.setFont(new Font("Segoe UI", Font.BOLD, 13));
            summary.setForeground(slKhaDung > 0 ? SUCCESS : DANGER);
            summary.setAlignmentX(LEFT_ALIGNMENT);
            pnlNLInfo.add(summary);
            pnlNLInfo.setVisible(true);

        } else {
            txtSoLuong.setEditable(true);
            txtSoLuong.setBackground(Color.WHITE);
            txtSoLuong.setToolTipText("Nhập số lượng thủ công");
            if (maMon > 0) {
                JLabel hint = new JLabel(
                    "<html><i>Chưa có công thức NL.<br>Nhập thủ công hoặc nhấn [Công Thức NL].</i></html>");
                hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                hint.setForeground(new Color(155, 90, 0));
                hint.setAlignmentX(LEFT_ALIGNMENT);
                pnlNLInfo.add(hint);
                pnlNLInfo.setVisible(true);
            } else {
                pnlNLInfo.setVisible(false);
            }
        }

        pnlNLInfo.revalidate();
        pnlNLInfo.repaint();
    }

    // ─────────────────────── THÊM / SỬA / XÓA ───────────────────────
    private void themMonAn() {
        if (!validateInput()) return;
        if (selectedImagePath!=null) {
            savedImageName=copyImageToAssets(new File(selectedImagePath));
            if (savedImageName==null) return;
        }
        int soLuong;
        try { soLuong=Integer.parseInt(txtSoLuong.getText().trim()); }
        catch(NumberFormatException ex){ soLuong=0; }

        String sql="INSERT INTO monan(TenMon,GiaTien,DonViTinh,SoLuongConLai,MaNhom,HinhAnh) VALUES(?,?,?,?,?,?)";
        try(Connection c=DatabaseConfig.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,txtTenMon.getText().trim());
            ps.setBigDecimal(2,java.math.BigDecimal.valueOf(getPriceValue()));
            ps.setString(3,txtDonViTinh.getText().trim());
            ps.setInt(4,soLuong);
            ps.setInt(5,((Nhom)cboNhom.getSelectedItem()).getMaNhom());
            ps.setString(6,savedImageName);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"✓ Thêm món thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadMonAnData();
        } catch(SQLException e){ err("Lỗi thêm món: "+e.getMessage()); }
    }

    private void suaMonAn() {
        if (selectedMaMon<=0){ err("Vui lòng chọn món cần sửa!"); return; }
        if (!validateInput()) return;
        if (selectedImagePath!=null){ String n=copyImageToAssets(new File(selectedImagePath)); if(n!=null) savedImageName=n; }
        List<Object[]> ctNL=MonAnDAO.getCongThucNguyenLieu(selectedMaMon);
        int soLuong;
        if (!ctNL.isEmpty()) { soLuong=MonAnDAO.tinhSoLuongKhaDung(selectedMaMon); }
        else { try{ soLuong=Integer.parseInt(txtSoLuong.getText().trim()); } catch(NumberFormatException ex){ soLuong=0; } }
        String sql="UPDATE monan SET TenMon=?,GiaTien=?,DonViTinh=?,SoLuongConLai=?,MaNhom=?,HinhAnh=? WHERE MaMon=?";
        try(Connection c=DatabaseConfig.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,txtTenMon.getText().trim());
            ps.setBigDecimal(2,java.math.BigDecimal.valueOf(getPriceValue()));
            ps.setString(3,txtDonViTinh.getText().trim());
            ps.setInt(4,soLuong);
            ps.setInt(5,((Nhom)cboNhom.getSelectedItem()).getMaNhom());
            ps.setString(6,savedImageName); ps.setInt(7,selectedMaMon);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"✓ Cập nhật thành công!","OK",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadMonAnData();
        } catch(SQLException e){ err("Lỗi sửa: "+e.getMessage()); }
    }

    private void xoaMonAn() {
        if (selectedMaMon<=0){ err("Vui lòng chọn món cần xóa!"); return; }
        int cf=JOptionPane.showConfirmDialog(this,"Xóa món này sẽ xóa luôn công thức liên kết. Bạn chắc chắn?",
            "Xác nhận xóa",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (cf!=JOptionPane.YES_OPTION) return;
        try(Connection c=DatabaseConfig.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("DELETE FROM congthucmon WHERE MaMon=?")){ ps.setInt(1,selectedMaMon); ps.executeUpdate(); }
            try(PreparedStatement ps=c.prepareStatement("DELETE FROM monan WHERE MaMon=?")){ ps.setInt(1,selectedMaMon); ps.executeUpdate(); }
            c.commit();
            JOptionPane.showMessageDialog(this,"✓ Xóa thành công!","OK",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadMonAnData();
        } catch(SQLException e){ err("Lỗi xóa: "+e.getMessage()); }
    }

    private void onTinhLaiSL() {
        if (selectedMaMon<=0){ JOptionPane.showMessageDialog(this,"Vui lòng chọn món ăn trước!","Thông báo",JOptionPane.INFORMATION_MESSAGE); return; }
        MonAnDAO.tinhLaiSoLuong(selectedMaMon);
        refreshSoLuongPanel(selectedMaMon);
        loadMonAnData();
    }

    private void moFormCongThuc() {
        if (selectedMaMon<=0){ JOptionPane.showMessageDialog(this,"Vui lòng thêm/chọn món ăn trước!","Thông báo",JOptionPane.INFORMATION_MESSAGE); return; }
        JFrame fCT=new JFrame("Công Thức Nguyên Liệu - Món #"+selectedMaMon);
        FormCongThucMon panelCT=new FormCongThucMon();
        panelCT.filterByMonNgoai(selectedMaMon);
        fCT.add(panelCT); fCT.setSize(1000,700);
        fCT.setLocationRelativeTo(this);
        fCT.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        fCT.addWindowListener(new WindowAdapter(){
            @Override public void windowClosed(WindowEvent e){
                MonAnDAO.tinhLaiSoLuong(selectedMaMon);
                refreshSoLuongPanel(selectedMaMon);
                loadMonAnData();
            }
        });
        fCT.setVisible(true);
    }

    // ─────────────────────── VALIDATE ───────────────────────
    private boolean validateInput() {
        if (isBlankOrPlaceholder(txtTenMon,"Nhập tên món ăn...")){ err("Vui lòng nhập tên món!"); return false; }
        String priceStr = getRawNumber(txtGiaTien.getText().trim());
        if (priceStr.isEmpty() || txtGiaTien.getText().trim().equals("Nhập giá tiền...")){ err("Vui lòng nhập giá tiền!"); return false; }
        try{ if(Double.parseDouble(priceStr)<=0){ err("Giá tiền phải > 0!"); return false; } }
        catch(NumberFormatException e){ err("Giá tiền không hợp lệ!"); return false; }
        if (isBlankOrPlaceholder(txtDonViTinh,"Nhập đơn vị tính...")){ err("Vui lòng nhập đơn vị tính!"); return false; }
        if (txtSoLuong.isEditable()){
            try{ if(Integer.parseInt(txtSoLuong.getText().trim())<0){ err("Số lượng không được âm!"); return false; } }
            catch(NumberFormatException e){ err("Số lượng không hợp lệ!"); return false; }
        }
        if (cboNhom.getSelectedItem()==null){ err("Vui lòng chọn nhóm!"); return false; }
        return true;
    }

    private boolean isBlankOrPlaceholder(JTextField f, String ph) {
        String t=f.getText().trim(); return t.isEmpty()||t.equals(ph);
    }

    // ─────────────────────── CLEAR FORM ───────────────────────
    private void clearForm() {
        setPlaceholder(txtTenMon,    "Nhập tên món ăn...");
        // Reset giá tiền về placeholder
        txtGiaTien.setText("Nhập giá tiền...");
        txtGiaTien.setForeground(TEXT_MID);
        txtGiaTien.setBackground(Color.WHITE);
        setPlaceholder(txtDonViTinh, "Nhập đơn vị tính...");
        txtSoLuong.setText("0"); txtSoLuong.setEditable(true);
        txtSoLuong.setBackground(Color.WHITE); txtSoLuong.setToolTipText(null);
        if (cboNhom.getItemCount()>0) cboNhom.setSelectedIndex(0);
        lblImagePreview.setIcon(null); lblImagePreview.setText("Chưa có ảnh");
        selectedImagePath=null; savedImageName=null; selectedMaMon=-1;
        pnlNLInfo.removeAll(); pnlNLInfo.setVisible(false);
        tableMonAn.clearSelection();
        btnThem.setEnabled(true); btnSua.setEnabled(false); btnXoa.setEnabled(false);
    }

    // ─────────────────────── IMAGE ───────────────────────
    private void chonAnhMonAn(){
        JFileChooser fc=new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image Files","jpg","jpeg","png","gif"));
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            selectedImagePath=fc.getSelectedFile().getAbsolutePath();
            displayImagePreview(fc.getSelectedFile());
        }
    }
    private void displayImagePreview(File f){
        try{ Image img=ImageIO.read(f).getScaledInstance(240,185,Image.SCALE_SMOOTH);
             lblImagePreview.setIcon(new ImageIcon(img)); lblImagePreview.setText(""); }
        catch(IOException e){ err("Không đọc được ảnh!"); }
    }
    private void displayImageFromPath(String path){
        try{ File f=new File(path);
             if(f.exists()){ Image img=ImageIO.read(f).getScaledInstance(240,185,Image.SCALE_SMOOTH);
                             lblImagePreview.setIcon(new ImageIcon(img)); lblImagePreview.setText(""); } }
        catch(IOException e){ lblImagePreview.setIcon(null); lblImagePreview.setText("Lỗi load ảnh"); }
    }
    private String copyImageToAssets(File src){
        try{ String ext=src.getName().substring(src.getName().lastIndexOf('.'));
             String name=System.currentTimeMillis()+ext;
             Files.copy(src.toPath(),Paths.get(IMAGE_FOLDER+name),StandardCopyOption.REPLACE_EXISTING);
             return name; }
        catch(IOException e){ err("Lỗi copy ảnh: "+e.getMessage()); return null; }
    }
    private void createImageFolderIfNotExists(){
        try{ Files.createDirectories(Paths.get(IMAGE_FOLDER)); } catch(IOException ignored){}
    }

    // ─────────────────────── UI HELPERS ───────────────────────
    private JTextField createField(String ph){
        JTextField tf=new JTextField(ph);
        tf.setFont(new Font("Segoe UI",Font.PLAIN,14));
        tf.setForeground(TEXT_MID); tf.setBackground(Color.WHITE); tf.setCaretColor(PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR,1),new EmptyBorder(8,12,8,12)));
        tf.setPreferredSize(new Dimension(0,42));
        tf.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e){
                if(tf.getText().equals(ph)){ tf.setText(""); tf.setForeground(TEXT_DARK); }
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY,2),new EmptyBorder(7,11,7,11)));
            }
            @Override public void focusLost(FocusEvent e){
                if(tf.getText().isEmpty()){ tf.setText(ph); tf.setForeground(TEXT_MID); }
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR,1),new EmptyBorder(8,12,8,12)));
            }
        });
        return tf;
    }

    private JButton bigBtn(String txt, Color bg){
        JButton b=new JButton(txt){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color col=!isEnabled()?new Color(180,180,180)
                         :getModel().isPressed()?bg.darker().darker()
                         :getModel().isRollover()?bg.brighter():bg;
                g2.setColor(col);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,14));
        b.setForeground(Color.WHITE);
        b.setBorder(null); b.setFocusPainted(false);
        b.setContentAreaFilled(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120,44));
        return b;
    }

    private JPanel btnRow(JButton b1, JButton b2){
        JPanel p=new JPanel(new GridLayout(1,2,10,0));
        p.setBackground(SIDEBAR_BG); p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        b1.setPreferredSize(new Dimension(0,44)); b2.setPreferredSize(new Dimension(0,44));
        p.add(b1); p.add(b2); return p;
    }

    private JPanel formRow(String labelText, JComponent comp){
        JPanel p=new JPanel(new BorderLayout(0,4));
        p.setBackground(SIDEBAR_BG); p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,68));
        JLabel l=new JLabel(labelText);
        l.setFont(new Font("Segoe UI",Font.BOLD,12)); l.setForeground(TEXT_DARK);
        p.add(l,BorderLayout.NORTH);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        p.add(comp,BorderLayout.CENTER); return p;
    }

    private JPanel sectionHeader(String text){
        JPanel p=new JPanel(new BorderLayout());
        p.setBackground(SIDEBAR_BG); p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        JLabel l=new JLabel(text);
        l.setFont(new Font("Segoe UI",Font.BOLD,13)); l.setForeground(NAV_BG);
        p.add(l,BorderLayout.CENTER);
        JPanel ul=new JPanel(); ul.setBackground(PRIMARY); ul.setPreferredSize(new Dimension(0,2));
        p.add(ul,BorderLayout.SOUTH); return p;
    }

    private JPanel buildHintBox(){
        JPanel box=new JPanel();
        box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS));
        box.setBackground(new Color(230,244,255));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(170,210,240),1),new EmptyBorder(10,12,10,12)));
        box.setAlignmentX(LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE,110));
        JLabel ht=new JLabel("Hướng dẫn:");
        ht.setFont(new Font("Segoe UI",Font.BOLD,12)); ht.setForeground(new Color(30,70,130));
        ht.setAlignmentX(LEFT_ALIGNMENT); box.add(ht); box.add(Box.createVerticalStrut(5));
        for(String line:new String[]{
            "• Thiết lập nguyên liệu cho 1 phần món ăn.",
            "• SL khả dụng = MIN(Tồn NL ÷ SL cần).",
            "• Nhấn [Tính Lại SL] sau khi thêm/sửa công thức."}){
            JLabel hl=new JLabel(line);
            hl.setFont(new Font("Segoe UI",Font.PLAIN,11)); hl.setForeground(TEXT_DARK);
            hl.setAlignmentX(LEFT_ALIGNMENT); box.add(hl);
        }
        return box;
    }

    private JLabel legendChip(Color bg, String text){
        JLabel l=new JLabel("  "+text+"  ");
        l.setFont(new Font("Segoe UI",Font.PLAIN,11)); l.setOpaque(true); l.setBackground(bg);
        l.setBorder(BorderFactory.createLineBorder(bg.darker(),1)); return l;
    }

    private Component vgap(int h){ return Box.createRigidArea(new Dimension(0,h)); }

    private void setField(JTextField f,String val){ f.setText(val); f.setForeground(TEXT_DARK); }
    private void setPlaceholder(JTextField f,String ph){ f.setText(ph); f.setForeground(TEXT_MID); }
    private void err(String msg){ JOptionPane.showMessageDialog(this,msg,"Lỗi",JOptionPane.ERROR_MESSAGE); }
}