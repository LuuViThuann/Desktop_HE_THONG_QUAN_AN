package dao;

import config.DatabaseConfig;
import model.ChamCongNhanVien;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ChamCongNhanVienDAO {
    
    // Lấy tất cả chấm công
    public static List<ChamCongNhanVien> getAllChamCong() {
        List<ChamCongNhanVien> list = new ArrayList<>();
        String query = "SELECT cc.*, nv.HoTen " +
                      "FROM ChamCongNhanVien cc " +
                      "JOIN NhanVien nv ON cc.MaNV = nv.MaNV " +
                      "ORDER BY cc.NgayCong DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ChamCongNhanVien cc = new ChamCongNhanVien();
                cc.setMaCong(rs.getInt("MaCong"));
                cc.setNgayCong(rs.getDate("NgayCong").toLocalDate());
                cc.setTrangThai(rs.getString("TrangThai"));
                cc.setMaNV(rs.getInt("MaNV"));
                cc.setHoTenNV(rs.getString("HoTen"));
                list.add(cc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm chấm công theo ngày
    public static List<ChamCongNhanVien> searchByDate(LocalDate ngayCong) {
        List<ChamCongNhanVien> list = new ArrayList<>();
        String query = "SELECT cc.*, nv.HoTen " +
                      "FROM ChamCongNhanVien cc " +
                      "JOIN NhanVien nv ON cc.MaNV = nv.MaNV " +
                      "WHERE cc.NgayCong = ? " +
                      "ORDER BY nv.HoTen";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(ngayCong));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ChamCongNhanVien cc = new ChamCongNhanVien();
                cc.setMaCong(rs.getInt("MaCong"));
                cc.setNgayCong(rs.getDate("NgayCong").toLocalDate());
                cc.setTrangThai(rs.getString("TrangThai"));
                cc.setMaNV(rs.getInt("MaNV"));
                cc.setHoTenNV(rs.getString("HoTen"));
                list.add(cc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm chấm công theo tên nhân viên
    public static List<ChamCongNhanVien> searchByNhanVien(String keyword) {
        List<ChamCongNhanVien> list = new ArrayList<>();
        String query = "SELECT cc.*, nv.HoTen " +
                      "FROM ChamCongNhanVien cc " +
                      "JOIN NhanVien nv ON cc.MaNV = nv.MaNV " +
                      "WHERE nv.HoTen LIKE ? " +
                      "ORDER BY cc.NgayCong DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ChamCongNhanVien cc = new ChamCongNhanVien();
                cc.setMaCong(rs.getInt("MaCong"));
                cc.setNgayCong(rs.getDate("NgayCong").toLocalDate());
                cc.setTrangThai(rs.getString("TrangThai"));
                cc.setMaNV(rs.getInt("MaNV"));
                cc.setHoTenNV(rs.getString("HoTen"));
                list.add(cc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Tìm kiếm chấm công theo tháng/năm
    public static List<ChamCongNhanVien> searchByMonthYear(int thang, int nam) {
        List<ChamCongNhanVien> list = new ArrayList<>();
        String query = "SELECT cc.*, nv.HoTen " +
                      "FROM ChamCongNhanVien cc " +
                      "JOIN NhanVien nv ON cc.MaNV = nv.MaNV " +
                      "WHERE MONTH(cc.NgayCong) = ? AND YEAR(cc.NgayCong) = ? " +
                      "ORDER BY cc.NgayCong DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, thang);
            pstmt.setInt(2, nam);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ChamCongNhanVien cc = new ChamCongNhanVien();
                cc.setMaCong(rs.getInt("MaCong"));
                cc.setNgayCong(rs.getDate("NgayCong").toLocalDate());
                cc.setTrangThai(rs.getString("TrangThai"));
                cc.setMaNV(rs.getInt("MaNV"));
                cc.setHoTenNV(rs.getString("HoTen"));
                list.add(cc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
  
    public static boolean insertChamCong(ChamCongNhanVien cc) {
        // Kiểm tra trùng lặp
        if (isDuplicate(cc.getMaNV(), cc.getNgayCong())) {
            return false;
        }
        
        String query = "INSERT INTO ChamCongNhanVien (NgayCong, TrangThai, MaNV) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(cc.getNgayCong()));
            pstmt.setString(2, cc.getTrangThai());
            pstmt.setInt(3, cc.getMaNV());
            
            if (pstmt.executeUpdate() > 0) {
                //  Tự động cập nhật lương sau khi thêm chấm công
                updateLuongAfterChamCong(cc.getMaNV());
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ✅ SỬA CHẤM CÔNG + TỰ ĐỘNG CẬP NHẬT LƯƠNG
    public static boolean updateChamCong(ChamCongNhanVien cc) {
        String query = "UPDATE ChamCongNhanVien SET NgayCong = ?, TrangThai = ?, MaNV = ? WHERE MaCong = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(cc.getNgayCong()));
            pstmt.setString(2, cc.getTrangThai());
            pstmt.setInt(3, cc.getMaNV());
            pstmt.setInt(4, cc.getMaCong());
            
            if (pstmt.executeUpdate() > 0) {
                //  Tự động cập nhật lương sau khi sửa chấm công
                updateLuongAfterChamCong(cc.getMaNV());
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
   
    public static boolean deleteChamCong(int maCong) {
        // Lấy mã nhân viên trước khi xóa
        int maNV = getNhanVienFromChamCong(maCong);
        
        String query = "DELETE FROM ChamCongNhanVien WHERE MaCong = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCong);
            
            if (pstmt.executeUpdate() > 0) {
                // Tự động cập nhật lương sau khi xóa chấm công
                if (maNV > 0) {
                    updateLuongAfterChamCong(maNV);
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Kiểm tra trùng lặp chấm công
    private static boolean isDuplicate(int maNV, LocalDate ngayCong) {
        String query = "SELECT COUNT(*) FROM ChamCongNhanVien WHERE MaNV = ? AND NgayCong = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            pstmt.setDate(2, Date.valueOf(ngayCong));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    //  LẤY MÃ NHÂN VIÊN TỪ CHẤM CÔNG
    private static int getNhanVienFromChamCong(int maCong) {
        String query = "SELECT MaNV FROM ChamCongNhanVien WHERE MaCong = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maCong);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("MaNV");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    //  CẬP NHẬT LƯƠNG SAU KHI CHẤM CÔNG THAY ĐỔI
    private static boolean updateLuongAfterChamCong(int maNV) {
        // Gọi phương thức từ LuongDAO để cập nhật
        return LuongDAO.calculateAndUpdateLuong(maNV);
    }
    
    // Đếm số ngày công của nhân viên trong tháng
    public static int countNgayCongThang(int maNV, int thang, int nam) {
        String query = "SELECT COUNT(*) FROM ChamCongNhanVien " +
                      "WHERE MaNV = ? AND MONTH(NgayCong) = ? AND YEAR(NgayCong) = ? " +
                      "AND TrangThai = 'Đi làm'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            pstmt.setInt(2, thang);
            pstmt.setInt(3, nam);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
   
    @Deprecated
    public static boolean updateTongNgayCong(int maNV) {
        return updateLuongAfterChamCong(maNV);
    }
}