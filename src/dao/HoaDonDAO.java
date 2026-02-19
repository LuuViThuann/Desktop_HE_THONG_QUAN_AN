package dao;

import config.DatabaseConfig;
import model.HoaDon;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class HoaDonDAO {

    /**
     * Lấy tất cả hóa đơn
     */
    public static List<HoaDon> getAllHoaDon() {
        List<HoaDon> list = new ArrayList<>();
        String query = "SELECT h.*, b.TenBan " +
                      "FROM hoadonkhachhang h " +
                      "LEFT JOIN ban b ON h.MaBan = b.MaBan " +
                      "ORDER BY h.NgayThanhToan DESC, h.MaCTHD DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                HoaDon hd = mapResultSetToHoaDon(rs);
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Lấy hóa đơn theo khoảng thời gian
     */
    public static List<HoaDon> getHoaDonByDateRange(Date fromDate, Date toDate) {
        List<HoaDon> list = new ArrayList<>();
        String query = "SELECT h.*, b.TenBan " +
                      "FROM hoadonkhachhang h " +
                      "LEFT JOIN ban b ON h.MaBan = b.MaBan " +
                      "WHERE h.NgayThanhToan BETWEEN ? AND ? " +
                      "ORDER BY h.NgayThanhToan DESC, h.MaCTHD DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, fromDate);
            pstmt.setDate(2, toDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HoaDon hd = mapResultSetToHoaDon(rs);
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Lấy hóa đơn hôm nay
     */
    public static List<HoaDon> getHoaDonToday() {
        List<HoaDon> list = new ArrayList<>();
        String query = "SELECT h.*, b.TenBan " +
                      "FROM hoadonkhachhang h " +
                      "LEFT JOIN ban b ON h.MaBan = b.MaBan " +
                      "WHERE DATE(h.NgayThanhToan) = CURDATE() " +
                      "ORDER BY h.MaCTHD DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                HoaDon hd = mapResultSetToHoaDon(rs);
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Lọc hóa đơn theo loại (Tại quán / Mang về)
     */
    public static List<HoaDon> getHoaDonByLoai(String loaiHoaDon) {
        List<HoaDon> list = new ArrayList<>();
        String query = "SELECT h.*, b.TenBan " +
                      "FROM hoadonkhachhang h " +
                      "LEFT JOIN ban b ON h.MaBan = b.MaBan " +
                      "WHERE h.LoaiHoaDon = ? " +
                      "ORDER BY h.NgayThanhToan DESC, h.MaCTHD DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, loaiHoaDon);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HoaDon hd = mapResultSetToHoaDon(rs);
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Tìm kiếm hóa đơn theo mã hóa đơn, mã bàn, tên bàn
     */
    public static List<HoaDon> searchHoaDon(String keyword) {
        List<HoaDon> list = new ArrayList<>();
        String query = "SELECT h.*, b.TenBan " +
                      "FROM hoadonkhachhang h " +
                      "LEFT JOIN ban b ON h.MaBan = b.MaBan " +
                      "WHERE h.MaCTHD LIKE ? " +
                      "   OR h.MaBan LIKE ? " +
                      "   OR b.TenBan LIKE ? " +
                      "ORDER BY h.NgayThanhToan DESC, h.MaCTHD DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HoaDon hd = mapResultSetToHoaDon(rs);
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Lấy chi tiết hóa đơn theo mã
     */
    public static HoaDon getHoaDonById(int maCTHD) {
        String query = "SELECT h.*, b.TenBan " +
                      "FROM hoadonkhachhang h " +
                      "LEFT JOIN ban b ON h.MaBan = b.MaBan " +
                      "WHERE h.MaCTHD = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCTHD);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToHoaDon(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Lấy danh sách món ăn trong hóa đơn
     */
    public static List<String> getChiTietMonAnByHoaDon(int maCTHD) {
        List<String> danhSachMon = new ArrayList<>();
        String query = "SELECT ct.SoLuong, m.TenMon, ct.GiaTien " +
                      "FROM chitiethoadon ct " +
                      "INNER JOIN monan m ON ct.MaMon = m.MaMon " +
                      "WHERE ct.MaCTHD = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCTHD);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int soLuong = rs.getInt("SoLuong");
                String tenMon = rs.getString("TenMon");
                BigDecimal giaTien = rs.getBigDecimal("GiaTien");
                
                String monInfo = String.format("%dx %s (%,d VND)", 
                    soLuong, tenMon, giaTien.longValue());
                danhSachMon.add(monInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return danhSachMon;
    }

    /**
     * Xóa hóa đơn (chỉ admin)
     */
    public static boolean deleteHoaDon(int maCTHD) {
        String deleteChiTiet = "DELETE FROM chitiethoadon WHERE MaCTHD = ?";
        String deleteHoaDon = "DELETE FROM hoadonkhachhang WHERE MaCTHD = ?";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // Xóa chi tiết trước
            PreparedStatement pstmt1 = conn.prepareStatement(deleteChiTiet);
            pstmt1.setInt(1, maCTHD);
            pstmt1.executeUpdate();
            
            // Xóa hóa đơn
            PreparedStatement pstmt2 = conn.prepareStatement(deleteHoaDon);
            pstmt2.setInt(1, maCTHD);
            pstmt2.executeUpdate();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thống kê doanh thu theo ngày
     */
    public static BigDecimal getTongDoanhThuByDate(Date date) {
        String query = "SELECT SUM(TongTienThanhToan) AS TongDoanhThu " +
                      "FROM hoadonkhachhang " +
                      "WHERE DATE(NgayThanhToan) = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, date);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("TongDoanhThu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Đếm số hóa đơn hôm nay
     */
    public static int countHoaDonToday() {
        String query = "SELECT COUNT(*) AS Total FROM hoadonkhachhang " +
                      "WHERE DATE(NgayThanhToan) = CURDATE()";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Helper: Map ResultSet sang HoaDon object
     */
    private static HoaDon mapResultSetToHoaDon(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaCTHD(rs.getInt("MaCTHD"));
        hd.setNgayThanhToan(rs.getDate("NgayThanhToan"));
        hd.setMaBan(rs.getInt("MaBan"));
        hd.setMaCTBH(rs.getInt("MaCTBH"));
        hd.setDanhSachMon(rs.getString("DanhSachMon"));
        hd.setTongSoLuongMon(rs.getInt("TongSoLuongMon"));
        hd.setTongTienThanhToan(rs.getBigDecimal("TongTienThanhToan"));
        hd.setPhanTramGiamGia(rs.getBigDecimal("PhanTramGiamGia"));
        hd.setLoaiHoaDon(rs.getString("LoaiHoaDon"));
        
        return hd;
    }
}