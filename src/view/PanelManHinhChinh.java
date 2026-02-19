package view;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import config.DatabaseConfig;
import dao.BanDAO;
import dao.MonAnDAO;
import model.Ban;
import model.MonAn;
import model.NhanVien;
import model.KhuVucQuan;
import config.UserSession;


public class PanelManHinhChinh extends JPanel {

    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);
    private static final Color DANGER_RED = new Color(192, 57, 43);
    private static final Color WARNING_AMBER = new Color(241, 196, 15);

    private static final Color BG_MAIN = new Color(241, 244, 247);
    private static final Color BG_SECONDARY = new Color(255, 255, 255);
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);

    private static final String IMAGE_FOLDER = "src/Assets/images/";

    // ============ UI Components ============
    private JList<KhuVucQuan> listKhuVuc;
    private DefaultListModel<KhuVucQuan> modelKhuVuc;
    private JTabbedPane tabbedPaneBan;
    private Map<Integer, JPanel> mapPanelBanByKhuVuc;

    private JTable tableThongTinBan;
    private DefaultTableModel modelThongTinBan;

    // THÔNG TIN BÀN
    private JTextField txtTenBan;
    private JTextField txtTongTien;
    private JTextField txtGiamGia;
    private JTextField txtTenNVTinhTien;
    private JTextField txtMaNVTinhTien;
    private JTextField txtMaNVMoBan;
    private JTextField txtNgayThang;
    private JTextField txtTongThu;

    private JPanel infoPanelRight;
    private JLabel lblUserInfo;
    private JLabel lblDateTime;
    private JPanel goiMonMiniInfoPanel;

    // ============ RIGHT TABBED PANE ============
    private JTabbedPane rightTabbedPane;

    // ============ GOI MON INLINE ============
    private JTabbedPane tabbedPaneNhomInline;
    private JPanel goiMonInlinePanel;
    private JLabel lblGoiMonBanInfo;
    private JLabel lblGoiMonHint;

    // Map to track stock labels per MaMon for in-place update (no full reload)
    private final Map<Integer, JLabel> inlineStockLabels = new HashMap<>();
    // Map to track add buttons per MaMon for in-place disable
    private final Map<Integer, JButton> inlineAddButtons = new HashMap<>();
    // Map to track current stock per MaMon (local cache)
    private final Map<Integer, Integer> inlineStockCache = new HashMap<>();
    // Map to track "Đã +N tên món" labels per MaMon
    private final Map<Integer, JLabel> inlineAddedCountLabels = new HashMap<>();
    // Map to track how many times each mon has been added in this session
    private final Map<Integer, Integer> inlineAddedCounts = new HashMap<>();

    // ============ Timer & Variables ============
    private javax.swing.Timer dateTimeTimer;

    private int currentBanId = -1;
    private String currentTenBan = "";
    private boolean isDiscountApplied = false;
    private JButton selectedBanButton = null;
    private Color selectedBanOriginalColor;
    private int currentKhuVucId = -1;

    private boolean isMangVeMode = false;
    private final List<MangVeItem> mangVeCart = new ArrayList<>();
    private DefaultTableModel mangVeTableModel;
    private JLabel lblMangVeTongTien;
    private JToggleButton btnToggleMangVe;
    private JPanel mangVeCartPanel;
    private JPanel miniInfoHeaderPanel;

    public PanelManHinhChinh() {
        mapPanelBanByKhuVuc = new HashMap<>();
        initComponents();
        loadKhuVuc();
        loadBan();
        updateDateTime();

        SwingUtilities.invokeLater(() -> {
            updateEmployeeInfoFields();
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_MAIN);

        JPanel headerPanel = createHeaderPanel();
        JPanel bodyPanel = createBodyPanel();
        JPanel footerPanel = createFooterPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // ========== HEADER PANEL ==========
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(PRIMARY_DARK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(0, 70));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(loadIcon("src/Assets/images/logoin.jpg", 50, 50));

        if (logoLabel.getIcon() == null) {
            logoLabel.setText("🍽️");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 32));
            logoLabel.setForeground(ACCENT_ORANGE);
        } else {
            logoLabel.setIcon(new ImageIcon(makeRoundedImage(
                ((ImageIcon) logoLabel.getIcon()).getImage(),
                40, 40, 20
            )));
        }

        JLabel titleLabel = new JLabel("VUA LẨU TI-TI / CHI NHÁNH CẦN THƠ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        logoPanel.add(logoLabel);
        logoPanel.add(titleLabel);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.setOpaque(false);

        lblDateTime = new JLabel();
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDateTime.setForeground(new Color(200, 200, 200));
        centerPanel.add(lblDateTime);

        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeaderPanel.setOpaque(false);

        JButton btnLogout = createHeaderButton("src/Assets/images/exit.png", "Đăng xuất");
        btnLogout.addActionListener(e -> confirmAndLogout());

        lblUserInfo = new JLabel();
        lblUserInfo.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        lblUserInfo.setForeground(Color.WHITE);
        updateUserInfo();

        rightHeaderPanel.add(lblUserInfo);
        rightHeaderPanel.add(createVerticalSeparator());
        rightHeaderPanel.add(btnLogout);

        panel.add(logoPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightHeaderPanel, BorderLayout.EAST);

        return panel;
    }

    private void confirmAndLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?\nHệ thống sẽ quay về màn hình đăng nhập.",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            showLogoutAnimation();
            cleanupBeforeLogout();

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                ((JFrame) window).dispose();
            }

            SwingUtilities.invokeLater(() -> {
                new FormDangNhap().setVisible(true);
            });
        }
    }

    private void showLogoutAnimation() {
        JDialog loadingDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setSize(300, 150);
        loadingDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), getHeight(), PRIMARY_LIGHT);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel lblIcon = new JLabel("👋");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIcon.setForeground(Color.WHITE);

        JLabel lblMessage = new JLabel("Đang đăng xuất...");
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMessage.setForeground(Color.WHITE);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(200, 10));
        progressBar.setBackground(new Color(255, 255, 255, 50));
        progressBar.setForeground(ACCENT_ORANGE);

        contentPanel.add(lblIcon);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(lblMessage);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(progressBar);

        loadingDialog.setContentPane(contentPanel);

        Timer timer = new Timer(1000, e -> loadingDialog.dispose());
        timer.setRepeats(false);
        timer.start();

        loadingDialog.setVisible(true);
    }

    private void cleanupBeforeLogout() {
        if (dateTimeTimer != null && dateTimeTimer.isRunning()) {
            dateTimeTimer.stop();
        }
        currentBanId = -1;
        currentTenBan = "";
        if (selectedBanButton != null) {
            selectedBanButton = null;
        }
        UserSession.getInstance().logout();
        System.out.println("ĐĂNG XUẤT THÀNH CÔNG: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    private Image makeRoundedImage(Image img, int width, int height, int cornerRadius) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, width, height, cornerRadius * 2, cornerRadius * 2);
        g2.clip(roundedRect);
        g2.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g2.dispose();
        return output;
    }

    private JButton createHeaderButton(String iconPath, String tooltip) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2d);
            }
        };

        ImageIcon icon = loadIcon(iconPath, 45, 45);
        if (icon != null) {
            btn.setIcon(icon);
        } else {
            btn.setText("?");
            btn.setFont(new Font("Arial", Font.BOLD, 12));
        }

        btn.setPreferredSize(new Dimension(40, 40));
        btn.setBackground(new Color(60, 80, 120));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_LIGHT);
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(60, 80, 120));
                btn.repaint();
            }
        });

        return btn;
    }

    private JComponent createVerticalSeparator() {
        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(100, 100, 100));
                g.drawLine(0, 5, 0, getHeight() - 5);
            }
        };
        separator.setOpaque(false);
        separator.setPreferredSize(new Dimension(1, 30));
        return separator;
    }

    // ========== BODY PANEL ==========
    private JPanel createBodyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel sidebarPanel = createSidebarPanel();

        // RIGHT: two tabs — "Thông tin bàn" and "Gọi Món"
        rightTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        rightTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightTabbedPane.setBackground(BG_SECONDARY);
        rightTabbedPane.setForeground(TEXT_DARK);

        // Tab 1: Thông tin bàn (detail + ban grid)
        JPanel banInfoTab = createBanInfoTab();

        // Tab 2: Gọi Món inline
        goiMonInlinePanel = createGoiMonInlinePanel();

        rightTabbedPane.addTab("Thông Tin Bàn", banInfoTab);
        rightTabbedPane.addTab("Gọi Món", goiMonInlinePanel);

        // Switch to Gọi Món tab -> ensure a ban is selected
        rightTabbedPane.addChangeListener(e -> {
            if (rightTabbedPane.getSelectedIndex() == 1) {
                if (currentBanId == -1) {
                    JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn bàn trước khi gọi món!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    rightTabbedPane.setSelectedIndex(0);
                } else {
                    refreshGoiMonInlinePanel();
                }
            }
        });

        JSplitPane outerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        outerSplit.setLeftComponent(sidebarPanel);
        outerSplit.setRightComponent(rightTabbedPane);
        outerSplit.setDividerLocation(220);
        outerSplit.setDividerSize(8);
        outerSplit.setResizeWeight(0.0);
        outerSplit.setContinuousLayout(true);
        outerSplit.setBorder(null);

        panel.add(outerSplit, BorderLayout.CENTER);
        return panel;
    }

    // ========== BAN INFO TAB (original layout) ==========
    private JPanel createBanInfoTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel detailPanel = createRightPanel();
        JPanel mainPanel = createMainPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setLeftComponent(detailPanel);
        rightSplit.setRightComponent(mainPanel);
        rightSplit.setDividerLocation(450);
        rightSplit.setDividerSize(8);
        rightSplit.setResizeWeight(0.4);
        rightSplit.setContinuousLayout(true);
        rightSplit.setBorder(null);

        panel.add(rightSplit, BorderLayout.CENTER);
        return panel;
    }

    // ========== SIDEBAR - KHU VỰC ==========
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel headerSidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), getHeight(), PRIMARY_LIGHT);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };

        headerSidebar.setOpaque(false);
        headerSidebar.setLayout(new BorderLayout(10, 0));
        headerSidebar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerSidebar.setPreferredSize(new Dimension(0, 75));

        JLabel iconKV = new JLabel("📍");
        iconKV.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconKV.setVerticalAlignment(JLabel.TOP);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleKV = new JLabel("KHU VỰC QUÁN");
        titleKV.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleKV.setForeground(Color.WHITE);
        titleKV.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleKV = new JLabel("Chọn khu vực phục vụ");
        subtitleKV.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleKV.setForeground(new Color(220, 230, 240));
        subtitleKV.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleKV.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        titlePanel.add(titleKV);
        titlePanel.add(subtitleKV);

        headerSidebar.add(iconKV, BorderLayout.WEST);
        headerSidebar.add(titlePanel, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(new Color(248, 250, 252));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchPanel.setPreferredSize(new Dimension(0, 40));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JTextField txtSearch = new JTextField("Tìm khu vực...");
        txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        txtSearch.setForeground(TEXT_LIGHT);
        txtSearch.setBorder(null);
        txtSearch.setBackground(new Color(248, 250, 252));

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm khu vực...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_DARK);
                    txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm khu vực...");
                    txtSearch.setForeground(TEXT_LIGHT);
                    txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterKhuVuc(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterKhuVuc(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterKhuVuc(); }

            private void filterKhuVuc() {
                String searchText = txtSearch.getText().toLowerCase();
                if (searchText.equals("tìm khu vực...")) return;
                List<KhuVucQuan> allKhuVuc = getKhuVucList();
                modelKhuVuc.clear();
                for (KhuVucQuan kv : allKhuVuc) {
                    if (searchText.isEmpty() || kv.getTenKV().toLowerCase().contains(searchText)) {
                        modelKhuVuc.addElement(kv);
                    }
                }
            }
        });

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        modelKhuVuc = new DefaultListModel<>();
        listKhuVuc = new JList<>(modelKhuVuc);
        listKhuVuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listKhuVuc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listKhuVuc.setBackground(BG_SECONDARY);
        listKhuVuc.setForeground(TEXT_DARK);
        listKhuVuc.setCellRenderer(new ModernKhuVucListCellRenderer());
        listKhuVuc.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        listKhuVuc.setFixedCellHeight(52);

        listKhuVuc.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = listKhuVuc.getSelectedIndex();
                if (selectedIndex >= 0) {
                    KhuVucQuan khuVuc = modelKhuVuc.get(selectedIndex);
                    currentKhuVucId = khuVuc.getMaKV();
                    selectTabByKhuVuc(khuVuc.getMaKV());
                    // Switch to ban info tab when area is selected
                    if (rightTabbedPane != null) rightTabbedPane.setSelectedIndex(0);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(listKhuVuc);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_SECONDARY);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setBackground(BG_SECONDARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel footerSidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        footerSidebar.setBackground(new Color(248, 250, 252));
        footerSidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        footerSidebar.setPreferredSize(new Dimension(0, 40));

        JLabel lblTotalKV = new JLabel("Tổng: 0 khu vực");
        lblTotalKV.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTotalKV.setForeground(TEXT_LIGHT);
        footerSidebar.add(lblTotalKV);

        modelKhuVuc.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) { updateCount(); }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { updateCount(); }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { updateCount(); }
            private void updateCount() {
                lblTotalKV.setText("Tổng: " + modelKhuVuc.getSize() + " khu vực");
            }
        });

        panel.add(headerSidebar, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(footerSidebar, BorderLayout.SOUTH);

        return panel;
    }

    class ModernKhuVucListCellRenderer extends JPanel implements ListCellRenderer<KhuVucQuan> {
        private JLabel lblIcon;
        private JLabel lblTenKV;
        private JLabel lblSoBan;
        private JLabel lblIndicator;

        public ModernKhuVucListCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setOpaque(true);

            lblIcon = new JLabel("📍");
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            lblIcon.setPreferredSize(new Dimension(30, 30));
            lblIcon.setHorizontalAlignment(JLabel.CENTER);
            lblIcon.setVerticalAlignment(JLabel.CENTER);

            JPanel infoPanel = new JPanel(new BorderLayout(0, 2));
            infoPanel.setOpaque(false);

            lblTenKV = new JLabel();
            lblTenKV.setFont(new Font("Segoe UI", Font.BOLD, 13));

            lblSoBan = new JLabel();
            lblSoBan.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            infoPanel.add(lblTenKV, BorderLayout.NORTH);
            infoPanel.add(lblSoBan, BorderLayout.CENTER);

            lblIndicator = new JLabel("→");
            lblIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblIndicator.setPreferredSize(new Dimension(20, 20));
            lblIndicator.setHorizontalAlignment(JLabel.CENTER);
            lblIndicator.setVisible(false);

            add(lblIcon, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
            add(lblIndicator, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends KhuVucQuan> list,
                                                     KhuVucQuan value, int index,
                                                     boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                lblTenKV.setText(value.getTenKV());
                int soBan = countBanInKhuVuc(value.getMaKV());
                lblSoBan.setText(soBan + " bàn");

                if (isSelected) {
                    setBackground(new Color(70, 130, 180, 25));
                    lblTenKV.setForeground(PRIMARY_LIGHT);
                    lblSoBan.setForeground(PRIMARY_LIGHT);
                    lblIndicator.setVisible(true);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, PRIMARY_LIGHT),
                        BorderFactory.createEmptyBorder(8, 9, 8, 12)
                    ));
                } else {
                    setBackground(BG_SECONDARY);
                    lblTenKV.setForeground(TEXT_DARK);
                    lblSoBan.setForeground(TEXT_LIGHT);
                    lblIndicator.setVisible(false);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(0, 0, 0, 0)),
                        BorderFactory.createEmptyBorder(8, 9, 8, 12)
                    ));
                }
            }
            return this;
        }

        private int countBanInKhuVuc(int maKV) {
            int count = 0;
            List<Ban> danhSachBan = BanDAO.getAllBanWithKhuVuc();
            for (Ban ban : danhSachBan) {
                if (ban.getMaKV() == maKV && ban.getMaBan() != 0) count++;
            }
            return count;
        }
    }

    // ========== MAIN PANEL - TABBED PANE (ban grid) ==========
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel headerMain = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerMain.setBackground(BG_SECONDARY);

        JLabel lblCacBan = new JLabel("🪑");
        lblCacBan.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel titleMain = new JLabel("CÁC BÀN PHỤC VỤ");
        titleMain.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleMain.setForeground(TEXT_DARK);

        headerMain.add(lblCacBan);
        headerMain.add(titleMain);

        tabbedPaneBan = new JTabbedPane(JTabbedPane.TOP);
        tabbedPaneBan.setBackground(BG_SECONDARY);
        tabbedPaneBan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabbedPaneBan.setForeground(TEXT_DARK);
        tabbedPaneBan.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        tabbedPaneBan.addChangeListener(e -> {
            int selectedIndex = tabbedPaneBan.getSelectedIndex();
            if (selectedIndex >= 0) {
                String tabName = tabbedPaneBan.getTitleAt(selectedIndex);
                for (int j = 0; j < modelKhuVuc.getSize(); j++) {
                    KhuVucQuan kv = modelKhuVuc.get(j);
                    if (kv.getTenKV().equals(tabName)) {
                        currentKhuVucId = kv.getMaKV();
                        break;
                    }
                }
            }
        });

        panel.add(headerMain, BorderLayout.NORTH);
        panel.add(tabbedPaneBan, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPanelBanForKhuVuc(int maKV, String tenKV) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel containerBan = new JPanel(new GridLayout(0, 3, 20, 20));
        containerBan.setBackground(BG_SECONDARY);

        List<Ban> danhSachBan = BanDAO.getAllBanWithKhuVuc();

        for (Ban ban : danhSachBan) {
            if (ban.getMaKV() == maKV && ban.getMaBan() != 0) {
                JButton banButton = createModernCircularBanButton(ban);
                containerBan.add(banButton);
            }
        }

        JScrollPane scrollPane = new JScrollPane(containerBan);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_SECONDARY);

        panel.add(scrollPane, BorderLayout.CENTER);
        mapPanelBanByKhuVuc.put(maKV, panel);

        return panel;
    }

    private JButton createModernCircularBanButton(Ban ban) {
        Color btnColor;
        Color gradientStart;
        Color gradientEnd;
        Color shadowColor;

        if ("Đang sử dụng".equals(ban.getTrangThai())) {
            btnColor = SUCCESS_GREEN;
            gradientStart = new Color(52, 168, 83);
            gradientEnd = new Color(46, 152, 102);
            shadowColor = new Color(46, 152, 102, 60);
        } else if ("Đã đặt".equals(ban.getTrangThai())) {
            btnColor = WARNING_AMBER;
            gradientStart = new Color(255, 214, 10);
            gradientEnd = new Color(241, 196, 15);
            shadowColor = new Color(241, 196, 15, 60);
        } else {
            btnColor = new Color(236, 240, 244);
            gradientStart = new Color(248, 250, 252);
            gradientEnd = new Color(236, 240, 244);
            shadowColor = new Color(0, 0, 0, 15);
        }

        JButton banButton = new JButton() {
            private boolean isHovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int diameter = Math.min(getWidth(), getHeight()) - 8;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;

                if (!"Trống".equals(ban.getTrangThai())) {
                    g2d.setColor(shadowColor);
                    for (int i = 0; i < 6; i++) {
                        g2d.fillOval(x + i, y + i + 2, diameter, diameter);
                    }
                }

                GradientPaint gradient;
                if (this == selectedBanButton) {
                    gradient = new GradientPaint(x, y, new Color(243, 156, 18), x, y + diameter, ACCENT_ORANGE);
                } else if (isHovered) {
                    gradient = new GradientPaint(x, y, gradientStart.brighter(), x, y + diameter, gradientEnd.brighter());
                } else {
                    gradient = new GradientPaint(x, y, gradientStart, x, y + diameter, gradientEnd);
                }

                g2d.setPaint(gradient);
                g2d.fillOval(x, y, diameter, diameter);

                if ("Trống".equals(ban.getTrangThai())) {
                    g2d.setColor(new Color(189, 195, 199));
                    g2d.setStroke(new BasicStroke(2.5f));
                } else {
                    g2d.setColor(btnColor.darker());
                    g2d.setStroke(new BasicStroke(3f));
                }
                g2d.drawOval(x, y, diameter, diameter);

                if (!"Trống".equals(ban.getTrangThai())) {
                    g2d.setColor(new Color(255, 255, 255, 40));
                    g2d.fillOval(x + diameter / 4, y + diameter / 8, diameter / 3, diameter / 4);
                }

                g2d.dispose();
            }

            @Override
            public boolean contains(int x, int y) {
                int diameter = Math.min(getWidth(), getHeight()) - 8;
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = diameter / 2;
                int dx = x - centerX;
                int dy = y - centerY;
                return (dx * dx + dy * dy) <= (radius * radius);
            }
        };

        banButton.setLayout(new BorderLayout(0, 0));
        banButton.setPreferredSize(new Dimension(140, 140));
        banButton.setOpaque(false);
        banButton.setContentAreaFilled(false);
        banButton.setFocusPainted(false);
        banButton.setBorderPainted(false);
        banButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel contentPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {}
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));

        JLabel lblIcon = new JLabel("🪑");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIcon.setHorizontalAlignment(JLabel.CENTER);

        JLabel lblTen = new JLabel(ban.getTenBan());
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTen.setHorizontalAlignment(JLabel.CENTER);
        lblTen.setForeground("Trống".equals(ban.getTrangThai()) ? TEXT_DARK : Color.WHITE);

        JLabel lblTrangThai = new JLabel(ban.getTrangThai());
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTrangThai.setHorizontalAlignment(JLabel.CENTER);
        lblTrangThai.setForeground("Trống".equals(ban.getTrangThai()) ? TEXT_LIGHT : new Color(255, 255, 255, 200));

        

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(lblIcon);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblTen);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(lblTrangThai);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(Box.createVerticalGlue());

        banButton.add(contentPanel, BorderLayout.CENTER);

        banButton.putClientProperty("originalColor", btnColor);
        banButton.putClientProperty("lblTen", lblTen);
        banButton.putClientProperty("lblTrangThai", lblTrangThai);

        banButton.addActionListener(e -> onBanSelected(ban, banButton));

        return banButton;
    }

    // ========== GOI MON INLINE PANEL ==========
    private JPanel createGoiMonInlinePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_MAIN);

        // Header with ban info
        JPanel headerGoiMon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, new Color(40, 80, 140));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerGoiMon.setOpaque(false);
        headerGoiMon.setLayout(new BorderLayout(10, 0));
        headerGoiMon.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerGoiMon.setPreferredSize(new Dimension(0, 55));

        lblGoiMonBanInfo = new JLabel("GỌI MÓN — Chưa chọn bàn");
        lblGoiMonBanInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGoiMonBanInfo.setForeground(Color.WHITE);

        lblGoiMonHint = new JLabel("Nhấn vào món để thêm vào đơn hàng");
        lblGoiMonHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblGoiMonHint.setForeground(new Color(200, 220, 240));
        lblGoiMonHint.setHorizontalAlignment(JLabel.RIGHT);

        headerGoiMon.add(lblGoiMonBanInfo, BorderLayout.WEST);
        headerGoiMon.add(lblGoiMonHint, BorderLayout.EAST);

        // Search bar
     // Search bar + toggle Mang Về
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchBar.setBackground(BG_SECONDARY);
        searchBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JLabel lblSrchIcon = new JLabel("🔍");
        lblSrchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JTextField txtInlineSearch = new JTextField(18);  // ← giảm từ 20 → 18 để nhường chỗ
        txtInlineSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInlineSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JButton btnSearch = new JButton("Tìm") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        btnSearch.setBackground(PRIMARY_LIGHT);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setFocusPainted(false); btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false); btnSearch.setOpaque(false);
        btnSearch.setPreferredSize(new Dimension(70, 32));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnReset = new JButton("Tất cả") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setFocusPainted(false); btnReset.setBorderPainted(false);
        btnReset.setContentAreaFilled(false); btnReset.setOpaque(false);
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ── TOGGLE NÚT MANG VỀ ──────────────────────────────────────────────────
        // Dùng JSeparator để tách khu vực tìm kiếm vs chế độ
        JLabel lblSep = new JLabel(" | ");
        lblSep.setForeground(BORDER_COLOR);
        lblSep.setFont(new Font("Segoe UI", Font.BOLD, 18));

        btnToggleMangVe = new JToggleButton("Mang Về") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btnToggleMangVe.setBackground(new Color(149, 165, 166)); // xám = tắt
        btnToggleMangVe.setForeground(Color.WHITE);
        btnToggleMangVe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleMangVe.setFocusPainted(false);
        btnToggleMangVe.setBorderPainted(false);
        btnToggleMangVe.setContentAreaFilled(false);
        btnToggleMangVe.setOpaque(false);
        btnToggleMangVe.setPreferredSize(new Dimension(120, 32));
        btnToggleMangVe.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleMangVe.setToolTipText("Bật/tắt chế độ gọi món mang về");

        btnToggleMangVe.addItemListener(ev -> {
            isMangVeMode = btnToggleMangVe.isSelected();
            if (isMangVeMode) {
                btnToggleMangVe.setText("Mang Về (BẬT)");
                btnToggleMangVe.setBackground(new Color(255, 165, 0)); // tím = bật 142, 68, 173
                // Xóa giỏ mang về cũ khi bật lại
                mangVeCart.clear();
                refreshMangVeCartPanel();
                // Cập nhật header
                if (lblGoiMonBanInfo != null) {
                    lblGoiMonBanInfo.setText("GỌI MÓN MANG VỀ — Chọn món rồi xác nhận");
                    lblGoiMonBanInfo.setForeground(new Color(255, 220, 100));
                }
            } else {
                btnToggleMangVe.setText("Mang Về");
                btnToggleMangVe.setBackground(new Color(149, 165, 166));
                mangVeCart.clear();
                refreshMangVeCartPanel();
                // Khôi phục header
                if (lblGoiMonBanInfo != null) {
                    String txt = currentBanId == -1
                        ? "GỌI MÓN — Chưa chọn bàn"
                        : "GỌI MÓN — " + currentTenBan;
                    lblGoiMonBanInfo.setText(txt);
                    lblGoiMonBanInfo.setForeground(Color.WHITE);
                }
            }
            // Cập nhật panel bên phải
            updateMiniInfoPanelMode();
        });

        searchBar.add(lblSrchIcon);
        searchBar.add(txtInlineSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnReset);
        searchBar.add(lblSep);
        searchBar.add(btnToggleMangVe);

        // Nhom tabbed pane for inline goi mon
        tabbedPaneNhomInline = new JTabbedPane(JTabbedPane.TOP);
        tabbedPaneNhomInline.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPaneNhomInline.setBackground(BG_SECONDARY);
        tabbedPaneNhomInline.setForeground(TEXT_DARK);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(BG_SECONDARY);
        topSection.add(searchBar, BorderLayout.NORTH);
        topSection.add(tabbedPaneNhomInline, BorderLayout.CENTER);

        
        JPanel miniInfoPanel = createGoiMonMiniInfoPanel();
        goiMonMiniInfoPanel = miniInfoPanel;
        JSplitPane splitGoiMon = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitGoiMon.setLeftComponent(topSection);
        splitGoiMon.setRightComponent(miniInfoPanel);
        splitGoiMon.setDividerLocation(0.72); // 72% cho menu, 28% cho info
        splitGoiMon.setResizeWeight(0.75);
        splitGoiMon.setDividerSize(6);
        splitGoiMon.setBorder(null);
        splitGoiMon.setContinuousLayout(true);
        // =========================================================
       
        
        // Wire search buttons
        btnSearch.addActionListener(e -> {
            String kw = txtInlineSearch.getText().trim();
            if (!kw.isEmpty()) filterInlineMonAn(kw);
        });
        btnReset.addActionListener(e -> {
            txtInlineSearch.setText("");
            refreshGoiMonInlinePanel();
        });
        txtInlineSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String kw = txtInlineSearch.getText().trim();
                    if (!kw.isEmpty()) filterInlineMonAn(kw);
                }
            }
        });

        panel.add(headerGoiMon, BorderLayout.NORTH);
        panel.add(splitGoiMon, BorderLayout.CENTER); 

        return panel;
    }
    

    /** Refresh the inline Gọi Món panel with all nhom tabs */
    private void refreshGoiMonInlinePanel() {
        if (tabbedPaneNhomInline == null) return;

        // Clear UI reference maps so new cards re-register fresh references.
        // NOTE: inlineAddedCounts is intentionally NOT cleared here —
        //       counts persist across nhom tab switches within the same ban session.
        inlineStockLabels.clear();
        inlineAddButtons.clear();
        inlineStockCache.clear();
        inlineAddedCountLabels.clear();

        tabbedPaneNhomInline.removeAll();

        // Update header
        if (lblGoiMonBanInfo != null) {
            if (currentBanId != -1) {
                lblGoiMonBanInfo.setText("GỌI MÓN — " + currentTenBan);
                lblGoiMonHint.setText("Nhấn vào món để thêm vào đơn | Chuột phải để xóa khỏi giỏ");
            } else {
                lblGoiMonBanInfo.setText("GỌI MÓN — Chưa chọn bàn");
            }
        }

        String queryNhom = "SELECT MaNhom, TenNhom FROM Nhom ORDER BY MaNhom";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryNhom)) {

            while (rs.next()) {
                int maNhom = rs.getInt("MaNhom");
                String tenNhom = rs.getString("TenNhom");
                JPanel nhomPanel = createInlineNhomPanel(maNhom);
                tabbedPaneNhomInline.addTab(tenNhom, nhomPanel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabbedPaneNhomInline.revalidate();
        tabbedPaneNhomInline.repaint();
    }
    /**
     * Panel thông tin bàn thu gọn hiển thị bên phải tab Gọi Món.
     * Tự động cập nhật khi loadDanhSachMon() / tinhTongTien() được gọi.
     */
    private JPanel createGoiMonMiniInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));
        panel.setMinimumSize(new Dimension(360, 0));
        panel.setPreferredSize(new Dimension(380, 0));

        // ── Header ──────────────────────────────────────────────
        JPanel hdr = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, PRIMARY_DARK, getWidth(), 0, new Color(40,90,150));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        hdr.setOpaque(false);
        hdr.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        hdr.setPreferredSize(new Dimension(0, 48));

        JLabel lblHdrIcon = new JLabel("📋");
        lblHdrIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JLabel lblHdrTitle = new JLabel("THÔNG TIN BÀN");
        lblHdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHdrTitle.setForeground(Color.WHITE);
        hdr.add(lblHdrIcon);
        hdr.add(lblHdrTitle);

        // ── Summary fields (dùng GridLayout 2 cột, cân đối) ────
        JPanel summaryGrid = new JPanel(new GridLayout(6, 2, 8, 8));
        summaryGrid.setBackground(BG_SECONDARY);
        summaryGrid.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        // Helper tạo label bên trái
        java.util.function.Function<String, JLabel> makeLabel = text -> {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(TEXT_DARK);
            return lbl;
        };

        // Helper tạo value label bind với JTextField
        java.util.function.Function<JTextField, JLabel> makeVal = field -> {
            JLabel val = new JLabel(field.getText());
            val.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            val.setForeground(field.getForeground());
            val.setOpaque(true);
            val.setBackground(new Color(248, 250, 252));
            val.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220), 1),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)
            ));
            field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void changedUpdate(javax.swing.event.DocumentEvent e) { sync(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { sync(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { sync(); }
                void sync() {
                    SwingUtilities.invokeLater(() -> {
                        val.setText(field.getText());
                        val.setForeground(field.getForeground());
                    });
                }
            });
            return val;
        };

        summaryGrid.add(makeLabel.apply("Bàn:"));
        summaryGrid.add(makeVal.apply(txtTenBan));
        summaryGrid.add(makeLabel.apply("Tổng tiền:"));
        summaryGrid.add(makeVal.apply(txtTongTien));
        summaryGrid.add(makeLabel.apply("Giảm (%):"));
        summaryGrid.add(makeVal.apply(txtGiamGia));
        summaryGrid.add(makeLabel.apply("Tổng thu:"));
        summaryGrid.add(makeVal.apply(txtTongThu));
        summaryGrid.add(makeLabel.apply("NV tính:"));
        summaryGrid.add(makeVal.apply(txtTenNVTinhTien));
        summaryGrid.add(makeLabel.apply("Ngày:"));
        summaryGrid.add(makeVal.apply(txtNgayThang));

        // ── Divider ──────────────────────────────────────────────
        JPanel dividerPanel = new JPanel(new BorderLayout(6, 0));
        dividerPanel.setBackground(BG_SECONDARY);
        dividerPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JSeparator sepLeft  = new JSeparator(JSeparator.HORIZONTAL);
        JSeparator sepRight = new JSeparator(JSeparator.HORIZONTAL);
        sepLeft.setForeground(BORDER_COLOR);
        sepRight.setForeground(BORDER_COLOR);

        JLabel lblDivider = new JLabel("Chi tiết đơn bàn");
        lblDivider.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDivider.setForeground(PRIMARY_LIGHT);
        lblDivider.setHorizontalAlignment(JLabel.CENTER);
        lblDivider.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        dividerPanel.add(sepLeft,    BorderLayout.WEST);
        dividerPanel.add(lblDivider, BorderLayout.CENTER);
        dividerPanel.add(sepRight,   BorderLayout.EAST);
        // Cho 2 đường kẻ chiều rộng bằng nhau
        sepLeft.setPreferredSize(new Dimension(40, 10));
        sepRight.setPreferredSize(new Dimension(40, 10));
        
        
        // ── Mini order table ─────────────────────────────────────
        String[] miniCols = {"Tên Món", "SL", "Thành Tiền", "Thao Tác"};
        DefaultTableModel miniModel = new DefaultTableModel(miniCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };

        JTable miniTable = new JTable(miniModel);
        miniTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        miniTable.setRowHeight(34);
        miniTable.setShowGrid(true);
        miniTable.setGridColor(new Color(235,235,235));
        miniTable.setBackground(Color.WHITE);
        miniTable.setForeground(TEXT_DARK);
        miniTable.setFillsViewportHeight(true);  // fill toàn bộ viewport
        miniTable.setSelectionBackground(new Color(70,130,180,25));
        miniTable.setIntercellSpacing(new Dimension(8, 0));

        miniTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        miniTable.getTableHeader().setBackground(new Color(235,242,250));
        miniTable.getTableHeader().setForeground(PRIMARY_DARK);
        miniTable.getTableHeader().setPreferredSize(new Dimension(0, 28));
        miniTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0, PRIMARY_LIGHT));

        // Cân chỉnh độ rộng cột
        miniTable.getColumnModel().getColumn(0).setPreferredWidth(110);  // Tên Món
        miniTable.getColumnModel().getColumn(1).setPreferredWidth(28);   // SL
        miniTable.getColumnModel().getColumn(2).setPreferredWidth(95);   // Thành Tiền
        miniTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        miniTable.getColumnModel().getColumn(3).setMaxWidth(80);

        miniTable.getColumnModel().getColumn(3).setCellRenderer(new ActionButtonRenderer());
        
        miniTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private int editingRow = -1;

            private final JButton bEdit = new JButton();
            private final JButton bDel  = new JButton();
            private final JPanel p;

            {
                setClickCountToStart(1);

                // ── Kích thước nút cố định ──────────────────
                Dimension btnSize = new Dimension(32, 26);

                // ── Edit ────────────────────────────────────
                bEdit.setIcon(loadIcon("src/Assets/images/edit.png", 18, 18));
                if (bEdit.getIcon() == null) {
                    bEdit.setText("✏");
                    bEdit.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                bEdit.setBackground(PRIMARY_LIGHT);
                bEdit.setForeground(Color.WHITE);
                bEdit.setPreferredSize(btnSize);
                bEdit.setMinimumSize(btnSize);
                bEdit.setMaximumSize(btnSize);
                bEdit.setFocusPainted(false);
                bEdit.setBorderPainted(false);
                bEdit.setContentAreaFilled(true);
                bEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
                bEdit.setToolTipText("Sửa số lượng");
                bEdit.addActionListener(e -> {
                    int row = editingRow;
                    stopCellEditing();
                    onEditDish(row);
                });

                // ── Delete ───────────────────────────────────
                bDel.setIcon(loadIcon("src/Assets/images/delete.png", 18, 18));
                if (bDel.getIcon() == null) {
                    bDel.setText("🗑");
                    bDel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                bDel.setBackground(DANGER_RED);
                bDel.setForeground(Color.WHITE);
                bDel.setPreferredSize(btnSize);
                bDel.setMinimumSize(btnSize);
                bDel.setMaximumSize(btnSize);
                bDel.setFocusPainted(false);
                bDel.setBorderPainted(false);
                bDel.setContentAreaFilled(true);
                bDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                bDel.setToolTipText("Xóa món");
                bDel.addActionListener(e -> {
                    int row = editingRow;
                    stopCellEditing();
                    onDeleteDish(row);
                });

                // ── Panel dùng GridLayout 1x2 — KHÔNG wrap ──
                p = new JPanel(new GridLayout(1, 2, 4, 0));
                p.setBackground(new Color(245, 248, 252));
                p.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
                p.add(bEdit);
                p.add(bDel);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                editingRow = row;
                p.setBackground(isSelected ? new Color(235, 245, 255) : new Color(245, 248, 252));
                return p;
            }

            @Override public Object getCellEditorValue() { return ""; }
        });

        // Alternate row colors
        miniTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        JScrollPane miniScroll = new JScrollPane(miniTable);
        miniScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 12, 12, 12, BG_SECONDARY),
            BorderFactory.createLineBorder(BORDER_COLOR, 1)
        ));
        miniScroll.getVerticalScrollBar().setUnitIncrement(12);
        miniScroll.setBackground(BG_SECONDARY);

        // ── Sync miniModel từ modelThongTinBan ──────────────────
        modelThongTinBan.addTableModelListener(e -> {
            SwingUtilities.invokeLater(() -> {
                miniModel.setRowCount(0);
                for (int r = 0; r < modelThongTinBan.getRowCount(); r++) {
                    miniModel.addRow(new Object[]{
                        modelThongTinBan.getValueAt(r, 1),  // Tên Món
                        modelThongTinBan.getValueAt(r, 2),  // SL
                        modelThongTinBan.getValueAt(r, 4),  // Thành Tiền
                        ""                                   // Thao Tác
                    });
                }
            });
        });

        // ── Ghép layout ─────────────────────────────────────────
        JPanel topFixed = new JPanel(new BorderLayout(0, 0));
        topFixed.setBackground(BG_SECONDARY);
        topFixed.add(summaryGrid,  BorderLayout.NORTH);
        topFixed.add(dividerPanel, BorderLayout.SOUTH);
        
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 0));
        bodyPanel.setBackground(BG_SECONDARY);
        bodyPanel.add(topFixed,   BorderLayout.NORTH);   // summary cố định trên
        bodyPanel.add(miniScroll, BorderLayout.CENTER); 

        
        miniScroll.setPreferredSize(null);
        miniScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 12, 12, 12, BG_SECONDARY),
            BorderFactory.createLineBorder(BORDER_COLOR, 1)
        ));
        
        panel.add(hdr,       BorderLayout.NORTH);
        panel.add(bodyPanel, BorderLayout.CENTER); // toàn bộ nội dung co giãn tự nhiên

        
        
        // ── PANEL MANG VỀ (ẩn mặc định, hiện khi toggle bật) ────────────────
        mangVeCartPanel = buildMangVeCartPanel();
        // Lưu panel tại bàn hiện tại vào một biến
        // Dùng CardLayout để switch
        // → Thêm vào body dưới dạng CardLayout
        
        // Wrap bodyPanel + mangVeCartPanel vào CardLayout
        JPanel cardWrapper = new JPanel(new CardLayout());
        cardWrapper.add(bodyPanel, "TABAI");
        cardWrapper.add(mangVeCartPanel, "MANGVE");
        
        miniInfoHeaderPanel = hdr; // lưu reference
        panel.add(hdr,         BorderLayout.NORTH);
        panel.add(cardWrapper, BorderLayout.CENTER);
        
        // Lưu reference để updateMiniInfoPanelMode() có thể switch
        panel.putClientProperty("cardWrapper", cardWrapper);
        
        return panel;
    }

    
    /**
     * Tạo panel giỏ hàng mang về hiển thị bên phải khi chế độ Mang Về bật.
     */
    private JPanel buildMangVeCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SECONDARY);

        // ── Header ───────────────────────────────────────────────────────────
        JPanel hdr = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, new Color(255, 165, 0), getWidth(), 0, new Color(255, 165, 0));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        hdr.setOpaque(false);
        hdr.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        hdr.setPreferredSize(new Dimension(0, 48));

        JLabel lblHdrIcon  = new JLabel("🛍");
        lblHdrIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JLabel lblHdrTitle = new JLabel("GIỎ HÀNG MANG VỀ");
        lblHdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHdrTitle.setForeground(Color.WHITE);
        hdr.add(lblHdrIcon);
        hdr.add(lblHdrTitle);

        // ── Bảng giỏ hàng ────────────────────────────────────────────────────
        String[] cols = {"Tên Món", "SL", "Thành Tiền", "", ""};
        mangVeTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3 || c == 4; }
        };

        JTable mangVeTable = new JTable(mangVeTableModel);
        mangVeTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mangVeTable.setRowHeight(36);
        mangVeTable.setShowGrid(true);
        mangVeTable.setGridColor(new Color(235,235,235));
        mangVeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        mangVeTable.getTableHeader().setBackground(new Color(255, 165, 0));
        mangVeTable.getTableHeader().setForeground(Color.BLACK);

        // Cột độ rộng
        mangVeTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        mangVeTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        mangVeTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        mangVeTable.getColumnModel().getColumn(3).setPreferredWidth(38);
        mangVeTable.getColumnModel().getColumn(3).setMaxWidth(42);
        mangVeTable.getColumnModel().getColumn(4).setPreferredWidth(38);
        mangVeTable.getColumnModel().getColumn(4).setMaxWidth(42);

        // ── Renderer cột Edit (3) ──────────────────────────────────────────────
        mangVeTable.getColumnModel().getColumn(3).setCellRenderer(
            (table, value, isSelected, hasFocus, row, col) -> {
                JButton btn = new JButton();
                btn.setIcon(loadIcon("src/Assets/images/editnew.png", 18, 18));
                if (btn.getIcon() == null) { btn.setText("✏"); btn.setFont(new Font("Segoe UI", Font.BOLD, 11)); }
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                return btn;
            }
        );

        // ── Editor cột Edit (3) ───────────────────────────────────────────────
        mangVeTable.getColumnModel().getColumn(3).setCellEditor(
            new DefaultCellEditor(new JCheckBox()) {
                int editRow = -1;
                final JButton editBtn = new JButton();
                {
                    setClickCountToStart(1);
                    editBtn.setIcon(loadIcon("src/Assets/images/editnew.png", 18, 18));
                    if (editBtn.getIcon() == null) { editBtn.setText("✏"); editBtn.setFont(new Font("Segoe UI", Font.BOLD, 11)); }
                    editBtn.setBackground(Color.WHITE);
                    editBtn.setForeground(Color.WHITE);
                    editBtn.setOpaque(true);
                    editBtn.setBorderPainted(false);
                    editBtn.addActionListener(e -> {
                        int r = editRow;
                        stopCellEditing();
                        if (r >= 0 && r < mangVeCart.size()) {
                            MangVeItem item = mangVeCart.get(r);
                            // Lấy stock hiện tại từ DB
                            int maxStock = inlineStockCache.getOrDefault(item.maMon, 999);
                            String input = JOptionPane.showInputDialog(
                                PanelManHinhChinh.this,
                                "Nhập số lượng mới cho \"" + item.tenMon + "\" (tối đa: " + maxStock + "):",
                                item.soLuong
                            );
                            if (input != null && !input.trim().isEmpty()) {
                                try {
                                    int newQty = Integer.parseInt(input.trim());
                                    if (newQty <= 0) {
                                        JOptionPane.showMessageDialog(PanelManHinhChinh.this,
                                            "Số lượng phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                    } else if (newQty > maxStock) {
                                        JOptionPane.showMessageDialog(PanelManHinhChinh.this,
                                            "Số lượng vượt tồn kho (" + maxStock + ")!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        item.soLuong = newQty;
                                        inlineAddedCounts.put(item.maMon, newQty);
                                        refreshMangVeCartPanel();
                                        // Cập nhật nút "Đã +N" trên card
                                        String shortName = item.tenMon.length() > 10
                                            ? item.tenMon.substring(0, 10) + "…" : item.tenMon;
                                        SwingUtilities.invokeLater(() ->
                                            updateAddedCountLabelInPlace(item.maMon, newQty, shortName));
                                    }
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(PanelManHinhChinh.this,
                                        "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    });
                }
                @Override public Component getTableCellEditorComponent(
                        JTable t, Object v, boolean sel, int row, int col) {
                    editRow = row; return editBtn;
                }
                @Override public Object getCellEditorValue() { return ""; }
            }
        );

        // ── Renderer cột Delete (4) ───────────────────────────────────────────
        mangVeTable.getColumnModel().getColumn(4).setCellRenderer(
            (table, value, isSelected, hasFocus, row, col) -> {
                JButton btn = new JButton();
                btn.setIcon(loadIcon("src/Assets/images/dlnew.png", 18, 18));
                if (btn.getIcon() == null) { btn.setText("🗑"); btn.setFont(new Font("Segoe UI", Font.BOLD, 11)); }
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                return btn;
            }
        );

        // ── Editor cột Delete (4) ─────────────────────────────────────────────
        mangVeTable.getColumnModel().getColumn(4).setCellEditor(
            new DefaultCellEditor(new JCheckBox()) {
                int editRow = -1;
                final JButton delBtn = new JButton();
                {
                    setClickCountToStart(1);
                    delBtn.setIcon(loadIcon("src/Assets/images/dlnew.png", 18, 18));
                    if (delBtn.getIcon() == null) { delBtn.setText("🗑"); delBtn.setFont(new Font("Segoe UI", Font.BOLD, 11)); }
                    delBtn.setBackground(Color.WHITE);
                    delBtn.setForeground(Color.WHITE);
                    delBtn.setOpaque(true);
                    delBtn.setBorderPainted(false);
                    delBtn.addActionListener(e -> {
                        int r = editRow;
                        stopCellEditing();
                        if (r >= 0 && r < mangVeCart.size()) {
                            MangVeItem item = mangVeCart.get(r);
                            inlineAddedCounts.remove(item.maMon);
                            mangVeCart.remove(r);
                            refreshMangVeCartPanel();
                            // Đặt lại nút về "Thêm"
                            JButton btnAdd = inlineAddButtons.get(item.maMon);
                            if (btnAdd != null) {
                                SwingUtilities.invokeLater(() -> {
                                    btnAdd.setText("+ Thêm");
                                    btnAdd.setBackground(SUCCESS_GREEN);
                                    btnAdd.repaint();
                                });
                            }
                        }
                    });
                }
                @Override public Component getTableCellEditorComponent(
                        JTable t, Object v, boolean sel, int row, int col) {
                    editRow = row; return delBtn;
                }
                @Override public Object getCellEditorValue() { return ""; }
            }
        );

        JScrollPane scroll = new JScrollPane(mangVeTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 12, 0, 12, BG_SECONDARY),
            BorderFactory.createLineBorder(BORDER_COLOR, 1)
        ));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        // ── Tổng tiền ─────────────────────────────────────────────────────────
        lblMangVeTongTien = new JLabel("Tổng: 0 đ");
        lblMangVeTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMangVeTongTien.setForeground(DANGER_RED);
        lblMangVeTongTien.setHorizontalAlignment(JLabel.CENTER);
        lblMangVeTongTien.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // ── Phương thức thanh toán ────────────────────────────────────────────
        JPanel payMethodPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        payMethodPanel.setBackground(BG_SECONDARY);

        JLabel lblPayMethod = new JLabel("Thanh toán:");
        lblPayMethod.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JComboBox<String> cboPayMethod = new JComboBox<>(
            new String[]{"Tiền mặt", "Chuyển khoản QR"}
        );
        cboPayMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboPayMethod.setPreferredSize(new Dimension(160, 30));

        payMethodPanel.add(lblPayMethod);
        payMethodPanel.add(cboPayMethod);

        // ── Nút Xác Nhận Mang Về ─────────────────────────────────────────────
        JButton btnXacNhanMangVe = new JButton("XÁC NHẬN MANG VỀ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btnXacNhanMangVe.setBackground(new Color(255, 165, 0));
        btnXacNhanMangVe.setForeground(Color.WHITE);
        btnXacNhanMangVe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhanMangVe.setFocusPainted(false);
        btnXacNhanMangVe.setBorderPainted(false);
        btnXacNhanMangVe.setContentAreaFilled(false);
        btnXacNhanMangVe.setOpaque(false);
        btnXacNhanMangVe.setPreferredSize(new Dimension(220, 40));
        btnXacNhanMangVe.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhanMangVe.addActionListener(e ->
            xacNhanDonMangVe((String) cboPayMethod.getSelectedItem())
        );

        JButton btnXoaGio = new JButton("Xóa giỏ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btnXoaGio.setBackground(new Color(149, 165, 166));
        btnXoaGio.setForeground(Color.WHITE);
        btnXoaGio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaGio.setFocusPainted(false); btnXoaGio.setBorderPainted(false);
        btnXoaGio.setContentAreaFilled(false); btnXoaGio.setOpaque(false);
        btnXoaGio.setPreferredSize(new Dimension(110, 40));
        btnXoaGio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnXoaGio.addActionListener(e -> {
            if (!mangVeCart.isEmpty()) {
                mangVeCart.clear();
                inlineAddedCounts.clear();
                refreshMangVeCartPanel();
           
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnRow.setBackground(BG_SECONDARY);
        btnRow.add(btnXacNhanMangVe);
        btnRow.add(btnXoaGio);

        // ── Ghép layout ───────────────────────────────────────────────────────
        JPanel bottomSection = new JPanel(new BorderLayout(0, 0));
        bottomSection.setBackground(BG_SECONDARY);
        bottomSection.add(lblMangVeTongTien, BorderLayout.NORTH);
        bottomSection.add(payMethodPanel,    BorderLayout.CENTER);
        bottomSection.add(btnRow,            BorderLayout.SOUTH);

        panel.add(hdr,           BorderLayout.NORTH);
        panel.add(scroll,        BorderLayout.CENTER);
        panel.add(bottomSection, BorderLayout.SOUTH);

        return panel;
    }
    
    /** Cập nhật bảng giỏ hàng Mang Về và tổng tiền */
    private void refreshMangVeCartPanel() {
        if (mangVeTableModel == null) return;
        mangVeTableModel.setRowCount(0);
        java.math.BigDecimal tong = java.math.BigDecimal.ZERO;
        for (MangVeItem item : mangVeCart) {
            java.math.BigDecimal tt = item.thanhTien();
            tong = tong.add(tt);
            mangVeTableModel.addRow(new Object[]{
            	    item.tenMon,
            	    item.soLuong,
            	    String.format("%,d đ", tt.longValue()),
            	    "",  // cột Edit
            	    ""   // cột Delete
            	});
        }
        if (lblMangVeTongTien != null) {
            lblMangVeTongTien.setText(String.format("Tổng: %,d đ", tong.longValue()));
            lblMangVeTongTien.setForeground(tong.compareTo(java.math.BigDecimal.ZERO) > 0
                ? DANGER_RED : TEXT_LIGHT);
        }
    }
    /**
     * Switch CardLayout bên phải giữa chế độ "Tại Bàn" và "Mang Về".
     * Gọi mỗi khi btnToggleMangVe thay đổi trạng thái.
     */
    private void updateMiniInfoPanelMode() {
        if (goiMonMiniInfoPanel == null) return;
        Object obj = goiMonMiniInfoPanel.getClientProperty("cardWrapper");
        if (obj instanceof JPanel) {
            JPanel cardWrapper = (JPanel) obj;
            CardLayout cl = (CardLayout) cardWrapper.getLayout();
            if (isMangVeMode) {
                cl.show(cardWrapper, "MANGVE");
                refreshMangVeCartPanel();
                // Ẩn header "THÔNG TIN BÀN"
                if (miniInfoHeaderPanel != null) {
                    miniInfoHeaderPanel.setVisible(false);
                    goiMonMiniInfoPanel.revalidate();
                    goiMonMiniInfoPanel.repaint();
                }
            } else {
                cl.show(cardWrapper, "TABAI");
                // Hiện lại header "THÔNG TIN BÀN"
                if (miniInfoHeaderPanel != null) {
                    miniInfoHeaderPanel.setVisible(true);
                    goiMonMiniInfoPanel.revalidate();
                    goiMonMiniInfoPanel.repaint();
                }
            }
        }
    }
    /**
     * Xác nhận đơn hàng Mang Về: trừ stock, ghi DB, in thông báo.
     */
    private void xacNhanDonMangVe(String phuongThucTT) {
        if (mangVeCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng Mang Về đang trống!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xác nhận
        java.math.BigDecimal tong = mangVeCart.stream()
            .map(MangVeItem::thanhTien)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Xác nhận đơn Mang Về?\n• %d món  |  Tổng: %,d đ\n• Thanh toán: %s",
                mangVeCart.stream().mapToInt(i -> i.soLuong).sum(),
                tong.longValue(), phuongThucTT),
            "Xác Nhận Mang Về", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Tạo ChiTietBanHang (lấy maCTBH cho hóa đơn)
            MangVeItem first = mangVeCart.get(0);
            String insCTBH = "INSERT INTO ChiTietBanHang (SoLuong, MaMon) VALUES (?, ?)";
            PreparedStatement psCTBH = conn.prepareStatement(insCTBH,
                java.sql.Statement.RETURN_GENERATED_KEYS);
            psCTBH.setInt(1, first.soLuong);
            psCTBH.setInt(2, first.maMon);
            psCTBH.executeUpdate();
            ResultSet rsKey = psCTBH.getGeneratedKeys();
            int maCTBH = rsKey.next() ? rsKey.getInt(1) : -1;
            psCTBH.close();

            if (maCTBH == -1) throw new SQLException("Không lấy được MaCTBH");

            // 2. Tính tổng số lượng
            int tongSoLuong = mangVeCart.stream().mapToInt(i -> i.soLuong).sum();

            // 3. Tạo HoaDonKhachHang (MaBan = NULL)
            String insHD = "INSERT INTO HoaDonKhachHang " +
                "(NgayThanhToan, MaBan, MaCTBH, TongTienThanhToan, " +
                "PhanTramGiamGia, TongSoLuongMon, LoaiHoaDon, PhuongThucThanhToan, TrangThaiThanhToan) " +
                "VALUES (CURDATE(), NULL, ?, ?, 0, ?, 'Mang về', ?, 'Chờ thanh toán')";
            PreparedStatement psHD = conn.prepareStatement(insHD,
                java.sql.Statement.RETURN_GENERATED_KEYS);
            psHD.setInt(1, maCTBH);
            psHD.setBigDecimal(2, tong);
            psHD.setInt(3, tongSoLuong);
            psHD.setString(4, phuongThucTT);
            psHD.executeUpdate();
            ResultSet rsHD = psHD.getGeneratedKeys();
            int maCTHD = rsHD.next() ? rsHD.getInt(1) : -1;
            psHD.close();

            // 4. Tạo ChiTietHoaDon cho từng món
            String insCTHD = "INSERT INTO ChiTietHoaDon (MaCTHD, MaMon, SoLuong, GiaTien) VALUES (?,?,?,?)";
            PreparedStatement psCTHD = conn.prepareStatement(insCTHD);
            for (MangVeItem item : mangVeCart) {
                psCTHD.setInt(1, maCTHD);
                psCTHD.setInt(2, item.maMon);
                psCTHD.setInt(3, item.soLuong);
                psCTHD.setBigDecimal(4, item.giaTien);
                psCTHD.addBatch();
            }
            psCTHD.executeBatch();
            psCTHD.close();

            // 5. Trừ stock từng món
            String decStock = "UPDATE MonAn SET SoLuongConLai = SoLuongConLai - ? WHERE MaMon = ?";
            PreparedStatement psStock = conn.prepareStatement(decStock);
            for (MangVeItem item : mangVeCart) {
                psStock.setInt(1, item.soLuong);
                psStock.setInt(2, item.maMon);
                psStock.addBatch();
            }
            psStock.executeBatch();
            psStock.close();

            conn.commit();

            // ── Thành công ────────────────────────────────────────────────────
            JOptionPane.showMessageDialog(this,
                String.format("Đơn Mang Về đã được tạo!\nMã HĐ: #%d\nTổng: %,d đ\nTT: %s",
                    maCTHD, tong.longValue(), phuongThucTT),
                "Thành Công", JOptionPane.INFORMATION_MESSAGE);

         // ── In hóa đơn Mang Về ─────────────────────────────────────────────
            if (maCTHD > 0) {
                final int finalMaCTHD = maCTHD;
                SwingUtilities.invokeLater(() -> {
                    HoaDonPreviewFrame preview = new HoaDonPreviewFrame(
                        (JFrame) SwingUtilities.getWindowAncestor(PanelManHinhChinh.this),
                        finalMaCTHD, "Mang về"
                    );
                    preview.setVisible(true);
                });
            }
            
            // Reset giỏ, tắt toggle, reload
            mangVeCart.clear();
            inlineAddedCounts.clear();
            SwingUtilities.invokeLater(() -> {
                btnToggleMangVe.setSelected(false); // triggers ItemListener → isMangVeMode = false
                refreshMangVeCartPanel();
               
                // Cập nhật lại stock badge
                inlineStockCache.clear();
            });

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tạo đơn Mang Về:\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    /** Tạo 1 hàng nhỏ: [icon + label] — [value từ JTextField đã có] */
    private JPanel makeMiniRow(String emoji, String labelText, JTextField sourceField) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(emoji + " " + labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_DARK);
        lbl.setPreferredSize(new Dimension(90, 24));

        // Tạo label hiển thị — bind với sourceField thông qua DocumentListener
        JLabel valLbl = new JLabel(sourceField.getText());
        valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        valLbl.setForeground(sourceField.getForeground());
        valLbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        valLbl.setOpaque(true);
        valLbl.setBackground(new Color(248, 250, 252));

        sourceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { sync(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { sync(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { sync(); }
            void sync() {
                SwingUtilities.invokeLater(() -> {
                    valLbl.setText(sourceField.getText());
                    valLbl.setForeground(sourceField.getForeground());
                });
            }
        });

        row.add(lbl,    BorderLayout.WEST);
        row.add(valLbl, BorderLayout.CENTER);
        return row;
    }

    /** Filter inline mon an by keyword */
    private void filterInlineMonAn(String keyword) {
        if (tabbedPaneNhomInline == null) return;

        // Clear UI reference maps for search result cards
        // NOTE: inlineAddedCounts preserved so counts restore correctly when going back to tabs
        inlineStockLabels.clear();
        inlineAddButtons.clear();
        inlineStockCache.clear();
        inlineAddedCountLabels.clear();

        tabbedPaneNhomInline.removeAll();

        // Use GridLayout with 5 columns for consistent vertical scrolling
        final int CARD_W = 160, CARD_H = 235, HGAP = 10, VGAP = 10, PADDING = 10;
        JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, HGAP, VGAP)) {
            @Override
            public Dimension getPreferredSize() {
                int containerW = getParent() != null ? getParent().getWidth() : 800;
                if (containerW <= 0) containerW = 800;
                int cols = Math.max(1, (containerW - PADDING * 2 + HGAP) / (CARD_W + HGAP));
                int count = getComponentCount();
                int rows  = (int) Math.ceil((double) count / cols);
                int totalH = PADDING + rows * CARD_H + (rows > 0 ? (rows - 1) * VGAP : 0) + PADDING;
                return new Dimension(containerW, totalH);
            }
        };
        gridPanel.setBackground(BG_MAIN);
        
        gridPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        String query = "SELECT MaMon, TenMon, GiaTien, DonViTinh, HinhAnh, SoLuongConLai, MaNhom " +
                      "FROM MonAn WHERE TenMon LIKE ? ORDER BY TenMon";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));
                monAn.setMaNhom(rs.getInt("MaNhom"));

                inlineStockCache.put(monAn.getMaMon(), monAn.getSoLuongConLai());
                JPanel card = createInlineMonCard(monAn);
                gridPanel.add(card);
                count++;
            }

            if (count == 0) {
                JLabel lbl = new JLabel("Không tìm thấy món ăn phù hợp");
                lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                lbl.setForeground(TEXT_LIGHT);
                gridPanel.add(lbl);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane sp = new JScrollPane(gridPanel);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.setBorder(null);
        sp.setBackground(BG_MAIN);
        sp.getViewport().setBackground(BG_MAIN);

        sp.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gridPanel.revalidate();
            }
        });
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_MAIN);
        wrapper.add(sp, BorderLayout.CENTER);

        tabbedPaneNhomInline.addTab("Kết quả tìm kiếm", wrapper);
        tabbedPaneNhomInline.setSelectedIndex(0);
        tabbedPaneNhomInline.revalidate();
        tabbedPaneNhomInline.repaint();
    }

    /** Create a panel of mon an cards for a nhom */
    private JPanel createInlineNhomPanel(int maNhom) {
        final int CARD_W = 160;
        final int CARD_H = 235;
        final int HGAP   = 10;
        final int VGAP   = 10;
        final int PADDING = 10;

        // Dùng FlowLayout nhưng override getPreferredSize để tính chiều cao đúng
        JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, HGAP, VGAP)) {
            @Override
            public Dimension getPreferredSize() {
                int containerW = getParent() != null ? getParent().getWidth() : 800;
                if (containerW <= 0) containerW = 800;
                // Tính số card mỗi hàng
                int cols = Math.max(1, (containerW - PADDING * 2 + HGAP) / (CARD_W + HGAP));
                int count = getComponentCount();
                int rows  = (int) Math.ceil((double) count / cols);
                int totalH = PADDING + rows * CARD_H + (rows > 0 ? (rows - 1) * VGAP : 0) + PADDING;
                return new Dimension(containerW, totalH);
            }
        };
        gridPanel.setBackground(BG_MAIN);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        String query = "SELECT MaMon, TenMon, GiaTien, DonViTinh, HinhAnh, SoLuongConLai " +
                       "FROM MonAn WHERE MaNhom = ? ORDER BY TenMon";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, maNhom);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));

                inlineStockCache.put(monAn.getMaMon(), monAn.getSoLuongConLai());
                gridPanel.add(createInlineMonCard(monAn));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane sp = new JScrollPane(gridPanel);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.setBorder(null);
        sp.setBackground(BG_MAIN);
        sp.getViewport().setBackground(BG_MAIN);

        // Khi viewport thay đổi width → revalidate gridPanel để tính lại số cột
        sp.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gridPanel.revalidate();
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_MAIN);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    /** Create a single inline mon an card.
     *  Layout (top→bottom inside card):
     *    [Image 100% width, square-ish, rounded corners]
     *    [Tên món — bold]
     *    [Giá — red bold]
     *    [Stock badge — small pill]
     *    [Nút "▶ + Thêm" — morphs to "✓ Đã +N ..." on add]
     */
    private JPanel createInlineMonCard(MonAn monAn) {
        boolean available = monAn.getSoLuongConLai() > 0;

        // --- Card container ---------------------------------------------------
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft shadow
                g2d.setColor(new Color(0, 0, 0, 14));
                g2d.fillRoundRect(2, 4, getWidth() - 2, getHeight() - 4, 14, 14);
                // Card body
                g2d.setColor(available ? Color.WHITE : new Color(250, 250, 250));
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, 14, 14);
                // Border
                g2d.setColor(available ? new Color(218, 228, 238) : new Color(215, 215, 215));
                g2d.setStroke(new BasicStroke(1.1f));
                g2d.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 5, 14, 14);
            }
        };
        card.setLayout(new BorderLayout(0, 0));
        card.setOpaque(false);
        // Fixed card size: image ~55% height, info ~45% — 5 cards fit a ~1200px panel
        card.setPreferredSize(new Dimension(155, 235));
        card.setMinimumSize(new Dimension(155, 235));
        card.setMaximumSize(new Dimension(155, 235));
        if (available) card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- Image panel (top) ------------------------------------------------
        // We render the image inside a fixed-size panel so it always fills nicely
        int IMG_W = 149;  // card width minus border gaps
        int IMG_H = 120;  // ~52% of card height → balanced with content below
        JPanel imgPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        imgPanel.setOpaque(false);
        imgPanel.setPreferredSize(new Dimension(IMG_W, IMG_H + 8)); // +8 for top padding

        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(IMG_W, IMG_H));
        lblImg.setHorizontalAlignment(JLabel.CENTER);
        lblImg.setVerticalAlignment(JLabel.CENTER);
        lblImg.setText("🍲");
        lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        // top-left-right padding; 0 bottom so image sits flush above content
        lblImg.setBorder(BorderFactory.createEmptyBorder(7, 7, 1, 7));

        if (monAn.getHinhAnh() != null && !monAn.getHinhAnh().isEmpty()) {
            File imgFile = new File(IMAGE_FOLDER + monAn.getHinhAnh());
            if (imgFile.exists()) {
                new SwingWorker<ImageIcon, Void>() {
                    @Override
                    protected ImageIcon doInBackground() throws Exception {
                        int iw = IMG_W - 14; // 135 (minus left+right border)
                        int ih = IMG_H - 8;  // 112
                        BufferedImage src = ImageIO.read(imgFile);
                        // Center-crop to square before scaling → avoids stretch
                        int side = Math.min(src.getWidth(), src.getHeight());
                        int cx = (src.getWidth() - side) / 2;
                        int cy = (src.getHeight() - side) / 2;
                        BufferedImage cropped = src.getSubimage(cx, cy, side, side);
                        // Scale to target
                        java.awt.Image scaled = cropped.getScaledInstance(iw, ih, Image.SCALE_SMOOTH);
                        // Rounded-corner clip
                        BufferedImage result = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = result.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, iw, ih, 10, 10));
                        g2d.drawImage(scaled, 0, 0, null);
                        g2d.dispose();
                        return new ImageIcon(result);
                    }
                    @Override
                    protected void done() {
                        try {
                            lblImg.setIcon(get());
                            lblImg.setText(null);
                            card.repaint();
                        } catch (Exception ex) {
                            lblImg.setText("🍲");
                        }
                    }
                }.execute();
            }
        }

        imgPanel.add(lblImg, BorderLayout.CENTER);

        // --- Info panel (bottom) ----------------------------------------------
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(6, 9, 8, 9));

        // Tên món — truncate long names with tooltip
        JLabel lblTen = new JLabel(monAn.getTenMon());
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTen.setForeground(available ? TEXT_DARK : TEXT_LIGHT);
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTen.setToolTipText(monAn.getTenMon());

        // Giá
        JLabel lblGia = new JLabel(String.format("%,d đ", monAn.getGiaTien().longValue()));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGia.setForeground(available ? DANGER_RED : TEXT_LIGHT);
        lblGia.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Stock badge pill
        int currentStock = monAn.getSoLuongConLai();
        String stockText = currentStock > 0 ? "Còn " + currentStock + " " + monAn.getDonViTinh() : "Hết hàng";
        Color stockBg = currentStock > 0 ? new Color(212, 237, 218) : new Color(248, 215, 218);
        Color stockFg = currentStock > 0 ? new Color(21, 87, 36)  : new Color(114, 28, 36);

        JLabel lblStock = new JLabel(stockText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        lblStock.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStock.setForeground(stockFg);
        lblStock.setBackground(stockBg);
        lblStock.setOpaque(false);
        lblStock.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        lblStock.setAlignmentX(Component.LEFT_ALIGNMENT);
        inlineStockLabels.put(monAn.getMaMon(), lblStock);

        // --- Morph button: "+ Thêm" → "✓ Đã +N tên" -------------------------
        // Short name for button label (max 11 chars to fit the button width)
        String shortName = monAn.getTenMon().length() > 11
                ? monAn.getTenMon().substring(0, 11) + "…"
                : monAn.getTenMon();

        // Determine initial added count (persisted across nhom tab switches)
        int existingCount = inlineAddedCounts.getOrDefault(monAn.getMaMon(), 0);
        boolean alreadyAdded = existingCount > 0;

        JButton btnAdd = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };

        // Helper to apply the "added" visual state to the button
        Runnable applyAddedState = () -> {
            int cnt = inlineAddedCounts.getOrDefault(monAn.getMaMon(), 0);
            btnAdd.setText("Đã +" + cnt + "  " + shortName);
            btnAdd.setBackground(new Color(39, 174, 96));   // slightly different green = "confirmed"
            btnAdd.setForeground(Color.WHITE);
        };

        Runnable applyDefaultState = () -> {
            btnAdd.setText("+ Thêm");
            btnAdd.setBackground(SUCCESS_GREEN);
            btnAdd.setForeground(Color.WHITE);
        };

        // Initial appearance
        if (alreadyAdded) {
            applyAddedState.run();
        } else {
            applyDefaultState.run();
        }

        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setOpaque(false);
        btnAdd.setEnabled(available);
        btnAdd.setCursor(available ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        btnAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        if (!available) {
            btnAdd.setText("Hết hàng");
            btnAdd.setBackground(new Color(200, 200, 200));
        }

        // Register button so updateAddedCountLabelInPlace can morph it
        inlineAddButtons.put(monAn.getMaMon(), btnAdd);
        // Also register a dummy label entry to satisfy existing map lookups
        inlineAddedCountLabels.put(monAn.getMaMon(), new JLabel());

        if (available) {
            btnAdd.addActionListener(e ->
                addMonToCurrentBan(monAn.getMaMon(), monAn.getTenMon(), monAn.getGiaTien(), monAn.getDonViTinh(), card));
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    // Don't double-fire when the click landed on the button itself
                    Point p = SwingUtilities.convertPoint(card, e.getPoint(), btnAdd);
                    if (btnAdd.contains(p)) return;
                    addMonToCurrentBan(monAn.getMaMon(), monAn.getTenMon(), monAn.getGiaTien(), monAn.getDonViTinh(), card);
                }
            });
        }

        infoPanel.add(lblTen);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(lblGia);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblStock);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(btnAdd);

        card.add(imgPanel,   BorderLayout.NORTH);
        card.add(infoPanel,  BorderLayout.CENTER);

        return card;
    }

    /** 
     * Add a mon an to the current ban's HoaDonBanHang.
     * Uses in-place update of stock label — NO full tab reload.
     * Immediately updates ban status button and order detail table.
     */
    private void addMonToCurrentBan(int maMon, String tenMon, BigDecimal giaTien, String donViTinh, JPanel card) {
    	
    	if (isMangVeMode) {
    	        addMonToMangVeCart(maMon, tenMon, giaTien, donViTinh, card);
    	        return;
    	    }

    	
    	if (currentBanId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check current local stock cache first (fast check)
        int cachedStock = inlineStockCache.getOrDefault(maMon, -1);
        if (cachedStock == 0) {
            JOptionPane.showMessageDialog(this, "Món \"" + tenMon + "\" đã hết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Verify actual stock in DB
            String checkStock = "SELECT SoLuongConLai FROM MonAn WHERE MaMon = ?";
            PreparedStatement psStock = conn.prepareStatement(checkStock);
            psStock.setInt(1, maMon);
            ResultSet rsStock = psStock.executeQuery();
            int actualStock = 0;
            if (rsStock.next()) actualStock = rsStock.getInt(1);
            psStock.close();

            if (actualStock <= 0) {
                conn.rollback();
                // Update cache and badge to out-of-stock
                inlineStockCache.put(maMon, 0);
                updateStockBadgeInPlace(maMon, 0, donViTinh);
                JOptionPane.showMessageDialog(this, "Món \"" + tenMon + "\" đã hết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Check if mon already in HoaDonBanHang for this ban
            String checkExist = "SELECT MaHDBH, SoLuong FROM HoaDonBanHang WHERE MaBan = ? AND MaMon = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkExist);
            psCheck.setInt(1, currentBanId);
            psCheck.setInt(2, maMon);
            ResultSet rsCheck = psCheck.executeQuery();

            if (rsCheck.next()) {
                // Update quantity
                int newQty = rsCheck.getInt("SoLuong") + 1;
                String upd = "UPDATE HoaDonBanHang SET SoLuong = ? WHERE MaBan = ? AND MaMon = ?";
                PreparedStatement psUpd = conn.prepareStatement(upd);
                psUpd.setInt(1, newQty);
                psUpd.setInt(2, currentBanId);
                psUpd.setInt(3, maMon);
                psUpd.executeUpdate();
                psUpd.close();
            } else {
                // Insert new row
                NhanVien nv = UserSession.getInstance().getCurrentUser();
                int maNV = nv != null ? nv.getMaNV() : 1;
                String ins = "INSERT INTO HoaDonBanHang (NgayVao, MaBan, MaMon, SoLuong, MaNV) VALUES (CURDATE(), ?, ?, 1, ?)";
                PreparedStatement psIns = conn.prepareStatement(ins);
                psIns.setInt(1, currentBanId);
                psIns.setInt(2, maMon);
                psIns.setInt(3, maNV);
                psIns.executeUpdate();
                psIns.close();
            }
            psCheck.close();

            // 3. Decrement stock in DB
            String decStock = "UPDATE MonAn SET SoLuongConLai = SoLuongConLai - 1 WHERE MaMon = ?";
            PreparedStatement psDecStock = conn.prepareStatement(decStock);
            psDecStock.setInt(1, maMon);
            psDecStock.executeUpdate();
            psDecStock.close();

            // 4. Mark ban as 'Đang sử dụng'
            String updBan = "UPDATE Ban SET TrangThai = 'Đang sử dụng' WHERE MaBan = ?";
            PreparedStatement psBan = conn.prepareStatement(updBan);
            psBan.setInt(1, currentBanId);
            psBan.executeUpdate();
            psBan.close();

            conn.commit();

            // ── IN-PLACE UPDATES (no full tab reload) ──────────────────────

            // 5a. Update local stock cache
            int newStock = actualStock - 1;
            inlineStockCache.put(maMon, newStock);

            // 5b. Update stock badge label in-place
            SwingUtilities.invokeLater(() -> updateStockBadgeInPlace(maMon, newStock, donViTinh));

            // 5c. Flash card green briefly
            SwingUtilities.invokeLater(() -> {
                card.setBackground(new Color(212, 237, 218));
                card.repaint();
                Timer flashTimer = new Timer(350, ev -> {
                    card.setBackground(null);
                    card.repaint();
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
            });

            // 5d. Immediately refresh the order detail table (left panel)
            SwingUtilities.invokeLater(() -> {
                loadDanhSachMon(currentBanId);
                tinhTongTien(currentBanId);
            });

            // 5e. Immediately update the ban button in the grid (status -> Đang sử dụng)
            // We reload only the ban grid, not the mon tab
            SwingUtilities.invokeLater(() -> loadBan());

            // 5f. Update "Đã +N tên" counter label in-place
            int newCount = inlineAddedCounts.getOrDefault(maMon, 0) + 1;
            inlineAddedCounts.put(maMon, newCount);
            final int finalCount = newCount;
            final String shortName = tenMon.length() > 10 ? tenMon.substring(0, 10) + "…" : tenMon;
            SwingUtilities.invokeLater(() -> updateAddedCountLabelInPlace(maMon, finalCount, shortName));

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm món: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    /**
     * Thêm món vào giỏ hàng Mang Về (không cần bàn, không ghi DB ngay).
     * Chỉ giảm stock sau khi xác nhận.
     */
    private void addMonToMangVeCart(int maMon, String tenMon, BigDecimal giaTien,
                                      String donViTinh, JPanel card) {
        // Kiểm tra stock cache
        int cachedStock = inlineStockCache.getOrDefault(maMon, -1);
        if (cachedStock == 0) {
            JOptionPane.showMessageDialog(this, "Món \"" + tenMon + "\" đã hết!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra stock thực trong DB
        try (Connection conn = DatabaseConfig.getConnection()) {
            String checkStock = "SELECT SoLuongConLai FROM MonAn WHERE MaMon = ?";
            PreparedStatement ps = conn.prepareStatement(checkStock);
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            int actualStock = rs.next() ? rs.getInt(1) : 0;
            ps.close();

            if (actualStock <= 0) {
                inlineStockCache.put(maMon, 0);
                updateStockBadgeInPlace(maMon, 0, donViTinh);
                JOptionPane.showMessageDialog(this, "Món \"" + tenMon + "\" đã hết!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Thêm/cộng dồn vào giỏ Mang Về
            boolean found = false;
            for (MangVeItem item : mangVeCart) {
                if (item.maMon == maMon) {
                    // Kiểm tra không vượt stock
                    if (item.soLuong >= actualStock) {
                        JOptionPane.showMessageDialog(this,
                            "Số lượng đã đạt tối đa trong kho (" + actualStock + ")!",
                            "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    item.soLuong++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                mangVeCart.add(new MangVeItem(maMon, tenMon, giaTien, donViTinh, 1));
            }

            // Flash card xanh lá
            SwingUtilities.invokeLater(() -> {
                card.setBackground(new Color(212, 237, 218));
                card.repaint();
                Timer flashTimer = new Timer(350, ev -> {
                    card.setBackground(null);
                    card.repaint();
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
            });

            // Cập nhật counter trên nút + Refresh giỏ hàng Mang Về
            int newCount = inlineAddedCounts.getOrDefault(maMon, 0) + 1;
            inlineAddedCounts.put(maMon, newCount);
            final String shortName = tenMon.length() > 10 ? tenMon.substring(0, 10) + "…" : tenMon;
            SwingUtilities.invokeLater(() -> {
                updateAddedCountLabelInPlace(maMon, newCount, shortName);
                refreshMangVeCartPanel();
            });

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra kho: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Update the stock badge label of a specific mon card in-place.
     * Also disables the add button (or overrides to "Hết hàng") when stock reaches 0.
     * Called on EDT. Does NOT reload the tab.
     */
    private void updateStockBadgeInPlace(int maMon, int newStock, String donViTinh) {
        JLabel lblStock = inlineStockLabels.get(maMon);
        JButton btnAdd  = inlineAddButtons.get(maMon);
        if (lblStock == null) return;

        if (newStock > 0) {
            lblStock.setText("Còn " + newStock + " " + donViTinh);
            lblStock.setForeground(new Color(21, 87, 36));
            lblStock.setBackground(new Color(212, 237, 218));
        } else {
            lblStock.setText("Hết hàng");
            lblStock.setForeground(new Color(114, 28, 36));
            lblStock.setBackground(new Color(248, 215, 218));
            // Override button to "Hết hàng" state — replaces morphed or default text
            if (btnAdd != null) {
                btnAdd.setText("Hết hàng");
                btnAdd.setBackground(new Color(200, 200, 200));
                btnAdd.setEnabled(false);
                btnAdd.setCursor(Cursor.getDefaultCursor());
            }
        }
        lblStock.repaint();
    }

    /**
     * Morph the "+ Thêm" button into "✓ Đã +N tên" in-place after a successful add.
     * Called on EDT. No tab reload.
     */
    private void updateAddedCountLabelInPlace(int maMon, int count, String shortName) {
        JButton btnAdd = inlineAddButtons.get(maMon);
        if (btnAdd == null) return;

        // Morph button text
        btnAdd.setText("Đã +" + count + "  " + shortName);

        // Pulse: bright flash → settle to confirmed green
        btnAdd.setBackground(new Color(82, 196, 26));   // bright flash
        btnAdd.repaint();

        Timer settleTimer = new Timer(500, ev -> {
            btnAdd.setBackground(new Color(39, 174, 96));  // confirmed darker green
            btnAdd.repaint();
        });
        settleTimer.setRepeats(false);
        settleTimer.start();
    }

    private void selectTabByKhuVuc(int maKV) {
        for (int i = 0; i < tabbedPaneBan.getTabCount(); i++) {
            String tabName = tabbedPaneBan.getTitleAt(i);
            for (int j = 0; j < modelKhuVuc.getSize(); j++) {
                KhuVucQuan kv = modelKhuVuc.get(j);
                if (kv.getMaKV() == maKV && kv.getTenKV().equals(tabName)) {
                    tabbedPaneBan.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    // ========== RIGHT PANEL - THANH TOÁN & CHI TIẾT ==========
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        infoPanelRight = createInfoPanel();
        JPanel detailPanel = createTablePanel();
        JPanel bottomPanel = createPaymentPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(infoPanelRight);
        splitPane.setBottomComponent(detailPanel);
        splitPane.setDividerLocation(320);
        splitPane.setDividerSize(10);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.4);
        infoPanelRight.setMinimumSize(new Dimension(200, 180));
        detailPanel.setMinimumSize(new Dimension(200, 100));

        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = getWidth(); int h = getHeight();
                        GradientPaint gp = new GradientPaint(0, 0, new Color(200, 210, 220), 0, h, new Color(160, 175, 190));
                        g2.setPaint(gp);
                        g2.fillRect(0, 0, w, h);
                        g2.setColor(new Color(255, 255, 255, 120));
                        g2.drawLine(0, 0, w, 0);
                        g2.setColor(new Color(0, 0, 0, 50));
                        g2.drawLine(0, h - 1, w, h - 1);
                        g2.setColor(new Color(100, 115, 135));
                        int centerY = h / 2;
                        int dotW = 16; int dotH = 4; int spacing = 6;
                        for (int i = -3; i <= 3; i++) {
                            int y = centerY + i * (dotH + spacing) - dotH / 2;
                            if (y < 15 || y > h - 25) continue;
                            g2.fillRoundRect((w - dotW) / 2, y, dotW, dotH, 2, 2);
                        }
                        g2.dispose();
                    }
                };
            }
        });

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(0, 380));

        JPanel panelTenBan = createLabelPanel("🪑", "Bàn:", "bann.png");
        txtTenBan = createStyledTextField(12, true);

        JPanel panelTongTien = createLabelPanel("💰", "Tổng:", "tongthu.png");
        txtTongTien = createStyledTextField(12, true);
        txtTongTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTongTien.setForeground(DANGER_RED);
        txtTongTien.setText("0 VND");

        JPanel panelGiamGia = createLabelPanel("🏷️", "Giảm (%):", "disc.png");
        txtGiamGia = createStyledTextField(12, false);

        txtGiamGia.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTongThuFromField(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTongThuFromField(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTongThuFromField(); }

            private void updateTongThuFromField() {
                try {
                    String tongTienText = txtTongTien.getText().replaceAll("[^0-9]", "");
                    if (!tongTienText.isEmpty()) {
                        BigDecimal tongTien = new BigDecimal(tongTienText);
                        String giamGiaText = txtGiamGia.getText().trim();
                        if (!giamGiaText.isEmpty()) {
                            double giamGia = Double.parseDouble(giamGiaText);
                            if (giamGia > 0 && giamGia <= 100) {
                                BigDecimal tienGiam = tongTien.multiply(BigDecimal.valueOf(giamGia / 100));
                                BigDecimal tongThu = tongTien.subtract(tienGiam);
                                txtTongThu.setText(String.format("%,d VND", tongThu.longValue()));
                                return;
                            }
                        }
                        txtTongThu.setText(String.format("%,d VND", tongTien.longValue()));
                    }
                } catch (NumberFormatException ex) { }
            }
        });

        JPanel panelTenNV = createLabelPanel("👤", "NV Tính:", "nhanvien.png");
        txtTenNVTinhTien = createStyledTextField(11, true);

        JPanel panelMaNVTinh = createLabelPanel("🆔", "Mã NV:", "nhanvien.png");
        txtMaNVTinhTien = createStyledTextField(11, true);

        JPanel panelMaNVMo = createLabelPanel("🔑", "NV Mở:", "nhanvien.png");
        txtMaNVMoBan = createStyledTextField(11, true);
        txtMaNVMoBan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        txtMaNVMoBan.setForeground(new Color(192, 57, 43));
        txtMaNVMoBan.setText("Chưa có");

        JPanel panelNgayThang = createLabelPanel("📅", "Ngày:", "date.png");
        txtNgayThang = createStyledTextField(11, true);
        txtNgayThang.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        JPanel panelTongThu = createLabelPanel("💵", "Tổng Thu:", "tongthu.png");
        txtTongThu = createStyledTextField(12, true);
        txtTongThu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTongThu.setForeground(SUCCESS_GREEN);
        txtTongThu.setText("0 VND");

        panel.add(panelTenBan); panel.add(txtTenBan);
        panel.add(panelTongTien); panel.add(txtTongTien);
        panel.add(panelGiamGia); panel.add(txtGiamGia);
        panel.add(panelTenNV); panel.add(txtTenNVTinhTien);
        panel.add(panelMaNVTinh); panel.add(txtMaNVTinhTien);
        panel.add(panelMaNVMo); panel.add(txtMaNVMoBan);
        panel.add(panelNgayThang); panel.add(txtNgayThang);
        // placeholder rows to fill 9
        panel.add(new JLabel()); panel.add(new JLabel());
        panel.add(panelTongThu); panel.add(txtTongThu);

        return panel;
    }

    private JPanel createLabelPanel(String emoji, String text, String iconPath) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(BG_SECONDARY);

        JLabel lblIcon = new JLabel();
        lblIcon.setIcon(loadIcon("src/Assets/images/" + iconPath, 18, 18));
        if (lblIcon.getIcon() == null) {
            lblIcon.setText(emoji);
            lblIcon.setFont(new Font("Arial", Font.PLAIN, 16));
        }

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblText.setForeground(TEXT_DARK);

        panel.add(lblIcon);
        panel.add(lblText);

        return panel;
    }

    private JTextField createStyledTextField(int fontSize, boolean readOnly) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        textField.setForeground(TEXT_DARK);
        textField.setBackground(Color.WHITE);
        textField.setEditable(!readOnly);
        textField.setCaretColor(PRIMARY_LIGHT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return textField;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel headerTable = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerTable.setBackground(BG_SECONDARY);

        JLabel iconTable = new JLabel("📋");
        iconTable.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel titleTable = new JLabel("CHI TIẾT ĐƠN HÀNG");
        titleTable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleTable.setForeground(TEXT_DARK);
        headerTable.add(iconTable);
        headerTable.add(titleTable);

        String[] columns = {"Mã", "Tên Món", "SL", "Đơn Giá", "Thành Tiền", "Thao Tác"};
        modelThongTinBan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 5; }
        };

        tableThongTinBan = new JTable(modelThongTinBan);
        tableThongTinBan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tableThongTinBan.setForeground(TEXT_DARK);
        tableThongTinBan.setBackground(Color.WHITE);
        tableThongTinBan.setRowHeight(32);
        tableThongTinBan.setShowGrid(true);
        tableThongTinBan.setGridColor(new Color(230, 230, 230));
        tableThongTinBan.setSelectionBackground(new Color(70, 130, 180, 30));
        tableThongTinBan.setSelectionForeground(TEXT_DARK);

        tableThongTinBan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableThongTinBan.getTableHeader().setBackground(new Color(240, 245, 250));
        tableThongTinBan.getTableHeader().setForeground(TEXT_DARK);
        tableThongTinBan.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        tableThongTinBan.getTableHeader().setPreferredSize(new Dimension(0, 30));

        TableColumn actionColumn = tableThongTinBan.getColumnModel().getColumn(5);
        actionColumn.setCellRenderer(new ActionButtonRenderer());
        actionColumn.setCellEditor(new ActionButtonEditor(new JCheckBox()));
        actionColumn.setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(tableThongTinBan);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(Color.WHITE);

        panel.add(headerTable, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setPreferredSize(new Dimension(0, 50));

        JButton btnGoiMon = createActionButton("src/Assets/images/ordermon.png", "GỌI MÓN", PRIMARY_LIGHT);
        btnGoiMon.addActionListener(e -> onGoiMon());

        JButton btnThanhToan = createActionButton("src/Assets/images/payment.png", "THANH TOÁN", SUCCESS_GREEN);
        btnThanhToan.addActionListener(e -> onThanhToan());

        JButton btnDatBan = createActionButton("src/Assets/images/datban.png", "ĐẶT BÀN", ACCENT_ORANGE);
        btnDatBan.addActionListener(e -> onDatBan());

        panel.add(btnGoiMon);
        panel.add(btnThanhToan);
        panel.add(btnDatBan);

        return panel;
    }

    private JButton createActionButton(String iconPath, String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };

        ImageIcon icon = loadIcon(iconPath, 28, 28);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            btn.setIconTextGap(8);
        }

        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });

        return btn;
    }

    // ========== FOOTER PANEL ==========
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(60, 70, 85));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        panel.setPreferredSize(new Dimension(0, 35));

        JLabel lblStatus = new JLabel("Đã kết nối");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(SUCCESS_GREEN);

        JLabel lblVersion = new JLabel("v1.0.0 © 2026 Hệ thống quán lẩu - TiTi");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVersion.setForeground(new Color(180, 180, 180));

        panel.add(lblStatus, BorderLayout.WEST);
        panel.add(lblVersion, BorderLayout.CENTER);

        return panel;
    }

    // ========== HELPER METHODS ==========
    private void updateUserInfo() {
        NhanVien currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            String vaiTro = currentUser.getMaPQ() == 1 ? "Admin" : currentUser.getMaPQ() == 2 ? "Nhân viên" : "Khác";
            lblUserInfo.setText(String.format("%s (%s)", currentUser.getHoTen(), vaiTro));
            updateEmployeeInfoFields();
        } else {
            lblUserInfo.setText("Chưa đăng nhập");
        }
    }

    private void updateDateTime() {
        dateTimeTimer = new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy");
            lblDateTime.setText(now.format(formatter));
            if (txtNgayThang != null) {
                txtNgayThang.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });
        dateTimeTimer.start();
    }

    private void loadKhuVuc() {
        modelKhuVuc.clear();
        for (KhuVucQuan kv : getKhuVucList()) {
            modelKhuVuc.addElement(kv);
        }
    }

    private List<KhuVucQuan> getKhuVucList() {
        List<KhuVucQuan> list = new ArrayList<>();
        String query = "SELECT MaKV, TenKV FROM KhuVucQuan ORDER BY MaKV";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                KhuVucQuan kv = new KhuVucQuan();
                kv.setMaKV(rs.getInt("MaKV"));
                kv.setTenKV(rs.getString("TenKV"));
                list.add(kv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void loadBan() {
        // Remember current tab index to restore after reload
        int savedTabIndex = tabbedPaneBan.getSelectedIndex();

        tabbedPaneBan.removeAll();
        mapPanelBanByKhuVuc.clear();

        for (int i = 0; i < modelKhuVuc.getSize(); i++) {
            KhuVucQuan kv = modelKhuVuc.get(i);
            JPanel panelBan = createPanelBanForKhuVuc(kv.getMaKV(), kv.getTenKV());
            tabbedPaneBan.addTab(kv.getTenKV(), panelBan);
        }

        if (tabbedPaneBan.getTabCount() > 0) {
            // Restore previously selected tab, or default to 0
            int restoreIndex = (savedTabIndex >= 0 && savedTabIndex < tabbedPaneBan.getTabCount()) ? savedTabIndex : 0;
            tabbedPaneBan.setSelectedIndex(restoreIndex);
            if (modelKhuVuc.getSize() > restoreIndex) {
                currentKhuVucId = modelKhuVuc.get(restoreIndex).getMaKV();
                listKhuVuc.setSelectedIndex(restoreIndex);
            }
        }

        if (infoPanelRight != null) infoPanelRight.repaint();
    }

    private void onBanSelected(Ban ban, JButton banButton) {
        if (selectedBanButton == banButton) return;

        if (selectedBanButton != null) {
            JLabel lblTen = (JLabel) selectedBanButton.getClientProperty("lblTen");
            JLabel lblTrangThai = (JLabel) selectedBanButton.getClientProperty("lblTrangThai");
            if ("Trống".equals(ban.getTrangThai())) {
                if (lblTen != null) lblTen.setForeground(TEXT_DARK);
                if (lblTrangThai != null) lblTrangThai.setForeground(TEXT_LIGHT);
            } else {
                if (lblTen != null) lblTen.setForeground(Color.WHITE);
                if (lblTrangThai != null) lblTrangThai.setForeground(new Color(255, 255, 255, 200));
            }
            selectedBanButton.repaint();
        }

        currentBanId = ban.getMaBan();
        currentTenBan = ban.getTenBan();
        txtTenBan.setText(currentTenBan);

        // Reset per-session add counts when switching to a different ban
        inlineAddedCounts.clear();
        inlineAddedCountLabels.clear();

        selectedBanButton = banButton;
        selectedBanButton.repaint();

        updateEmployeeInfoFields();
        loadThongTinNhanVienMoBan(currentBanId);

        if ("Trống".equals(ban.getTrangThai())) {
            modelThongTinBan.setRowCount(0);
            txtTongTien.setText("0 VND");
            txtGiamGia.setText("");
            txtTongThu.setText("0 VND");
        } else if ("Đã đặt".equals(ban.getTrangThai())) {
            ThongTinDatBanFrame formThongTin = new ThongTinDatBanFrame(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                ban.getMaBan(), ban.getTenBan()
            );
            formThongTin.setVisible(true);
            formThongTin.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadBan();
                    if (currentBanId != -1) {
                        String trangThaiMoi = BanDAO.getTrangThaiBan(currentBanId);
                        if ("Đang sử dụng".equals(trangThaiMoi)) {
                            loadDanhSachMon(currentBanId);
                            tinhTongTien(currentBanId);
                            loadThongTinNhanVienMoBan(currentBanId);
                            updateEmployeeInfoFields();
                        } else {
                            modelThongTinBan.setRowCount(0);
                            txtTongTien.setText("0 VND");
                            txtGiamGia.setText("");
                            txtTongThu.setText("0 VND");
                        }
                    }
                }
            });
        } else {
            loadDanhSachMon(ban.getMaBan());
            tinhTongTien(ban.getMaBan());
        }

        infoPanelRight.repaint();
    }

    private void loadThongTinNhanVienMoBan(int maBan) {
        NhanVien currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            txtMaNVMoBan.setText("NV-" + currentUser.getMaNV() + " (" + currentUser.getHoTen() + ")");
        } else {
            String query = "SELECT h.MaNV, n.HoTen FROM hoadonbanhang h INNER JOIN nhanvien n ON h.MaNV = n.MaNV WHERE h.MaBan = ? ORDER BY h.MaHDBH ASC LIMIT 1";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, maBan);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    txtMaNVMoBan.setText("NV-" + rs.getInt("MaNV") + " (" + rs.getString("HoTen") + ")");
                } else {
                    txtMaNVMoBan.setText("Chưa có");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                txtMaNVMoBan.setText("Lỗi");
            }
        }
    }

    private void loadDanhSachMon(int maBan) {
        String query = "SELECT mn.MaMon, mn.TenMon, h.SoLuong, mn.GiaTien, " +
                      "(h.SoLuong * mn.GiaTien) AS ThanhTien " +
                      "FROM HoaDonBanHang h " +
                      "INNER JOIN MonAn mn ON h.MaMon = mn.MaMon " +
                      "WHERE h.MaBan = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            modelThongTinBan.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaMon"),
                    rs.getString("TenMon"),
                    rs.getInt("SoLuong"),
                    String.format("%,d VND", rs.getBigDecimal("GiaTien").longValue()),
                    String.format("%,d VND", rs.getBigDecimal("ThanhTien").longValue()),
                    ""
                };
                modelThongTinBan.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin món: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tinhTongTien(int maBan) {
        String query = "SELECT SUM(m.GiaTien * h.SoLuong) FROM HoaDonBanHang h INNER JOIN MonAn m ON h.MaMon = m.MaMon WHERE h.MaBan = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getBigDecimal(1) != null) {
                BigDecimal tongTien = rs.getBigDecimal(1);
                txtTongTien.setText(String.format("%,d VND", tongTien.longValue()));
                updateTongThu(tongTien);
            } else {
                txtTongTien.setText("0 VND");
                txtTongThu.setText("0 VND");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tính tổng tiền: " + e.getMessage());
        }
    }

    private void updateTongThu(BigDecimal tongTien) {
        try {
            String giamGiaText = txtGiamGia.getText().trim();
            BigDecimal tongThu = tongTien;
            if (!giamGiaText.isEmpty()) {
                double giamGia = Double.parseDouble(giamGiaText);
                if (giamGia > 0 && giamGia <= 100) {
                    BigDecimal tienGiam = tongTien.multiply(BigDecimal.valueOf(giamGia / 100));
                    tongThu = tongTien.subtract(tienGiam);
                }
            }
            txtTongThu.setText(String.format("%,d VND", tongThu.longValue()));
        } catch (NumberFormatException e) {
            txtTongThu.setText(String.format("%,d VND", tongTien.longValue()));
        }
    }

    private void onGoiMon() {
        if (currentBanId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Switch to inline Gọi Món tab
        if (rightTabbedPane != null) {
            rightTabbedPane.setSelectedIndex(1);
            return;
        }

        // Fallback: open GoiMonFrame
        GoiMonFrame formGoiMon = new GoiMonFrame(currentBanId, currentTenBan);
        formGoiMon.setVisible(true);
        formGoiMon.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadBan();
                if (currentBanId != -1) {
                    String trangThaiMoi = BanDAO.getTrangThaiBan(currentBanId);
                    if ("Đang sử dụng".equals(trangThaiMoi)) {
                        loadDanhSachMon(currentBanId);
                        tinhTongTien(currentBanId);
                        loadThongTinNhanVienMoBan(currentBanId);
                        updateEmployeeInfoFields();
                    } else {
                        modelThongTinBan.setRowCount(0);
                        txtTongTien.setText("0 VND");
                        txtGiamGia.setText("");
                        txtTongThu.setText("0 VND");
                    }
                }
            }
        });
    }

    private void onThanhToan() {
        if (currentBanId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước khi thanh toán!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!BanDAO.checkIfBanHasOrdered(currentBanId)) {
            JOptionPane.showMessageDialog(this, "Bàn này chưa có món để thanh toán!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String giamGiaHienTai = txtGiamGia.getText().trim();
        ThanhToanFrame formThanhToan = new ThanhToanFrame(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            currentBanId, giamGiaHienTai
        );
        formThanhToan.setVisible(true);

        formThanhToan.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (formThanhToan.isDaThanhToan()) {
                    loadBan();
                    modelThongTinBan.setRowCount(0);
                    txtTongTien.setText("0 VND");
                    txtGiamGia.setText("");
                    txtTongThu.setText("0 VND");
                    txtMaNVMoBan.setText("Chưa có");
                    currentBanId = -1;
                    currentTenBan = "";
                    txtTenBan.setText("");
                } else {
                    loadBan();
                    if (currentBanId != -1) {
                        loadDanhSachMon(currentBanId);
                        tinhTongTien(currentBanId);
                    }
                }
            }
        });
    }

    private void onDatBan() {
        if (currentBanId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String trangThai = BanDAO.getTrangThaiBan(currentBanId);
        if ("Đang sử dụng".equals(trangThai)) {
            JOptionPane.showMessageDialog(this, "Bàn này đang được sử dụng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Đã đặt".equals(trangThai)) {
            JOptionPane.showMessageDialog(this, "Bàn này đã được đặt trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatBanFrame formDatBan = new DatBanFrame(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            currentBanId, currentTenBan
        );
        formDatBan.setVisible(true);
        formDatBan.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { loadBan(); }
        });
    }

    // ========== ACTION BUTTON RENDERER/EDITOR (original) ==========
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnEdit, btnDelete;

        public ActionButtonRenderer() {
        	setLayout(new GridLayout(1, 2, 4, 0));
        	setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
            setBackground(Color.WHITE);

            btnEdit = new JButton();
            btnEdit.setIcon(loadIcon("src/Assets/images/editnew.png", 24, 24));
            btnEdit.setFocusPainted(false);
            btnEdit.setBorderPainted(false);
            btnEdit.setContentAreaFilled(true);
            btnEdit.setPreferredSize(new Dimension(28, 28));
            btnEdit.setToolTipText("Sửa");
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (btnEdit.getIcon() == null) { btnEdit.setText("✏️"); btnEdit.setFont(new Font("Arial", Font.PLAIN, 12)); }

            btnDelete = new JButton();
            btnDelete.setIcon(loadIcon("src/Assets/images/dlnew.png", 24, 24));
            btnDelete.setFocusPainted(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setContentAreaFilled(true);
            btnDelete.setPreferredSize(new Dimension(28, 28));
            btnDelete.setToolTipText("Xóa");
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (btnDelete.getIcon() == null) { btnDelete.setText("🗑️"); btnDelete.setFont(new Font("Arial", Font.PLAIN, 12)); }

            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnEdit, btnDelete;

        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setBackground(Color.WHITE);

            btnEdit = new JButton();
            btnEdit.setIcon(loadIcon("src/Assets/images/edit.png", 24, 24));
            btnEdit.setBackground(PRIMARY_LIGHT);
            btnEdit.setFocusPainted(false);
            btnEdit.setBorderPainted(false);
            btnEdit.setContentAreaFilled(true);
            btnEdit.setPreferredSize(new Dimension(28, 28));
            btnEdit.setToolTipText("Sửa");
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (btnEdit.getIcon() == null) { btnEdit.setText("✏️"); btnEdit.setFont(new Font("Arial", Font.PLAIN, 12)); }

            btnDelete = new JButton();
            btnDelete.setIcon(loadIcon("src/Assets/images/delete.png", 24, 24));
            btnDelete.setBackground(DANGER_RED);
            btnDelete.setFocusPainted(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setContentAreaFilled(true);
            btnDelete.setPreferredSize(new Dimension(28, 28));
            btnDelete.setToolTipText("Xóa");
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (btnDelete.getIcon() == null) { btnDelete.setText("🗑️"); btnDelete.setFont(new Font("Arial", Font.PLAIN, 12)); }

            btnEdit.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btnEdit.setBackground(PRIMARY_LIGHT.darker()); }
                @Override public void mouseExited(MouseEvent e) { btnEdit.setBackground(PRIMARY_LIGHT); }
            });
            btnDelete.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btnDelete.setBackground(DANGER_RED.darker()); }
                @Override public void mouseExited(MouseEvent e) { btnDelete.setBackground(DANGER_RED); }
            });

            btnEdit.addActionListener(e -> onEditDish(tableThongTinBan.getSelectedRow()));
            btnDelete.addActionListener(e -> onDeleteDish(tableThongTinBan.getSelectedRow()));

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return panel;
        }
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(path));
            java.awt.Image scaledImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            return null;
        }
    }

    private void onEditDish(int row) {
        if (row < 0) return;
        int maMon = (Integer) modelThongTinBan.getValueAt(row, 0);
        String tenMon = (String) modelThongTinBan.getValueAt(row, 1);
        int soLuong = (Integer) modelThongTinBan.getValueAt(row, 2);

        String newQuantity = JOptionPane.showInputDialog(this, "Nhập số lượng mới cho " + tenMon + ":", soLuong);
        if (newQuantity != null && !newQuantity.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(newQuantity);
                if (quantity > 0) {
                    updateDishQuantity(currentBanId, maMon, quantity);
                    loadDanhSachMon(currentBanId);
                    tinhTongTien(currentBanId);
                } else {
                    JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteDish(int row) {
        if (row < 0) return;
        int maMon = (Integer) modelThongTinBan.getValueAt(row, 0);
        String tenMon = (String) modelThongTinBan.getValueAt(row, 1);
        int soLuongHienTai = (Integer) modelThongTinBan.getValueAt(row, 2);

        if (soLuongHienTai > 1) {
            String[] options = {"Xóa toàn bộ", "Xóa một phần"};
            int choice = JOptionPane.showOptionDialog(this,
                "Món " + tenMon + " có " + soLuongHienTai + " cái.\nBạn muốn làm gì?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == JOptionPane.CLOSED_OPTION) return;
            if (choice == 0) {
                deleteDish(currentBanId, maMon);
            } else {
                String soLuongXoa = JOptionPane.showInputDialog(this, "Nhập số lượng cần xóa (1 - " + soLuongHienTai + "):", "1");
                if (soLuongXoa != null && !soLuongXoa.trim().isEmpty()) {
                    try {
                        int quantityToDelete = Integer.parseInt(soLuongXoa);
                        if (quantityToDelete > 0 && quantityToDelete <= soLuongHienTai) {
                            int newQuantity = soLuongHienTai - quantityToDelete;
                            if (newQuantity > 0) updateDishQuantity(currentBanId, maMon, newQuantity);
                            else deleteDish(currentBanId, maMon);
                            loadDanhSachMon(currentBanId);
                            tinhTongTien(currentBanId);
                        } else {
                            JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng từ 1 đến " + soLuongHienTai + "!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa " + tenMon + " khỏi bàn này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteDish(currentBanId, maMon);
                loadDanhSachMon(currentBanId);
                tinhTongTien(currentBanId);
            }
        }
    }

    private void updateEmployeeInfoFields() {
        NhanVien currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (txtTenNVTinhTien != null) txtTenNVTinhTien.setText(currentUser.getHoTen());
            if (txtMaNVTinhTien != null) txtMaNVTinhTien.setText("NV-" + currentUser.getMaNV());
            if (txtMaNVMoBan != null && currentBanId == -1) txtMaNVMoBan.setText("Chưa có");
            if (infoPanelRight != null) { infoPanelRight.revalidate(); infoPanelRight.repaint(); }
        }
    }

    private void updateDishQuantity(int maBan, int maMon, int newQuantity) {
        stopTableEditing();
        String query = "UPDATE HoaDonBanHang SET SoLuong = ? WHERE MaBan = ? AND MaMon = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, maBan);
            pstmt.setInt(3, maMon);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật số lượng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadDanhSachMon(maBan);
            tinhTongTien(maBan);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopTableEditing() {
        if (tableThongTinBan.isEditing()) tableThongTinBan.getCellEditor().stopCellEditing();
    }

    private void deleteDish(int maBan, int maMon) {
        stopTableEditing();
        String query = "DELETE FROM HoaDonBanHang WHERE MaBan = ? AND MaMon = ?";
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, maBan);
            pstmt.setInt(2, maMon);
            pstmt.executeUpdate();

            String checkQuery = "SELECT COUNT(*) FROM HoaDonBanHang WHERE MaBan = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, maBan);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int remainingCount = rs.getInt(1);

            if (remainingCount == 0) {
                String updateBan = "UPDATE Ban SET TrangThai = 'Trống' WHERE MaBan = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateBan);
                updateStmt.setInt(1, maBan);
                updateStmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Xóa món thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadBan();

            if (remainingCount > 0) {
                loadDanhSachMon(maBan);
                tinhTongTien(maBan);
            } else {
                modelThongTinBan.setRowCount(0);
                txtTongTien.setText("0 VND");
                txtGiamGia.setText("");
                txtTongThu.setText("0 VND");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /** Đại diện một dòng trong giỏ hàng mang về */
    private static class MangVeItem {
        int maMon;
        String tenMon;
        java.math.BigDecimal giaTien;
        String donViTinh;
        int soLuong;

        MangVeItem(int maMon, String tenMon, java.math.BigDecimal giaTien,
                   String donViTinh, int soLuong) {
            this.maMon    = maMon;
            this.tenMon   = tenMon;
            this.giaTien  = giaTien;
            this.donViTinh = donViTinh;
            this.soLuong  = soLuong;
        }

        java.math.BigDecimal thanhTien() {
            return giaTien.multiply(java.math.BigDecimal.valueOf(soLuong));
        }
    }
    
}