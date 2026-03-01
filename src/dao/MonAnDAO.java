package dao;

import config.DatabaseConfig;
import model.MonAn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MonAnDAO {

    // ════════════════ READ ════════════════

    public static List<MonAn> getAllMonAn() {
        List<MonAn> list = new ArrayList<>();
        String sql = "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai FROM monan ORDER BY TenMon";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<MonAn> getMonAnByNhom(int maNhom) {
        List<MonAn> list = new ArrayList<>();
        String sql = "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai " +
                     "FROM monan WHERE MaNhom=? ORDER BY TenMon";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNhom);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static MonAn getMonAnById(int maMon) {
        String sql = "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai FROM monan WHERE MaMon=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Tính số lượng khả dụng THỰC TẾ từ tồn kho nguyên liệu.
     *   - Nếu có công thức NL → MIN(TonKhoNL / SoLuongCan)
     *   - Nếu không có công thức → lấy SoLuongConLai thủ công
     */
    public static int tinhSoLuongKhaDung(int maMon) {
        // Kiểm tra có công thức không
        String checkSql = "SELECT COUNT(*) FROM congthucmon WHERE MaMon=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(checkSql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                // Không có công thức → trả về SoLuongConLai thủ công
                MonAn m = getMonAnById(maMon);
                return m != null ? m.getSoLuongConLai() : 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Có công thức → tính từ tồn kho
        String sql = "SELECT FLOOR(MIN(nl.SoLuong / NULLIF(ct.SoLuongCan,0))) AS KhaDung " +
                     "FROM congthucmon ct " +
                     "JOIN nguyenlieu nl ON nl.MaNL=ct.MaNL " +
                     "WHERE ct.MaMon=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int val = rs.getInt("KhaDung");
                return rs.wasNull() ? 0 : Math.max(0, val);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /**
     * Lấy danh sách nguyên liệu + số lượng cần + tồn kho + số phần có thể làm.
     * Dùng cho FormThemMonAn để hiển thị panel NL chi tiết.
     *
     * @return List<Object[]> mỗi phần tử: {TenNguyenLieu, SoLuongCan, DonViTinh, TonKho, CoTheLam}
     */
    public static List<Object[]> getCongThucNguyenLieu(int maMon) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nl.TenNguyenLieu, ct.SoLuongCan, ct.DonViTinh, nl.SoLuong AS TonKho, " +
                     "FLOOR(nl.SoLuong / NULLIF(ct.SoLuongCan,0)) AS CoTheLam " +
                     "FROM congthucmon ct " +
                     "JOIN nguyenlieu nl ON nl.MaNL=ct.MaNL " +
                     "WHERE ct.MaMon=? ORDER BY nl.TenNguyenLieu";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenNguyenLieu"),
                    rs.getDouble("SoLuongCan"),
                    rs.getString("DonViTinh"),
                    rs.getDouble("TonKho"),
                    rs.getInt("CoTheLam")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy tất cả món kèm SL khả dụng real-time (dùng trong bảng FormThemMonAn).
     * @return List<Object[]>: {MaMon, TenMon, GiaTien, DonViTinh, SoLuongConLai,
     *                          SLKhaDung, TenNhom, HinhAnh, TrangThai}
     */
    public static List<Object[]> getAllMonAnVoiKhaDung() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT m.MaMon, m.TenMon, m.GiaTien, m.DonViTinh, m.SoLuongConLai, " +
            "COALESCE(FLOOR(MIN(nl.SoLuong / NULLIF(ct.SoLuongCan,0))), m.SoLuongConLai) AS SLKhaDung, " +
            "n.TenNhom, m.HinhAnh " +
            "FROM monan m " +
            "LEFT JOIN nhom n ON n.MaNhom=m.MaNhom " +
            "LEFT JOIN congthucmon ct ON ct.MaMon=m.MaMon " +
            "LEFT JOIN nguyenlieu nl ON nl.MaNL=ct.MaNL " +
            "GROUP BY m.MaMon, m.TenMon, m.GiaTien, m.DonViTinh, m.SoLuongConLai, n.TenNhom, m.HinhAnh " +
            "ORDER BY m.MaMon DESC";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int sl = rs.getInt("SLKhaDung");
                String tt = sl <= 0 ? "Hết món" : (sl <= 5 ? "Sắp hết" : "Còn hàng");
                list.add(new Object[]{
                    rs.getInt("MaMon"), rs.getString("TenMon"), rs.getDouble("GiaTien"),
                    rs.getString("DonViTinh"), rs.getInt("SoLuongConLai"),
                    sl, rs.getString("TenNhom"), rs.getString("HinhAnh"), tt
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ════════════════ WRITE ════════════════

    public static boolean addMonAn(MonAn monAn) {
        String sql = "INSERT INTO monan (TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, monAn.getTenMon());
            ps.setString(2, monAn.getHinhAnh());
            ps.setString(3, monAn.getDonViTinh());
            ps.setBigDecimal(4, monAn.getGiaTien());
            ps.setInt(5, monAn.getMaNhom());
            ps.setInt(6, monAn.getSoLuongConLai());
            if (ps.executeUpdate() > 0) {
                ResultSet k = ps.getGeneratedKeys();
                if (k.next()) monAn.setMaMon(k.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cập nhật SoLuongConLai thủ công.
     * Nếu món có công thức NL → nên gọi tinhLaiSoLuong() thay thế.
     */
    public static boolean updateSoLuong(int maMon, int soLuongMoi) {
        String sql = "UPDATE monan SET SoLuongConLai=? WHERE MaMon=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, soLuongMoi);
            ps.setInt(2, maMon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Gọi Stored Procedure để tính lại SoLuongConLai của 1 món từ tồn kho NL.
     * Gọi sau khi thêm/sửa/xóa congthucmon.
     */
    public static void tinhLaiSoLuong(int maMon) {
        try (Connection c = DatabaseConfig.getConnection();
             CallableStatement cs = c.prepareCall("{CALL sp_tinh_soLuong_mon(?)}")) {
            cs.setInt(1, maMon);
            cs.execute();
        } catch (SQLException e) {
            // Fallback: tính trực tiếp nếu stored proc chưa tồn tại
            tinhLaiSoLuongFallback(maMon);
        }
    }

    /**
     * Gọi Stored Procedure tính lại TẤT CẢ món.
     * Nên gọi sau khi nhập kho hoặc thay đổi nhiều công thức.
     */
    public static void tinhLaiTatCaMon() {
        try (Connection c = DatabaseConfig.getConnection();
             CallableStatement cs = c.prepareCall("{CALL sp_tinh_lai_tat_ca_mon()}")) {
            cs.execute();
        } catch (SQLException e) {
            // Fallback: tính trực tiếp
            String sql = "SELECT DISTINCT MaMon FROM congthucmon";
            try (Connection c2 = DatabaseConfig.getConnection();
                 Statement st = c2.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) tinhLaiSoLuongFallback(rs.getInt("MaMon"));
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    /** Fallback: tính trực tiếp không qua stored procedure. */
    private static void tinhLaiSoLuongFallback(int maMon) {
        String sql = "UPDATE monan SET SoLuongConLai = GREATEST(0, COALESCE(" +
                     "(SELECT FLOOR(MIN(nl.SoLuong / NULLIF(ct.SoLuongCan,0))) " +
                     "FROM congthucmon ct JOIN nguyenlieu nl ON nl.MaNL=ct.MaNL WHERE ct.MaMon=monan.MaMon), " +
                     "SoLuongConLai)) WHERE MaMon=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maMon);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ════════════════ SEARCH ════════════════

    public static List<MonAn> searchMonAn(int maNhom, String keyword) {
        List<MonAn> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT MaMon, TenMon, HinhAnh, DonViTinh, GiaTien, MaNhom, SoLuongConLai FROM monan WHERE 1=1 ");
        if (maNhom > 0) sql.append("AND MaNhom=? ");
        if (keyword != null && !keyword.trim().isEmpty()) sql.append("AND TenMon LIKE ? ");
        sql.append("ORDER BY TenMon");
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (maNhom > 0) ps.setInt(i++, maNhom);
            if (keyword != null && !keyword.trim().isEmpty()) ps.setString(i, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ════════════════ MAP ROW ════════════════

    private static MonAn mapRow(ResultSet rs) throws SQLException {
        MonAn m = new MonAn();
        m.setMaMon(rs.getInt("MaMon"));
        m.setTenMon(rs.getString("TenMon"));
        m.setHinhAnh(rs.getString("HinhAnh"));
        m.setDonViTinh(rs.getString("DonViTinh"));
        m.setGiaTien(rs.getBigDecimal("GiaTien"));
        m.setMaNhom(rs.getInt("MaNhom"));
        m.setSoLuongConLai(rs.getInt("SoLuongConLai"));
        return m;
    }
}