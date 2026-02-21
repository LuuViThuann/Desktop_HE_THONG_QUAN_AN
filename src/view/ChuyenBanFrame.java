package view;

import config.DatabaseConfig;
import config.UserSession;
import dao.BanDAO;
import model.Ban;
import model.NhanVien;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog CHUYỂN BÀN
 * Cho phép chuyển toàn bộ món từ bàn nguồn sang bàn đích (phải đang Trống).
 * Ghi lịch sử vào bảng LichSuChuyenBan.
 */
public class ChuyenBanFrame extends JDialog {

    // ── Màu sắc nhất quán với PanelManHinhChinh ───────────────────────────────
    private static final Color PRIMARY_DARK  = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);
    private static final Color DANGER_RED    = new Color(192, 57, 43);
    private static final Color WARNING_AMBER = new Color(241, 196, 15);
    private static final Color BG_MAIN       = new Color(241, 244, 247);
    private static final Color BG_SECONDARY  = new Color(255, 255, 255);
    private static final Color TEXT_DARK     = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT    = new Color(127, 140, 141);
    private static final Color BORDER_COLOR  = new Color(189, 195, 199);

    private final int maBanNguon;
    private final String tenBanNguon;
    private boolean daChuyenBan = false;

    // UI
    private JPanel gridBanDich;
    private JScrollPane scrollBanDich;
    private JPanel selectedBanButton = null;
    private int selectedMaBanDich = -1;
    private String selectedTenBanDich = "";
    private JTextField txtGhiChu;
    private JLabel lblThongBao;
    private JComboBox<String> cboKhuVuc;
    private List<Ban> danhSachBanTrong = new ArrayList<>();
    private JTextField txtTimKiem;

    public ChuyenBanFrame(JFrame parent, int maBanNguon, String tenBanNguon) {
        super(parent, "Chuyển Bàn — " + tenBanNguon, true);
        this.maBanNguon  = maBanNguon;
        this.tenBanNguon = tenBanNguon;

        initUI();
        loadKhuVuc();
        loadBanTrong(-1); // tất cả khu vực
        setSize(820, 580);
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
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, new Color(40, 80, 140));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        hdr.setOpaque(false);
        hdr.setPreferredSize(new Dimension(0, 60));
        hdr.setLayout(new BorderLayout(12, 0));
        hdr.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblIcon  = new JLabel("🔄");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblIcon.setForeground(Color.WHITE);

        JPanel txtPanel = new JPanel(new BorderLayout(0, 3));
        txtPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("CHUYỂN BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Từ: " + tenBanNguon + "  →  Chọn bàn đích (phải đang Trống)");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(200, 220, 240));

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

        // Top: filter + ghi chú
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topBar.setBackground(BG_SECONDARY);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel lblKV = new JLabel("Khu vực:");
        lblKV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboKhuVuc = new JComboBox<>();
        cboKhuVuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboKhuVuc.setPreferredSize(new Dimension(160, 30));
        cboKhuVuc.addActionListener(e -> {
            int idx = cboKhuVuc.getSelectedIndex();
            loadBanTrong(idx == 0 ? -1 : getMaKVFromCombo(idx));
        });

        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtGhiChu = new JTextField(18);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setPreferredSize(new Dimension(200, 30));

        topBar.add(lblKV);
        topBar.add(cboKhuVuc);
        topBar.add(Box.createHorizontalStrut(12));

        // ── Tìm kiếm bàn ──
        JLabel lblTim = new JLabel("Tìm bàn:");
        lblTim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtTimKiem = new JTextField(12);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiem.setPreferredSize(new Dimension(140, 30));
        txtTimKiem.setToolTipText("Nhập tên bàn để lọc nhanh...");
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { locBanTheoTen(); }
        });

        topBar.add(lblTim);
        topBar.add(txtTimKiem);
        topBar.add(Box.createHorizontalStrut(12));
        topBar.add(lblGhiChu);
        topBar.add(txtGhiChu);

        // Grid bàn đích
        gridBanDich = new JPanel(new GridLayout(0, 4, 14, 14)); // 0 = tự tăng hàng, 4 cột
        gridBanDich.setBackground(BG_MAIN);
        JPanel wrapPanel = new JPanel(new BorderLayout());
        wrapPanel.setBackground(BG_MAIN);
        wrapPanel.add(gridBanDich, BorderLayout.NORTH); // NORTH để không giãn dọc
        
        scrollBanDich = new JScrollPane(gridBanDich);
        scrollBanDich.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scrollBanDich.getVerticalScrollBar().setUnitIncrement(16);
        scrollBanDich.setBackground(BG_MAIN);
        scrollBanDich.getViewport().setBackground(BG_MAIN);

        // Thông báo
        lblThongBao = new JLabel("Chọn bàn đích bên dưới rồi nhấn Xác Nhận Chuyển", JLabel.CENTER);
        lblThongBao.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblThongBao.setForeground(TEXT_LIGHT);
        lblThongBao.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));

        body.add(topBar,        BorderLayout.NORTH);
        body.add(scrollBanDich, BorderLayout.CENTER);
        body.add(lblThongBao,   BorderLayout.SOUTH);
        return body;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(BG_SECONDARY);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnHuy = createBtn("Huỷ", new Color(149, 165, 166));
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = createBtn("Xác Nhận Chuyển", SUCCESS_GREEN);
        btnXacNhan.addActionListener(e -> xacNhanChuyenBan());

        footer.add(btnHuy);
        footer.add(btnXacNhan);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void loadKhuVuc() {
        cboKhuVuc.addItem("— Tất cả khu vực —");
        String q = "SELECT MaKV, TenKV FROM KhuVucQuan ORDER BY MaKV";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                cboKhuVuc.addItem(rs.getInt("MaKV") + " | " + rs.getString("TenKV"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int getMaKVFromCombo(int idx) {
        String item = (String) cboKhuVuc.getItemAt(idx);
        try { return Integer.parseInt(item.split("\\|")[0].trim()); }
        catch (Exception e) { return -1; }
    }

    /** Load danh sách bàn Trống (không phải bàn nguồn). maKV == -1 → tất cả */
    private void loadBanTrong(int maKV) {
        danhSachBanTrong.clear();
        gridBanDich.removeAll();
        selectedBanButton    = null;
        selectedMaBanDich    = -1;
        selectedTenBanDich   = "";
        if (txtTimKiem != null) txtTimKiem.setText(""); 

        String q = "SELECT b.MaBan, b.TenBan, b.TrangThai, b.MaKV, kv.TenKV " +
                   "FROM Ban b JOIN KhuVucQuan kv ON b.MaKV = kv.MaKV " +
                   "WHERE b.TrangThai = 'Trống' AND b.MaBan != ? " +
                   (maKV > 0 ? "AND b.MaKV = ? " : "") +
                   "ORDER BY b.MaKV, b.MaBan";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, maBanNguon);
            if (maKV > 0) ps.setInt(2, maKV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ban ban = new Ban();
                ban.setMaBan(rs.getInt("MaBan"));
                ban.setTenBan(rs.getString("TenBan"));
                ban.setTrangThai(rs.getString("TrangThai"));
                ban.setMaKV(rs.getInt("MaKV"));
                ban.setTenKV(rs.getString("TenKV"));
                danhSachBanTrong.add(ban);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (danhSachBanTrong.isEmpty()) {
            JLabel empty = new JLabel("Không có bàn trống phù hợp", JLabel.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(TEXT_LIGHT);
            gridBanDich.add(empty);
        } else {
            for (Ban ban : danhSachBanTrong) {
                gridBanDich.add(createBanCard(ban));
            }
        }

        gridBanDich.revalidate();
        gridBanDich.repaint();
    }

    private JPanel createBanCard(Ban ban) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBackground(BG_SECONDARY);
        card.setPreferredSize(new Dimension(120, 110));
        card.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel("🪑", JLabel.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel lblTen = new JLabel(ban.getTenBan(), JLabel.CENTER);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTen.setForeground(TEXT_DARK);

        JLabel lblKV = new JLabel(ban.getTenKV(), JLabel.CENTER);
        lblKV.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblKV.setForeground(TEXT_LIGHT);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.add(lblTen);
        infoPanel.add(lblKV);

        card.add(lblIcon,    BorderLayout.CENTER);
        card.add(infoPanel,  BorderLayout.SOUTH);

        // Click to select
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectCard(card, ban, lblTen);
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (card != selectedBanButton) {
                    card.setBackground(new Color(230, 245, 255));
                    card.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (card != selectedBanButton) {
                    card.setBackground(BG_SECONDARY);
                    card.repaint();
                }
            }
        };
        card.addMouseListener(ma);
        lblIcon.addMouseListener(ma);
        lblTen.addMouseListener(ma);
        lblKV.addMouseListener(ma);

        return card;
    }
    /** Lọc card bàn theo tên — không cần query DB lại */
    private void locBanTheoTen() {
        String keyword = txtTimKiem.getText().trim().toLowerCase();
        gridBanDich.removeAll();

        boolean coKetQua = false;
        for (Ban ban : danhSachBanTrong) {
            if (keyword.isEmpty() || ban.getTenBan().toLowerCase().contains(keyword)) {
                gridBanDich.add(createBanCard(ban));
                coKetQua = true;
            }
        }

        if (!coKetQua) {
            JLabel empty = new JLabel("Không tìm thấy bàn: \"" + txtTimKiem.getText().trim() + "\"", JLabel.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(DANGER_RED);
            gridBanDich.add(empty);
        }

        gridBanDich.revalidate();
        gridBanDich.repaint();
    }
    
    private void selectCard(JPanel card, Ban ban, JLabel lblTen) {
        // Deselect previous
        if (selectedBanButton != null) {
            selectedBanButton.setBackground(BG_SECONDARY);
            selectedBanButton.repaint();
        }
        selectedBanButton  = card;
        selectedMaBanDich  = ban.getMaBan();
        selectedTenBanDich = ban.getTenBan();
        card.setBackground(new Color(200, 235, 200));
        card.repaint();
        lblThongBao.setText("Đã chọn bàn đích: " + ban.getTenBan() + " (" + ban.getTenKV() + ")");
        lblThongBao.setForeground(SUCCESS_GREEN);
        lblThongBao.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    // ── Xác nhận chuyển bàn ──────────────────────────────────────────────────
    private void xacNhanChuyenBan() {
        if (selectedMaBanDich == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn đích!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Chuyển toàn bộ món từ %s → %s?\nHành động này không thể hoàn tác.",
                tenBanNguon, selectedTenBanDich),
            "Xác nhận chuyển bàn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Cập nhật HoaDonBanHang: đổi MaBan
            String upd = "UPDATE HoaDonBanHang SET MaBan = ? WHERE MaBan = ?";
            PreparedStatement ps1 = conn.prepareStatement(upd);
            ps1.setInt(1, selectedMaBanDich);
            ps1.setInt(2, maBanNguon);
            ps1.executeUpdate();
            ps1.close();

            // 2. Bàn nguồn → Trống
            String updSrc = "UPDATE Ban SET TrangThai = 'Trống' WHERE MaBan = ?";
            PreparedStatement ps2 = conn.prepareStatement(updSrc);
            ps2.setInt(1, maBanNguon);
            ps2.executeUpdate();
            ps2.close();

            // 3. Bàn đích → Đang sử dụng
            String updDst = "UPDATE Ban SET TrangThai = 'Đang sử dụng' WHERE MaBan = ?";
            PreparedStatement ps3 = conn.prepareStatement(updDst);
            ps3.setInt(1, selectedMaBanDich);
            ps3.executeUpdate();
            ps3.close();

            // 4. Ghi lịch sử chuyển bàn
            NhanVien nv = UserSession.getInstance().getCurrentUser();
            int maNV = nv != null ? nv.getMaNV() : 1;
            String insLog = "INSERT INTO LichSuChuyenBan (MaBanCu, MaBanMoi, NgayChuyen, MaNV, GhiChu) " +
                            "VALUES (?, ?, CURDATE(), ?, ?)";
            PreparedStatement ps4 = conn.prepareStatement(insLog);
            ps4.setInt(1, maBanNguon);
            ps4.setInt(2, selectedMaBanDich);
            ps4.setInt(3, maNV);
            ps4.setString(4, txtGhiChu.getText().trim().isEmpty() ? null : txtGhiChu.getText().trim());
            ps4.executeUpdate();
            ps4.close();

            conn.commit();

            daChuyenBan = true;
            JOptionPane.showMessageDialog(this,
                "Chuyển bàn thành công!\n" + tenBanNguon + " → " + selectedTenBanDich,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi chuyển bàn:\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaChuyenBan() { return daChuyenBan; }

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
        btn.setPreferredSize(new Dimension(180, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);          btn.repaint(); }
        });
        return btn;
    }
}