package dao;

import config.DatabaseConfig;
import model.NguyenLieu;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Thao tác CRUD cho bảng nguyenlieu
 */
public class NguyenLieuDAO {

    // ======================== READ ========================

    public static List<NguyenLieu> getAll() {
        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu WHERE TrangThai = 1 ORDER BY TenNguyenLieu";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<NguyenLieu> getAllKeTongNgung() {
        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu ORDER BY TrangThai DESC, TenNguyenLieu";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static NguyenLieu getById(int maNL) {
        String sql = "SELECT * FROM nguyenlieu WHERE MaNL = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNL);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Danh sách nguyên liệu sắp hết (tồn <= mức tối thiểu) */
    public static List<NguyenLieu> getCanhBao() {
        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu WHERE TrangThai=1 AND SoLuong <= MucTonToiThieu ORDER BY SoLuong";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Tìm kiếm */
    public static List<NguyenLieu> search(String keyword) {
        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu WHERE TrangThai=1 AND TenNguyenLieu LIKE ? ORDER BY TenNguyenLieu";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ======================== CREATE ========================

    public static boolean add(NguyenLieu nl) {
        String sql = "INSERT INTO nguyenlieu (TenNguyenLieu, DonViTinh, DonGia, SoLuong, " +
                     "DonViTinhNhap, HeSoQuyDoi, MucTonToiThieu, MucTonToiDa, GhiChu, TrangThai) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nl.getTenNguyenLieu());
            ps.setString(2, nl.getDonViTinh());
            ps.setBigDecimal(3, nl.getDonGia());
            ps.setDouble(4, nl.getSoLuong());
            ps.setString(5, nl.getDonViTinhNhap());
            ps.setDouble(6, nl.getHeSoQuyDoi());
            ps.setDouble(7, nl.getMucTonToiThieu());
            ps.setDouble(8, nl.getMucTonToiDa());
            ps.setString(9, nl.getGhiChu());
            ps.setInt(10, nl.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ======================== UPDATE ========================

    public static boolean update(NguyenLieu nl) {
        String sql = "UPDATE nguyenlieu SET TenNguyenLieu=?, DonViTinh=?, DonGia=?, SoLuong=?, " +
                     "DonViTinhNhap=?, HeSoQuyDoi=?, MucTonToiThieu=?, MucTonToiDa=?, GhiChu=?, TrangThai=? " +
                     "WHERE MaNL=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nl.getTenNguyenLieu());
            ps.setString(2, nl.getDonViTinh());
            ps.setBigDecimal(3, nl.getDonGia());
            ps.setDouble(4, nl.getSoLuong());
            ps.setString(5, nl.getDonViTinhNhap());
            ps.setDouble(6, nl.getHeSoQuyDoi());
            ps.setDouble(7, nl.getMucTonToiThieu());
            ps.setDouble(8, nl.getMucTonToiDa());
            ps.setString(9, nl.getGhiChu());
            ps.setInt(10, nl.getTrangThai());
            ps.setInt(11, nl.getMaNL());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Cập nhật tồn kho (cộng/trừ) */
    public static boolean updateSoLuong(int maNL, double delta) {
        String sql = "UPDATE nguyenlieu SET SoLuong = SoLuong + ? WHERE MaNL = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setInt(2, maNL);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ======================== DELETE (soft) ========================

    /** Xóa mềm: đặt TrangThai = 0 */
    public static boolean deactivate(int maNL) {
        String sql = "UPDATE nguyenlieu SET TrangThai = 0 WHERE MaNL = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNL);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Xóa cứng (chỉ dùng khi NL chưa được tham chiếu) */
    public static boolean delete(int maNL) {
        String sql = "DELETE FROM nguyenlieu WHERE MaNL = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNL);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ======================== HELPER ========================

    private static NguyenLieu mapRow(ResultSet rs) throws SQLException {
        NguyenLieu nl = new NguyenLieu();
        nl.setMaNL(rs.getInt("MaNL"));
        nl.setTenNguyenLieu(rs.getString("TenNguyenLieu"));
        nl.setDonViTinh(rs.getString("DonViTinh"));
        nl.setDonGia(rs.getBigDecimal("DonGia"));
        nl.setSoLuong(rs.getDouble("SoLuong"));
        try {
            nl.setDonViTinhNhap(rs.getString("DonViTinhNhap"));
            nl.setHeSoQuyDoi(rs.getDouble("HeSoQuyDoi"));
            nl.setMucTonToiThieu(rs.getDouble("MucTonToiThieu"));
            nl.setMucTonToiDa(rs.getDouble("MucTonToiDa"));
            nl.setGhiChu(rs.getString("GhiChu"));
            nl.setTrangThai(rs.getInt("TrangThai"));
        } catch (SQLException ignored) {} // backward compat nếu bảng chưa có cột
        return nl;
    }
}