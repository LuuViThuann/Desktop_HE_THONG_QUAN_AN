package dao;

import config.DatabaseConfig;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class LuongDAO {
    
  
    public static boolean calculateAndUpdateLuong(int maNV) {
        //  Dùng COALESCE để tránh NULL
        String query = "UPDATE nhanvien n " +
                      "SET TongLuong = COALESCE(" +
                      "    ROUND(" +
                      "        (SELECT COUNT(*) FROM chamcongnhanvien " +
                      "         WHERE MaNV = ? AND TrangThai = 'Đi làm') " +
                      "        * COALESCE(" +
                      "            (SELECT MucLuong FROM phanquyen WHERE MaPQ = n.MaPQ), " +
                      "            0" +
                      "        ), " +
                      "        0" +
                      "    ), " +
                      "    0" +
                      ") " +
                      "WHERE n.MaNV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            pstmt.setInt(2, maNV);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Cập nhật lương thành công cho MaNV = " + maNV);
            }
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ LỖI khi cập nhật lương cho MaNV = " + maNV);
            e.printStackTrace();
        }
        
        return false;
    }
    
    //  Cập nhật tất cả với xử lý lỗi từng NV
    public static int calculateAndUpdateLuongAll() {
        int count = 0;
        int errors = 0;
        
        String query = "SELECT MaNV FROM nhanvien";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int maNV = rs.getInt("MaNV");
                try {
                    if (calculateAndUpdateLuong(maNV)) {
                        count++;
                    }
                } catch (Exception e) {
                    System.err.println("❌ LỖI khi cập nhật NV: " + maNV);
                    e.printStackTrace();
                    errors++;
                    // Tiếp tục cập nhật những NV còn lại
                }
            }
            
            System.out.println("✅ Cập nhật lương xong - Thành công: " + count + ", Lỗi: " + errors);
            
        } catch (SQLException e) {
            System.err.println("❌ LỖI khi lấy danh sách nhân viên");
            e.printStackTrace();
        }
        
        return count;
    }
    
    //  Kiểm tra dữ liệu MucLuong
    public static boolean checkMucLuongData() {
        String query = "SELECT COUNT(*) as NullCount FROM phanquyen WHERE MucLuong IS NULL OR MucLuong = 0";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int nullCount = rs.getInt("NullCount");
                if (nullCount > 0) {
                    System.err.println("⚠️ CẢNH BÁO: " + nullCount + " vai trò có MucLuong = NULL hoặc 0");
                    System.err.println("⚠️ Hãy chạy: UPDATE phanquyen SET MucLuong = ... WHERE ...");
                    return false;
                } else {
                    System.out.println("✅ Dữ liệu MucLuong toàn bộ hợp lệ");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ CẢNH BÁO: Có thể bảng phanquyen chưa có cột MucLuong");
            e.printStackTrace();
        }
        
        return false;
    }
    
    //  Khởi tạo MucLuong nếu NULL
    public static void initializeMucLuong() {
        // Cập nhật NULL → 0
        String updateNull = "UPDATE phanquyen SET MucLuong = 0 WHERE MucLuong IS NULL";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int updated = stmt.executeUpdate(updateNull);
            if (updated > 0) {
                System.out.println("✅ Khởi tạo MucLuong = 0 cho " + updated + " vai trò");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ CẢNH BÁO: Lỗi khi khởi tạo MucLuong");
            e.printStackTrace();
        }
    }
    
    // Lấy số ngày công của nhân viên trong khoảng thời gian
    public static int getngayCongthuThang(int maNV, int thang, int nam) {
        String query = "SELECT COUNT(*) FROM chamcongnhanvien " +
                      "WHERE MaNV = ? AND TrangThai = 'Đi làm' " +
                      "AND MONTH(NgayCong) = ? AND YEAR(NgayCong) = ?";
        
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
    
    // Lấy mức lương/ngày của một nhân viên
    public static BigDecimal getMucLuong(int maNV) {
        String query = "SELECT COALESCE(p.MucLuong, 0) as MucLuong FROM nhanvien n " +
                      "LEFT JOIN phanquyen p ON n.MaPQ = p.MaPQ " +
                      "WHERE n.MaNV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new BigDecimal(rs.getLong("MucLuong"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }
    
    // Lấy tổng lương của nhân viên trong tháng/năm cụ thể
    public static BigDecimal getLuongThang(int maNV, int thang, int nam) {
        int ngayCong = getngayCongthuThang(maNV, thang, nam);
        BigDecimal mucLuong = getMucLuong(maNV);
        
        return mucLuong.multiply(new BigDecimal(ngayCong));
    }
    
    // Lấy thông tin lương chi tiết của một nhân viên
    public static Object[] getLuongInfo(int maNV) {
        String query = "SELECT " +
                      "n.MaNV, " +
                      "n.HoTen, " +
                      "p.TenQuyen, " +
                      "COALESCE(p.MucLuong, 0) AS MucLuong, " +
                      "COALESCE(n.TongNgayCong, 0) AS TongNgayCong, " +
                      "COALESCE(n.TongLuong, 0) AS TongLuong " +
                      "FROM nhanvien n " +
                      "LEFT JOIN phanquyen p ON n.MaPQ = p.MaPQ " +
                      "WHERE n.MaNV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Object[]{
                    rs.getInt("MaNV"),
                    rs.getString("HoTen"),
                    rs.getString("TenQuyen"),
                    new BigDecimal(rs.getLong("MucLuong")),
                    rs.getInt("TongNgayCong"),
                    new BigDecimal(rs.getLong("TongLuong"))
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Lấy danh sách lương của tất cả nhân viên
    public static List<Object[]> getAllLuongInfo() {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT " +
                      "n.MaNV, " +
                      "n.HoTen, " +
                      "p.TenQuyen, " +
                      "COALESCE(p.MucLuong, 0) AS MucLuong, " +
                      "COALESCE(n.TongNgayCong, 0) AS TongNgayCong, " +
                      "COALESCE(n.TongLuong, 0) AS TongLuong " +
                      "FROM nhanvien n " +
                      "LEFT JOIN phanquyen p ON n.MaPQ = p.MaPQ " +
                      "ORDER BY n.MaNV";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("MaNV"),
                    rs.getString("HoTen"),
                    rs.getString("TenQuyen"),
                    new BigDecimal(rs.getLong("MucLuong")),
                    rs.getInt("TongNgayCong"),
                    new BigDecimal(rs.getLong("TongLuong"))
                };
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Kiểm tra xem lương của nhân viên có cần cập nhật không
    public static boolean isLuongOutOfSync(int maNV) {
        String query = "SELECT n.TongLuong, ROUND(COALESCE(n.TongNgayCong, 0) * COALESCE(p.MucLuong, 0), 0) AS LuongTinh " +
                      "FROM nhanvien n " +
                      "LEFT JOIN phanquyen p ON n.MaPQ = p.MaPQ " +
                      "WHERE n.MaNV = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNV);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                long tongLuong = rs.getLong("TongLuong");
                long luongTinh = rs.getLong("LuongTinh");
                return tongLuong != luongTinh;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    // Lấy danh sách nhân viên cần cập nhật lương
    public static List<Integer> getOutOfSyncNhanVien() {
        List<Integer> list = new ArrayList<>();
        String query = "SELECT n.MaNV " +
                      "FROM nhanvien n " +
                      "LEFT JOIN phanquyen p ON n.MaPQ = p.MaPQ " +
                      "WHERE COALESCE(n.TongLuong, 0) != ROUND(COALESCE(n.TongNgayCong, 0) * COALESCE(p.MucLuong, 0), 0)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                list.add(rs.getInt("MaNV"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Đồng bộ lương cho tất cả nhân viên có vấn đề
    public static int syncAllOutOfSyncLuong() {
        List<Integer> list = getOutOfSyncNhanVien();
        int count = 0;
        
        System.out.println("📊 Tìm thấy " + list.size() + " nhân viên cần đồng bộ lương");
        
        for (int maNV : list) {
            if (calculateAndUpdateLuong(maNV)) {
                count++;
            }
        }
        
        System.out.println("✅ Đã đồng bộ lương cho " + count + " nhân viên");
        
        return count;
    }
    
    // Gọi Stored Procedure để tính lương (nếu dùng DB trigger)
    public static boolean callCalculateLuongProcedure(int maNV) {
        String query = "CALL sp_calculate_luong_by_mucluong(?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cstmt = conn.prepareCall(query)) {
            
            cstmt.setInt(1, maNV);
            cstmt.execute();
            return true;
            
        } catch (SQLException e) {
            System.err.println("⚠️ CẢNH BÁO: Procedure sp_calculate_luong_by_mucluong không tồn tại");
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Gọi Procedure tính lương cho tất cả
    public static boolean callCalculateLuongAllProcedure() {
        String query = "CALL sp_calculate_luong_all()";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cstmt = conn.prepareCall(query)) {
            
            cstmt.execute();
            return true;
            
        } catch (SQLException e) {
            System.err.println("⚠️ CẢNH BÁO: Procedure sp_calculate_luong_all không tồn tại");
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy lương theo tháng/năm cụ thể (nếu dùng Procedure)
    public static Object[] getLuongThangInfo(int maNV, int thang, int nam) {
        String query = "CALL sp_calculate_luong_by_month(?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cstmt = conn.prepareCall(query)) {
            
            cstmt.setInt(1, maNV);
            cstmt.setInt(2, thang);
            cstmt.setInt(3, nam);
            
            ResultSet rs = cstmt.executeQuery();
            if (rs.next()) {
                return new Object[]{
                    rs.getInt("MaNV"),
                    rs.getString("HoTen"),
                    rs.getString("VaiTro"),
                    new BigDecimal(rs.getLong("MucLuong")),
                    rs.getInt("NgayTangThang"),
                    new BigDecimal(rs.getLong("LuongThang"))
                };
            }
        } catch (SQLException e) {
            System.err.println("⚠️ CẢNH BÁO: Procedure sp_calculate_luong_by_month không tồn tại");
            e.printStackTrace();
        }
        
        return null;
    }
}