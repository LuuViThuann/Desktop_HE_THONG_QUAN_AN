package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import config.DatabaseConfig;
import config.UserSession;
import util.HoaDonPrinter;
import model.NhanVien;


public class HoaDonPreviewFrame extends JDialog {

    private static final long serialVersionUID = 1L;
    private int maCTHD;
    private String loaiHoaDon;
    private JPanel contentPanel;
    
  
    private String ghiChu;
    
    /**
     * Xem trước hóa đơn bàn
     */
    public HoaDonPreviewFrame(JFrame parent, int maBan) {
        super(parent, "Hóa đơn thanh toán", true);
        this.loaiHoaDon = "Tại quán";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT h.MaCTHD FROM HoaDonKhachHang h " +
                          "WHERE h.MaBan = ? ORDER BY h.MaCTHD DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                this.maCTHD = rs.getInt("MaCTHD");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        initComponents();
        buildPreviewContent();
    }
    
    /**
     * Xem trước hóa đơn mang về
     */
    public HoaDonPreviewFrame(JFrame parent, int maCTHD, String loaiHD) {
        super(parent, "Hóa đơn thanh toán", true);
        this.maCTHD = maCTHD;
        this.loaiHoaDon = loaiHD;
        this.ghiChu = getGhiChuFromDB(maCTHD);  // ← LẤY GHI CHÚ TỪ DATABASE
        
        initComponents();
        buildPreviewContent();
    }
    
    // ✅ PHƯƠNG THỨC LẤY GHI CHÚ TỪ DATABASE
    private String getGhiChuFromDB(int maCTHD) {
        String query = "SELECT DanhSachMon FROM HoaDonKhachHang WHERE MaCTHD = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, maCTHD);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String ghiChu = rs.getString("DanhSachMon");
                return ghiChu != null ? ghiChu : "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    private void initComponents() {
    	setSize(380, 900);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(44, 62, 80));

        // ── Panel trắng chính (giấy in)
        JPanel paperPanel = new JPanel();
        paperPanel.setLayout(new BorderLayout());  // Dùng BorderLayout để dễ center
        paperPanel.setBackground(Color.WHITE);
        paperPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)  // Tăng padding đều 4 cạnh
        ));

        // ── Content bên trong paper (dùng BoxLayout Y + center)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bọc contentPanel vào một panel trung gian để center ngang
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.add(contentPanel);

        paperPanel.add(centerWrapper, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(paperPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(44, 62, 80));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        buttonPanel.setBackground(new Color(44, 62, 80));
        
        JButton btnPrint = new JButton("In Hóa Đơn");
        btnPrint.setFont(new Font("Arial", Font.BOLD, 13));
        btnPrint.setBackground(new Color(52, 152, 219));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setBorderPainted(false);
        btnPrint.setPreferredSize(new Dimension(140, 40));
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrint.addActionListener(e -> printHoaDon());
        
        btnPrint.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnPrint.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(MouseEvent e) {
                btnPrint.setBackground(new Color(52, 152, 219));
            }
        });
        
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        btnClose.setBackground(new Color(231, 76, 60));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setPreferredSize(new Dimension(140, 40));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnClose.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(MouseEvent e) {
                btnClose.setBackground(new Color(231, 76, 60));
            }
        });
        
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnClose);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void buildPreviewContent() {
        contentPanel.removeAll();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Lấy thông tin hóa đơn
            String queryHD = "SELECT h.NgayThanhToan, h.TongTienThanhToan, " +
                           "h.PhanTramGiamGia, h.TongSoLuongMon, h.LoaiHoaDon, " +
                           "h.MaBan, b.TenBan " +
                           "FROM HoaDonKhachHang h " +
                           "LEFT JOIN Ban b ON h.MaBan = b.MaBan " +
                           "WHERE h.MaCTHD = ?";
            
            PreparedStatement pstmtHD = conn.prepareStatement(queryHD);
            pstmtHD.setInt(1, maCTHD);
            ResultSet rsHD = pstmtHD.executeQuery();
            
            if (!rsHD.next()) {
                addLabel("Không tìm thấy hóa đơn!", Font.PLAIN, 12, Color.RED, JLabel.CENTER);
                return;
            }
            
            Date ngayThanhToan = rsHD.getDate("NgayThanhToan");
            BigDecimal tongTien = rsHD.getBigDecimal("TongTienThanhToan");
            BigDecimal phanTramGiamGia = rsHD.getBigDecimal("PhanTramGiamGia");
            int tongSoLuong = rsHD.getInt("TongSoLuongMon");
            String loaiHD = rsHD.getString("LoaiHoaDon");
            String tenBan = rsHD.getString("TenBan");
            
            // TÍNH TOÁN: Lấy tổng tiền gốc từ chi tiết
            BigDecimal tongTienGoc = BigDecimal.ZERO;
            BigDecimal tienGiamGiaActual = BigDecimal.ZERO;
            
            if (phanTramGiamGia != null && phanTramGiamGia.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heSo = BigDecimal.ONE.subtract(
                    phanTramGiamGia.divide(new BigDecimal(100), 10, java.math.RoundingMode.HALF_UP)
                );
                tongTienGoc = tongTien.divide(heSo, 0, java.math.RoundingMode.HALF_UP);
                tienGiamGiaActual = tongTienGoc.subtract(tongTien);
            } else {
                tongTienGoc = tongTien;
                phanTramGiamGia = BigDecimal.ZERO;
                tienGiamGiaActual = BigDecimal.ZERO;
            }
            
            // Lấy thông tin công ty/quán
            NhanVien currentUser = UserSession.getInstance().getCurrentUser();
            String tenNhanVien = currentUser != null ? currentUser.getHoTen() : "Admin";
            
            // ============ HEADER - CÂN BẰNG VÀ ĐẸP ============
            addLabel("QUÁN LẨU THÁI TI-TI", Font.BOLD, 20, new Color(231, 76, 60), JLabel.CENTER);
            addSpace(4);

            addLabel("Chuyên lẩu Thái – hải sản", Font.ITALIC, 11, new Color(52, 73, 94), JLabel.CENTER);
            addLabel("Phục vụ tại chỗ & mang về", Font.ITALIC, 11, new Color(52, 73, 94), JLabel.CENTER);
            addSpace(8);

            addLabel("ĐC: 123 Nguyễn Văn Cừ, P. An Hòa, Q. Ninh Kiều, Cần Thơ", Font.PLAIN, 10, new Color(52, 73, 94), JLabel.CENTER);
            addLabel("ĐT: 0909 123 456", Font.PLAIN, 10, new Color(52, 73, 94), JLabel.CENTER);

            addSpace(16);
            addSeparator();
            addSpace(14);

            addLabel("HÓA ĐƠN THANH TOÁN", Font.BOLD, 18, new Color(231, 76, 60), JLabel.CENTER);
            addSpace(20);
            
            // ============ THÔNG TIN HÓA ĐƠN - CĂN GIỮA ============
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.setMaximumSize(new Dimension(350, 100));
            
            addInfoRowCenter(infoPanel, "Số:", String.valueOf(maCTHD), true);
            addInfoRowCenter(infoPanel, "Ngày:", sdf.format(ngayThanhToan), false);
            addInfoRowCenter(infoPanel, "Bàn:", tenBan != null ? tenBan : "---", false);
            addInfoRowCenter(infoPanel, "Khách:", "Khách lẻ", false);
            
            contentPanel.add(infoPanel);
            addSpace(12);
            
            // ============ BẢNG CHI TIẾT ============
            // Header bảng
            JPanel tableHeader = new JPanel(new GridLayout(1, 4, 0, 0));
            tableHeader.setBackground(new Color(52, 152, 219));
            tableHeader.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
            tableHeader.setAlignmentX(Component.CENTER_ALIGNMENT);   // ← thêm dòng này
            tableHeader.setMaximumSize(new Dimension(340, 32));      // Giảm nhẹ width để vừa
            
            String[] headers = {"Món", "SL", "ĐG", "TT"};
            
            for (String header : headers) {
                JLabel lblHeader = new JLabel(header, JLabel.CENTER);
                lblHeader.setFont(new Font("Arial", Font.BOLD, 11));
                lblHeader.setForeground(Color.BLACK);
                lblHeader.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 3));
                tableHeader.add(lblHeader);
            }
            
            contentPanel.add(tableHeader);
            
            // Chi tiết món
            String queryDetail = "SELECT m.TenMon, c.SoLuong, c.GiaTien, " +
                               "(c.SoLuong * c.GiaTien) AS ThanhTien " +
                               "FROM ChiTietHoaDon c " +
                               "JOIN MonAn m ON c.MaMon = m.MaMon " +
                               "WHERE c.MaCTHD = ?";
            
            PreparedStatement pstmtDetail = conn.prepareStatement(queryDetail);
            pstmtDetail.setInt(1, maCTHD);
            ResultSet rsDetail = pstmtDetail.executeQuery();
            
            int stt = 1;
            
            while (rsDetail.next()) {
                String tenMon = rsDetail.getString("TenMon");
                int soLuong = rsDetail.getInt("SoLuong");
                BigDecimal donGia = rsDetail.getBigDecimal("GiaTien");
                BigDecimal thanhTien = rsDetail.getBigDecimal("ThanhTien");
                
                JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
                row.setBackground(stt % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                row.setBorder(BorderFactory.createMatteBorder(0, 2, 1, 2, new Color(189, 195, 199)));
                row.setAlignmentX(Component.CENTER_ALIGNMENT);           // ← thêm
                row.setMaximumSize(new Dimension(340, 28));
                
                addTableCell(row, tenMon, JLabel.LEFT, 10);
                addTableCell(row, String.valueOf(soLuong), JLabel.CENTER, 10);
                addTableCell(row, String.format("%,d", donGia.longValue()), JLabel.RIGHT, 10);
                addTableCell(row, String.format("%,d", thanhTien.longValue()), JLabel.RIGHT, 10);
                
                contentPanel.add(row);
                stt++;
            }
            
            addSpace(12);
            
            // ============ TỔNG CỘNG ============
            JPanel totalPanel = new JPanel();
            totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
            totalPanel.setBackground(Color.WHITE);
            totalPanel.setAlignmentX(Component.CENTER_ALIGNMENT);    // ← sửa quan trọng
            totalPanel.setMaximumSize(new Dimension(340, 140));
            
            //  HIỂN THỊ CHI TIẾT GIẢM GIÁ
            addTotalRow(totalPanel, "Tổng tiền:", 
                String.format("%,d đ", tongTienGoc.longValue()), false);
            addTotalRow(totalPanel, "Giảm giá:", 
                String.format("%.1f%%", phanTramGiamGia.doubleValue()), false);
            addTotalRow(totalPanel, "Tiền giảm:", 
                String.format("%,d đ", tienGiamGiaActual.longValue()), false);
            
            addSpace(totalPanel, 8);
            
            addTotalRow(totalPanel, "Phải thu:", 
                String.format("%,d đ", tongTien.longValue()), true);
            
            addSpace(totalPanel, 6);
            
            // Số tiền bằng chữ
            JPanel chuviPanel = new JPanel(new BorderLayout());
            chuviPanel.setBackground(Color.WHITE);
            chuviPanel.setMaximumSize(new Dimension(350, 24));
            chuviPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel lblChuViLabel = new JLabel("Bằng chữ:");
            lblChuViLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            
            JLabel lblChuViValue = new JLabel(convertNumberToWords(tongTien.longValue()), JLabel.RIGHT);
            lblChuViValue.setFont(new Font("Arial", Font.ITALIC, 10));
            lblChuViValue.setForeground(new Color(52, 73, 94));
            
            chuviPanel.add(lblChuViLabel, BorderLayout.WEST);
            chuviPanel.add(lblChuViValue, BorderLayout.EAST);
            
            totalPanel.add(chuviPanel);
            
            contentPanel.add(totalPanel);
            
            addSpace(12);
            
            //  Ghi chú - Hiển thị từ database nếu có
            JTextArea txtGhiChu = new JTextArea(3, 40);
            String ghiChuDisplay = (ghiChu != null && !ghiChu.isEmpty()) ? ghiChu : 
                "Quý khách vui lòng kiểm tra kỹ hàng hóa trước khi thanh toán.\nXin cảm ơn và hẹn gặp lại!";
            txtGhiChu.setText(ghiChuDisplay);
            txtGhiChu.setFont(new Font("Arial", Font.PLAIN, 11));
            txtGhiChu.setLineWrap(true);
            txtGhiChu.setWrapStyleWord(true);
            txtGhiChu.setEditable(false);
            txtGhiChu.setBackground(new Color(255, 250, 240));
            txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(243, 156, 18), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            txtGhiChu.setAlignmentX(Component.CENTER_ALIGNMENT);
            txtGhiChu.setMaximumSize(new Dimension(340, 65));
            contentPanel.add(txtGhiChu);
            
            addSpace(25);  // Tạo khoảng trắng đẹp trước chữ ký

            JPanel signMainPanel = new JPanel();
            signMainPanel.setLayout(new BoxLayout(signMainPanel, BoxLayout.Y_AXIS));
            signMainPanel.setBackground(Color.WHITE);
            signMainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            signMainPanel.setMaximumSize(new Dimension(340, 120));

            // Panel ngang chứa 2 cột chữ ký
            JPanel signColumns = new JPanel(new GridLayout(1, 2, 60, 0));  // gap 60 để hai bên cách xa, cân đối
            signColumns.setBackground(Color.WHITE);
            signColumns.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Cột trái: Người lập + tên
            JPanel leftColumn = new JPanel();
            leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
            leftColumn.setBackground(Color.WHITE);
            leftColumn.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblNguoiLap = new JLabel("Người lập", JLabel.CENTER);
            lblNguoiLap.setFont(new Font("Arial", Font.ITALIC, 10));
            lblNguoiLap.setForeground(new Color(80, 80, 80));
            lblNguoiLap.setAlignmentX(Component.CENTER_ALIGNMENT);

            leftColumn.add(lblNguoiLap);
            leftColumn.add(Box.createVerticalStrut(35));  // Khoảng trắng cho chữ ký (dòng kẻ tay)

            JLabel lblTenLap = new JLabel(tenNhanVien, JLabel.CENTER);  // tenNhanVien từ UserSession
            lblTenLap.setFont(new Font("Arial", Font.BOLD, 12));
            lblTenLap.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftColumn.add(lblTenLap);

            // Cột phải: Thu ngân (thường để trống tên)
            JPanel rightColumn = new JPanel();
            rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
            rightColumn.setBackground(Color.WHITE);
            rightColumn.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblThuNgan = new JLabel("Thu ngân", JLabel.CENTER);
            lblThuNgan.setFont(new Font("Arial", Font.ITALIC, 10));
            lblThuNgan.setForeground(new Color(80, 80, 80));
            lblThuNgan.setAlignmentX(Component.CENTER_ALIGNMENT);

            rightColumn.add(lblThuNgan);
            rightColumn.add(Box.createVerticalStrut(35));  // Đồng bộ khoảng trắng

            // Không thêm tên thu ngân (để khách ký tay hoặc in dấu)

            signColumns.add(leftColumn);
            signColumns.add(rightColumn);

            signMainPanel.add(signColumns);

          

            contentPanel.add(signMainPanel);

            // Footer quán - căn giữa
            addSpace(12);
            
            // ============ FOOTER ============
            addSeparator();
            addSpace(8);
            JLabel lblFooter = new JLabel("Quán lẩu Thái TiTi - TP Cần Thơ", JLabel.CENTER);
            lblFooter.setFont(new Font("Arial", Font.ITALIC, 8));
            lblFooter.setForeground(new Color(127, 140, 141));
            lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(lblFooter);
            
        } catch (SQLException e) {
            addLabel("Lỗi: " + e.getMessage(), Font.PLAIN, 10, Color.RED, JLabel.CENTER);
            e.printStackTrace();
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    // ============ HELPER METHODS ============
    
    private void addLabel(String text, int fontStyle, int fontSize, Color color, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(label);
    }
    
    private void addSpace(int height) {
        contentPanel.add(Box.createRigidArea(new Dimension(0, height)));
    }
    
    private void addSpace(JPanel panel, int height) {
        panel.add(Box.createRigidArea(new Dimension(0, height)));
    }
    
    private void addSeparator() {
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(350, 2));
        separator.setForeground(new Color(189, 195, 199));
        separator.setBackground(new Color(189, 195, 199));
        contentPanel.add(separator);
    }
    
    private void addInfoRow(JPanel panel, String label, String value, boolean isRed) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        if (isRed) {
            lblLabel.setForeground(new Color(231, 76, 60));
        } else {
            lblLabel.setForeground(new Color(52, 73, 94));
        }
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", isRed ? Font.BOLD : Font.PLAIN, 11));
        if (isRed) {
            lblValue.setForeground(new Color(231, 76, 60));
        } else {
            lblValue.setForeground(new Color(52, 73, 94));
        }
        
        panel.add(lblLabel);
        panel.add(lblValue);
    }
    
    private void addInfoRowCenter(JPanel panel, String label, String value, boolean isRed) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));  // Tăng gap
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(340, 26));  // Quan trọng: width cố định

        JLabel lblLabel = new JLabel(label + " ");
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lblLabel.setForeground(isRed ? new Color(231, 76, 60) : new Color(52, 73, 94));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", isRed ? Font.BOLD : Font.PLAIN, 11));
        lblValue.setForeground(isRed ? new Color(231, 76, 60) : new Color(52, 73, 94));

        row.add(lblLabel);
        row.add(lblValue);
        panel.add(row);
    }
    
    private void addTableCell(JPanel row, String text, int alignment, int fontSize) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(new Font("Arial", Font.PLAIN, fontSize));
        label.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        label.setForeground(new Color(44, 62, 80));
        row.add(label);
    }
    
    private void addTotalRow(JPanel panel, String label, String value, boolean isBold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(350, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, isBold ? 12 : 10));
        lblLabel.setForeground(isBold ? new Color(231, 76, 60) : new Color(52, 73, 94));
        
        JLabel lblValue = new JLabel(value, JLabel.RIGHT);
        lblValue.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, isBold ? 12 : 10));
        lblValue.setForeground(isBold ? new Color(231, 76, 60) : new Color(52, 73, 94));
        
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        
        panel.add(row);
    }
    
    private String convertNumberToWords(long number) {
        if (number == 0) return "Không đồng";
        
        String[] ones = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
        String[] tens = {"", "mười", "hai mươi", "ba mươi", "bốn mươi", "năm mươi", 
                        "sáu mươi", "bảy mươi", "tám mươi", "chín mươi"};
        
        if (number < 10) return Character.toUpperCase(ones[(int)number].charAt(0)) + 
                               ones[(int)number].substring(1) + " đồng";
        if (number < 100) {
            int ten = (int)(number / 10);
            int one = (int)(number % 10);
            String result = tens[ten];
            if (one > 0) result += " " + ones[one];
            return Character.toUpperCase(result.charAt(0)) + result.substring(1) + " đồng";
        }
        
        // Chuyển đổi số lớn
        long trieu = number / 1000000;
        long nghin = (number % 1000000) / 1000;
        long tram = (number % 1000) / 100;
        long chuc = (number % 100) / 10;
        long donvi = number % 10;
        
        String result = "";
        
        if (trieu > 0) {
            result += trieu + " triệu ";
        }
        
        if (nghin > 0) {
            if (nghin < 10) {
                result += "không trăm " + ones[(int)nghin] + " nghìn ";
            } else if (nghin < 100) {
                int nTen = (int)(nghin / 10);
                int nOne = (int)(nghin % 10);
                result += tens[nTen];
                if (nOne > 0) result += " " + ones[nOne];
                result += " nghìn ";
            } else {
                result += nghin + " nghìn ";
            }
        }
        
        if (tram > 0) {
            result += ones[(int)tram] + " trăm ";
        }
        
        if (chuc > 0) {
            result += tens[(int)chuc] + " ";
        }
        
        if (donvi > 0) {
            result += ones[(int)donvi] + " ";
        }
        
        result += "đồng";
        result = result.trim();
        
        // Viết hoa chữ cái đầu
        if (result.length() > 0) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        
        return result;
    }
    
    private void printHoaDon() {
        HoaDonPrinter printer = new HoaDonPrinter();
        if ("Mang về".equals(loaiHoaDon)) {
            printer.printHoaDonMangVe(maCTHD, this);
        } else {
            // Lấy maBan từ database
            try (Connection conn = DatabaseConfig.getConnection()) {
                String query = "SELECT MaBan FROM HoaDonKhachHang WHERE MaCTHD = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, maCTHD);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next() && rs.getObject("MaBan") != null) {
                    int maBan = rs.getInt("MaBan");
                    printer.printHoaDonBan(maBan, this);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            }
        }
    }
}