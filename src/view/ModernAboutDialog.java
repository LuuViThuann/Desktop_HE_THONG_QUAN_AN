package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.net.URISyntaxException;


public class ModernAboutDialog extends JDialog {

    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK   = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT  = new Color(70, 130, 180);
    private static final Color ACCENT_ORANGE  = new Color(230, 126, 34);
    private static final Color BG_MAIN        = new Color(241, 244, 247);
    private static final Color BG_SECONDARY   = new Color(255, 255, 255);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color BORDER_COLOR   = new Color(189, 195, 199);

    public ModernAboutDialog(Frame parent) {
        super(parent, "Về Ứng Dụng - Quán lẩu TiTi", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();

        setSize(620, 720);
        setLocationRelativeTo(parent);
        setResizable(false);

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // Header với gradient
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Nội dung chính (scrollable)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_MAIN);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        mainContent.add(createInfoCard());
        mainContent.add(Box.createVerticalStrut(30));

        mainContent.add(createFeaturesCard());
        mainContent.add(Box.createVerticalStrut(30));

        mainContent.add(createContactCard());

        add(new JScrollPane(mainContent), BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footer.setBackground(BG_MAIN);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton closeBtn = createModernButton("Đóng", ACCENT_ORANGE);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }

    // ================= HEADER =================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, 0, getHeight(), PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 160));
        header.setLayout(new BorderLayout());

        // Tiêu đề chính
        JLabel title = new JLabel("HỆ THỐNG QUÁN LẨU - TiTi", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        // Phiên bản
        JLabel version = new JLabel("Phiên bản 1.0.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        version.setForeground(new Color(255, 255, 255, 200));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(Box.createVerticalStrut(30));
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(version);

        header.add(textPanel, BorderLayout.CENTER);

        // Nút đóng góc phải
        JLabel closeBtn = new JLabel("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 36));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(0, 0, 0, 20));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
            @Override public void mouseEntered(MouseEvent e) { closeBtn.setForeground(new Color(255, 100, 100)); }
            @Override public void mouseExited(MouseEvent e)  { closeBtn.setForeground(Color.WHITE); }
        });

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setOpaque(false);
        closePanel.add(closeBtn);
        header.add(closePanel, BorderLayout.EAST);

        return header;
    }

    // ================= INFO CARD =================
    private JPanel createInfoCard() {
        JPanel card = createCard();
        card.setLayout(new GridLayout(0, 2, 16, 16));

        card.add(createInfoLabel("Ngày phát hành:"));
        card.add(createValueLabel("31/01/2026"));

        card.add(createInfoLabel("Nền tảng:"));
        card.add(createValueLabel("Java Swing"));

        card.add(createInfoLabel("Cơ sở dữ liệu:"));
        card.add(createValueLabel("MySQL 8.0"));

        card.add(createInfoLabel("Phát triển bởi:"));
        card.add(createValueLabel("Nhóm TiTi"));

        return card;
    }

    // ================= FEATURES CARD =================
    private JPanel createFeaturesCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TÍNH NĂNG NỔI BẬT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        String[] features = {
            "Quản lý bàn ăn thời gian thực",
            "Đặt bàn trước online",
            "Quản lý menu món lẩu & nước uống",
            "Quản lý nhân viên và ca làm",
            "Thống kê doanh thu chi tiết",
            "Báo cáo & phân tích hiệu quả",
            "Giao diện hiện đại, dễ sử dụng"
        };

        for (String f : features) {
            card.add(createFeatureItem(f));
            card.add(Box.createVerticalStrut(10));
        }

        return card;
    }

    // ================= CONTACT CARD =================
    private JPanel createContactCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("LIÊN HỆ & HỖ TRỢ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        card.add(createContactItem("Email:", "support@titi.vn", "mailto:support@titi.vn"));
        card.add(Box.createVerticalStrut(12));

        card.add(createContactItem("Website:", "www.titi.vn", "https://www.titi.vn"));
        card.add(Box.createVerticalStrut(12));

        card.add(createContactItem("Hotline:", "1900-xxxx", null));
        card.add(Box.createVerticalStrut(20));

        JLabel copyright = new JLabel("© 2026 Hệ thống quán lẩu TiTi. All rights reserved.");
        copyright.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        copyright.setForeground(TEXT_LIGHT);
        copyright.setAlignmentX(LEFT_ALIGNMENT);
        card.add(copyright);

        return card;
    }

    // ================= HELPER METHODS =================

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215), 1),
                new EmptyBorder(24, 28, 24, 28)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JLabel createInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private JLabel createValueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_LIGHT);
        return lbl;
    }

    private JPanel createFeatureItem(String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        item.setOpaque(false);
        item.setAlignmentX(LEFT_ALIGNMENT);

        JLabel bullet = new JLabel("•");
        bullet.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bullet.setForeground(PRIMARY_LIGHT);
        bullet.setBorder(new EmptyBorder(0, 0, 0, 12));

        JLabel txt = new JLabel(text);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(TEXT_DARK);

        item.add(bullet);
        item.add(txt);
        return item;
    }

    private JPanel createContactItem(String labelText, String valueText, String link) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        item.setOpaque(false);
        item.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        label.setBorder(new EmptyBorder(0, 0, 0, 12));

        if (link != null) {
            JLabel value = new JLabel("<html><u>" + valueText + "</u></html>");
            value.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            value.setForeground(PRIMARY_LIGHT);
            value.setCursor(new Cursor(Cursor.HAND_CURSOR));

            value.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(link));
                    } catch (URISyntaxException | java.io.IOException ex) {
                        JOptionPane.showMessageDialog(ModernAboutDialog.this,
                                "Không thể mở liên kết: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    value.setForeground(ACCENT_ORANGE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    value.setForeground(PRIMARY_LIGHT);
                }
            });

            item.add(label);
            item.add(value);
        } else {
            JLabel value = new JLabel(valueText);
            value.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            value.setForeground(TEXT_LIGHT);
            item.add(label);
            item.add(value);
        }

        return item;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color fill = getModel().isPressed() ? bgColor.darker() :
                             getModel().isRollover() ? bgColor.brighter() : bgColor;

                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(140, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // Test main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            new ModernAboutDialog(frame).setVisible(true);
        });
    }
}