package view;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import dao.BanDAO;
import model.DatBanTruoc;

public class ThongTinDatBanFrame extends JDialog {
    private static final long serialVersionUID = 1L;
    private int maBan;
    private String tenBan;
    private DatBanTruoc datBan;

    /**
     * true nếu user nhấn "GỌI MÓN NGAY".
     * PanelManHinhChinh đọc flag này để chuyển sang tab Gọi Món inline
     * thay vì mở GoiMonFrame riêng.
     */
    private boolean goiMonRequested = false;

    public boolean isGoiMonRequested() { return goiMonRequested; }

    // ── Palette (đồng bộ DatBanFrame) ──────────────────────────────
    private static final Color BG_DARK        = new Color(18, 18, 24);
    private static final Color BG_CARD        = new Color(28, 28, 38);
    private static final Color BG_FIELD       = new Color(22, 22, 30);
    private static final Color BG_ROW_ALT     = new Color(32, 32, 44);
    private static final Color ACCENT_GOLD    = new Color(212, 175, 55);
    private static final Color ACCENT_GOLD_DIM= new Color(140, 112, 20);
    private static final Color TEXT_PRIMARY   = new Color(240, 235, 220);
    private static final Color TEXT_SECONDARY = new Color(140, 135, 120);
    private static final Color TEXT_MUTED     = new Color(90, 88, 80);
    private static final Color BORDER_SUBTLE  = new Color(55, 55, 72);
    private static final Color SUCCESS_COLOR  = new Color(52, 199, 89);
    private static final Color DANGER_COLOR   = new Color(255, 69, 58);
    private static final Color NEUTRAL_COLOR  = new Color(90, 90, 105);

    public ThongTinDatBanFrame(JFrame parent, int maBan, String tenBan) {
        super(parent, "Thông Tin Đặt Bàn", true);
        this.maBan  = maBan;
        this.tenBan = tenBan;
        this.datBan = BanDAO.getThongTinDatBan(maBan);

        if (datBan != null) {
            initComponents();
            setLocationRelativeTo(parent);
        } else {
            JOptionPane.showMessageDialog(parent,
                "Không tìm thấy thông tin đặt bàn!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(560, 620);
        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(false);

        RoundedPanel root = new RoundedPanel(20, BG_CARD);
        root.setLayout(new BorderLayout());
        setContentPane(root);

        // ── BANNER ─────────────────────────────────────────────────
        JPanel banner = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 40, 10),
                        getWidth(), getHeight(), new Color(28, 22, 4));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 20, 20);

                // Gold accent line bottom
                g2.setColor(ACCENT_GOLD_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(36, getHeight() - 1, getWidth() - 36, getHeight() - 1);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(560, 115));
        banner.setLayout(new GridBagLayout());

        JPanel bannerInner = new JPanel();
        bannerInner.setOpaque(false);
        bannerInner.setLayout(new BoxLayout(bannerInner, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("THÔNG TIN ĐẶT BÀN");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 24));
        lblTitle.setForeground(ACCENT_GOLD);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Decorative rule
        JPanel rule = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(ACCENT_GOLD_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, cy, cx - 10, cy);
                g2.drawLine(cx + 10, cy, getWidth(), cy);
                // Diamond center
                int[] xp = {cx, cx + 5, cx, cx - 5};
                int[] yp = {cy - 4, cy, cy + 4, cy};
                g2.setColor(ACCENT_GOLD);
                g2.fillPolygon(xp, yp, 4);
                g2.dispose();
            }
        };
        rule.setOpaque(false);
        rule.setPreferredSize(new Dimension(280, 14));
        rule.setMaximumSize(new Dimension(300, 14));
        rule.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel(tenBan.toUpperCase() + "  ·  Chi tiết đặt chỗ");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        bannerInner.add(lblTitle);
        bannerInner.add(Box.createVerticalStrut(8));
        bannerInner.add(rule);
        bannerInner.add(Box.createVerticalStrut(8));
        bannerInner.add(lblSub);

        banner.add(bannerInner);
        root.add(banner, BorderLayout.NORTH);

        // ── INFO TABLE ─────────────────────────────────────────────
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd / MM / yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH : mm");

        JPanel tablePanel = new JPanel();
        tablePanel.setOpaque(false);
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBorder(new EmptyBorder(28, 36, 12, 36));

        // Section label
        tablePanel.add(buildSectionLabel("KHÁCH HÀNG"));
        tablePanel.add(Box.createVerticalStrut(10));
        tablePanel.add(buildRow("Họ và Tên",      datBan.getHoTenKhachHang(), false, TEXT_PRIMARY));
        tablePanel.add(buildRow("Số Điện Thoại",  datBan.getSdt(),            true,  TEXT_PRIMARY));

        tablePanel.add(Box.createVerticalStrut(20));
        tablePanel.add(buildSectionLabel("CHI TIẾT ĐẶT CHỖ"));
        tablePanel.add(Box.createVerticalStrut(10));
        tablePanel.add(buildRow("Bàn",             tenBan,                                         false, ACCENT_GOLD));
        tablePanel.add(buildRow("Số Lượng Khách", datBan.getSoLuongKhach() + " người",             true,  TEXT_PRIMARY));
        tablePanel.add(buildRow("Ngày Đặt",        sdfDate.format(datBan.getNgayDat()),            false, TEXT_PRIMARY));
        tablePanel.add(buildRow("Giờ Đặt",         sdfTime.format(datBan.getGioDat()),             true,  TEXT_PRIMARY));

        root.add(tablePanel, BorderLayout.CENTER);

        // ── BUTTON AREA ────────────────────────────────────────────
        JPanel btnArea = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_SUBTLE);
                g2.drawLine(36, 0, getWidth() - 36, 0);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnArea.setOpaque(false);
        btnArea.setLayout(new BoxLayout(btnArea, BoxLayout.Y_AXIS));
        btnArea.setBorder(new EmptyBorder(20, 36, 28, 36));

        // Primary row: Gọi Món + Hủy Đặt
        JPanel primaryRow = new JPanel(new GridLayout(1, 2, 12, 0));
        primaryRow.setOpaque(false);
        primaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JButton btnGoiMon = buildSolidButton("GỌI MÓN NGAY",   SUCCESS_COLOR, Color.WHITE);
        JButton btnHuyDat = buildSolidButton("HỦY ĐẶT BÀN",    DANGER_COLOR,  Color.WHITE);
        btnGoiMon.addActionListener(e -> onGoiMonNgay());
        btnHuyDat.addActionListener(e -> onHuyDatBan());

        primaryRow.add(btnGoiMon);
        primaryRow.add(btnHuyDat);

        JButton btnDong = buildGhostButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        btnDong.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnArea.add(primaryRow);
        btnArea.add(Box.createVerticalStrut(10));
        btnArea.add(btnDong);

        root.add(btnArea, BorderLayout.SOUTH);

        makeDraggable(root);
    }

    // ── Row builder ─────────────────────────────────────────────────

    private JPanel buildSectionLabel(String text) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);

        // Trailing line
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_SUBTLE);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        line.setOpaque(false);

        p.add(lbl, BorderLayout.WEST);
        p.add(line, BorderLayout.CENTER);
        return p;
    }

    /** Single info row: label left, value right */
    private JPanel buildRow(String label, String value, boolean alternate, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(alternate ? BG_ROW_ALT : BG_FIELD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblKey.setForeground(TEXT_SECONDARY);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Serif", Font.BOLD, 14));
        lblVal.setForeground(valueColor);
        lblVal.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);

        // Wrapper to enforce MaximumSize within BoxLayout
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        wrap.add(row, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(0, 0, 4, 0));
        return wrap;
    }

    // ── Button builders ─────────────────────────────────────────────

    private JButton buildSolidButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker().darker()
                        : getModel().isRollover() ? bg.darker() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(fg);
                g2.setFont(getFont());
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildGhostButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(getModel().isRollover() ? TEXT_PRIMARY : TEXT_SECONDARY);
                g2.setFont(getFont());
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setPreferredSize(new Dimension(480, 34));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Logic ────────────────────────────────────────────────────────

    private void onGoiMonNgay() {
        // FIX: Không cập nhật trạng thái bàn ở đây, không mở GoiMonFrame.
        // Chỉ set flag để PanelManHinhChinh biết cần chuyển sang tab Gọi Món inline.
        // Trạng thái bàn sẽ được cập nhật thành 'Đang sử dụng' chỉ khi
        // thực sự có món được thêm vào (trong addMonToCurrentBan).
        goiMonRequested = true;
        dispose();
    }

    private void onHuyDatBan() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn hủy đặt bàn này?",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (BanDAO.huyDatBan(maBan)) {
                JOptionPane.showMessageDialog(this,
                    "Đã hủy đặt bàn thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi hủy đặt bàn!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Draggable ────────────────────────────────────────────────────

    private void makeDraggable(JComponent comp) {
        final int[] origin = new int[2];
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                origin[0] = e.getX(); origin[1] = e.getY();
            }
        });
        comp.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point p = getLocation();
                setLocation(p.x + e.getX() - origin[0],
                            p.y + e.getY() - origin[1]);
            }
        });
    }

    // ── Inner helpers ────────────────────────────────────────────────

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
            g2.dispose();
        }
    }
}