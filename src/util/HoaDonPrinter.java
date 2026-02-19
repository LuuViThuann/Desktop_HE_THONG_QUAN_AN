package util;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import config.DatabaseConfig;

/**
 * Class in hóa đơn sử dụng Java Print Service API
 * Hỗ trợ in hóa đơn cho bàn và mang về
 */
public class HoaDonPrinter implements Printable {
    
    private int maCTHD;
    private String loaiHoaDon; // "Tại quán" hoặc "Mang về"
    private java.util.List<String> lines;
    
    public HoaDonPrinter() {
        lines = new java.util.ArrayList<>();
    }
    
    /**
     * In hóa đơn cho bàn
     */
    public void printHoaDonBan(int maBan, Component parent) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Lấy thông tin hóa đơn từ bàn
            String query = "SELECT h.MaCTHD, h.NgayThanhToan, h.TongTienThanhToan, " +
                          "h.PhanTramGiamGia, b.TenBan " +
                          "FROM HoaDonKhachHang h " +
                          "LEFT JOIN Ban b ON h.MaBan = b.MaBan " +
                          "WHERE h.MaBan = ? " +
                          "ORDER BY h.MaCTHD DESC LIMIT 1";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                maCTHD = rs.getInt("MaCTHD");
                loaiHoaDon = "Tại quán";
                String tenBan = rs.getString("TenBan");
                
                buildHoaDonContent(tenBan);
                print(parent);
            } else {
                JOptionPane.showMessageDialog(parent, 
                    "Không tìm thấy hóa đơn để in!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, 
                "Lỗi khi lấy thông tin hóa đơn: " + e.getMessage());
        }
    }
    
    /**
     * In hóa đơn mang về
     */
    public void printHoaDonMangVe(int maCTHD, Component parent) {
        this.maCTHD = maCTHD;
        this.loaiHoaDon = "Mang về";
        
        buildHoaDonContent(null);
        print(parent);
    }
    
    /**
     * Xây dựng nội dung hóa đơn
     */
    private void buildHoaDonContent(String tenBan) {
        lines.clear();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Lấy thông tin hóa đơn
            String queryHD = "SELECT NgayThanhToan, TongTienThanhToan, " +
                           "PhanTramGiamGia, TongSoLuongMon, LoaiHoaDon " +
                           "FROM HoaDonKhachHang WHERE MaCTHD = ?";
            
            PreparedStatement pstmtHD = conn.prepareStatement(queryHD);
            pstmtHD.setInt(1, maCTHD);
            ResultSet rsHD = pstmtHD.executeQuery();
            
            if (!rsHD.next()) {
                lines.add("Không tìm thấy hóa đơn!");
                return;
            }
            
            Date ngayThanhToan = rsHD.getDate("NgayThanhToan");
            BigDecimal tongTien = rsHD.getBigDecimal("TongTienThanhToan");
            BigDecimal giamGia = rsHD.getBigDecimal("PhanTramGiamGia");
            int tongSoLuong = rsHD.getInt("TongSoLuongMon");
            String loaiHD = rsHD.getString("LoaiHoaDon");
            
            // Header
            lines.add("========================================");
            lines.add("          QUÁN ĂN ABC");
            lines.add("     Địa chỉ: 123 Đường XYZ");
            lines.add("       ĐT: 0123-456-789");
            lines.add("========================================");
            lines.add("");
            lines.add("           HÓA ĐƠN THANH TOÁN");
            lines.add("           (" + (loaiHD != null ? loaiHD : loaiHoaDon) + ")");
            lines.add("");
            lines.add("----------------------------------------");
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lines.add("Ngày: " + sdf.format(ngayThanhToan));
            lines.add("Số HĐ: " + maCTHD);
            
            if (tenBan != null) {
                lines.add("Bàn: " + tenBan);
            } else {
                lines.add("Loại: Mang về");
            }
            
            lines.add("----------------------------------------");
            lines.add("");
            
            // Chi tiết món
            String queryDetail = "SELECT m.TenMon, c.SoLuong, c.GiaTien, " +
                               "(c.SoLuong * c.GiaTien) AS ThanhTien " +
                               "FROM ChiTietHoaDon c " +
                               "JOIN MonAn m ON c.MaMon = m.MaMon " +
                               "WHERE c.MaCTHD = ?";
            
            PreparedStatement pstmtDetail = conn.prepareStatement(queryDetail);
            pstmtDetail.setInt(1, maCTHD);
            ResultSet rsDetail = pstmtDetail.executeQuery();
            
            lines.add("STT | Tên món              | SL | Thành tiền");
            lines.add("----------------------------------------");
            
            int stt = 1;
            BigDecimal tongTienGoc = BigDecimal.ZERO;
            
            while (rsDetail.next()) {
                String tenMon = rsDetail.getString("TenMon");
                int soLuong = rsDetail.getInt("SoLuong");
                BigDecimal thanhTien = rsDetail.getBigDecimal("ThanhTien");
                tongTienGoc = tongTienGoc.add(thanhTien);
                
                // Format tên món (max 20 ký tự)
                if (tenMon.length() > 20) {
                    tenMon = tenMon.substring(0, 17) + "...";
                }
                
                String line = String.format("%-3d | %-20s | %-2d | %,10d",
                    stt++, tenMon, soLuong, thanhTien.longValue());
                lines.add(line);
            }
            
            lines.add("========================================");
            lines.add("");
            
            // Tính toán
            BigDecimal tienGiam = tongTienGoc.multiply(giamGia)
                                            .divide(new BigDecimal(100));
            
            lines.add(String.format("Tổng số lượng: %d món", tongSoLuong));
            lines.add(String.format("Tổng tiền:         %,13d VND", tongTienGoc.longValue()));
            
            if (giamGia.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(String.format("Giảm giá (%s%%):    -%,12d VND", 
                    giamGia.toString(), tienGiam.longValue()));
            }
            
            lines.add("----------------------------------------");
            lines.add(String.format("THÀNH TIỀN:        %,13d VND", tongTien.longValue()));
            lines.add("========================================");
            lines.add("");
            lines.add("      Cảm ơn quý khách!");
            lines.add("         Hẹn gặp lại!");
            lines.add("");
            lines.add("========================================");
            
        } catch (SQLException e) {
            lines.add("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Thực hiện in
     */
    private void print(Component parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        // Thiết lập kích thước giấy (80mm - giấy in hóa đơn nhiệt)
        PageFormat pageFormat = job.defaultPage();
        Paper paper = new Paper();
        
        // 80mm = ~227 points, chiều cao tùy ý
        double width = 227;
        double height = 842; // A4 height
        
        paper.setSize(width, height);
        paper.setImageableArea(5, 5, width - 10, height - 10);
        pageFormat.setPaper(paper);
        
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(parent,
                    "In hóa đơn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(parent,
                    "Lỗi khi in: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Implement phương thức print từ Printable
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
            throws PrinterException {
        
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Font cho in
        Font font = new Font("Monospaced", Font.PLAIN, 9);
        Font boldFont = new Font("Monospaced", Font.BOLD, 9);
        
        g2d.setFont(font);
        
        int y = 20;
        int lineHeight = 12;
        
        for (String line : lines) {
            // In đậm các dòng quan trọng
            if (line.contains("HÓA ĐƠN") || line.contains("THÀNH TIỀN") || 
                line.contains("QUÁN ĂN")) {
                g2d.setFont(boldFont);
            } else {
                g2d.setFont(font);
            }
            
            g2d.drawString(line, 10, y);
            y += lineHeight;
        }
        
        return PAGE_EXISTS;
    }
}