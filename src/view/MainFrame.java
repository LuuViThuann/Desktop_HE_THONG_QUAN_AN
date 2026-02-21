package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import config.UserSession;
import model.NhanVien;


public class MainFrame extends JFrame {

    private PanelManHinhChinh panelManHinhChinh;
    private JPanel contentPanel;

    // ============ COLOR PALETTE - Chuyên Nghiệp ============
    private static final Color PRIMARY_DARK   = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT  = new Color(70, 130, 180);
    private static final Color ACCENT_ORANGE  = new Color(230, 126, 34);
    private static final Color SUCCESS_GREEN  = new Color(46, 152, 102);
    private static final Color DANGER_RED     = new Color(192, 57, 43);
    private static final Color WARNING_AMBER  = new Color(241, 196, 15);

    private static final Color BG_MAIN        = new Color(241, 244, 247);
    private static final Color BG_SECONDARY   = new Color(255, 255, 255);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT     = new Color(127, 140, 141);
    private static final Color BORDER_COLOR   = new Color(189, 195, 199);

    public MainFrame() {
        initLookAndFeel();
        initComponents();

        setTitle("RESTAURANT MANAGER PRO - Hệ Thống Quản Lý Quán Ăn");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        setVisible(true);
    }

    // ===================== LOOK & FEEL - MODERNIZED =====================
    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            UIManager.put("MenuBar.background", PRIMARY_DARK);
            UIManager.put("MenuBar.foreground", Color.WHITE);
            UIManager.put("MenuBar.border", BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

            UIManager.put("Menu.background", PRIMARY_DARK);
            UIManager.put("Menu.foreground", Color.WHITE);
            UIManager.put("Menu.selectionBackground", PRIMARY_LIGHT);
            UIManager.put("Menu.selectionForeground", Color.WHITE);
            UIManager.put("Menu.acceleratorForeground", Color.WHITE);
            UIManager.put("Menu.font", new Font("Segoe UI", Font.BOLD, 13));

            UIManager.put("MenuItem.background", BG_SECONDARY);
            UIManager.put("MenuItem.foreground", TEXT_DARK);
            UIManager.put("MenuItem.selectionBackground", PRIMARY_LIGHT);
            UIManager.put("MenuItem.selectionForeground", Color.WHITE);
            UIManager.put("MenuItem.acceleratorForeground", TEXT_LIGHT);
            UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("MenuItem.border", BorderFactory.createEmptyBorder(6, 12, 6, 12));

            UIManager.put("Separator.foreground", BORDER_COLOR);

            UIManager.put("control", BG_MAIN);
            UIManager.put("info", Color.WHITE);
            UIManager.put("nimbusBase", PRIMARY_LIGHT);
            UIManager.put("nimbusBlueGrey", TEXT_LIGHT);
            UIManager.put("nimbusFocus", ACCENT_ORANGE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== COMPONENT =====================
    private void initComponents() {
        setJMenuBar(createMenuBar());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_MAIN);

        panelManHinhChinh = new PanelManHinhChinh();
        contentPanel.add(panelManHinhChinh, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        getContentPane().setBackground(BG_MAIN);
    }

    // ===================== MENU BAR - ✅ CẬP NHẬT PHÂN QUYỀN =====================
    private JMenuBar createMenuBar() {
        // ✅ LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI
        NhanVien currentUser = UserSession.getInstance().getCurrentUser();
        boolean isAdmin = (currentUser != null && currentUser.getMaPQ() == 1);

        JMenuBar menuBar = new JMenuBar() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, 0, getHeight(),
                        new Color(35, 60, 110));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        menuBar.setBackground(PRIMARY_DARK);
        menuBar.setForeground(Color.WHITE);
        menuBar.setOpaque(false);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        menuBar.setPreferredSize(new Dimension(0, 40));

        // ===== MENU HỆ THỐNG (Tất cả đều truy cập được) =====
        JMenu menuHeThong = createMenu("HỆ THỐNG", true);
        menuHeThong.add(createMenuItem("Màn Hình Chính", e -> showMainPanel(), "Ctrl+H"));
        menuHeThong.add(createMenuItem("Đổi Mật Khẩu", e -> openFormDoiMatKhau(), "Ctrl+P"));
        menuHeThong.addSeparator();
        menuHeThong.add(createMenuItem("Thoát Ứng Dụng", e -> exitApplication(), "Alt+F4"));

        // ===== MENU QUẢN LÝ - ✅ CHỈ ADMIN MỚI THẤY =====
        JMenu menuQuanLy = null;
        if (isAdmin) {
            menuQuanLy = createMenu("QUẢN LÝ", true);
            menuQuanLy.add(createMenuItem("Quản Lý Nhân Viên",  e -> openFormQuanLyNhanVien(),  ""));
            menuQuanLy.add(createMenuItem("Quản Lý Phân Quyền", e -> openFormQuanLyPhanQuyen(), ""));
            menuQuanLy.add(createMenuItem("Quản Lý Nhóm Món",   e -> openFormQuanLyNhomMon(),   ""));
            menuQuanLy.add(createMenuItem("Quản Lý Món Ăn",     e -> openFormQuanLyMonNuoc(),   ""));
            menuQuanLy.addSeparator();
            menuQuanLy.add(createMenuItem("Quản Lý Khu Vực", e -> openFormQuanLyKhuVuc(), ""));
            menuQuanLy.add(createMenuItem("Quản Lý Bàn",     e -> openFormQuanLyBan(),    ""));
        }

        // ===== MENU CA LÀM - ✅ CHỈ ADMIN MỚI THẤY =====
        JMenu menuCaLam = null;
        if (isAdmin) {
            menuCaLam = createMenu("CA LÀM", true);
            menuCaLam.add(createMenuItem("Quản Lý Ca Làm",      e -> openFormCaLam(),    ""));
            menuCaLam.add(createMenuItem("Phân Công Ca Làm",    e -> openFormPhanCong(), ""));
            menuCaLam.add(createMenuItem("Chấm Công Nhân Viên", e -> openFormChamCong(), ""));
        }

        // ===== MENU THỐNG KÊ - ✅ CHỈ ADMIN MỚI THẤY =====
        JMenu menuThongKe = null;
        if (isAdmin) {
            menuThongKe = createMenu("THỐNG KÊ", true);
            menuThongKe.add(createMenuItem("Thống Kê Tổng Quan", e -> showThongKeTongQuan(), "Ctrl+T"));
            menuThongKe.add(createMenuItem("Lịch Sử Đặt Bàn",   e -> showLichSuDatBan(),    "Ctrl+L"));
        }

        // ===== MENU HÓA ĐƠN (Tất cả đều truy cập được) =====
        JMenu menuHoaDon = createMenu("HÓA ĐƠN", true);
        menuHoaDon.add(createMenuItem("Danh sách hóa đơn", e -> openFormQuanLyHoaDon(), "Ctrl+I"));

        // ===== MENU TRỢ GIÚP (Tất cả đều truy cập được) =====
        JMenu menuTro = createMenu("TRỢ GIÚP", true);
        menuTro.add(createMenuItem("Hướng Dẫn Sử Dụng", e -> openHelpDialog(),  "F1"));
        menuTro.add(createMenuItem("Về Ứng Dụng",        e -> openAboutDialog(), ""));

        // ===== ✅ MỚI: MENU CHUYỂN/GỘP BÀN =====
        // Tất cả role xem được lịch sử; Admin có thêm Thống Kê + Xóa cũ
        JMenu menuChuyenBan = createMenu("CHUYỂN/GỘP BÀN", true);

        menuChuyenBan.add(createMenuItem(
            "Lịch Sử Chuyển & Gộp Bàn",
            e -> showLichSuChuyenBanPanel(),
            "Ctrl+B"
        ));

       

        // ===== ADD TO MENUBAR =====
        menuBar.add(menuHeThong);
        menuBar.add(Box.createHorizontalStrut(15));

        // ✅ CHỈ THÊM MENU QUẢN LÝ NẾU LÀ ADMIN
        if (isAdmin && menuQuanLy != null) {
            menuBar.add(menuQuanLy);
            menuBar.add(Box.createHorizontalStrut(15));
        }

        // ✅ CHỈ THÊM MENU CA LÀM NẾU LÀ ADMIN
        if (isAdmin && menuCaLam != null) {
            menuBar.add(menuCaLam);
            menuBar.add(Box.createHorizontalStrut(15));
        }

        // ✅ CHỈ THÊM MENU THỐNG KÊ NẾU LÀ ADMIN
        if (isAdmin && menuThongKe != null) {
            menuBar.add(menuThongKe);
            menuBar.add(Box.createHorizontalStrut(15));
        }

        menuBar.add(menuHoaDon);
        menuBar.add(Box.createHorizontalStrut(15));

        menuBar.add(menuTro);
        menuBar.add(Box.createHorizontalStrut(15));

        // ✅ THÊM MENU CHUYỂN/GỘP BÀN (đã có submenu hoàn chỉnh)
        menuBar.add(menuChuyenBan);

        menuBar.add(Box.createHorizontalGlue());

        // ✅ HIỂN THỊ VAI TRÒ NGƯỜI DÙNG
        JLabel roleLabel = new JLabel();
        if (currentUser != null) {
            String role = isAdmin ? "Admin" : "Nhân viên";
            roleLabel.setText(role);
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            roleLabel.setForeground(isAdmin ? new Color(255, 215, 0) : new Color(200, 200, 200));
            roleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }
        menuBar.add(roleLabel);

        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(180, 180, 180));
        versionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        menuBar.add(versionLabel);

        return menuBar;
    }

    // ===================== MENU FACTORY =====================
    private JMenu createMenu(String title, boolean withIcon) {
        JMenu menu = new JMenu(title);
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menu.setOpaque(false);
        menu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menu.setForeground(new Color(180, 220, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menu.setForeground(Color.WHITE);
            }
        });

        return menu;
    }

    private JMenuItem createMenuItem(String title, ActionListener listener, String accelerator) {
        JMenuItem item = new JMenuItem(title);
        item.setOpaque(true);
        item.setBackground(BG_SECONDARY);
        item.setForeground(TEXT_DARK);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(PRIMARY_LIGHT);
                item.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(BG_SECONDARY);
                item.setForeground(TEXT_DARK);
            }
        });

        if (listener != null) {
            item.addActionListener(listener);
        }

        if (!accelerator.isEmpty()) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }

        return item;
    }

    /**
     * ✅ MỚI: MenuItem màu đỏ cho hành động nguy hiểm (xóa dữ liệu, v.v.)
     */
    private JMenuItem createMenuItemDanger(String title, ActionListener listener) {
        JMenuItem item = new JMenuItem(title);
        item.setOpaque(true);
        item.setBackground(BG_SECONDARY);
        item.setForeground(DANGER_RED);
        item.setFont(new Font("Segoe UI", Font.BOLD, 12));
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(DANGER_RED);
                item.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(BG_SECONDARY);
                item.setForeground(DANGER_RED);
            }
        });

        if (listener != null) {
            item.addActionListener(listener);
        }

        return item;
    }

    // ===================== PANEL SWITCHING =====================

    private void switchToPanel(JPanel newPanel) {
        contentPanel.removeAll();
        contentPanel.add(newPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        String title = "RESTAURANT MANAGER PRO - ";

        if (newPanel instanceof ThongKeTongQuanForm) {
            title += "Thống Kê Tổng Quan";
        } else if (newPanel instanceof LichSuDatBanForm) {
            title += "Lịch Sử Đặt Bàn";
        } else if (newPanel instanceof PanelManHinhChinh) {
            title += "Hệ Thống Quản Lý Quán Ăn";
        } else if (newPanel instanceof LichSuChuyenBanForm) {      // ✅ MỚI
            title += "Lịch Sử Chuyển & Gộp Bàn";
        } else {
            title += newPanel.getClass().getSimpleName();
        }

        setTitle(title);
    }

    // ===================== ACTION HANDLERS =====================

    private void showMainPanel() {
        try {
            if (panelManHinhChinh == null) {
                panelManHinhChinh = new PanelManHinhChinh();
            }
            switchToPanel(panelManHinhChinh);
            showStatusMessage("Đã chuyển về Màn Hình Chính", SUCCESS_GREEN);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi chuyển về màn hình chính: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showThongKeTongQuan() {
        try {
            ThongKeTongQuanForm thongKeForm = new ThongKeTongQuanForm();
            switchToPanel(thongKeForm);
            showStatusMessage("Đã mở Thống Kê Tổng Quan", PRIMARY_LIGHT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở Thống Kê Tổng Quan: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openFormQuanLyHoaDon() {
        try {
            new FormQuanLyHoaDon().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Hóa Đơn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLichSuDatBan() {
        try {
            LichSuDatBanForm lichSuForm = new LichSuDatBanForm();
            switchToPanel(lichSuForm);
            showStatusMessage("Đã mở Lịch Sử Đặt Bàn", PRIMARY_LIGHT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở Lịch Sử Đặt Bàn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ✅ MỚI: Mở LichSuChuyenBanForm nhúng vào contentPanel
    private void showLichSuChuyenBanPanel() {
        try {
            LichSuChuyenBanForm form = new LichSuChuyenBanForm();
            switchToPanel(form);
            showStatusMessage("Đã mở Lịch Sử Chuyển & Gộp Bàn", new Color(142, 68, 173));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở Lịch Sử Chuyển Bàn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ✅ MỚI: Popup thống kê nhanh số lượt chuyển/gộp hôm nay (Admin only)
    private void showThongKeChuyenBanHomNay() {
        String sql =
            "SELECT " +
            "  COUNT(*) AS Tong, " +
            "  SUM(CASE WHEN GhiChu = 'Gộp bàn' THEN 1 ELSE 0 END) AS SoGop, " +
            "  SUM(CASE WHEN GhiChu IS NULL OR GhiChu != 'Gộp bàn' THEN 1 ELSE 0 END) AS SoChuyen " +
            "FROM LichSuChuyenBan WHERE NgayChuyen = CURDATE()";

        try (java.sql.Connection conn = config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int tong     = rs.getInt("Tong");
                int soGop    = rs.getInt("SoGop");
                int soChuyen = rs.getInt("SoChuyen");

                String today = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                String msg = String.format(
                    "📊  THỐNG KÊ CHUYỂN/GỘP BÀN HÔM NAY (%s)\n\n"  +
                    "  🔄  Chuyển bàn :  %d lượt\n"                   +
                    "  🔗  Gộp bàn    :  %d lượt\n"                   +
                    "  ─────────────────────────────────\n"            +
                    "  📋  Tổng cộng  :  %d lượt",
                    today, soChuyen, soGop, tong
                );

                JTextArea ta = new JTextArea(msg);
                ta.setFont(new Font("Consolas", Font.PLAIN, 13));
                ta.setEditable(false);
                ta.setBackground(new Color(248, 250, 252));
                ta.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

                JOptionPane.showMessageDialog(this, ta,
                    "Thống Kê Hôm Nay", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi tải thống kê chuyển bàn: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ✅ MỚI: Xóa lịch sử cũ hơn 90 ngày (Admin only)
    private void xoaLichSuCu() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠   Xóa toàn bộ lịch sử chuyển & gộp bàn\n"  +
            "     cũ hơn 90 ngày?\n\n"                       +
            "Hành động này KHÔNG thể hoàn tác!",
            "Xác Nhận Xóa Lịch Sử Cũ",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (java.sql.Connection conn = config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM LichSuChuyenBan " +
                "WHERE NgayChuyen < DATE_SUB(CURDATE(), INTERVAL 90 DAY)")) {

            int deleted = ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                "Đã xóa thành công " + deleted + " bản ghi lịch sử cũ.",
                "Thành Công", JOptionPane.INFORMATION_MESSAGE);

        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xóa lịch sử cũ: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showStatusMessage(String message, Color color) {
        JLabel statusLabel = new JLabel(message);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBackground(color);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setBackground(color);
        statusPanel.add(statusLabel);

        JDialog statusDialog = new JDialog(this, false);
        statusDialog.setUndecorated(true);
        statusDialog.add(statusPanel);
        statusDialog.pack();

        int x = getX() + getWidth()  - statusDialog.getWidth()  - 20;
        int y = getY() + getHeight() - statusDialog.getHeight() - 60;
        statusDialog.setLocation(x, y);

        statusDialog.setVisible(true);

        Timer timer = new Timer(2000, e -> statusDialog.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    // ===================== EXISTING ACTION HANDLERS =====================

    private void openFormQuanLyNhanVien() {
        try {
            new FormQuanLyNhanVien().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Nhân Viên: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormQuanLyBan() {
        try {
            new FormQuanLyBan().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Bàn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormQuanLyNhomMon() {
        try {
            new FormQuanLyNhom().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Nhóm Món: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormQuanLyMonNuoc() {
        try {
            new FormThemMonAn().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Món Ăn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormQuanLyKhuVuc() {
        try {
            new FormQuanLyKhuVuc().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Khu Vực: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormQuanLyPhanQuyen() {
        try {
            new FormQuanLyPhanQuyen().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Phân Quyền: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormDoiMatKhau() {
        try {
            new FormQuenMatKhau(this).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Đổi Mật Khẩu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormCaLam() {
        try {
            new FormQuanLyCaLam().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Quản Lý Ca Làm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormPhanCong() {
        try {
            new FormPhanCongCaLam().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Phân Công Ca Làm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFormChamCong() {
        try {
            new FormChamCongNhanVien().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở Form Chấm Công: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn thoát ứng dụng?\n\n" +
                "Tất cả dữ liệu chưa lưu sẽ bị mất.",
                "Xác Nhận Thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void openHelpDialog() {
        try {
            new ModernHelpDialog(this).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở Hướng Dẫn Sử Dụng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openAboutDialog() {
        try {
            new ModernAboutDialog(this).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở Về Ứng Dụng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ===================== APP ICON =====================
    private Image createAppIcon() {
        int size = 32;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, PRIMARY_LIGHT, size, size, PRIMARY_DARK);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, size, size);

        g2d.setColor(PRIMARY_DARK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(0, 0, size - 1, size - 1);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("🍽", 6, 24);

        g2d.dispose();
        return image;
    }

    // ===================== MAIN =====================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}