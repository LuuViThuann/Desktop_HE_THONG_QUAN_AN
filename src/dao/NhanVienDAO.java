package dao;
import config.DatabaseConfig;
import model.NhanVien;
import java.sql.*;

public class NhanVienDAO {
    
    // Kiểm tra email tồn tại
    public static boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM NhanVien WHERE Email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Đăng nhập
    public static NhanVien login(String email, String matKhau) {
        String query = "SELECT n.MaNV, n.HoTen, n.GioiTinh, n.SDT, n.DiaChi, n.Email, " +
                      "n.MatKhau, n.TongNgayCong, n.TongLuong, n.MaPQ, p.TenQuyen " +
                      "FROM NhanVien n " +
                      "LEFT JOIN PhanQuyen p ON n.MaPQ = p.MaPQ " +
                      "WHERE n.Email = ? AND n.MatKhau = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, matKhau);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getInt("MaNV"));
                nv.setHoTen(rs.getString("HoTen"));
                nv.setGioiTinh(rs.getString("GioiTinh"));
                nv.setSdt(rs.getString("SDT"));
                nv.setDiaChi(rs.getString("DiaChi"));
                nv.setEmail(rs.getString("Email"));
                nv.setMatKhau(rs.getString("MatKhau"));
                nv.setTongNgayCong(rs.getInt("TongNgayCong"));
                nv.setTongLuong(rs.getBigDecimal("TongLuong"));
                nv.setMaPQ(rs.getInt("MaPQ"));
                nv.setTenQuyen(rs.getString("TenQuyen"));
                
                return nv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Đăng ký
    public static boolean register(String hoTen, String email, String sdt, String matKhau) {
        String query = "INSERT INTO NhanVien (HoTen, Email, SDT, MatKhau, TongLuong, MaPQ) " +
                      "VALUES (?, ?, ?, ?, 0, 1)"; // MaPQ = 1 (Default: Nhân viên)
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, hoTen);
            pstmt.setString(2, email);
            pstmt.setString(3, sdt);
            pstmt.setString(4, matKhau);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Quên mật khẩu - Cập nhật mật khẩu mới
    public static boolean updateMatKhau(String email, String matKhauMoi) {
        String query = "UPDATE NhanVien SET MatKhau = ? WHERE Email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, matKhauMoi);
            pstmt.setString(2, email);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy thông tin nhân viên theo email
    public static NhanVien getByEmail(String email) {
        String query = "SELECT MaNV, HoTen, Email FROM NhanVien WHERE Email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getInt("MaNV"));
                nv.setHoTen(rs.getString("HoTen"));
                nv.setEmail(rs.getString("Email"));
                return nv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}