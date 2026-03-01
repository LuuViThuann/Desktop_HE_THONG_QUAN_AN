package model;

import java.time.LocalDateTime;

/**
 * Model: Lịch sử xuất kho / sử dụng nguyên liệu
 * Bảng: lichsuxuatkho
 */
public class LichSuXuatKho {
    private int maXuat;
    private LocalDateTime ngayXuat;
    private int maNL;
    private double soLuong;
    private String donViTinh;
    private String lyDo;
    private int maHoaDon;
    private int maMon;
    private int maNV;
    private String ghiChu;

    // Join fields
    private String tenNguyenLieu;
    private String tenMon;
    private String tenNV;

    public LichSuXuatKho() {
        this.ngayXuat = LocalDateTime.now();
        this.lyDo     = "Sử dụng sản xuất";
    }

    // ======================== GETTERS / SETTERS ========================

    public int getMaXuat()                       { return maXuat; }
    public void setMaXuat(int v)                 { this.maXuat = v; }

    public LocalDateTime getNgayXuat()           { return ngayXuat; }
    public void setNgayXuat(LocalDateTime v)     { this.ngayXuat = v; }

    public int getMaNL()                         { return maNL; }
    public void setMaNL(int v)                   { this.maNL = v; }

    public double getSoLuong()                   { return soLuong; }
    public void setSoLuong(double v)             { this.soLuong = v; }

    public String getDonViTinh()                 { return donViTinh; }
    public void setDonViTinh(String v)           { this.donViTinh = v; }

    public String getLyDo()                      { return lyDo; }
    public void setLyDo(String v)                { this.lyDo = v; }

    public int getMaHoaDon()                     { return maHoaDon; }
    public void setMaHoaDon(int v)               { this.maHoaDon = v; }

    public int getMaMon()                        { return maMon; }
    public void setMaMon(int v)                  { this.maMon = v; }

    public int getMaNV()                         { return maNV; }
    public void setMaNV(int v)                   { this.maNV = v; }

    public String getGhiChu()                   { return ghiChu; }
    public void setGhiChu(String v)             { this.ghiChu = v; }

    public String getTenNguyenLieu()            { return tenNguyenLieu; }
    public void setTenNguyenLieu(String v)      { this.tenNguyenLieu = v; }

    public String getTenMon()                   { return tenMon; }
    public void setTenMon(String v)             { this.tenMon = v; }

    public String getTenNV()                    { return tenNV; }
    public void setTenNV(String v)              { this.tenNV = v; }
}