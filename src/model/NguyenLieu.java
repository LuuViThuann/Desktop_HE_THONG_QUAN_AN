package model;

import java.math.BigDecimal;

/**
 * Model: Nguyên Liệu
 * Bảng: nguyenlieu
 */
public class NguyenLieu {
    private int maNL;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal donGia;
    private double soLuong;

    // Các trường bổ sung
    private String donViTinhNhap;
    private double heSoQuyDoi;
    private double mucTonToiThieu;
    private double mucTonToiDa;
    private String ghiChu;
    private int trangThai; // 1 = Đang dùng, 0 = Ngừng

    public NguyenLieu() {
        this.heSoQuyDoi   = 1.0;
        this.mucTonToiThieu = 0;
        this.mucTonToiDa  = 9999;
        this.trangThai    = 1;
    }

    public NguyenLieu(int maNL, String tenNguyenLieu, String donViTinh,
                      BigDecimal donGia, double soLuong) {
        this();
        this.maNL           = maNL;
        this.tenNguyenLieu  = tenNguyenLieu;
        this.donViTinh      = donViTinh;
        this.donGia         = donGia;
        this.soLuong        = soLuong;
    }

    // ======================== GETTERS / SETTERS ========================

    public int getMaNL()                         { return maNL; }
    public void setMaNL(int maNL)                { this.maNL = maNL; }

    public String getTenNguyenLieu()             { return tenNguyenLieu; }
    public void setTenNguyenLieu(String v)       { this.tenNguyenLieu = v; }

    public String getDonViTinh()                 { return donViTinh; }
    public void setDonViTinh(String v)           { this.donViTinh = v; }

    public BigDecimal getDonGia()                { return donGia; }
    public void setDonGia(BigDecimal v)          { this.donGia = v; }

    public double getSoLuong()                   { return soLuong; }
    public void setSoLuong(double v)             { this.soLuong = v; }

    public String getDonViTinhNhap()             { return donViTinhNhap; }
    public void setDonViTinhNhap(String v)       { this.donViTinhNhap = v; }

    public double getHeSoQuyDoi()                { return heSoQuyDoi; }
    public void setHeSoQuyDoi(double v)          { this.heSoQuyDoi = v; }

    public double getMucTonToiThieu()            { return mucTonToiThieu; }
    public void setMucTonToiThieu(double v)      { this.mucTonToiThieu = v; }

    public double getMucTonToiDa()               { return mucTonToiDa; }
    public void setMucTonToiDa(double v)         { this.mucTonToiDa = v; }

    public String getGhiChu()                    { return ghiChu; }
    public void setGhiChu(String v)              { this.ghiChu = v; }

    public int getTrangThai()                    { return trangThai; }
    public void setTrangThai(int v)              { this.trangThai = v; }

    /** Trạng thái tồn kho */
    public String getTrangThaiTon() {
        if (soLuong <= 0)              return "Hết hàng";
        if (soLuong <= mucTonToiThieu) return "Sắp hết";
        if (soLuong >= mucTonToiDa)    return "Tồn quá nhiều";
        return "Bình thường";
    }

    @Override
    public String toString() { return tenNguyenLieu; }
}