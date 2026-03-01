package dao;

import config.DatabaseConfig;
import model.CongThucMon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Công thức món ăn
 */
public class CongThucMonDAO {

    public static List<CongThucMon> getByMaMon(int maMon) {
        List<CongThucMon> list = new ArrayList<>();
        String sql = "SELECT ct.*, nl.TenNguyenLieu, m.TenMon " +
                     "FROM congthucmon ct " +
                     "JOIN nguyenlieu nl ON nl.MaNL = ct.MaNL " +
                     "JOIN monan m ON m.MaMon = ct.MaMon " +
                     "WHERE ct.MaMon = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<CongThucMon> getAll() {
        List<CongThucMon> list = new ArrayList<>();
        String sql = "SELECT ct.*, nl.TenNguyenLieu, m.TenMon " +
                     "FROM congthucmon ct " +
                     "JOIN nguyenlieu nl ON nl.MaNL = ct.MaNL " +
                     "JOIN monan m ON m.MaMon = ct.MaMon " +
                     "ORDER BY m.TenMon, nl.TenNguyenLieu";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean add(CongThucMon ct) {
        String sql = "INSERT INTO congthucmon (MaMon, MaNL, SoLuongCan, DonViTinh, GhiChu) " +
                     "VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE SoLuongCan=VALUES(SoLuongCan), DonViTinh=VALUES(DonViTinh)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ct.getMaMon());
            ps.setInt(2, ct.getMaNL());
            ps.setDouble(3, ct.getSoLuongCan());
            ps.setString(4, ct.getDonViTinh());
            ps.setString(5, ct.getGhiChu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean update(CongThucMon ct) {
        String sql = "UPDATE congthucmon SET SoLuongCan=?, DonViTinh=?, GhiChu=? WHERE MaCT=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, ct.getSoLuongCan());
            ps.setString(2, ct.getDonViTinh());
            ps.setString(3, ct.getGhiChu());
            ps.setInt(4, ct.getMaCT());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean delete(int maCT) {
        String sql = "DELETE FROM congthucmon WHERE MaCT = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maCT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Xóa toàn bộ công thức của một món */
    public static boolean deleteByMaMon(int maMon) {
        String sql = "DELETE FROM congthucmon WHERE MaMon = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private static CongThucMon mapRow(ResultSet rs) throws SQLException {
        CongThucMon ct = new CongThucMon();
        ct.setMaCT(rs.getInt("MaCT"));
        ct.setMaMon(rs.getInt("MaMon"));
        ct.setMaNL(rs.getInt("MaNL"));
        ct.setSoLuongCan(rs.getDouble("SoLuongCan"));
        ct.setDonViTinh(rs.getString("DonViTinh"));
        ct.setGhiChu(rs.getString("GhiChu"));
        try {
            ct.setTenNguyenLieu(rs.getString("TenNguyenLieu"));
            ct.setTenMon(rs.getString("TenMon"));
        } catch (SQLException ignored) {}
        return ct;
    }
}