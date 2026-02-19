package dao;

import config.DatabaseConfig;
import model.DatBanTruoc;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class DatBanTruocDAO {
    
    // Lấy tất cả lịch sử đặt bàn
    public static List<DatBanTruoc> getAllDatBan() {
        List<DatBanTruoc> list = new ArrayList<>();
        String query = "SELECT d.*, b.TenBan, k.TenKV " +
                      "FROM DatBanTruoc d " +
                      "JOIN Ban b ON d.MaBan = b.MaBan " +
                      "JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "ORDER BY d.NgayDat DESC, d.GioDat DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                DatBanTruoc datBan = mapResultSet(rs);
                list.add(datBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Lấy đặt bàn theo trạng thái
    public static List<DatBanTruoc> getDatBanByTrangThai(String trangThai) {
        List<DatBanTruoc> list = new ArrayList<>();
        String query = "SELECT d.*, b.TenBan, k.TenKV " +
                      "FROM DatBanTruoc d " +
                      "JOIN Ban b ON d.MaBan = b.MaBan " +
                      "JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "WHERE d.TrangThai = ? " +
                      "ORDER BY d.NgayDat DESC, d.GioDat DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, trangThai);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DatBanTruoc datBan = mapResultSet(rs);
                list.add(datBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm theo tên khách hàng hoặc SĐT
    public static List<DatBanTruoc> searchDatBan(String keyword) {
        List<DatBanTruoc> list = new ArrayList<>();
        String query = "SELECT d.*, b.TenBan, k.TenKV " +
                      "FROM DatBanTruoc d " +
                      "JOIN Ban b ON d.MaBan = b.MaBan " +
                      "JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "WHERE d.HoTenKhachHang LIKE ? OR d.SDT LIKE ? " +
                      "ORDER BY d.NgayDat DESC, d.GioDat DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DatBanTruoc datBan = mapResultSet(rs);
                list.add(datBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm theo ngày
    public static List<DatBanTruoc> getDatBanByDate(LocalDate ngay) {
        List<DatBanTruoc> list = new ArrayList<>();
        String query = "SELECT d.*, b.TenBan, k.TenKV " +
                      "FROM DatBanTruoc d " +
                      "JOIN Ban b ON d.MaBan = b.MaBan " +
                      "JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "WHERE d.NgayDat = ? " +
                      "ORDER BY d.GioDat";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(ngay));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DatBanTruoc datBan = mapResultSet(rs);
                list.add(datBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Lấy đặt bàn theo khoảng thời gian
    public static List<DatBanTruoc> getDatBanByDateRange(LocalDate tuNgay, LocalDate denNgay) {
        List<DatBanTruoc> list = new ArrayList<>();
        String query = "SELECT d.*, b.TenBan, k.TenKV " +
                      "FROM DatBanTruoc d " +
                      "JOIN Ban b ON d.MaBan = b.MaBan " +
                      "JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "WHERE d.NgayDat BETWEEN ? AND ? " +
                      "ORDER BY d.NgayDat DESC, d.GioDat DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(tuNgay));
            pstmt.setDate(2, Date.valueOf(denNgay));
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DatBanTruoc datBan = mapResultSet(rs);
                list.add(datBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Cập nhật trạng thái đặt bàn
    public static boolean updateTrangThai(int maDatBan, String trangThaiMoi) {
        String query = "UPDATE DatBanTruoc SET TrangThai = ? WHERE MaDatBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, trangThaiMoi);
            pstmt.setInt(2, maDatBan);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Xác nhận khách đã đến
    public static boolean xacNhanKhachDen(int maDatBan) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Cập nhật trạng thái đặt bàn
            String updateDatBan = "UPDATE DatBanTruoc SET TrangThai = 'Đã đến' WHERE MaDatBan = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(updateDatBan);
            pstmt1.setInt(1, maDatBan);
            pstmt1.executeUpdate();
            
            // 2. Cập nhật trạng thái bàn sang "Đang sử dụng"
            String updateBan = "UPDATE Ban b " +
                             "JOIN DatBanTruoc d ON b.MaBan = d.MaBan " +
                             "SET b.TrangThai = 'Đang sử dụng' " +
                             "WHERE d.MaDatBan = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateBan);
            pstmt2.setInt(1, maDatBan);
            pstmt2.executeUpdate();
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy chi tiết một đặt bàn
    public static DatBanTruoc getDatBanById(int maDatBan) {
        String query = "SELECT d.*, b.TenBan, k.TenKV " +
                      "FROM DatBanTruoc d " +
                      "JOIN Ban b ON d.MaBan = b.MaBan " +
                      "JOIN KhuVucQuan k ON b.MaKV = k.MaKV " +
                      "WHERE d.MaDatBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maDatBan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Thống kê số lượng đặt bàn theo trạng thái
    public static int countByTrangThai(String trangThai) {
        String query = "SELECT COUNT(*) FROM DatBanTruoc WHERE TrangThai = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, trangThai);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // Thống kê đặt bàn hôm nay
    public static List<DatBanTruoc> getDatBanHomNay() {
        return getDatBanByDate(LocalDate.now());
    }
    
    // Map ResultSet to DatBanTruoc object
    private static DatBanTruoc mapResultSet(ResultSet rs) throws SQLException {
        DatBanTruoc datBan = new DatBanTruoc();
        datBan.setMaDatBan(rs.getInt("MaDatBan"));
        datBan.setHoTenKhachHang(rs.getString("HoTenKhachHang"));
        datBan.setSdt(rs.getString("SDT"));
        datBan.setSoLuongKhach(rs.getInt("SoLuongKhach"));
        datBan.setNgayDat(rs.getDate("NgayDat"));
        datBan.setGioDat(rs.getTime("GioDat"));
        datBan.setMaBan(rs.getInt("MaBan"));
        datBan.setTrangThai(rs.getString("TrangThai"));
        datBan.setTenBan(rs.getString("TenBan"));
        datBan.setTenKV(rs.getString("TenKV"));
        return datBan;
    }
}