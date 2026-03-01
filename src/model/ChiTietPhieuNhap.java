package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model: Chi tiết phiếu nhập hàng
 * Bảng: chitietphieunhap
 */
public class ChiTietPhieuNhap {
    private int maCTPN;
    private int maPhieu;
    private int maNL;
    private double soLuong;
    private String donViTinh;
    private BigDecimal donGiaNhap;
    private LocalDate hanSuDung;
    private String ghiChu;

    // Join fields
    private String tenNguyenLieu;
    private BigDecimal thanhTien; // soLuong * donGiaNhap

    public ChiTietPhieuNhap() {
        this.donGiaNhap = BigDecimal.ZERO;
    }

    public ChiTietPhieuNhap(int maNL, double soLuong, String donViTinh, BigDecimal donGiaNhap) {
        this.maNL        = maNL;
        this.soLuong     = soLuong;
        this.donViTinh   = donViTinh;
        this.donGiaNhap  = donGiaNhap;
    }

    /** Tính thành tiền */
    public BigDecimal tinhThanhTien() {
        if (donGiaNhap == null) return BigDecimal.ZERO;
        return donGiaNhap.multiply(BigDecimal.valueOf(soLuong));
    }

    // ======================== GETTERS / SETTERS ========================

    public int getMaCTPN()                      { return maCTPN; }
    public void setMaCTPN(int v)                { this.maCTPN = v; }

    public int getMaPhieu()                     { return maPhieu; }
    public void setMaPhieu(int v)               { this.maPhieu = v; }

    public int getMaNL()                        { return maNL; }
    public void setMaNL(int v)                  { this.maNL = v; }

    public double getSoLuong()                  { return soLuong; }
    public void setSoLuong(double v)            { this.soLuong = v; }

    public String getDonViTinh()                { return donViTinh; }
    public void setDonViTinh(String v)          { this.donViTinh = v; }

    public BigDecimal getDonGiaNhap()           { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal v)     { this.donGiaNhap = v; }

    public LocalDate getHanSuDung()             { return hanSuDung; }
    public void setHanSuDung(LocalDate v)       { this.hanSuDung = v; }

    public String getGhiChu()                  { return ghiChu; }
    public void setGhiChu(String v)            { this.ghiChu = v; }

    public String getTenNguyenLieu()           { return tenNguyenLieu; }
    public void setTenNguyenLieu(String v)     { this.tenNguyenLieu = v; }

    public BigDecimal getThanhTien()           { return thanhTien != null ? thanhTien : tinhThanhTien(); }
    public void setThanhTien(BigDecimal v)     { this.thanhTien = v; }
}