package dao;

import config.DatabaseConfig;
import model.LichSuXuatKho;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO: Xuất kho thủ công + Báo cáo nguyên liệu
 */
public class XuatKhoDAO {

    // ======================== XUẤT KHO THỦ CÔNG ========================

    public static List<LichSuXuatKho> getAll() {
        List<LichSuXuatKho> list = new ArrayList<>();
        String sql = "SELECT x.*, nl.TenNguyenLieu, m.TenMon, nv.HoTen AS TenNV " +
                     "FROM lichsuxuatkho x " +
                     "JOIN nguyenlieu nl ON nl.MaNL = x.MaNL " +
                     "LEFT JOIN monan m ON m.MaMon = x.MaMon " +
                     "LEFT JOIN nhanvien nv ON nv.MaNV = x.MaNV " +
                     "ORDER BY x.NgayXuat DESC LIMIT 500";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<LichSuXuatKho> getByDate(String ngay) {
        List<LichSuXuatKho> list = new ArrayList<>();
        String sql = "SELECT x.*, nl.TenNguyenLieu, m.TenMon, nv.HoTen AS TenNV " +
                     "FROM lichsuxuatkho x " +
                     "JOIN nguyenlieu nl ON nl.MaNL = x.MaNL " +
                     "LEFT JOIN monan m ON m.MaMon = x.MaMon " +
                     "LEFT JOIN nhanvien nv ON nv.MaNV = x.MaNV " +
                     "WHERE DATE(x.NgayXuat) = ? ORDER BY x.NgayXuat DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ngay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Xuất kho thủ công (không theo hóa đơn) */
    public static boolean xuatKhoThuCong(LichSuXuatKho xk) {
        String sqlInsert = "INSERT INTO lichsuxuatkho (NgayXuat, MaNL, SoLuong, DonViTinh, LyDo, MaMon, MaNV, GhiChu) " +
                           "VALUES (?,?,?,?,?,?,?,?)";
        String sqlUpdate = "UPDATE nguyenlieu SET SoLuong = SoLuong - ? WHERE MaNL = ? AND SoLuong >= ?";

        try (Connection c = DatabaseConfig.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Kiểm tra đủ tồn kho
                try (PreparedStatement ps = c.prepareStatement(sqlUpdate)) {
                    ps.setDouble(1, xk.getSoLuong());
                    ps.setInt(2, xk.getMaNL());
                    ps.setDouble(3, xk.getSoLuong());
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        c.rollback();
                        return false; // Không đủ tồn
                    }
                }
                // Ghi lịch sử
                try (PreparedStatement ps = c.prepareStatement(sqlInsert)) {
                    ps.setTimestamp(1, Timestamp.valueOf(xk.getNgayXuat() != null ? xk.getNgayXuat() : LocalDateTime.now()));
                    ps.setInt(2, xk.getMaNL());
                    ps.setDouble(3, xk.getSoLuong());
                    ps.setString(4, xk.getDonViTinh());
                    ps.setString(5, xk.getLyDo());
                    if (xk.getMaMon() > 0) ps.setInt(6, xk.getMaMon()); else ps.setNull(6, Types.INTEGER);
                    if (xk.getMaNV() > 0) ps.setInt(7, xk.getMaNV()); else ps.setNull(7, Types.INTEGER);
                    ps.setString(8, xk.getGhiChu());
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException ex) {
                c.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ======================== BÁO CÁO ========================

    /**
     * Báo cáo tổng nguyên liệu đã xuất theo ngày
     * Trả về: [TenNguyenLieu -> [TongXuat, DonViTinh, MonDung, TonHienTai, TrangThaiTon]]
     */
    public static List<Object[]> baoCaoNguyenLieuNgay(String ngay) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nl.MaNL, nl.TenNguyenLieu, nl.DonViTinh, " +
                     "       SUM(x.SoLuong) AS TongXuat, " +
                     "       nl.SoLuong AS TonHienTai, " +
                     "       nl.MucTonToiThieu, " +
                     "       GROUP_CONCAT(DISTINCT m.TenMon SEPARATOR ', ') AS MonDung " +
                     "FROM lichsuxuatkho x " +
                     "JOIN nguyenlieu nl ON nl.MaNL = x.MaNL " +
                     "LEFT JOIN monan m ON m.MaMon = x.MaMon " +
                     "WHERE DATE(x.NgayXuat) = ? " +
                     "GROUP BY nl.MaNL, nl.TenNguyenLieu, nl.DonViTinh, nl.SoLuong, nl.MucTonToiThieu " +
                     "ORDER BY TongXuat DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ngay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double ton = rs.getDouble("TonHienTai");
                double min = rs.getDouble("MucTonToiThieu");
                String trangThai = ton <= 0 ? "Hết hàng" : (ton <= min ? "Sắp hết" : "Đủ hàng");
                list.add(new Object[]{
                    rs.getInt("MaNL"),
                    rs.getString("TenNguyenLieu"),
                    rs.getDouble("TongXuat"),
                    rs.getString("DonViTinh"),
                    rs.getDouble("TonHienTai"),
                    trangThai,
                    rs.getString("MonDung")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Báo cáo trong khoảng ngày */
    public static List<Object[]> baoCaoKhoangNgay(String tuNgay, String denNgay) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nl.MaNL, nl.TenNguyenLieu, nl.DonViTinh, " +
                     "       SUM(x.SoLuong) AS TongXuat, " +
                     "       nl.SoLuong AS TonHienTai, nl.MucTonToiThieu, " +
                     "       COUNT(DISTINCT DATE(x.NgayXuat)) AS SoNgayDung, " +
                     "       GROUP_CONCAT(DISTINCT m.TenMon SEPARATOR ', ') AS MonDung " +
                     "FROM lichsuxuatkho x " +
                     "JOIN nguyenlieu nl ON nl.MaNL = x.MaNL " +
                     "LEFT JOIN monan m ON m.MaMon = x.MaMon " +
                     "WHERE DATE(x.NgayXuat) BETWEEN ? AND ? " +
                     "GROUP BY nl.MaNL, nl.TenNguyenLieu, nl.DonViTinh, nl.SoLuong, nl.MucTonToiThieu " +
                     "ORDER BY TongXuat DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tuNgay);
            ps.setString(2, denNgay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double ton = rs.getDouble("TonHienTai");
                double min = rs.getDouble("MucTonToiThieu");
                String tt = ton <= 0 ? "Hết hàng" : (ton <= min ? "Sắp hết" : "Đủ hàng");
                list.add(new Object[]{
                    rs.getInt("MaNL"),
                    rs.getString("TenNguyenLieu"),
                    rs.getDouble("TongXuat"),
                    rs.getString("DonViTinh"),
                    rs.getDouble("TonHienTai"),
                    tt,
                    rs.getInt("SoNgayDung"),
                    rs.getString("MonDung")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Nguyên liệu cần nhập thêm */
    public static List<Object[]> getNguyenLieuCanNhap() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT MaNL, TenNguyenLieu, DonViTinh, SoLuong, MucTonToiThieu, " +
                     "       (MucTonToiThieu - SoLuong) AS CanNhapThem " +
                     "FROM nguyenlieu " +
                     "WHERE TrangThai = 1 AND SoLuong <= MucTonToiThieu " +
                     "ORDER BY (MucTonToiThieu - SoLuong) DESC";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaNL"),
                    rs.getString("TenNguyenLieu"),
                    rs.getDouble("SoLuong"),
                    rs.getDouble("MucTonToiThieu"),
                    rs.getDouble("CanNhapThem"),
                    rs.getString("DonViTinh")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private static LichSuXuatKho mapRow(ResultSet rs) throws SQLException {
        LichSuXuatKho xk = new LichSuXuatKho();
        xk.setMaXuat(rs.getInt("MaXuat"));
        Timestamp ts = rs.getTimestamp("NgayXuat");
        if (ts != null) xk.setNgayXuat(ts.toLocalDateTime());
        xk.setMaNL(rs.getInt("MaNL"));
        xk.setSoLuong(rs.getDouble("SoLuong"));
        xk.setDonViTinh(rs.getString("DonViTinh"));
        xk.setLyDo(rs.getString("LyDo"));
        xk.setGhiChu(rs.getString("GhiChu"));
        try {
            xk.setTenNguyenLieu(rs.getString("TenNguyenLieu"));
            xk.setTenMon(rs.getString("TenMon"));
            xk.setTenNV(rs.getString("TenNV"));
        } catch (SQLException ignored) {}
        return xk;
    }
}