package view;

import dao.HoaDonDAO;
import model.HoaDon;
import model.NhanVien;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FormQuanLyHoaDon extends JFrame {

    // ─────────────── Màu sắc đồng bộ ───────────────
    private static final Color NAV_BG       = new Color(25,  45,  85);
    private static final Color SIDEBAR_BG   = new Color(255, 255, 255);
    private static final Color CONTENT_BG   = new Color(243, 246, 250);
    private static final Color PRIMARY      = new Color(52,  130, 200);
    private static final Color SUCCESS      = new Color(39,  174,  96);
    private static final Color DANGER       = new Color(192,  57,  43);
    private static final Color WARNING      = new Color(230, 170,  20);
    private static final Color STAT_BG      = new Color(22,  38,  68);
    private static final Color TEXT_DARK    = new Color(44,  62,  80);
    private static final Color TEXT_MID     = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(214, 220, 229);
    private static final Color ROW_ALT      = new Color(248, 250, 253);
    private static final Color ROW_SEL      = new Color(213, 232, 255);

    // Panel chi tiết món ăn
    private static final Color DETAIL_BG    = new Color(250, 252, 255);
    private static final Color DETAIL_HDR   = new Color(235, 244, 255);
    private static final Color MON_ROW_ODD  = new Color(255, 255, 255);
    private static final Color MON_ROW_EVEN = new Color(246, 249, 253);
    private static final Color MON_HDR_BG   = new Color(25,  45,  85);
    private static final Color BADGE_BG     = new Color(52, 130, 200);

    // ─────────────── Filter components ───────────────
    private JSpinner spinnerDateFrom;
    private JSpinner spinnerDateTo;
    private JComboBox<String> cboLoaiHoaDon;
    private JTextField txtSearch;

    // ─────────────── Buttons ───────────────
    private JButton btnLocNgay, btnHomNay, btnReset;
    private JButton btnXemChiTiet, btnXoa;

    // ─────────────── Bảng ───────────────
    private JTable tableHoaDon;
    private DefaultTableModel modelHoaDon;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // ─────────────── Stats ───────────────
    private JLabel lblTongHoaDon, lblTongDoanhThu, lblHoaDonHomNay;

    // ─────────────── Panel chi tiết món ăn (MỚI) ───────────────
    private JPanel panelChiTietMon;
    private JPanel panelMonContent;
    private JLabel lblChiTietTitle;
    private JLabel lblChiTietInfo;
    private int lastSelectedMaCTHD = -1;

    private NhanVien currentUser;

    // ─────────────── Định dạng tiền ───────────────
    private static final DecimalFormat VND_FMT;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("vi", "VN"));
        sym.setGroupingSeparator('.');
        VND_FMT = new DecimalFormat("#,###", sym);
    }

    // ════════════════════════════════════════════
    public FormQuanLyHoaDon(NhanVien currentUser) {
        this.currentUser = currentUser;
        setTitle("RESTAURANT MANAGER PRO - Quản Lý Hóa Đơn");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CONTENT_BG);

        initComponents();
        buildUI();
        loadAllHoaDon();
        updateStatistics();
    }

    // ─────────────────────── INIT ───────────────────────
    private void initComponents() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        spinnerDateFrom = buildDateSpinner(cal.getTime());
        spinnerDateTo   = buildDateSpinner(new java.util.Date());

        cboLoaiHoaDon = new JComboBox<>(new String[]{"Tất cả", "Tại quán", "Mang về"});
        styleComboBox(cboLoaiHoaDon);
        cboLoaiHoaDon.addActionListener(e -> filterByType());

        btnLocNgay    = bigBtn("Lọc Ngày",     PRIMARY);
        btnHomNay     = bigBtn("Hôm Nay",      SUCCESS);
        btnReset      = bigBtn("Đặt Lại",      WARNING);
        btnXemChiTiet = bigBtn("Xem Chi Tiết", PRIMARY);
        btnXoa        = bigBtn("Xóa Hóa Đơn",  DANGER);
        boolean isAdmin = (currentUser != null && currentUser.getMaPQ() == 1);
        btnXoa.setVisible(isAdmin);
        btnXoa.setEnabled(isAdmin);

        btnLocNgay   .addActionListener(e -> filterByDateRange());
        btnHomNay    .addActionListener(e -> filterToday());
        btnReset     .addActionListener(e -> resetFilters());
        btnXemChiTiet.addActionListener(e -> viewDetail());
        btnXoa       .addActionListener(e -> deleteHoaDon());

        // Table
        String[] cols = {"Mã HĐ", "Ngày", "Mã Bàn", "Số Món", "Tổng Tiền", "Giảm (%)", "Thanh Toán", "Loại"};
        modelHoaDon = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHoaDon = new JTable(modelHoaDon);
        rowSorter   = new TableRowSorter<>(modelHoaDon);
        tableHoaDon.setRowSorter(rowSorter);
        setupTable();

        txtSearch = createSearchField("Nhập từ khóa tìm kiếm hóa đơn...");

        lblTongHoaDon   = statValueLabel("0",      PRIMARY);
        lblTongDoanhThu = statValueLabel("0 VNĐ",  SUCCESS);
        lblHoaDonHomNay = statValueLabel("0",      WARNING);

        // ── Khởi tạo panel chi tiết món ăn (MỚI) ──
        buildChiTietMonPanel();

        // Listener chọn dòng → load món ăn inline
        tableHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        // Double-click → mở dialog chi tiết đầy đủ
        tableHoaDon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewDetail();
            }
        });
    }

    // ─────────────────────── BUILD PANEL CHI TIẾT MÓN (MỚI) ───────────────────────
    private void buildChiTietMonPanel() {
        panelChiTietMon = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DETAIL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        panelChiTietMon.setOpaque(false);
        panelChiTietMon.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ── Header của panel chi tiết ──
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DETAIL_HDR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        headerPanel.setPreferredSize(new Dimension(0, 46));

        // Icon + tiêu đề
        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleLeft.setOpaque(false);

        JLabel iconLbl = new JLabel("🍜");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        lblChiTietTitle = new JLabel("Danh Sách Món Ăn");
        lblChiTietTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChiTietTitle.setForeground(NAV_BG);

        titleLeft.add(iconLbl);
        titleLeft.add(lblChiTietTitle);

        // Info bên phải (tổng tiền, số món)
        lblChiTietInfo = new JLabel("← Chọn một hóa đơn để xem chi tiết món ăn");
        lblChiTietInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblChiTietInfo.setForeground(TEXT_MID);

        headerPanel.add(titleLeft,     BorderLayout.WEST);
        headerPanel.add(lblChiTietInfo, BorderLayout.EAST);

        // ── Nội dung: placeholder ban đầu ──
        panelMonContent = new JPanel(new BorderLayout());
        panelMonContent.setOpaque(false);
        panelMonContent.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel placeholder = new JLabel("Chọn một hóa đơn từ danh sách bên trên để xem chi tiết món ăn");
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        placeholder.setForeground(new Color(180, 190, 205));
        placeholder.setHorizontalAlignment(JLabel.CENTER);
        panelMonContent.add(placeholder, BorderLayout.CENTER);

        panelChiTietMon.add(headerPanel,    BorderLayout.NORTH);
        panelChiTietMon.add(panelMonContent, BorderLayout.CENTER);
    }

    // ─────────────────────── XỬ LÝ KHI CHỌN HÀNG (MỚI) ───────────────────────
    private void onRowSelected() {
        int viewRow = tableHoaDon.getSelectedRow();
        if (viewRow == -1) {
            resetChiTietPanel();
            return;
        }

        int modelRow = tableHoaDon.convertRowIndexToModel(viewRow);
        int maCTHD   = (int) modelHoaDon.getValueAt(modelRow, 0);

        // Tránh reload nếu đang chọn lại cùng hóa đơn
        if (maCTHD == lastSelectedMaCTHD) return;
        lastSelectedMaCTHD = maCTHD;

        // Lấy thông tin hóa đơn từ bảng
        String ngay     = String.valueOf(modelHoaDon.getValueAt(modelRow, 1));
        String maBan    = String.valueOf(modelHoaDon.getValueAt(modelRow, 2));
        String soMon    = String.valueOf(modelHoaDon.getValueAt(modelRow, 3));
        String tongTien = String.valueOf(modelHoaDon.getValueAt(modelRow, 6));
        String loai     = String.valueOf(modelHoaDon.getValueAt(modelRow, 7));

        // Cập nhật header
        lblChiTietTitle.setText("Hóa Đơn #" + maCTHD
            + "  —  Bàn " + maBan
            + "  —  " + ngay);
        lblChiTietInfo.setText(soMon + " món  |  " + tongTien + "  |  " + loai);
        lblChiTietInfo.setForeground(TEXT_DARK);

        // Load danh sách món bất đồng bộ để không block UI
        List<String> dsMon = HoaDonDAO.getChiTietMonAnByHoaDon(maCTHD);

        SwingUtilities.invokeLater(() -> renderMonList(dsMon, maCTHD));
    }

    /** Render bảng danh sách món vào panelMonContent */
    private void renderMonList(List<String> dsMon, int maCTHD) {
        panelMonContent.removeAll();
        panelMonContent.setLayout(new BorderLayout(0, 8));
        panelMonContent.setBorder(new EmptyBorder(10, 16, 10, 16));

        if (dsMon == null || dsMon.isEmpty()) {
            JLabel noData = new JLabel("Không có dữ liệu món ăn cho hóa đơn #" + maCTHD);
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(new Color(180, 190, 205));
            noData.setHorizontalAlignment(JLabel.CENTER);
            panelMonContent.add(noData, BorderLayout.CENTER);
        } else {
            // Tạo bảng món
            String[] monCols = {"#", "Tên Món", "Số Lượng", "Đơn Giá", "Thành Tiền"};
            DefaultTableModel monModel = new DefaultTableModel(monCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            BigDecimal tongCong = BigDecimal.ZERO;
            int stt = 1;

            for (String monInfo : dsMon) {
                // Parse lại từ chuỗi "Nx TenMon (GiaTien VND)" do HoaDonDAO trả về
                // Format: "2x Lẩu Thái chua cay (150,000 VND)"
                try {
                    int xIdx   = monInfo.indexOf('x');
                    int parenO = monInfo.lastIndexOf('(');
                    int parenC = monInfo.lastIndexOf(')');

                    int soLuong = 1;
                    String tenMon = monInfo;
                    long donGia = 0;

                    if (xIdx > 0 && parenO > xIdx && parenC > parenO) {
                        soLuong = Integer.parseInt(monInfo.substring(0, xIdx).trim());
                        tenMon  = monInfo.substring(xIdx + 1, parenO).trim();
                        String giaStr = monInfo.substring(parenO + 1, parenC)
                                               .replaceAll("[^0-9]", "");
                        donGia = giaStr.isEmpty() ? 0 : Long.parseLong(giaStr);
                    }

                    long thanhTien = donGia * soLuong;
                    tongCong = tongCong.add(BigDecimal.valueOf(thanhTien));

                    monModel.addRow(new Object[]{
                        stt++,
                        tenMon,
                        soLuong,
                        VND_FMT.format(donGia) + " đ",
                        VND_FMT.format(thanhTien) + " đ"
                    });
                } catch (Exception ex) {
                    // Fallback: hiển thị nguyên chuỗi
                    monModel.addRow(new Object[]{ stt++, monInfo, "—", "—", "—" });
                }
            }

            // Tạo JTable nhỏ gọn cho danh sách món
            JTable monTable = new JTable(monModel) {
                @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                    Component c = super.prepareRenderer(r, row, col);
                    if (isRowSelected(row)) {
                        c.setBackground(ROW_SEL);
                        c.setForeground(NAV_BG);
                    } else {
                        c.setBackground(row % 2 == 0 ? MON_ROW_ODD : MON_ROW_EVEN);
                        c.setForeground(TEXT_DARK);
                    }
                    return c;
                }
            };

            monTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            monTable.setRowHeight(34);
            monTable.setShowGrid(false);
            monTable.setIntercellSpacing(new Dimension(0, 1));
            monTable.setFocusable(false);
            monTable.setBackground(MON_ROW_ODD);
            monTable.setSelectionBackground(ROW_SEL);

            // Header bảng món
            JTableHeader mh = monTable.getTableHeader();
            mh.setFont(new Font("Segoe UI", Font.BOLD, 12));
            mh.setPreferredSize(new Dimension(0, 34));
            mh.setReorderingAllowed(false);
            mh.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    l.setBackground(MON_HDR_BG);
                    l.setForeground(Color.WHITE);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    l.setBorder(new EmptyBorder(0, 10, 0, 10));
                    return l;
                }
            });

            // Căn chỉnh cột
            DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
            centerR.setHorizontalAlignment(JLabel.CENTER);
            DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
            rightR.setHorizontalAlignment(JLabel.RIGHT);
            DefaultTableCellRenderer leftR = new DefaultTableCellRenderer();
            leftR.setHorizontalAlignment(JLabel.LEFT);
            leftR.setBorder(new EmptyBorder(0, 10, 0, 10));

            int[][] colWidths = {{0, 40}, {1, 200}, {2, 80}, {3, 120}, {4, 130}};
            TableCellRenderer[] renderers = {centerR, leftR, centerR, rightR, rightR};
            for (int[] cw : colWidths) {
                TableColumn tc = monTable.getColumnModel().getColumn(cw[0]);
                tc.setPreferredWidth(cw[1]);
                tc.setCellRenderer(renderers[cw[0]]);
            }

            JScrollPane monScroll = new JScrollPane(monTable);
            monScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            monScroll.getViewport().setBackground(MON_ROW_ODD);
            monScroll.setBackground(MON_ROW_ODD);

            // ── Footer: tổng cộng ──
            JPanel footerPanel = new JPanel(new BorderLayout());
            footerPanel.setBackground(DETAIL_HDR);
            footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(8, 16, 8, 16)
            ));

            JLabel lblTongLabel = new JLabel("Tổng cộng:  " + dsMon.size() + " món");
            lblTongLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTongLabel.setForeground(TEXT_MID);

            JLabel lblTongGia = new JLabel(VND_FMT.format(tongCong.longValue()) + " đ");
            lblTongGia.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTongGia.setForeground(SUCCESS);

            footerPanel.add(lblTongLabel, BorderLayout.WEST);
            footerPanel.add(lblTongGia,   BorderLayout.EAST);

            panelMonContent.add(monScroll,    BorderLayout.CENTER);
            panelMonContent.add(footerPanel,  BorderLayout.SOUTH);
        }

        panelMonContent.revalidate();
        panelMonContent.repaint();
    }

    /** Reset panel về trạng thái ban đầu */
    private void resetChiTietPanel() {
        lastSelectedMaCTHD = -1;
        lblChiTietTitle.setText("Danh Sách Món Ăn");
        lblChiTietInfo.setText("← Chọn một hóa đơn để xem chi tiết món ăn");
        lblChiTietInfo.setForeground(TEXT_MID);

        panelMonContent.removeAll();
        panelMonContent.setLayout(new BorderLayout());
        JLabel placeholder = new JLabel("Chọn một hóa đơn từ danh sách bên trên để xem chi tiết món ăn");
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        placeholder.setForeground(new Color(180, 190, 205));
        placeholder.setHorizontalAlignment(JLabel.CENTER);
        panelMonContent.add(placeholder, BorderLayout.CENTER);
        panelMonContent.revalidate();
        panelMonContent.repaint();
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

        add(buildStatsBar(), BorderLayout.SOUTH);
    }

    // ─────────────────────── SIDEBAR ───────────────────────
    private JPanel buildSidebar() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SIDEBAR_BG);
        content.setBorder(new EmptyBorder(16, 18, 20, 18));

        content.add(sectionHeader("Lọc Theo Thời Gian"));
        content.add(vgap(10));
        content.add(formRow("Từ Ngày",  spinnerDateFrom)); content.add(vgap(8));
        content.add(formRow("Đến Ngày", spinnerDateTo));   content.add(vgap(10));
        content.add(btnRow(btnLocNgay, btnHomNay));        content.add(vgap(14));

        content.add(sectionHeader("Lọc Theo Loại Hóa Đơn"));
        content.add(vgap(10));
        content.add(formRow("Loại Hóa Đơn", cboLoaiHoaDon)); content.add(vgap(10));

        JPanel resetWrap = new JPanel(new BorderLayout());
        resetWrap.setBackground(SIDEBAR_BG);
        resetWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnReset.setPreferredSize(new Dimension(0, 44));
        resetWrap.add(btnReset, BorderLayout.CENTER);
        content.add(resetWrap);
        content.add(vgap(14));

        content.add(makeSep()); content.add(vgap(14));

        content.add(sectionHeader("Thao Tác Hóa Đơn"));
        content.add(vgap(10));

        JPanel xctWrap = new JPanel(new BorderLayout());
        xctWrap.setBackground(SIDEBAR_BG);
        xctWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        xctWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnXemChiTiet.setPreferredSize(new Dimension(0, 44));
        xctWrap.add(btnXemChiTiet, BorderLayout.CENTER);
        content.add(xctWrap);
        content.add(vgap(8));

        JPanel xoaWrap = new JPanel(new BorderLayout());
        xoaWrap.setBackground(SIDEBAR_BG);
        xoaWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        xoaWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnXoa.setPreferredSize(new Dimension(0, 44));
        xoaWrap.add(btnXoa, BorderLayout.CENTER);
        content.add(xoaWrap);
        content.add(vgap(16));

        content.add(makeSep()); content.add(vgap(14));
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

    // ─────────────────────── BẢNG + PANEL CHI TIẾT ───────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 16, 16, 16)));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel title = new JLabel("DANH SÁCH HÓA ĐƠN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAV_BG);
        headerRow.add(title, BorderLayout.WEST);

        JPanel searchBar = buildSearchBar();

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerRow,  BorderLayout.NORTH);
        topPanel.add(searchBar, BorderLayout.SOUTH);

        // ScrollPane bảng hóa đơn
        JScrollPane sc = new JScrollPane(tableHoaDon);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sc.getViewport().setBackground(Color.WHITE);

        // ── JSplitPane dọc: bảng HĐ trên, chi tiết món dưới ──
        panelChiTietMon.setPreferredSize(new Dimension(0, 220));

        JSplitPane verticalSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, sc, panelChiTietMon);
        verticalSplit.setResizeWeight(0.60);        // 60% cho bảng HĐ
        verticalSplit.setDividerSize(6);
        verticalSplit.setDividerLocation(0.60);
        verticalSplit.setBorder(null);
        verticalSplit.setBackground(Color.WHITE);

        // Divider tùy chỉnh — mỏng, màu nhẹ với dấu hiệu kéo
        verticalSplit.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setColor(BORDER_COLOR);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        // Dấu hiệu kéo: 3 chấm ở giữa
                        g2.setColor(TEXT_MID);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        for (int i = -8; i <= 8; i += 8) {
                            g2.fillOval(cx + i - 2, cy - 2, 4, 4);
                        }
                    }
                };
            }
        });

        p.add(topPanel,      BorderLayout.NORTH);
        p.add(verticalSplit, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────── SEARCH BAR ───────────────────────
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 0, 4, 0));

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

        JButton btnClear = new JButton("✕");
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
            resetChiTietPanel();
            updateStatistics();
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

    // ─────────────────────── STATS BAR ───────────────────────
    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new GridLayout(1, 3, 1, 0));
        p.setBackground(STAT_BG);
        p.setPreferredSize(new Dimension(0, 76));
        p.add(statCard(lblTongHoaDon,   "Tổng Hóa Đơn",    PRIMARY));
        p.add(statCard(lblTongDoanhThu, "Tổng Doanh Thu",  SUCCESS));
        p.add(statCard(lblHoaDonHomNay, "Hóa Đơn Hôm Nay", WARNING));
        return p;
    }

    private JPanel statCard(JLabel valueLbl, String desc, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(STAT_BG);
        card.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        accentBar.setPreferredSize(new Dimension(0, 3));
        accentBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(new Color(160, 180, 215));
        descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(accentBar);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(3));
        card.add(descLbl);
        return card;
    }

    private JLabel statValueLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(color);
        return l;
    }

    // ─────────────────────── TABLE SETUP ───────────────────────
    private void setupTable() {
        tableHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableHoaDon.setRowHeight(38);
        tableHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableHoaDon.setGridColor(new Color(235, 238, 242));
        tableHoaDon.setShowHorizontalLines(true);
        tableHoaDon.setShowVerticalLines(false);
        tableHoaDon.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = tableHoaDon.getTableHeader();
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

        DefaultTableCellRenderer defRend = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                applyRowStyle(comp, sel, r);
                return comp;
            }
        };

        DefaultTableCellRenderer centerRend = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 6, 0, 6));
                applyRowStyle(comp, sel, r);
                return comp;
            }
        };

        DefaultTableCellRenderer rightRend = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setHorizontalAlignment(SwingConstants.RIGHT);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 6, 0, 12));
                applyRowStyle(comp, sel, r);
                return comp;
            }
        };

        Object[][] colDef = {
            {0, 80,  centerRend},
            {1, 110, defRend},
            {2, 85,  centerRend},
            {3, 80,  centerRend},
            {4, 140, rightRend},
            {5, 90,  centerRend},
            {6, 150, rightRend},
            {7, 110, defRend}
        };
        for (Object[] cd : colDef) {
            TableColumn col = tableHoaDon.getColumnModel().getColumn((int) cd[0]);
            col.setPreferredWidth((int) cd[1]);
            col.setCellRenderer((TableCellRenderer) cd[2]);
        }
    }

    private void applyRowStyle(Component comp, boolean sel, int r) {
        if (!sel) {
            comp.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
            comp.setForeground(TEXT_DARK);
        } else {
            comp.setBackground(ROW_SEL);
            comp.setForeground(NAV_BG);
        }
    }

    // ─────────────────────── LOAD / DISPLAY DATA ───────────────────────
    private void loadAllHoaDon() {
        displayList(HoaDonDAO.getAllHoaDon());
    }

    private void displayList(List<HoaDon> list) {
        modelHoaDon.setRowCount(0);
        resetChiTietPanel();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (HoaDon hd : list) {
            modelHoaDon.addRow(new Object[]{
                hd.getMaCTHD(),
                hd.getNgayThanhToan() != null ? sdf.format(hd.getNgayThanhToan()) : "",
                hd.getMaBan(),
                hd.getTongSoLuongMon(),
                fmtMoney(hd.getTongTienThanhToan()),
                hd.getPhanTramGiamGia() != null ? hd.getPhanTramGiamGia() + "%" : "0%",
                fmtMoney(hd.getTongTienThanhToan()),
                hd.getLoaiHoaDon() != null ? hd.getLoaiHoaDon() : "Tại quán"
            });
        }
        updateStatistics();
    }

    // ─────────────────────── FILTER / SEARCH ───────────────────────
    private void doFilter(JLabel lblCount) {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) {
            rowSorter.setRowFilter(null);
            lblCount.setText("");
        } else {
            rowSorter.setRowFilter(RowFilter.orFilter(List.of(
                RowFilter.regexFilter("(?i)" + kw, 0),
                RowFilter.regexFilter("(?i)" + kw, 2)
            )));
            int matched = tableHoaDon.getRowCount();
            int total   = modelHoaDon.getRowCount();
            lblCount.setText(matched + "/" + total + " hóa đơn");
        }
        resetChiTietPanel();
        updateStatistics();
    }

    private void filterByDateRange() {
        java.util.Date from = (java.util.Date) spinnerDateFrom.getValue();
        java.util.Date to   = (java.util.Date) spinnerDateTo.getValue();
        if (from == null || to == null) { warn("Vui lòng chọn đủ ngày bắt đầu và kết thúc!"); return; }
        if (from.after(to))            { warn("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc!"); return; }
        displayList(HoaDonDAO.getHoaDonByDateRange(new Date(from.getTime()), new Date(to.getTime())));
    }

    private void filterToday() {
        displayList(HoaDonDAO.getHoaDonToday());
    }

    private void filterByType() {
        String loai = (String) cboLoaiHoaDon.getSelectedItem();
        if ("Tất cả".equals(loai)) loadAllHoaDon();
        else displayList(HoaDonDAO.getHoaDonByLoai(loai));
    }

    private void resetFilters() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        spinnerDateFrom.setValue(cal.getTime());
        spinnerDateTo.setValue(new java.util.Date());
        cboLoaiHoaDon.setSelectedIndex(0);
        txtSearch.setText("");
        rowSorter.setRowFilter(null);
        loadAllHoaDon();
    }

    // ─────────────────────── THAO TÁC ───────────────────────
    private void viewDetail() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { warn("Vui lòng chọn hóa đơn cần xem!"); return; }
        int maCTHD = (int) modelHoaDon.getValueAt(tableHoaDon.convertRowIndexToModel(row), 0);
        HoaDon hd = HoaDonDAO.getHoaDonById(maCTHD);
        if (hd != null) showDetailDialog(hd);
    }

    private void showDetailDialog(HoaDon hd) {
        JDialog dlg = new JDialog(this, "Chi Tiết Hóa Đơn #" + hd.getMaCTHD(), true);
        dlg.setSize(620, 540);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel dHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, NAV_BG, getWidth(), 0, new Color(40, 70, 130)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dHeader.setPreferredSize(new Dimension(0, 52));
        JLabel dTitle = new JLabel("   THÔNG TIN HÓA ĐƠN #" + hd.getMaCTHD());
        dTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dTitle.setForeground(Color.WHITE);
        dHeader.add(dTitle, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(18, 26, 16, 26));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        content.add(dlgInfoRow("Mã hóa đơn",      "#" + hd.getMaCTHD()));
        content.add(dlgInfoRow("Ngày thanh toán", hd.getNgayThanhToan() != null
            ? sdf.format(hd.getNgayThanhToan()) : "—"));
        content.add(dlgInfoRow("Mã bàn",          "Bàn " + hd.getMaBan()));
        content.add(dlgInfoRow("Loại hóa đơn",    hd.getLoaiHoaDon() != null ? hd.getLoaiHoaDon() : "Tại quán"));
        content.add(dlgInfoRow("Tổng số món",     String.valueOf(hd.getTongSoLuongMon())));
        content.add(dlgInfoRow("Giảm giá",        (hd.getPhanTramGiamGia() != null ? hd.getPhanTramGiamGia() : 0) + "%"));
        content.add(dlgInfoRow("Tổng tiền thanh toán", fmtMoney(hd.getTongTienThanhToan())));
        content.add(vgap(12));

        JLabel lblMon = new JLabel("Chi Tiết Món Ăn:");
        lblMon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMon.setForeground(NAV_BG);
        lblMon.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblMon);
        content.add(vgap(6));

        List<String> dsMon = HoaDonDAO.getChiTietMonAnByHoaDon(hd.getMaCTHD());
        StringBuilder sb = new StringBuilder();
        for (String mon : dsMon) sb.append("- ").append(mon).append("\n");

        JTextArea txtMon = new JTextArea(sb.toString());
        txtMon.setEditable(false);
        txtMon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMon.setBackground(new Color(247, 249, 252));
        txtMon.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 12, 10, 12)));
        txtMon.setLineWrap(true);
        txtMon.setWrapStyleWord(true);

        JScrollPane sc = new JScrollPane(txtMon);
        sc.setAlignmentX(Component.LEFT_ALIGNMENT);
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        sc.setPreferredSize(new Dimension(0, 160));
        sc.setBorder(null);
        content.add(sc);

        JPanel dFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        dFooter.setBackground(Color.WHITE);
        dFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        JButton btnClose = bigBtn("Đóng", PRIMARY);
        btnClose.setPreferredSize(new Dimension(140, 40));
        btnClose.addActionListener(e -> dlg.dispose());
        dFooter.add(btnClose);

        JScrollPane contentScroll = new JScrollPane(content);
        contentScroll.setBorder(null);
        contentScroll.getViewport().setBackground(Color.WHITE);

        dlg.add(dHeader,       BorderLayout.NORTH);
        dlg.add(contentScroll, BorderLayout.CENTER);
        dlg.add(dFooter,       BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel dlgInfoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 242, 246)),
            new EmptyBorder(6, 0, 6, 0)));
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MID);
        lbl.setPreferredSize(new Dimension(180, 22));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(TEXT_DARK);
        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private void deleteHoaDon() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { warn("Vui lòng chọn hóa đơn cần xóa!"); return; }
        int maCTHD = (int) modelHoaDon.getValueAt(tableHoaDon.convertRowIndexToModel(row), 0);
        int cf = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa hóa đơn #" + maCTHD + "?\nHành động này không thể hoàn tác!",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf == JOptionPane.YES_OPTION) {
            if (HoaDonDAO.deleteHoaDon(maCTHD)) {
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                loadAllHoaDon();
            } else {
                err("Lỗi khi xóa hóa đơn!");
            }
        }
    }

    // ─────────────────────── STATISTICS ───────────────────────
    private void updateStatistics() {
        int visible = tableHoaDon.getRowCount();
        lblTongHoaDon.setText(String.valueOf(visible));

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < visible; i++) {
            int modelRow = tableHoaDon.convertRowIndexToModel(i);
            String s = String.valueOf(modelHoaDon.getValueAt(modelRow, 6)).replaceAll("[^0-9]", "");
            if (!s.isEmpty()) total = total.add(new BigDecimal(s));
        }
        lblTongDoanhThu.setText(VND_FMT.format(total.longValue()) + " VNĐ");

        try {
            lblHoaDonHomNay.setText(String.valueOf(HoaDonDAO.countHoaDonToday()));
        } catch (Exception ignored) {}
    }

    // ─────────────────────── UI HELPERS ───────────────────────
    private JSpinner buildDateSpinner(java.util.Date defaultDate) {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "dd/MM/yyyy");
        sp.setEditor(editor);
        sp.setPreferredSize(new Dimension(0, 42));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 8, 4, 8)));
        if (defaultDate != null) sp.setValue(defaultDate);
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(Color.WHITE);
            de.getTextField().setForeground(TEXT_DARK);
            de.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
            de.getTextField().setBorder(new EmptyBorder(0, 4, 0, 4));
        }
        return sp;
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
                    g2.drawString(placeholder, getInsets().left,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
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

    private void styleComboBox(JComboBox<?> cb) {
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
        b.setBorder(null); b.setFocusPainted(false);
        b.setContentAreaFilled(false); b.setOpaque(false);
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

    private JPanel buildHintBox() {
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
        box.add(ht); box.add(vgap(5));

        for (String line : new String[]{
                "- Chọn ngày rồi nhấn [Lọc Ngày] để lọc.",
                "- [Hôm Nay] hiển thị hóa đơn trong ngày.",
                "- Click 1 lần → xem món ăn bên dưới.",
                "- Nhấn đôi vào hàng để xem chi tiết đầy đủ.",
                "- Tìm kiếm theo Mã HĐ hoặc Mã Bàn."}) {
            JLabel hl = new JLabel(line);
            hl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hl.setForeground(TEXT_DARK);
            hl.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(hl);
        }
        return box;
    }

    private Component makeSep() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return sep;
    }

    private Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }

    private String fmtMoney(BigDecimal amt) {
        if (amt == null) return "0 VNĐ";
        return VND_FMT.format(amt.longValue()) + " VNĐ";
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE); }
    private void err(String msg)  { JOptionPane.showMessageDialog(this, msg, "Lỗi",      JOptionPane.ERROR_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormQuanLyHoaDon(null).setVisible(true));
    }
}