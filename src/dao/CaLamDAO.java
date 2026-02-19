package dao;

import config.DatabaseConfig;
import model.CaLam;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CaLamDAO {
    
    // Lấy tất cả ca làm
    public static List<CaLam> getAllCaLam() {
        List<CaLam> list = new ArrayList<>();
        String query = "SELECT * FROM CaLam ORDER BY MaCa";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                CaLam ca = new CaLam();
                ca.setMaCa(rs.getInt("MaCa"));
                ca.setTenCa(rs.getString("TenCa"));
                ca.setGioBatDau(rs.getTime("GioBatDau").toLocalTime());
                ca.setGioKetThuc(rs.getTime("GioKetThuc").toLocalTime());
                list.add(ca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm ca làm theo tên
    public static List<CaLam> searchCaLam(String keyword) {
        List<CaLam> list = new ArrayList<>();
        String query = "SELECT * FROM CaLam WHERE TenCa LIKE ? ORDER BY MaCa";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                CaLam ca = new CaLam();
                ca.setMaCa(rs.getInt("MaCa"));
                ca.setTenCa(rs.getString("TenCa"));
                ca.setGioBatDau(rs.getTime("GioBatDau").toLocalTime());
                ca.setGioKetThuc(rs.getTime("GioKetThuc").toLocalTime());
                list.add(ca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Thêm ca làm mới
    public static boolean insertCaLam(CaLam ca) {
        String query = "INSERT INTO CaLam (TenCa, GioBatDau, GioKetThuc) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, ca.getTenCa());
            pstmt.setTime(2, Time.valueOf(ca.getGioBatDau()));
            pstmt.setTime(3, Time.valueOf(ca.getGioKetThuc()));
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Cập nhật ca làm
    public static boolean updateCaLam(CaLam ca) {
        String query = "UPDATE CaLam SET TenCa = ?, GioBatDau = ?, GioKetThuc = ? WHERE MaCa = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, ca.getTenCa());
            pstmt.setTime(2, Time.valueOf(ca.getGioBatDau()));
            pstmt.setTime(3, Time.valueOf(ca.getGioKetThuc()));
            pstmt.setInt(4, ca.getMaCa());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Xóa ca làm
    public static boolean deleteCaLam(int maCa) {
        String query = "DELETE FROM CaLam WHERE MaCa = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCa);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy ca làm theo ID
    public static CaLam getCaLamById(int maCa) {
        String query = "SELECT * FROM CaLam WHERE MaCa = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCa);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                CaLam ca = new CaLam();
                ca.setMaCa(rs.getInt("MaCa"));
                ca.setTenCa(rs.getString("TenCa"));
                ca.setGioBatDau(rs.getTime("GioBatDau").toLocalTime());
                ca.setGioKetThuc(rs.getTime("GioKetThuc").toLocalTime());
                return ca;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}