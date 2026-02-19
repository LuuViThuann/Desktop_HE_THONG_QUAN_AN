package dao;

import config.DatabaseConfig;
import model.Ban;
import model.KhuVucQuan;
import java.sql.*;
import java.util.*;
import java.util.List;
import model.DatBanTruoc;
public class BanDAO {
    
    // Lấy tất cả bàn theo khu vực
    public static List<Ban> getAllBanWithKhuVuc() {
        List<Ban> banList = new ArrayList<>();
        String query = "SELECT kv.MaKV, kv.TenKV, b.MaBan, b.TenBan, b.TrangThai " +
                      "FROM KhuVucQuan kv " +
                      "LEFT JOIN Ban b ON kv.MaKV = b.MaKV " +
                      "ORDER BY kv.MaKV, b.MaBan";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Ban ban = new Ban();
                ban.setMaKV(rs.getInt("MaKV"));
                ban.setTenKV(rs.getString("TenKV"));
                
                if (rs.getInt("MaBan") != 0) {
                    ban.setMaBan(rs.getInt("MaBan"));
                    ban.setTenBan(rs.getString("TenBan"));
                    ban.setTrangThai(rs.getString("TrangThai"));
                }
                
                banList.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return banList;
    }
    
    // Lấy trạng thái bàn
    public static String getTrangThaiBan(int maBan) {
        String query = "SELECT TrangThai FROM Ban WHERE MaBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("TrangThai");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return "Trống";
    }
    
    // Cập nhật trạng thái bàn
    public static boolean updateTrangThaiBan(int maBan, String trangThaiMoi) {
        String query = "UPDATE Ban SET TrangThai = ? WHERE MaBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, trangThaiMoi);
            pstmt.setInt(2, maBan);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    public static void updateTrangThaiBan(int maBan, String trangThai, Connection conn) {
        try {
            String query = "UPDATE Ban SET TrangThai = ? WHERE MaBan = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, trangThai);
            pstmt.setInt(2, maBan);
            pstmt.executeUpdate();
           
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
 
    public static void xoaTatCaMonTrongBan1(int maBan) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "DELETE FROM HoaDonBanHang WHERE MaBan = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, maBan);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
   
    public static void xoaTatCaMonTrongBan(int maBan, Connection conn) {
        try {
            String query = "DELETE FROM HoaDonBanHang WHERE MaBan = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, maBan);
            pstmt.executeUpdate();
          
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Thêm bàn mới
    public static boolean addBan(String tenBan, int maKV) {
        String query = "INSERT INTO Ban (TenBan, TrangThai, MaKV) VALUES (?, 'Trống', ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, tenBan);
            pstmt.setInt(2, maKV);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Kiểm tra bàn có đơn hàng không
    public static boolean checkIfBanHasOrdered(int maBan) {
        String query = "SELECT COUNT(*) FROM HoaDonBanHang WHERE MaBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
 // Xóa tất cả món trong hóa đơn bàn
    public static boolean xoaTatCaMonTrongBan(int maBan) {
        String query = "DELETE FROM HoaDonBanHang WHERE MaBan = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maBan);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // Reset bàn về trạng thái trống
    public static boolean resetBan(int maBan) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Xóa hóa đơn
            String deleteHD = "DELETE FROM HoaDonBanHang WHERE MaBan = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(deleteHD);
            pstmt1.setInt(1, maBan);
            pstmt1.executeUpdate();
            
            // 2. Cập nhật trạng thái bàn
            String updateBan = "UPDATE Ban SET TrangThai = 'Trống' WHERE MaBan = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateBan);
            pstmt2.setInt(1, maBan);
            pstmt2.executeUpdate();
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    // ==========
 // Thêm đặt bàn
    public static boolean themDatBan(DatBanTruoc datBan) {
        String query = "INSERT INTO DatBanTruoc (HoTenKhachHang, SDT, SoLuongKhach, NgayDat, GioDat, MaBan, TrangThai) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, datBan.getHoTenKhachHang());
            pstmt.setString(2, datBan.getSdt());
            pstmt.setInt(3, datBan.getSoLuongKhach());
            pstmt.setDate(4, datBan.getNgayDat());
            pstmt.setTime(5, datBan.getGioDat());
            pstmt.setInt(6, datBan.getMaBan());
            pstmt.setString(7, datBan.getTrangThai());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // Lấy thông tin đặt bàn
    public static DatBanTruoc getThongTinDatBan(int maBan) {
        String query = "SELECT * FROM DatBanTruoc WHERE MaBan = ? AND TrangThai = 'Đã đặt'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maBan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                DatBanTruoc datBan = new DatBanTruoc();
                datBan.setMaDatBan(rs.getInt("MaDatBan"));
                datBan.setHoTenKhachHang(rs.getString("HoTenKhachHang"));
                datBan.setSdt(rs.getString("SDT"));
                datBan.setSoLuongKhach(rs.getInt("SoLuongKhach"));
                datBan.setNgayDat(rs.getDate("NgayDat"));
                datBan.setGioDat(rs.getTime("GioDat"));
                datBan.setMaBan(rs.getInt("MaBan"));
                datBan.setTrangThai(rs.getString("TrangThai"));
                return datBan;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // Hủy đặt bàn
    public static boolean huyDatBan(int maBan) {
        String query = "UPDATE DatBanTruoc SET TrangThai = 'Đã hủy' WHERE MaBan = ? AND TrangThai = 'Đã đặt'";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // Cập nhật trạng thái đặt bàn
            PreparedStatement pstmt1 = conn.prepareStatement(query);
            pstmt1.setInt(1, maBan);
            pstmt1.executeUpdate();
            
            // Cập nhật trạng thái bàn về Trống
            String updateBan = "UPDATE Ban SET TrangThai = 'Trống' WHERE MaBan = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateBan);
            pstmt2.setInt(1, maBan);
            pstmt2.executeUpdate();
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    //================================
}