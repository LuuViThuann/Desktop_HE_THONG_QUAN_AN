package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;


public class ModernHelpDialog extends JDialog {

    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK   = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT  = new Color(70, 130, 180);
    private static final Color ACCENT_ORANGE  = new Color(230, 126, 34);
    private static final Color BG_MAIN        = new Color(241, 244, 247);
    private static final Color BG_SECONDARY   = new Color(255, 255, 255);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color BORDER_COLOR   = new Color(189, 195, 199);

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public ModernHelpDialog(Frame parent) {
        super(parent, "Hướng Dẫn Sử Dụng - Quán lẩu TiTi", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();

        setSize(940, 680);
        setLocationRelativeTo(parent);
        setResizable(false);

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main area: Sidebar + Content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_MAIN);
        mainPanel.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_MAIN);
        
        // Padding content: top 28, left 40, bottom 28, right 52 → tăng khoảng cách bên phải
        contentPanel.setBorder(new EmptyBorder(28, 40, 28, 52));

        contentPanel.add(createOverviewPanel(),      "overview");
        contentPanel.add(createTableManagementPanel(),"tables");
        contentPanel.add(createPaymentPanel(),       "payment");
        contentPanel.add(createStatisticsPanel(),    "statistics");
        contentPanel.add(createShortcutsPanel(),     "shortcuts");
        contentPanel.add(createSupportPanel(),       "support");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ================= HEADER =================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 90));
        header.setLayout(new BorderLayout());

        JLabel title = new JLabel("HƯỚNG DẪN SỬ DỤNG", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Hệ thống quản lý quán lẩu - TiTi", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(255, 255, 255, 220));

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.add(Box.createVerticalStrut(18));
        textBox.add(title);
        textBox.add(Box.createVerticalStrut(8));
        textBox.add(subtitle);

        header.add(textBox, BorderLayout.CENTER);

        // Nút đóng (top-right)
        JLabel closeBtn = new JLabel("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 36));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(0, 0, 0, 24));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(new Color(255, 80, 80)); }
            public void mouseExited(MouseEvent e)  { closeBtn.setForeground(Color.WHITE); }
        });

        JPanel closeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeBox.setOpaque(false);
        closeBox.add(closeBtn);
        header.add(closeBox, BorderLayout.EAST);

        return header;
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SECONDARY);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        sidebar.add(Box.createVerticalStrut(32));

        sidebar.add(createSidebarButton("Tổng quan",      "overview",  true));
        sidebar.add(createSidebarButton("Quản lý bàn",    "tables",    false));
        sidebar.add(createSidebarButton("Thanh toán",     "payment",   false));
        sidebar.add(createSidebarButton("Thống kê",       "statistics",false));
        sidebar.add(createSidebarButton("Phím tắt",       "shortcuts", false));
        sidebar.add(createSidebarButton("Hỗ trợ",         "support",   false));

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createSidebarButton(String text, String cardName, boolean selected) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected() || getModel().isRollover()) {
                    g2.setColor(new Color(70, 130, 180, 50));
                    g2.fillRoundRect(12, 6, getWidth() - 24, getHeight() - 12, 12, 12);
                }
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(selected ? PRIMARY_LIGHT : TEXT_DARK);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
      
        btn.setBorder(new EmptyBorder(16, 28, 16, 40));

        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setSelected(selected);

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            updateSidebarSelection(btn);
        });

        return btn;
    }

    private void updateSidebarSelection(JButton selected) {
        Container parent = selected.getParent();
        for (Component c : parent.getComponents()) {
            if (c instanceof JButton b) {
                boolean sel = (b == selected);
                b.setSelected(sel);
                b.setForeground(sel ? PRIMARY_LIGHT : TEXT_DARK);
                b.repaint();
            }
        }
    }

    // ================= FOOTER =================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 18));
        footer.setBackground(BG_MAIN);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton closeBtn = createModernButton("Đóng", ACCENT_ORANGE);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        return footer;
    }

    // ================= CONTENT PANELS =================

    private JScrollPane createOverviewPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MAIN);

        p.add(createSectionTitle("TỔNG QUAN HỆ THỐNG"));
        p.add(createContentCard(
            null,
            "Hệ thống quán lẩu - TiTi là giải pháp quản lý toàn diện dành cho quán lẩu, hỗ trợ:\n\n" +
            "• Quản lý bàn ăn theo thời gian thực\n" +
            "• Đặt bàn trước qua ứng dụng hoặc website\n" +
            "• Quản lý menu món lẩu và nước uống\n" +
            "• Quản lý nhân viên & phân công ca làm\n" +
            "• Theo dõi doanh thu và thống kê chi tiết\n" +
            "• Giao diện thân thiện, dễ sử dụng trên máy tính"
        ));

        p.add(Box.createVerticalStrut(40));

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JScrollPane createTableManagementPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MAIN);

        p.add(createSectionTitle("QUẢN LÝ BÀN ĂN"));
        p.add(createContentCard(
            null,
            "Hướng dẫn sử dụng:\n\n" +
            "1. Xem danh sách bàn từ màn hình chính\n" +
            "2. Bàn màu xanh: đang trống\n" +
            "3. Bàn màu đỏ: đang có khách\n" +
            "4. Bàn màu vàng: đã đặt trước\n\n" +
            "Nhấn vào bàn để xem thông tin chi tiết, gọi món, thanh toán hoặc thay đổi trạng thái."
        ));

        p.add(Box.createVerticalStrut(24));
        p.add(createSectionTitle("GỌI MÓN"));
        p.add(createStepByStepCard(new String[]{
            "Chọn bàn đang phục vụ",
            "Nhấn nút 'GỌI MÓN'",
            "Chọn món lẩu / nước uống từ menu",
            "Nhập số lượng",
            "Nhấn 'THÊM VÀO ĐƠN'",
            "Xác nhận và in phiếu bếp (nếu cần)"
        }));

        p.add(Box.createVerticalStrut(40));

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JScrollPane createPaymentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MAIN);

        p.add(createSectionTitle("THANH TOÁN"));
        p.add(createStepByStepCard(new String[]{
            "Chọn bàn cần thanh toán",
            "Kiểm tra chi tiết đơn hàng (món lẩu, nước, topping…)",
            "Áp dụng giảm giá / khuyến mãi (nếu có)",
            "Chọn hình thức thanh toán",
            "Nhập số tiền khách đưa",
            "Nhấn 'THANH TOÁN'",
            "In hóa đơn và chào khách"
        }));

        p.add(Box.createVerticalStrut(24));
        p.add(createWarningCard(
            "LƯU Ý QUAN TRỌNG",
            "• Kiểm tra kỹ số lượng món và giá trước khi thanh toán\n" +
            "• Xác nhận khuyến mãi đúng chương trình\n" +
            "• In hóa đơn cho khách\n" +
            "• Kiểm tra tiền thừa trả lại chính xác"
        ));

        p.add(Box.createVerticalStrut(40));

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JScrollPane createStatisticsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MAIN);

        JLabel mainTitle = new JLabel("THỐNG KÊ & BÁO CÁO");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainTitle.setForeground(PRIMARY_DARK);
        mainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainTitle.setBorder(new EmptyBorder(0, 0, 24, 0));
        p.add(mainTitle);

        JLabel statsTitle = new JLabel("Thống kê tổng quan");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        statsTitle.setForeground(PRIMARY_DARK);
        statsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        p.add(statsTitle);

        p.add(createContentCard(
            null,
            "Hệ thống quán lẩu - TiTi cung cấp các báo cáo chi tiết:\n\n" +
            "• Doanh thu theo ngày / tháng / năm\n" +
            "• Số lượng đơn hàng\n" +
            "• Món ăn bán chạy\n" +
            "• Hiệu suất nhân viên\n" +
            "• Tỷ lệ lấp đầy bàn"
        ));

        p.add(Box.createVerticalStrut(40));

        JLabel historyTitle = new JLabel("Lịch sử đặt bàn");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        historyTitle.setForeground(PRIMARY_DARK);
        historyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        p.add(historyTitle);

        p.add(createContentCard(
            null,
            "Chức năng xem và quản lý lịch sử đặt bàn:\n\n" +
            "• Xem danh sách đặt bàn\n" +
            "• Lọc theo ngày / trạng thái\n" +
            "• Xem chi tiết thông tin khách hàng\n" +
            "• Xuất báo cáo Excel / PDF"
        ));

        p.add(Box.createVerticalStrut(40));

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JScrollPane createShortcutsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MAIN);

        p.add(createSectionTitle("PHÍM TẮT"));
        p.add(createShortcutsCard());

        p.add(Box.createVerticalStrut(40));

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JScrollPane createSupportPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MAIN);

        p.add(createSectionTitle("HỖ TRỢ & LIÊN HỆ"));
        p.add(createContentCard(
            null,
            "Thông tin liên hệ:\n\n" +
            "Email hỗ trợ: support@titi.vn\n" +
            "Hotline: 1900-xxxx\n" +
            "Website: www.titi.vn\n\n" +
            "Thời gian hỗ trợ:\n" +
            "• Thứ 2 - Thứ 6: 8:00 - 17:30\n" +
            "• Thứ 7: 8:00 - 12:00\n" +
            "• Chủ nhật: Nghỉ"
        ));

        p.add(Box.createVerticalStrut(24));
        p.add(createInfoCard(
            "MẸO SỬ DỤNG HIỆU QUẢ",
            "• Sao lưu dữ liệu định kỳ\n" +
            "• Cập nhật phần mềm thường xuyên\n" +
            "• Đào tạo nhân viên sử dụng hệ thống\n" +
            "• Đảm bảo kết nối mạng ổn định"
        ));

        p.add(Box.createVerticalStrut(40));

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ================= HELPER METHODS =================

    private JLabel createSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(PRIMARY_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        return lbl;
    }

    private JPanel createContentCard(String title, String content) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215), 1),
                new EmptyBorder(20, 24, 20, 24)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (title != null && !title.isEmpty()) {
            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLbl.setForeground(PRIMARY_DARK);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(titleLbl);
            card.add(Box.createVerticalStrut(8));
        }

        JTextArea text = new JTextArea(content);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        text.setForeground(TEXT_DARK);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setBorder(new EmptyBorder(4, 0, 0, 0));

        card.add(text);
        return card;
    }

    private JPanel createStepByStepCard(String[] steps) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215), 1),
                new EmptyBorder(18, 22, 18, 22)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < steps.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel num = new JLabel(String.valueOf(i + 1));
            num.setFont(new Font("Segoe UI", Font.BOLD, 14));
            num.setForeground(Color.WHITE);
            num.setOpaque(true);
            num.setBackground(PRIMARY_LIGHT);
            num.setPreferredSize(new Dimension(32, 32));
            num.setHorizontalAlignment(SwingConstants.CENTER);
            num.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

            JLabel txt = new JLabel(steps[i]);
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txt.setForeground(TEXT_DARK);
            txt.setBorder(new EmptyBorder(0, 14, 0, 0));

            row.add(num);
            row.add(txt);
            card.add(row);
        }
        return card;
    }

    private JPanel createShortcutsCard() {
        JPanel card = new JPanel(new GridLayout(0, 2, 28, 14));
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 215), 1),
                new EmptyBorder(20, 24, 20, 24)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] shortcuts = {
                {"Ctrl + H",   "Quay về màn hình chính"},
                {"Ctrl + T",   "Mở thống kê tổng quan"},
                {"Ctrl + L",   "Xem lịch sử đặt bàn"},
                {"Ctrl + P",   "Đổi mật khẩu"},
                {"F1",         "Mở hướng dẫn sử dụng"},
                {"Alt + F4",   "Thoát ứng dụng"}
        };

        for (String[] sc : shortcuts) {
            JLabel key = new JLabel(sc[0]);
            key.setFont(new Font("Consolas", Font.BOLD, 14));
            key.setForeground(Color.WHITE);
            key.setOpaque(true);
            key.setBackground(PRIMARY_DARK);
            key.setHorizontalAlignment(SwingConstants.CENTER);
            key.setBorder(new EmptyBorder(8, 14, 8, 14));

            JLabel desc = new JLabel(sc[1]);
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            desc.setForeground(TEXT_DARK);

            card.add(key);
            card.add(desc);
        }
        return card;
    }

    private JPanel createWarningCard(String title, String content) {
        JPanel card = createContentCard(title, content);
        card.setBackground(new Color(255, 245, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
                new EmptyBorder(20, 24, 20, 24)
        ));
        return card;
    }

    private JPanel createInfoCard(String title, String content) {
        JPanel card = createContentCard(title, content);
        card.setBackground(new Color(240, 248, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_LIGHT, 1),
                new EmptyBorder(20, 24, 20, 24)
        ));
        return card;
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
        btn.setPreferredSize(new Dimension(160, 48));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // Test main (để debug nhanh)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(500, 400);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            new ModernHelpDialog(f).setVisible(true);
        });
    }
}