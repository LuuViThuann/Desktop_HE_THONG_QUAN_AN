package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model: Phiếu Nhập Hàng (header)
 * Bảng: phieunhaphang
 */
public class PhieuNhapHang {
    private int maPhieu;
    private LocalDateTime ngayNhap;
    private String nhaCungCap;
    private int maNV;
    private BigDecimal tongTien;
    private String ghiChu;
    private String trangThai;

    // Join
    private String tenNV;

    // Chi tiết
    private List<ChiTietPhieuNhap> danhSachChiTiet = new ArrayList<>();

    public PhieuNhapHang() {
        this.ngayNhap  = LocalDateTime.now();
        this.trangThai = "Đã nhập";
        this.tongTien  = BigDecimal.ZERO;
    }

    // ======================== GETTERS / SETTERS ========================

    public int getMaPhieu()                        { return maPhieu; }
    public void setMaPhieu(int v)                  { this.maPhieu = v; }

    public LocalDateTime getNgayNhap()             { return ngayNhap; }
    public void setNgayNhap(LocalDateTime v)       { this.ngayNhap = v; }

    public String getNhaCungCap()                  { return nhaCungCap; }
    public void setNhaCungCap(String v)            { this.nhaCungCap = v; }

    public int getMaNV()                           { return maNV; }
    public void setMaNV(int v)                     { this.maNV = v; }

    public BigDecimal getTongTien()                { return tongTien; }
    public void setTongTien(BigDecimal v)          { this.tongTien = v; }

    public String getGhiChu()                     { return ghiChu; }
    public void setGhiChu(String v)               { this.ghiChu = v; }

    public String getTrangThai()                   { return trangThai; }
    public void setTrangThai(String v)             { this.trangThai = v; }

    public String getTenNV()                       { return tenNV; }
    public void setTenNV(String v)                 { this.tenNV = v; }

    public List<ChiTietPhieuNhap> getDanhSachChiTiet()            { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietPhieuNhap> list)   { this.danhSachChiTiet = list; }

    /** Tính lại tổng tiền từ danh sách chi tiết */
    public void tinhLaiTongTien() {
        tongTien = danhSachChiTiet.stream()
            .map(ct -> ct.getDonGiaNhap().multiply(BigDecimal.valueOf(ct.getSoLuong())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}