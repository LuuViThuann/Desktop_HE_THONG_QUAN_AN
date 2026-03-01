package dao;

import config.DatabaseConfig;
import java.sql.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;


public class ThongKeDAO {
    
    // ==================== DASHBOARD DATA ====================
    
    /**
     * Lấy tất cả dữ liệu cho dashboard
     */
    public static Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        // Doanh thu
        BigDecimal doanhThuHomNay = getDoanhThuHomNay();
        BigDecimal doanhThuHomQua = getDoanhThuHomQua();
        
        // Tính % tăng giảm
        BigDecimal tangGiam = BigDecimal.ZERO;
        if (doanhThuHomQua.compareTo(BigDecimal.ZERO) > 0) {
            tangGiam = doanhThuHomNay.subtract(doanhThuHomQua)
                                     .divide(doanhThuHomQua, 4, BigDecimal.ROUND_HALF_UP)
                                     .multiply(new BigDecimal(100));
        } else if (doanhThuHomNay.compareTo(BigDecimal.ZERO) > 0) {
            tangGiam = new BigDecimal(100);
        }
        
        data.put("doanhThuHomNay", doanhThuHomNay);
        data.put("doanhThuHomQua", doanhThuHomQua);
        data.put("tangGiamDoanhThu", tangGiam);
        
        // Hóa đơn
        data.put("soHoaDonHomNay", getTongSoHoaDon());
        data.put("soHoaDonDangMo", getSoHoaDonDangMo());
        data.put("giaTriTrungBinh", getGiaTriHoaDonTrungBinh());
        
        // Bàn
        data.put("thongKeBan", getThongKeBan());
        
        return data;
    }
    
    /**
     * Thống kê bàn
     */
    public static Map<String, Integer> getThongKeBan() {
        Map<String, Integer> map = new HashMap<>();
        map.put("TongBan", getTongSoBan());
        map.put("DangSuDung", getSoBanDangSuDung());
        map.put("Trong", getSoBanTrong());
        map.put("DaDat", getSoBanDaDat());
        return map;
    }
    
    // ==================== DOANH THU ====================
    
    public static BigDecimal getDoanhThuHomNay() {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE DATE(NgayThanhToan) = CURDATE()";
        return executeScalarBigDecimal(query);
    }
    
    public static BigDecimal getDoanhThuHomQua() {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE DATE(NgayThanhToan) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
        return executeScalarBigDecimal(query);
    }
    
    public static BigDecimal getDoanhThuTuanNay() {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE YEARWEEK(NgayThanhToan, 1) = YEARWEEK(CURDATE(), 1)";
        return executeScalarBigDecimal(query);
    }
    
    public static BigDecimal getDoanhThuTuanTruoc() {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE YEARWEEK(NgayThanhToan, 1) = YEARWEEK(DATE_SUB(CURDATE(), INTERVAL 1 WEEK), 1)";
        return executeScalarBigDecimal(query);
    }
    
    public static BigDecimal getDoanhThuThangNay() {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE MONTH(NgayThanhToan) = MONTH(CURDATE()) " +
                      "AND YEAR(NgayThanhToan) = YEAR(CURDATE())";
        return executeScalarBigDecimal(query);
    }
    
    public static BigDecimal getDoanhThuThangTruoc() {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE MONTH(NgayThanhToan) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) " +
                      "AND YEAR(NgayThanhToan) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))";
        return executeScalarBigDecimal(query);
    }
    
    public static BigDecimal getDoanhThuTheoKhoang(LocalDate tuNgay, LocalDate denNgay) {
        String query = "SELECT COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE DATE(NgayThanhToan) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(tuNgay));
            pstmt.setDate(2, Date.valueOf(denNgay));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new BigDecimal(rs.getLong("DoanhThu"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    public static List<Object[]> getDoanhThuTheoNgayTrongThang(int thang, int nam) {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT DAY(NgayThanhToan) as Ngay, " +
                      "COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE MONTH(NgayThanhToan) = ? AND YEAR(NgayThanhToan) = ? " +
                      "GROUP BY DAY(NgayThanhToan) " +
                      "ORDER BY Ngay";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, thang);
            pstmt.setInt(2, nam);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{ rs.getInt("Ngay"), new BigDecimal(rs.getLong("DoanhThu")) });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static Map<String, BigDecimal> getDoanhThu7NgayGanNhat() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String query = "SELECT DATE(NgayThanhToan) as Ngay, " +
                      "COALESCE(SUM(TongTienThanhToan), 0) as DoanhThu " +
                      "FROM HoaDonKhachHang " +
                      "WHERE NgayThanhToan >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                      "GROUP BY DATE(NgayThanhToan) ORDER BY Ngay";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) map.put(rs.getString("Ngay"), new BigDecimal(rs.getLong("DoanhThu")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    
    public static Map<String, BigDecimal> getDoanhThuTheoKhuVuc() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String query = "SELECT kv.TenKV, COALESCE(SUM(hd.TongTienThanhToan), 0) as DoanhThu " +
                      "FROM KhuVucQuan kv " +
                      "LEFT JOIN Ban b ON kv.MaKV = b.MaKV " +
                      "LEFT JOIN HoaDonKhachHang hd ON b.MaBan = hd.MaBan " +
                      "WHERE hd.NgayThanhToan IS NULL OR DATE(hd.NgayThanhToan) = CURDATE() " +
                      "GROUP BY kv.MaKV, kv.TenKV ORDER BY DoanhThu DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) map.put(rs.getString("TenKV"), new BigDecimal(rs.getLong("DoanhThu")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    
    // ==================== MÓN ĂN ====================
    
    /**
     * Top món bán chạy theo khoảng thời gian và số lượng
     */
    public static List<Object[]> getTopMonBanChay(int top, LocalDate tuNgay, LocalDate denNgay) {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT m.TenMon, " +
                      "COALESCE(SUM(ct.SoLuong), 0) as TongSoLuong, " +
                      "COALESCE(SUM(ct.GiaTien * ct.SoLuong), 0) as DoanhThu " +
                      "FROM MonAn m " +
                      "LEFT JOIN ChiTietHoaDon ct ON m.MaMon = ct.MaMon " +
                      "LEFT JOIN HoaDonKhachHang hd ON ct.MaCTHD = hd.MaCTHD " +
                      "WHERE DATE(hd.NgayThanhToan) BETWEEN ? AND ? " +
                      "GROUP BY m.MaMon, m.TenMon " +
                      "ORDER BY TongSoLuong DESC LIMIT ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(tuNgay));
            pstmt.setDate(2, Date.valueOf(denNgay));
            pstmt.setInt(3, top);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenMon"),
                    rs.getInt("TongSoLuong"),
                    new BigDecimal(rs.getLong("DoanhThu"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<Object[]> getTop5MonBanChay() {
        LocalDate denNgay = LocalDate.now();
        LocalDate tuNgay = denNgay.minusDays(30);
        return getTopMonBanChay(5, tuNgay, denNgay);
    }
    
    public static List<Object[]> getTop5MonBanCham() {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT m.TenMon, " +
                      "COALESCE(SUM(ct.SoLuong), 0) as TongSoLuong, " +
                      "COALESCE(SUM(ct.GiaTien * ct.SoLuong), 0) as DoanhThu " +
                      "FROM MonAn m " +
                      "LEFT JOIN ChiTietHoaDon ct ON m.MaMon = ct.MaMon " +
                      "LEFT JOIN HoaDonKhachHang hd ON ct.MaCTHD = hd.MaCTHD " +
                      "WHERE hd.NgayThanhToan >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                      "GROUP BY m.MaMon, m.TenMon " +
                      "ORDER BY TongSoLuong ASC LIMIT 5";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenMon"),
                    rs.getInt("TongSoLuong"),
                    new BigDecimal(rs.getLong("DoanhThu"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tổng số món đã bán hôm nay
     */
    public static int getTongMonDaBan() {
        String query = "SELECT COALESCE(SUM(ct.SoLuong), 0) as TongMon " +
                      "FROM ChiTietHoaDon ct " +
                      "JOIN HoaDonKhachHang hd ON ct.MaCTHD = hd.MaCTHD " +
                      "WHERE DATE(hd.NgayThanhToan) = CURDATE()";
        return executeScalarInt(query);
    }

    // ==================== THỐNG KÊ MÓN BÁN HÔM NAY (MỚI) ====================

    /**
     * Lấy chi tiết tất cả món bán hôm nay:
     * [TenMon, NhomMon, SoLuong, DoanhThu, TyLe(%), SoLuongConLai, GiaTien]
     */
    public static List<Object[]> getChiTietMonBanHomNay() {
        List<Object[]> list = new ArrayList<>();

        // Tính tổng số lượng bán hôm nay trước (để tính tỷ lệ %)
        String queryTotal = "SELECT COALESCE(SUM(ct.SoLuong), 0) " +
                           "FROM ChiTietHoaDon ct " +
                           "JOIN HoaDonKhachHang hd ON ct.MaCTHD = hd.MaCTHD " +
                           "WHERE DATE(hd.NgayThanhToan) = CURDATE()";
        int tongSoLuong = executeScalarInt(queryTotal);

        String query =
            "SELECT m.TenMon, " +
            "       n.TenNhom, " +
            "       COALESCE(SUM(ct.SoLuong), 0)              AS SoLuong, " +
            "       COALESCE(SUM(ct.GiaTien * ct.SoLuong), 0) AS DoanhThu, " +
            "       m.SoLuongConLai, " +
            "       m.GiaTien " +
            "FROM MonAn m " +
            "JOIN Nhom n ON m.MaNhom = n.MaNhom " +
            "LEFT JOIN ChiTietHoaDon ct ON m.MaMon = ct.MaMon " +
            "LEFT JOIN HoaDonKhachHang hd ON ct.MaCTHD = hd.MaCTHD " +
            "    AND DATE(hd.NgayThanhToan) = CURDATE() " +
            "GROUP BY m.MaMon, m.TenMon, n.TenNhom, m.SoLuongConLai, m.GiaTien " +
            "ORDER BY SoLuong DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int soLuong = rs.getInt("SoLuong");
                double tyLe = (tongSoLuong > 0) ? (soLuong * 100.0 / tongSoLuong) : 0.0;

                list.add(new Object[]{
                    rs.getString("TenMon"),            // [0] Tên món
                    rs.getString("TenNhom"),           // [1] Nhóm
                    soLuong,                           // [2] Số lượng đã bán
                    new BigDecimal(rs.getLong("DoanhThu")), // [3] Doanh thu
                    tyLe,                              // [4] Tỷ lệ % so với tổng
                    rs.getInt("SoLuongConLai"),        // [5] Tồn kho còn lại
                    new BigDecimal(rs.getLong("GiaTien"))   // [6] Đơn giá
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** ---------------------------------------------------------------------------------------
     * Tổng số lượng món bán theo từng nhóm hôm nay:
     * [TenNhom, TongSoLuong, TongDoanhThu]
     */
    public static List<Object[]> getMonBanTheoNhomHomNay() {
        List<Object[]> list = new ArrayList<>();
        String query =
            "SELECT n.TenNhom, " +
            "       COALESCE(SUM(ct.SoLuong), 0)              AS TongSoLuong, " +
            "       COALESCE(SUM(ct.GiaTien * ct.SoLuong), 0) AS TongDoanhThu " +
            "FROM Nhom n " +
            "LEFT JOIN MonAn m ON m.MaNhom = n.MaNhom " +
            "LEFT JOIN ChiTietHoaDon ct ON ct.MaMon = m.MaMon " +
            "LEFT JOIN HoaDonKhachHang hd ON hd.MaCTHD = ct.MaCTHD " +
            "    AND DATE(hd.NgayThanhToan) = CURDATE() " +
            "GROUP BY n.MaNhom, n.TenNhom " +
            "ORDER BY TongSoLuong DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenNhom"),
                    rs.getInt("TongSoLuong"),
                    new BigDecimal(rs.getLong("TongDoanhThu"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // ---------------------------------------------------------------------------------------
    /**
     * Thống kê số lượng bán trong ngày hôm nay:
     * [Gio (int), SoLuong, DoanhThu]
     */
    public static List<Object[]> getMonBanTheoGioHomNay() {
        List<Object[]> list = new ArrayList<>();
        String query =
            "SELECT HOUR(hd.NgayCapNhatThanhToan) AS Gio, " +
            "       COALESCE(SUM(ct.SoLuong), 0)              AS SoLuong, " +
            "       COALESCE(SUM(ct.GiaTien * ct.SoLuong), 0) AS DoanhThu " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDonKhachHang hd ON ct.MaCTHD = hd.MaCTHD " +
            "WHERE DATE(hd.NgayThanhToan) = CURDATE() " +
            "GROUP BY HOUR(hd.NgayCapNhatThanhToan) " +
            "ORDER BY Gio";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("Gio"),
                    rs.getInt("SoLuong"),
                    new BigDecimal(rs.getLong("DoanhThu"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== TÌNH TRẠNG BÀN ====================
    
    public static int getTongSoBan() {
        return executeScalarInt("SELECT COUNT(*) as TongBan FROM Ban");
    }
    
    public static int getSoBanDangSuDung() {
        return executeScalarInt("SELECT COUNT(*) as SoBan FROM Ban WHERE TrangThai = 'Đang sử dụng'");
    }
    
    public static int getSoBanTrong() {
        return executeScalarInt("SELECT COUNT(*) as SoBan FROM Ban WHERE TrangThai = 'Trống'");
    }
    
    public static int getSoBanDaDat() {
        return executeScalarInt("SELECT COUNT(*) as SoBan FROM Ban WHERE TrangThai = 'Đã đặt'");
    }
    
    public static double getCongSuatSuDung() {
        int tongBan = getTongSoBan();
        if (tongBan == 0) return 0;
        return (getSoBanDangSuDung() * 100.0) / tongBan;
    }
    
    public static List<Object[]> getThongKeBanTheoKhuVuc() {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT kv.TenKV, " +
                      "COUNT(b.MaBan) as TongBan, " +
                      "SUM(CASE WHEN b.TrangThai = 'Đang sử dụng' THEN 1 ELSE 0 END) as BanDangDung, " +
                      "SUM(CASE WHEN b.TrangThai = 'Trống' THEN 1 ELSE 0 END) as BanTrong, " +
                      "SUM(CASE WHEN b.TrangThai = 'Đã đặt' THEN 1 ELSE 0 END) as BanDaDat " +
                      "FROM KhuVucQuan kv " +
                      "LEFT JOIN Ban b ON kv.MaKV = b.MaKV " +
                      "GROUP BY kv.MaKV, kv.TenKV ORDER BY kv.MaKV";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int tongBan = rs.getInt("TongBan");
                int banDangDung = rs.getInt("BanDangDung");
                double congSuat = tongBan > 0 ? (banDangDung * 100.0 / tongBan) : 0;
                list.add(new Object[]{
                    rs.getString("TenKV"), tongBan, banDangDung,
                    rs.getInt("BanTrong"), congSuat
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // ==================== NHÂN VIÊN ====================
    
    public static List<Object[]> getSoHoaDonTheoNhanVien() {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT nv.HoTen, COUNT(DISTINCT hdbh.MaHDBH) as SoHoaDon " +
                      "FROM NhanVien nv " +
                      "LEFT JOIN HoaDonBanHang hdbh ON nv.MaNV = hdbh.MaNV " +
                      "WHERE DATE(hdbh.NgayVao) = CURDATE() " +
                      "GROUP BY nv.MaNV, nv.HoTen ORDER BY SoHoaDon DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("HoTen"), rs.getInt("SoHoaDon") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static String getNhanVienXuatSac() {
        String query = "SELECT nv.HoTen, COUNT(DISTINCT hdbh.MaHDBH) as SoHoaDon " +
                      "FROM NhanVien nv " +
                      "LEFT JOIN HoaDonBanHang hdbh ON nv.MaNV = hdbh.MaNV " +
                      "WHERE DATE(hdbh.NgayVao) = CURDATE() " +
                      "GROUP BY nv.MaNV, nv.HoTen ORDER BY SoHoaDon DESC LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getString("HoTen") + " (" + rs.getInt("SoHoaDon") + " HĐ)";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Chưa có dữ liệu";
    }
    
    // ==================== HÓA ĐƠN ====================
    
    public static int getTongSoHoaDon() {
        return executeScalarInt("SELECT COUNT(*) as TongHD FROM HoaDonKhachHang " +
                               "WHERE DATE(NgayThanhToan) = CURDATE()");
    }
    
    public static int getSoHoaDonDangMo() {
        return executeScalarInt("SELECT COUNT(DISTINCT MaBan) as SoHD FROM HoaDonBanHang");
    }
    
    public static BigDecimal getGiaTriHoaDonTrungBinh() {
        return executeScalarBigDecimal("SELECT COALESCE(AVG(TongTienThanhToan), 0) as TB " +
                                      "FROM HoaDonKhachHang " +
                                      "WHERE DATE(NgayThanhToan) = CURDATE()");
    }
    
    public static Map<String, Integer> getThongKeHoaDonTheoLoai() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String query = "SELECT COALESCE(LoaiHoaDon, 'Tại quán') as Loai, COUNT(*) as SoLuong " +
                      "FROM HoaDonKhachHang WHERE DATE(NgayThanhToan) = CURDATE() " +
                      "GROUP BY LoaiHoaDon";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) map.put(rs.getString("Loai"), rs.getInt("SoLuong"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    
    // ==================== PHƯƠNG THỨC HỖ TRỢ ====================
    
    private static int executeScalarInt(String query) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private static BigDecimal executeScalarBigDecimal(String query) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return new BigDecimal(rs.getLong(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    public static double tinhPhanTramThayDoi(BigDecimal giaTri, BigDecimal giaTriTruoc) {
        if (giaTriTruoc.compareTo(BigDecimal.ZERO) == 0) {
            return giaTri.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
        BigDecimal chenhLech = giaTri.subtract(giaTriTruoc);
        return chenhLech.divide(giaTriTruoc, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue();
    }
}