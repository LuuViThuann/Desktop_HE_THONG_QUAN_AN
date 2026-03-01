package dao;

import config.DatabaseConfig;
import model.ChiTietPhieuNhap;
import model.PhieuNhapHang;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: Phiếu nhập hàng
 */
public class PhieuNhapHangDAO {

    // ======================== READ ========================

    public static List<PhieuNhapHang> getAll() {
        List<PhieuNhapHang> list = new ArrayList<>();
        String sql = "SELECT p.*, nv.HoTen AS TenNV " +
                     "FROM phieunhaphang p " +
                     "LEFT JOIN nhanvien nv ON nv.MaNV = p.MaNV " +
                     "ORDER BY p.NgayNhap DESC";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapHeader(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<PhieuNhapHang> getByDateRange(String fromDate, String toDate) {
        List<PhieuNhapHang> list = new ArrayList<>();
        String sql = "SELECT p.*, nv.HoTen AS TenNV FROM phieunhaphang p " +
                     "LEFT JOIN nhanvien nv ON nv.MaNV = p.MaNV " +
                     "WHERE DATE(p.NgayNhap) BETWEEN ? AND ? ORDER BY p.NgayNhap DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapHeader(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<ChiTietPhieuNhap> getChiTiet(int maPhieu) {
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT ct.*, nl.TenNguyenLieu " +
                     "FROM chitietphieunhap ct " +
                     "JOIN nguyenlieu nl ON nl.MaNL = ct.MaNL " +
                     "WHERE ct.MaPhieu = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maPhieu);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapChiTiet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ======================== CREATE ========================

    /**
     * Thêm phiếu nhập + chi tiết trong một transaction
     * Trigger trg_nhap_kho sẽ tự động cộng tồn kho
     */
    public static int addPhieu(PhieuNhapHang phieu) {
        String sqlPhieu = "INSERT INTO phieunhaphang (NgayNhap, NhaCungCap, MaNV, TongTien, GhiChu, TrangThai) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCT    = "INSERT INTO chitietphieunhap (MaPhieu, MaNL, SoLuong, DonViTinh, DonGiaNhap, HanSuDung, GhiChu) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DatabaseConfig.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Insert header
                int maPhieu;
                try (PreparedStatement ps = c.prepareStatement(sqlPhieu, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayNhap() != null ? phieu.getNgayNhap() : LocalDateTime.now()));
                    ps.setString(2, phieu.getNhaCungCap());
                    ps.setInt(3, phieu.getMaNV());
                    ps.setBigDecimal(4, phieu.getTongTien());
                    ps.setString(5, phieu.getGhiChu());
                    ps.setString(6, phieu.getTrangThai() != null ? phieu.getTrangThai() : "Đã nhập");
                    ps.executeUpdate();
                    ResultSet gen = ps.getGeneratedKeys();
                    if (!gen.next()) { c.rollback(); return -1; }
                    maPhieu = gen.getInt(1);
                }

                // Insert chi tiết — trigger tự cộng kho
                try (PreparedStatement ps = c.prepareStatement(sqlCT)) {
                    for (ChiTietPhieuNhap ct : phieu.getDanhSachChiTiet()) {
                        ps.setInt(1, maPhieu);
                        ps.setInt(2, ct.getMaNL());
                        ps.setDouble(3, ct.getSoLuong());
                        ps.setString(4, ct.getDonViTinh());
                        ps.setBigDecimal(5, ct.getDonGiaNhap());
                        if (ct.getHanSuDung() != null)
                            ps.setDate(6, Date.valueOf(ct.getHanSuDung()));
                        else ps.setNull(6, Types.DATE);
                        ps.setString(7, ct.getGhiChu());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
                return maPhieu;
            } catch (SQLException ex) {
                c.rollback();
                ex.printStackTrace();
                return -1;
            }
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    // ======================== DELETE ========================

    public static boolean delete(int maPhieu) {
        // CASCADE sẽ xóa chi tiết; nhưng kho KHÔNG hoàn lại (cần xử lý riêng nếu cần)
        String sql = "DELETE FROM phieunhaphang WHERE MaPhieu = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maPhieu);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ======================== HELPER ========================

    private static PhieuNhapHang mapHeader(ResultSet rs) throws SQLException {
        PhieuNhapHang p = new PhieuNhapHang();
        p.setMaPhieu(rs.getInt("MaPhieu"));
        Timestamp ts = rs.getTimestamp("NgayNhap");
        if (ts != null) p.setNgayNhap(ts.toLocalDateTime());
        p.setNhaCungCap(rs.getString("NhaCungCap"));
        p.setMaNV(rs.getInt("MaNV"));
        p.setTongTien(rs.getBigDecimal("TongTien"));
        p.setGhiChu(rs.getString("GhiChu"));
        p.setTrangThai(rs.getString("TrangThai"));
        try { p.setTenNV(rs.getString("TenNV")); } catch (SQLException ignored) {}
        return p;
    }

    private static ChiTietPhieuNhap mapChiTiet(ResultSet rs) throws SQLException {
        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaCTPN(rs.getInt("MaCTPN"));
        ct.setMaPhieu(rs.getInt("MaPhieu"));
        ct.setMaNL(rs.getInt("MaNL"));
        ct.setSoLuong(rs.getDouble("SoLuong"));
        ct.setDonViTinh(rs.getString("DonViTinh"));
        ct.setDonGiaNhap(rs.getBigDecimal("DonGiaNhap"));
        Date d = rs.getDate("HanSuDung");
        if (d != null) ct.setHanSuDung(d.toLocalDate());
        ct.setGhiChu(rs.getString("GhiChu"));
        try { ct.setTenNguyenLieu(rs.getString("TenNguyenLieu")); } catch (SQLException ignored) {}
        return ct;
    }
}