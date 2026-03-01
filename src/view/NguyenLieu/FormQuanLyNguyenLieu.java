package view.NguyenLieu;

import dao.NguyenLieuDAO;
import model.NguyenLieu;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class FormQuanLyNguyenLieu extends JFrame {

    private static final Color PRIMARY_DARK = new Color(25, 45, 85);

    public FormQuanLyNguyenLieu() {
        setTitle("QUẢN LÝ NGUYÊN LIỆU - Restaurant Manager Pro");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);

        // Tab 1: Danh mục nguyên liệu
        tabs.addTab("Danh Mục Nguyên Liệu", new FormDanhMucNguyenLieu());

        // Tab 2: Công thức món
        tabs.addTab("Công Thức Món", new FormCongThucMon());

        // Tab 3: Nhập hàng
        tabs.addTab("Nhập Hàng", new FormNhapHang());

        // Tab 4: Xuất kho
        tabs.addTab("Xuất Kho", new FormXuatKho());

        // Tab 5: Báo cáo & phân tích
        tabs.addTab("Báo Cáo & Phân Tích", new FormBaoCaoNguyenLieu());

        // Badge cảnh báo trên tab Báo Cáo nếu có NL sắp hết
        SwingUtilities.invokeLater(() -> {
            List<NguyenLieu> canhBao = NguyenLieuDAO.getCanhBao();
            if (!canhBao.isEmpty()) {
                tabs.setTitleAt(4, "📊 Báo Cáo ⚠" + canhBao.size());
                tabs.setForegroundAt(4, new Color(192, 57, 43));
            }
        });

        add(tabs);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
        statusBar.setBackground(PRIMARY_DARK);
        JLabel statusLbl = new JLabel("🏪  Hệ Thống Quản Lý Nguyên Liệu - v1.0");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLbl.setForeground(new Color(180, 200, 230));
        statusBar.add(statusLbl);
        add(statusBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Phiên bản dùng để nhúng vào MainFrame (trả về JPanel)
     */
    public static JPanel createPanel() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("Danh Mục Nguyên Liệu",       new FormDanhMucNguyenLieu());
        tabs.addTab("Công Thức Món",      new FormCongThucMon());
        tabs.addTab("Nhập Hàng",          new FormNhapHang());
        tabs.addTab("Xuất Kho",           new FormXuatKho());
        tabs.addTab("Báo Cáo & Phân Tích", new FormBaoCaoNguyenLieu());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(tabs);
        return wrapper;
    }
}