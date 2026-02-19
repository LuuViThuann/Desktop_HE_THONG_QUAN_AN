package dao;

import config.DatabaseConfig;
import model.PhanCongCaLam;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhanCongCaLamDAO {
    
    // Lấy tất cả phân công ca làm
    public static List<PhanCongCaLam> getAllPhanCong() {
        List<PhanCongCaLam> list = new ArrayList<>();
        String query = "SELECT pc.*, nv.HoTen, c.TenCa, c.GioBatDau, c.GioKetThuc " +
                      "FROM PhanCongCaLam pc " +
                      "JOIN NhanVien nv ON pc.MaNV = nv.MaNV " +
                      "JOIN CaLam c ON pc.MaCa = c.MaCa " +
                      "ORDER BY pc.NgayLam DESC, c.GioBatDau";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                PhanCongCaLam pc = new PhanCongCaLam();
                pc.setMaPhanCong(rs.getInt("MaPhanCong"));
                pc.setMaNV(rs.getInt("MaNV"));
                pc.setMaCa(rs.getInt("MaCa"));
                pc.setNgayLam(rs.getDate("NgayLam").toLocalDate());
                pc.setHoTenNV(rs.getString("HoTen"));
                pc.setTenCa(rs.getString("TenCa"));
                pc.setGioBatDau(rs.getTime("GioBatDau").toString());
                pc.setGioKetThuc(rs.getTime("GioKetThuc").toString());
                list.add(pc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm phân công theo ngày
    public static List<PhanCongCaLam> searchByDate(LocalDate ngayLam) {
        List<PhanCongCaLam> list = new ArrayList<>();
        String query = "SELECT pc.*, nv.HoTen, c.TenCa, c.GioBatDau, c.GioKetThuc " +
                      "FROM PhanCongCaLam pc " +
                      "JOIN NhanVien nv ON pc.MaNV = nv.MaNV " +
                      "JOIN CaLam c ON pc.MaCa = c.MaCa " +
                      "WHERE pc.NgayLam = ? " +
                      "ORDER BY c.GioBatDau";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(ngayLam));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PhanCongCaLam pc = new PhanCongCaLam();
                pc.setMaPhanCong(rs.getInt("MaPhanCong"));
                pc.setMaNV(rs.getInt("MaNV"));
                pc.setMaCa(rs.getInt("MaCa"));
                pc.setNgayLam(rs.getDate("NgayLam").toLocalDate());
                pc.setHoTenNV(rs.getString("HoTen"));
                pc.setTenCa(rs.getString("TenCa"));
                pc.setGioBatDau(rs.getTime("GioBatDau").toString());
                pc.setGioKetThuc(rs.getTime("GioKetThuc").toString());
                list.add(pc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm phân công theo tên nhân viên
    public static List<PhanCongCaLam> searchByNhanVien(String keyword) {
        List<PhanCongCaLam> list = new ArrayList<>();
        String query = "SELECT pc.*, nv.HoTen, c.TenCa, c.GioBatDau, c.GioKetThuc " +
                      "FROM PhanCongCaLam pc " +
                      "JOIN NhanVien nv ON pc.MaNV = nv.MaNV " +
                      "JOIN CaLam c ON pc.MaCa = c.MaCa " +
                      "WHERE nv.HoTen LIKE ? " +
                      "ORDER BY pc.NgayLam DESC, c.GioBatDau";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PhanCongCaLam pc = new PhanCongCaLam();
                pc.setMaPhanCong(rs.getInt("MaPhanCong"));
                pc.setMaNV(rs.getInt("MaNV"));
                pc.setMaCa(rs.getInt("MaCa"));
                pc.setNgayLam(rs.getDate("NgayLam").toLocalDate());
                pc.setHoTenNV(rs.getString("HoTen"));
                pc.setTenCa(rs.getString("TenCa"));
                pc.setGioBatDau(rs.getTime("GioBatDau").toString());
                pc.setGioKetThuc(rs.getTime("GioKetThuc").toString());
                list.add(pc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Thêm phân công ca làm
    public static boolean insertPhanCong(PhanCongCaLam pc) {
        // Kiểm tra trùng lặp
        if (isDuplicate(pc.getMaNV(), pc.getMaCa(), pc.getNgayLam())) {
            return false;
        }
        
        String query = "INSERT INTO PhanCongCaLam (MaNV, MaCa, NgayLam) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pc.getMaNV());
            pstmt.setInt(2, pc.getMaCa());
            pstmt.setDate(3, Date.valueOf(pc.getNgayLam()));
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Cập nhật phân công ca làm
    public static boolean updatePhanCong(PhanCongCaLam pc) {
        String query = "UPDATE PhanCongCaLam SET MaNV = ?, MaCa = ?, NgayLam = ? WHERE MaPhanCong = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pc.getMaNV());
            pstmt.setInt(2, pc.getMaCa());
            pstmt.setDate(3, Date.valueOf(pc.getNgayLam()));
            pstmt.setInt(4, pc.getMaPhanCong());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Xóa phân công ca làm
    public static boolean deletePhanCong(int maPhanCong) {
        String query = "DELETE FROM PhanCongCaLam WHERE MaPhanCong = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maPhanCong);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Kiểm tra trùng lặp phân công
    private static boolean isDuplicate(int maNV, int maCa, LocalDate ngayLam) {
        String query = "SELECT COUNT(*) FROM PhanCongCaLam WHERE MaNV = ? AND MaCa = ? AND NgayLam = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            pstmt.setInt(2, maCa);
            pstmt.setDate(3, Date.valueOf(ngayLam));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy danh sách nhân viên chưa được phân công trong ngày
    public static List<Integer> getNhanVienChuaPhanCong(LocalDate ngayLam) {
        List<Integer> list = new ArrayList<>();
        String query = "SELECT MaNV FROM NhanVien " +
                      "WHERE MaNV NOT IN (SELECT MaNV FROM PhanCongCaLam WHERE NgayLam = ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(ngayLam));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(rs.getInt("MaNV"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
}