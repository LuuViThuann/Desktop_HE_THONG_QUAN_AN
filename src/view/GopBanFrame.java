package view;

import config.DatabaseConfig;
import config.UserSession;
import model.Ban;
import model.NhanVien;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog GỘP BÀN
 * Gộp toàn bộ món từ các bàn nguồn (Đang sử dụng) vào một bàn đích (Đang sử dụng).
 * Sau khi gộp, các bàn nguồn trở về Trống.
 */
public class GopBanFrame extends JDialog {

    // ── Màu sắc ───────────────────────────────────────────────────────────────
    private static final Color PRIMARY_DARK   = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT  = new Color(70, 130, 180);
    private static final Color SUCCESS_GREEN  = new Color(46, 152, 102);
    private static final Color DANGER_RED     = new Color(192, 57, 43);
    private static final Color WARNING_AMBER  = new Color(241, 196, 15);
    private static final Color ACCENT_ORANGE  = new Color(230, 126, 34);
    private static final Color BG_MAIN        = new Color(241, 244, 247);
    private static final Color BG_SECONDARY   = new Color(255, 255, 255);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color BORDER_COLOR   = new Color(189, 195, 199);

    private final int maBanHienTai;        // Bàn đang chọn trong PanelManHinhChinh
    private final String tenBanHienTai;
    private boolean daGopBan = false;

    // UI state
    /** Danh sách bàn đang dùng (trừ bàn hiện tại) → ô chọn làm nguồn */
    private final List<Ban>      danhSachBanDangDung = new ArrayList<>();
    /** Các bàn người dùng chọn để gộp VÀO bàn đích */
    private final List<Integer>  selectedNguonIds    = new ArrayList<>();
    private final List<JPanel>   cardPanels          = new ArrayList<>();

    /** Bàn đích: nơi nhận tất cả món */
    private int    maBanDich    = -1;
    private String tenBanDich   = "";

    private JPanel gridNguon;
    private JLabel lblThongBao;
    private JLabel lblDichInfo;
    private JComboBox<String> cboBanDich;
    private DefaultComboBoxModel<String> cboDichModel;
    private JTextField txtTimKiemNguon;

    public GopBanFrame(JFrame parent, int maBanHienTai, String tenBanHienTai) {
        super(parent, "Gộp Bàn", true);
        this.maBanHienTai  = maBanHienTai;
        this.tenBanHienTai = tenBanHienTai;

        initUI();
        loadDuLieu();
        setSize(860, 580);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_MAIN);
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(139, 69, 19), getWidth(), 0, ACCENT_ORANGE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        hdr.setOpaque(false);
        hdr.setPreferredSize(new Dimension(0, 60));
        hdr.setLayout(new BorderLayout(12, 0));
        hdr.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblIcon = new JLabel("🔗");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblIcon.setForeground(Color.WHITE);

        JPanel txtPanel = new JPanel(new BorderLayout(0, 3));
        txtPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("GỘP BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Chọn bàn đích nhận món, rồi chọn các bàn nguồn cần gộp vào");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(255, 230, 190));

        txtPanel.add(lblTitle, BorderLayout.NORTH);
        txtPanel.add(lblSub,   BorderLayout.SOUTH);

        hdr.add(lblIcon,   BorderLayout.WEST);
        hdr.add(txtPanel,  BorderLayout.CENTER);
        return hdr;
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(BG_MAIN);
        body.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        // ─ Top: chọn bàn đích ─
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topBar.setBackground(BG_SECONDARY);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblDich = new JLabel("Bàn đích (nhận món):");
        lblDich.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cboDichModel = new DefaultComboBoxModel<>();
        cboBanDich   = new JComboBox<>(cboDichModel);
        cboBanDich.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboBanDich.setPreferredSize(new Dimension(220, 30));
        cboBanDich.addActionListener(e -> onDichChanged());

        lblDichInfo = new JLabel("");
        lblDichInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDichInfo.setForeground(TEXT_LIGHT);

        topBar.add(lblDich);
        topBar.add(cboBanDich);
        topBar.add(lblDichInfo);

        // ─ Instruction ─
     // ─ Instruction + Search ─
        JPanel instructRow = new JPanel(new BorderLayout(10, 0));
        instructRow.setBackground(BG_MAIN);
        
        instructRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));

        JLabel lblInstruct = new JLabel(
            "Đánh dấu tô màu xanh lá các bàn nguồn muốn gộp vào bàn đích:");
        lblInstruct.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInstruct.setForeground(PRIMARY_DARK);

        // Ô tìm kiếm
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBox.setOpaque(false);
        JLabel lblTimIcon = new JLabel("Tìm bàn:");
        lblTimIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtTimKiemNguon = new JTextField(12);
        txtTimKiemNguon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiemNguon.setPreferredSize(new Dimension(150, 28));
        txtTimKiemNguon.setToolTipText("Nhập tên bàn để lọc nhanh...");
        txtTimKiemNguon.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { locNguonTheoTen(); }
        });

        searchBox.add(lblTimIcon);
        searchBox.add(txtTimKiemNguon);

        instructRow.add(lblInstruct, BorderLayout.WEST);
        instructRow.add(searchBox,   BorderLayout.EAST);

        // ─ Grid bàn nguồn ─
        gridNguon = new JPanel(new GridLayout(0, 4, 14, 14));
        gridNguon.setBackground(BG_MAIN);

        JPanel nguonWrapper = new JPanel(new BorderLayout());
        nguonWrapper.setBackground(BG_MAIN);
        nguonWrapper.add(gridNguon, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(nguonWrapper);
        
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG_MAIN);
        scroll.getViewport().setBackground(BG_MAIN);

        // ─ Thông báo ─
        lblThongBao = new JLabel("Chưa có bàn nguồn nào được chọn", JLabel.CENTER);
        lblThongBao.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblThongBao.setForeground(TEXT_LIGHT);

        JPanel northSection = new JPanel(new BorderLayout(0, 4));
        northSection.setBackground(BG_MAIN);
        northSection.add(topBar,        BorderLayout.NORTH);
        northSection.add(instructRow,   BorderLayout.SOUTH);

        body.add(northSection, BorderLayout.NORTH);
        body.add(scroll,       BorderLayout.CENTER);
        body.add(lblThongBao,  BorderLayout.SOUTH);
        return body;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(BG_SECONDARY);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnHuy    = createBtn("Huỷ",               new Color(149, 165, 166));
        JButton btnXacNhan = createBtn("Xác Nhận Gộp", ACCENT_ORANGE);

        btnHuy.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> xacNhanGopBan());

        footer.add(btnHuy);
        footer.add(btnXacNhan);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void loadDuLieu() {
        // Load tất cả bàn Đang sử dụng
        String q = "SELECT b.MaBan, b.TenBan, b.TrangThai, b.MaKV, kv.TenKV " +
                   "FROM Ban b JOIN KhuVucQuan kv ON b.MaKV = kv.MaKV " +
                   "WHERE b.TrangThai = 'Đang sử dụng' " +
                   "ORDER BY b.MaKV, b.MaBan";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                Ban ban = new Ban();
                ban.setMaBan(rs.getInt("MaBan"));
                ban.setTenBan(rs.getString("TenBan"));
                ban.setTrangThai(rs.getString("TrangThai"));
                ban.setMaKV(rs.getInt("MaKV"));
                ban.setTenKV(rs.getString("TenKV"));
                danhSachBanDangDung.add(ban);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (danhSachBanDangDung.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không có bàn nào đang sử dụng để gộp!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        // Populate combo bàn đích (tất cả bàn đang dùng)
        cboDichModel.addElement("— Chọn bàn đích —");
        for (Ban ban : danhSachBanDangDung) {
            cboDichModel.addElement(ban.getMaBan() + " | " + ban.getTenBan() + " (" + ban.getTenKV() + ")");
        }

        // Nếu bàn hiện tại đang dùng → pre-select làm đích
        if (maBanHienTai != -1) {
            for (int i = 0; i < danhSachBanDangDung.size(); i++) {
                if (danhSachBanDangDung.get(i).getMaBan() == maBanHienTai) {
                    cboBanDich.setSelectedIndex(i + 1);
                    break;
                }
            }
        }

        refreshNguonGrid();
    }

    /** Lọc card bàn nguồn theo tên — không query DB lại */
    private void locNguonTheoTen() {
        String keyword = txtTimKiemNguon.getText().trim().toLowerCase();
        gridNguon.removeAll();

        boolean coKetQua = false;
        for (Ban ban : danhSachBanDangDung) {
            if (ban.getMaBan() == maBanDich) continue; // bỏ qua bàn đích
            if (keyword.isEmpty() || ban.getTenBan().toLowerCase().contains(keyword)) {
                gridNguon.add(createNguonCard(ban));
                coKetQua = true;
            }
        }

        if (!coKetQua) {
            JLabel empty = new JLabel(
                "Không tìm thấy: \"" + txtTimKiemNguon.getText().trim() + "\"", JLabel.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(DANGER_RED);
            gridNguon.add(empty);
        }

        gridNguon.revalidate();
        gridNguon.repaint();
    }
    
    private void onDichChanged() {
        int idx = cboBanDich.getSelectedIndex();
        if (idx == 0) {
            maBanDich  = -1;
            tenBanDich = "";
            lblDichInfo.setText("");
        } else {
            Ban ban = danhSachBanDangDung.get(idx - 1);
            maBanDich  = ban.getMaBan();
            tenBanDich = ban.getTenBan();
            lblDichInfo.setText("Đã chọn: " + tenBanDich + " (" + ban.getTenKV() + ")");
            lblDichInfo.setForeground(SUCCESS_GREEN);
        }
        selectedNguonIds.clear();
        refreshNguonGrid();
    }

    private void refreshNguonGrid() {
    	 if (txtTimKiemNguon != null) txtTimKiemNguon.setText("");
        gridNguon.removeAll();
        cardPanels.clear();

        for (Ban ban : danhSachBanDangDung) {
            if (ban.getMaBan() == maBanDich) continue; // không hiện bàn đích trong danh sách nguồn
            JPanel card = createNguonCard(ban);
            gridNguon.add(card);
            cardPanels.add(card);
        }

        if (gridNguon.getComponentCount() == 0) {
            JLabel empty = new JLabel("Không có bàn nguồn nào khả dụng", JLabel.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(TEXT_LIGHT);
            gridNguon.add(empty);
        }

        gridNguon.revalidate();
        gridNguon.repaint();
        updateThongBao();
    }

    private JPanel createNguonCard(Ban ban) {
        boolean selected = selectedNguonIds.contains(ban.getMaBan());

        JCheckBox chk = new JCheckBox();
        chk.setSelected(selected);
        chk.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.setColor(selected ? SUCCESS_GREEN : BORDER_COLOR);
                g2.setStroke(new BasicStroke(selected ? 2.5f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBackground(selected ? new Color(210, 245, 220) : BG_SECONDARY);
        card.setPreferredSize(new Dimension(130, 120));
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel("🪑", JLabel.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JLabel lblTen = new JLabel(ban.getTenBan(), JLabel.CENTER);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblKV = new JLabel(ban.getTenKV(), JLabel.CENTER);
        lblKV.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblKV.setForeground(TEXT_LIGHT);

        // Số lượng món
        JLabel lblMon = new JLabel(getSoMon(ban.getMaBan()) + " món", JLabel.CENTER);
        lblMon.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblMon.setForeground(DANGER_RED);

        JPanel chkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chkRow.setOpaque(false);
        chkRow.add(chk);

        JPanel bottom = new JPanel(new GridLayout(4, 1, 0, 1));
        bottom.setOpaque(false);
        bottom.add(lblTen);
        bottom.add(lblKV);
        bottom.add(lblMon);
        bottom.add(chkRow);

        card.add(lblIcon, BorderLayout.CENTER);
        card.add(bottom,  BorderLayout.SOUTH);

        // Toggle on click
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                toggleNguon(ban.getMaBan());
                refreshNguonGrid();
            }
        };
        card.addMouseListener(ma);
        chk.addActionListener(e -> { toggleNguon(ban.getMaBan()); refreshNguonGrid(); });

        return card;
    }

    private void toggleNguon(int maBan) {
        if (selectedNguonIds.contains(maBan)) selectedNguonIds.remove(Integer.valueOf(maBan));
        else selectedNguonIds.add(maBan);
        updateThongBao();
    }

    private void updateThongBao() {
        if (selectedNguonIds.isEmpty()) {
            lblThongBao.setText("Chưa có bàn nguồn nào được chọn");
            lblThongBao.setForeground(TEXT_LIGHT);
            lblThongBao.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        } else {
            lblThongBao.setText("Đã chọn " + selectedNguonIds.size() + " bàn nguồn để gộp vào "
                + (tenBanDich.isEmpty() ? "[chưa chọn bàn đích]" : tenBanDich));
            lblThongBao.setForeground(ACCENT_ORANGE);
            lblThongBao.setFont(new Font("Segoe UI", Font.BOLD, 11));
        }
    }

    private int getSoMon(int maBan) {
        String q = "SELECT COUNT(*) FROM HoaDonBanHang WHERE MaBan = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, maBan);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    // ── Xác nhận gộp bàn ─────────────────────────────────────────────────────
    private void xacNhanGopBan() {
        if (maBanDich == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn đích!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedNguonIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một bàn nguồn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Gộp các bàn sau vào " + tenBanDich + "?\n");
        for (int id : selectedNguonIds) {
            danhSachBanDangDung.stream()
                .filter(b -> b.getMaBan() == id)
                .findFirst()
                .ifPresent(b -> sb.append("  • ").append(b.getTenBan()).append("\n"));
        }
        sb.append("Sau khi gộp, các bàn nguồn sẽ về Trống.");

        int confirm = JOptionPane.showConfirmDialog(this, sb.toString(),
            "Xác nhận gộp bàn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            for (int srcId : selectedNguonIds) {
                // 1. Xử lý xung đột: nếu bàn đích đã có cùng MaMon → cộng dồn SoLuong
                String findConflict =
                    "SELECT src.MaMon, src.SoLuong AS srcQty, dst.MaHDBH AS dstId, dst.SoLuong AS dstQty " +
                    "FROM HoaDonBanHang src " +
                    "JOIN HoaDonBanHang dst ON src.MaMon = dst.MaMon AND dst.MaBan = ? " +
                    "WHERE src.MaBan = ?";
                PreparedStatement psConflict = conn.prepareStatement(findConflict);
                psConflict.setInt(1, maBanDich);
                psConflict.setInt(2, srcId);
                ResultSet rsC = psConflict.executeQuery();

                while (rsC.next()) {
                    int maMon  = rsC.getInt("MaMon");
                    int srcQty = rsC.getInt("srcQty");
                    int dstId  = rsC.getInt("dstId");
                    int dstQty = rsC.getInt("dstQty");
                    // Cộng số lượng vào bàn đích
                    String updDst = "UPDATE HoaDonBanHang SET SoLuong = ? WHERE MaHDBH = ?";
                    PreparedStatement psUD = conn.prepareStatement(updDst);
                    psUD.setInt(1, srcQty + dstQty);
                    psUD.setInt(2, dstId);
                    psUD.executeUpdate();
                    psUD.close();
                    // Xóa dòng bàn nguồn đã merge
                    String delSrc = "DELETE FROM HoaDonBanHang WHERE MaBan = ? AND MaMon = ?";
                    PreparedStatement psDel = conn.prepareStatement(delSrc);
                    psDel.setInt(1, srcId);
                    psDel.setInt(2, maMon);
                    psDel.executeUpdate();
                    psDel.close();
                }
                psConflict.close();

                // 2. Di chuyển các dòng không xung đột
                String moveSrc = "UPDATE HoaDonBanHang SET MaBan = ? WHERE MaBan = ?";
                PreparedStatement psMove = conn.prepareStatement(moveSrc);
                psMove.setInt(1, maBanDich);
                psMove.setInt(2, srcId);
                psMove.executeUpdate();
                psMove.close();

                // 3. Bàn nguồn → Trống
                String updSrc = "UPDATE Ban SET TrangThai = 'Trống' WHERE MaBan = ?";
                PreparedStatement psTrong = conn.prepareStatement(updSrc);
                psTrong.setInt(1, srcId);
                psTrong.executeUpdate();
                psTrong.close();

                // 4. Ghi lịch sử (gộp = chuyển + gộp)
                NhanVien nv = UserSession.getInstance().getCurrentUser();
                int maNV = nv != null ? nv.getMaNV() : 1;
                String insLog = "INSERT INTO LichSuChuyenBan (MaBanCu, MaBanMoi, NgayChuyen, MaNV, GhiChu) " +
                                "VALUES (?, ?, CURDATE(), ?, 'Gộp bàn')";
                PreparedStatement psLog = conn.prepareStatement(insLog);
                psLog.setInt(1, srcId);
                psLog.setInt(2, maBanDich);
                psLog.setInt(3, maNV);
                psLog.executeUpdate();
                psLog.close();
            }

            conn.commit();
            daGopBan = true;
            JOptionPane.showMessageDialog(this,
                "Gộp bàn thành công! Các món đã được chuyển vào " + tenBanDich,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi gộp bàn:\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaGopBan() { return daGopBan; }

    // ── Util ─────────────────────────────────────────────────────────────────
    private JButton createBtn(String text, Color bg) {
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
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(190, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);          btn.repaint(); }
        });
        return btn;
    }
}