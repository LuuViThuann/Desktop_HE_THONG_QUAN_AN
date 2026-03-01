package model;

/**
 * Model: Công thức món (nguyên liệu cần cho từng món ăn)
 * Bảng: congthucmon
 */
public class CongThucMon {
    private int maCT;
    private int maMon;
    private int maNL;
    private double soLuongCan;
    private String donViTinh;
    private String ghiChu;

    // Join fields (hiển thị)
    private String tenMon;
    private String tenNguyenLieu;

    public CongThucMon() {}

    public CongThucMon(int maMon, int maNL, double soLuongCan, String donViTinh) {
        this.maMon       = maMon;
        this.maNL        = maNL;
        this.soLuongCan  = soLuongCan;
        this.donViTinh   = donViTinh;
    }

    // ======================== GETTERS / SETTERS ========================

    public int getMaCT()                    { return maCT; }
    public void setMaCT(int v)              { this.maCT = v; }

    public int getMaMon()                   { return maMon; }
    public void setMaMon(int v)             { this.maMon = v; }

    public int getMaNL()                    { return maNL; }
    public void setMaNL(int v)              { this.maNL = v; }

    public double getSoLuongCan()           { return soLuongCan; }
    public void setSoLuongCan(double v)     { this.soLuongCan = v; }

    public String getDonViTinh()            { return donViTinh; }
    public void setDonViTinh(String v)      { this.donViTinh = v; }

    public String getGhiChu()              { return ghiChu; }
    public void setGhiChu(String v)        { this.ghiChu = v; }

    public String getTenMon()              { return tenMon; }
    public void setTenMon(String v)        { this.tenMon = v; }

    public String getTenNguyenLieu()       { return tenNguyenLieu; }
    public void setTenNguyenLieu(String v) { this.tenNguyenLieu = v; }

    @Override
    public String toString() {
        return tenNguyenLieu + " - " + soLuongCan + " " + donViTinh;
    }
}