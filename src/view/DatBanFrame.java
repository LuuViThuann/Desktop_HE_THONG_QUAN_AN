package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.Date;
import java.sql.Time;
import dao.BanDAO;
import model.DatBanTruoc;

public class DatBanFrame extends JDialog {
    private static final long serialVersionUID = 1L;
    private int maBan;
    private String tenBan;

    private JTextField txtHoTenKhach;
    private JTextField txtSDT;
    private JSpinner spinnerSoLuongKhach;
    private JSpinner spinnerNgayDat;
    private JSpinner spinnerGioDat;

    // === Color Palette ===
    private static final Color BG_DARK        = new Color(18, 18, 24);
    private static final Color BG_CARD        = new Color(28, 28, 38);
    private static final Color BG_INPUT       = new Color(38, 38, 52);
    private static final Color ACCENT_GOLD    = new Color(212, 175, 55);
    private static final Color ACCENT_GOLD_DIM= new Color(180, 145, 30);
    private static final Color TEXT_PRIMARY   = new Color(240, 235, 220);
    private static final Color TEXT_SECONDARY = new Color(160, 155, 140);
    private static final Color BORDER_SUBTLE  = new Color(60, 60, 80);
    private static final Color SUCCESS_COLOR  = new Color(52, 199, 89);
    private static final Color DANGER_COLOR   = new Color(255, 69, 58);

    public DatBanFrame(JFrame parent, int maBan, String tenBan) {
        super(parent, "Đặt Bàn", true);
        this.maBan = maBan;
        this.tenBan = tenBan;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(520, 600);
        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(false);

        // Root panel with rounded corners
        RoundedPanel root = new RoundedPanel(20, BG_CARD);
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(root);

        // ── TOP BANNER ──────────────────────────────────────────────
        JPanel banner = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 40, 10),
                        getWidth(), getHeight(), new Color(30, 25, 5));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 20, 20);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(520, 110));
        banner.setLayout(new GridBagLayout());

        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));

        JLabel lblRestaurant = new JLabel("✦  ĐẶT BÀN TRƯỚC  ✦");
        lblRestaurant.setFont(loadFont("Serif", Font.BOLD, 22));
        lblRestaurant.setForeground(ACCENT_GOLD);
        lblRestaurant.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTable = new JLabel(tenBan.toUpperCase());
        lblTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTable.setForeground(TEXT_SECONDARY);
        lblTable.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTable.setBorder(new EmptyBorder(6, 0, 0, 0));

        bannerText.add(lblRestaurant);
        bannerText.add(lblTable);
        banner.add(bannerText);

        // Gold divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(ACCENT_GOLD_DIM);
        sep.setBackground(ACCENT_GOLD_DIM);

        root.add(banner, BorderLayout.NORTH);

        // ── FORM BODY ────────────────────────────────────────────────
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(28, 36, 10, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 16, 0);

        // Họ tên
        gbc.gridy = 0;
        body.add(buildField("👤  Họ & Tên Khách Hàng", txtHoTenKhach = buildTextField()), gbc);

        // SĐT
        gbc.gridy = 1;
        body.add(buildField("📱  Số Điện Thoại", txtSDT = buildTextField()), gbc);

        // Số lượng
        spinnerSoLuongKhach = buildStyledSpinner(new SpinnerNumberModel(2, 1, 30, 1));
        gbc.gridy = 2;
        body.add(buildField("👥  Số Lượng Khách", spinnerSoLuongKhach), gbc);

        // Ngày + Giờ cùng hàng
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spinnerNgayDat = buildStyledSpinner(dateModel);
        spinnerNgayDat.setEditor(new JSpinner.DateEditor(spinnerNgayDat, "dd/MM/yyyy"));

        SpinnerDateModel timeModel = new SpinnerDateModel();
        spinnerGioDat = buildStyledSpinner(timeModel);
        spinnerGioDat.setEditor(new JSpinner.DateEditor(spinnerGioDat, "HH:mm"));

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.weightx = 1.0;
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 3;
        gbcLeft.gridwidth = 1;
        gbcLeft.insets = new Insets(0, 0, 0, 8);
        body.add(buildField("📅  Ngày Đặt", spinnerNgayDat), gbcLeft);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.weightx = 1.0;
        gbcRight.gridx = 1;
        gbcRight.gridy = 3;
        gbcRight.gridwidth = 1;
        gbcRight.insets = new Insets(0, 8, 0, 0);
        body.add(buildField("🕐  Giờ Đặt", spinnerGioDat), gbcRight);

        root.add(body, BorderLayout.CENTER);

        // ── BUTTON PANEL ─────────────────────────────────────────────
        JPanel btnArea = new JPanel();
        btnArea.setOpaque(false);
        btnArea.setLayout(new BoxLayout(btnArea, BoxLayout.Y_AXIS));
        btnArea.setBorder(new EmptyBorder(8, 36, 30, 36));

        JButton btnDat = buildActionButton("XÁC NHẬN ĐẶT BÀN", ACCENT_GOLD, BG_DARK);
        JButton btnHuy = buildGhostButton("Hủy bỏ");

        btnDat.addActionListener(e -> onDatBan());
        btnHuy.addActionListener(e -> dispose());

        btnDat.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHuy.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnArea.add(btnDat);
        btnArea.add(Box.createVerticalStrut(10));
        btnArea.add(btnHuy);

        root.add(btnArea, BorderLayout.SOUTH);

        // Allow dragging
        makeDraggable(root);
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private JTextField buildTextField() {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_GOLD);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(BORDER_SUBTLE, 1, 10),
            new EmptyBorder(10, 14, 10, 14)
        ));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(ACCENT_GOLD_DIM, 1, 10),
                    new EmptyBorder(10, 14, 10, 14)));
                tf.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(BORDER_SUBTLE, 1, 10),
                    new EmptyBorder(10, 14, 10, 14)));
                tf.repaint();
            }
        });
        return tf;
    }

    private JSpinner buildStyledSpinner(SpinnerModel model) {
        JSpinner sp = new JSpinner(model);
        sp.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sp.setBackground(BG_INPUT);
        sp.setForeground(TEXT_PRIMARY);
        sp.setBorder(new RoundedLineBorder(BORDER_SUBTLE, 1, 10));
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(ACCENT_GOLD);
            tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
            tf.setBorder(new EmptyBorder(8, 12, 8, 4));
            tf.setHorizontalAlignment(JTextField.CENTER);
        }
        // Style arrow buttons
        for (Component c : sp.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component btn : p.getComponents()) {
                    if (btn instanceof JButton) {
                        JButton b = (JButton) btn;
                        b.setBackground(BG_INPUT);
                        b.setForeground(ACCENT_GOLD);
                        b.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                        b.setFocusPainted(false);
                        b.setOpaque(true);
                    }
                }
            }
        }
        return sp;
    }

    private JPanel buildField(String label, JComponent input) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 2, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(input);
        return panel;
    }

    private JButton buildActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker().darker()
                        : getModel().isRollover() ? bg.brighter() : bg;
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setPreferredSize(new Dimension(440, 50));
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
                    g2.setColor(new Color(255,255,255,10));
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
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setPreferredSize(new Dimension(440, 36));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Font loadFont(String fallback, int style, float size) {
        return new Font(fallback, style, (int) size);
    }

    private void makeDraggable(JComponent comp) {
        final int[] origin = new int[2];
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                origin[0] = e.getX();
                origin[1] = e.getY();
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

    // ── LOGIC ────────────────────────────────────────────────────────

    private void onDatBan() {
        String hoTen = txtHoTenKhach.getText().trim();
        String sdt   = txtSDT.getText().trim();
        int soLuong  = (Integer) spinnerSoLuongKhach.getValue();

        if (hoTen.isEmpty() || sdt.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ họ tên và số điện thoại.", false);
            return;
        }

        java.util.Date ngayUtil = (java.util.Date) spinnerNgayDat.getValue();
        java.util.Date gioUtil  = (java.util.Date) spinnerGioDat.getValue();

        Date ngayDat = new Date(ngayUtil.getTime());
        Time gioDat  = new Time(gioUtil.getTime());

        DatBanTruoc datBan = new DatBanTruoc(hoTen, sdt, soLuong, ngayDat, gioDat, maBan);
        datBan.setTrangThai("Đã đặt"); // FIX: constructor không tự set TrangThai → DB lưu NULL → query filter 'Đã đặt' không tìm thấy

        if (BanDAO.themDatBan(datBan)) {
            BanDAO.updateTrangThaiBan(maBan, "Đã đặt");
            showToast("Đặt bàn thành công!", true);
            Timer t = new Timer(1400, e -> dispose());
            t.setRepeats(false);
            t.start();
        } else {
            showToast("Lỗi khi lưu dữ liệu. Vui lòng thử lại.", false);
        }
    }

    /** Inline toast-style feedback inside the dialog */
    private void showToast(String message, boolean success) {
        Color bg = success ? new Color(30, 70, 40) : new Color(70, 25, 20);
        Color fg = success ? SUCCESS_COLOR : DANGER_COLOR;
        JOptionPane.showMessageDialog(this, message,
            success ? "Thành công" : "Thông báo",
            success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Inner helpers
    // ══════════════════════════════════════════════════════════════════

    /** Panel with rounded rectangle background */
    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
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

    /** Rounded border used for inputs */
    static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;
        RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color; this.thickness = thickness; this.radius = radius;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
    }
}