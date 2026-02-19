package dao;

import config.DatabaseConfig;
import model.MonAn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonAnDAO {
    
    // Lấy tất cả món ăn
    public static List<MonAn> getAllMonAn() {
        List<MonAn> monAnList = new ArrayList<>();
        String query = "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai " +
                      "FROM MonAn ORDER BY TenMon";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setMaNhom(rs.getInt("MaNhom"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));
                
                monAnList.add(monAn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return monAnList;
    }
    
    // Lấy món ăn theo nhóm
    public static List<MonAn> getMonAnByNhom(int maNhom) {
        List<MonAn> monAnList = new ArrayList<>();
        String query = "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai " +
                      "FROM MonAn WHERE MaNhom = ? ORDER BY TenMon";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maNhom);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setMaNhom(rs.getInt("MaNhom"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));
                
                monAnList.add(monAn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return monAnList;
    }
    
    // Tìm kiếm món ăn
    public static List<MonAn> searchMonAn(int maNhom, String keyword) {
        List<MonAn> monAnList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai ")
             .append("FROM MonAn WHERE 1=1 ");
        
        if (maNhom > 0) {
            query.append("AND MaNhom = ? ");
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND TenMon LIKE ? ");
        }
        
        query.append("ORDER BY TenMon");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            
            int paramIndex = 1;
            if (maNhom > 0) {
                pstmt.setInt(paramIndex++, maNhom);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex, "%" + keyword + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setMaNhom(rs.getInt("MaNhom"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));
                
                monAnList.add(monAn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return monAnList;
    }
    
    // Thêm món ăn mới
    public static boolean addMonAn(MonAn monAn) {
        String query = "INSERT INTO MonAn (TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, monAn.getTenMon());
            pstmt.setString(2, monAn.getHinhAnh());
            pstmt.setString(3, monAn.getDonViTinh());
            pstmt.setBigDecimal(4, monAn.getGiaTien());
            pstmt.setInt(5, monAn.getMaNhom());
            pstmt.setInt(6, monAn.getSoLuongConLai());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Cập nhật số lượng món ăn
    public static boolean updateSoLuong(int maMon, int soLuongMoi) {
        String query = "UPDATE MonAn SET SoLuongConLai = ? WHERE MaMon = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, soLuongMoi);
            pstmt.setInt(2, maMon);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy thông tin một món ăn
    public static MonAn getMonAnById(int maMon) {
        String query = "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai " +
                      "FROM MonAn WHERE MaMon = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, maMon);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                MonAn monAn = new MonAn();
                monAn.setMaMon(rs.getInt("MaMon"));
                monAn.setTenMon(rs.getString("TenMon"));
                monAn.setHinhAnh(rs.getString("HinhAnh"));
                monAn.setDonViTinh(rs.getString("DonViTinh"));
                monAn.setGiaTien(rs.getBigDecimal("GiaTien"));
                monAn.setMaNhom(rs.getInt("MaNhom"));
                monAn.setSoLuongConLai(rs.getInt("SoLuongConLai"));
                
                return monAn;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}