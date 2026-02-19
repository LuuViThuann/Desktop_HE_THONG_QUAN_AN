package model;
import java.math.BigDecimal;
public class MonAn {
    private int maMon;
    private String tenMon;
    private String hinhAnh;
    private String donViTinh;
    private BigDecimal giaTien;
    private int maNhom;
    private int soLuongConLai;
    
    // Constructors
    public MonAn() {}
    
    public MonAn(int maMon, String tenMon, String hinhAnh, String donViTinh, 
                 BigDecimal giaTien, int maNhom, int soLuongConLai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.hinhAnh = hinhAnh;
        this.donViTinh = donViTinh;
        this.giaTien = giaTien;
        this.maNhom = maNhom;
        this.soLuongConLai = soLuongConLai;
    }
    
    // Getters and Setters
    public int getMaMon() {
        return maMon;
    }
    
    public void setMaMon(int maMon) {
        this.maMon = maMon;
    }
    
    public String getTenMon() {
        return tenMon;
    }
    
    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }
    
    public String getHinhAnh() {
        return hinhAnh;
    }
    
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
    
    public String getDonViTinh() {
        return donViTinh;
    }
    
    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }
    
    public BigDecimal getGiaTien() {
        return giaTien;
    }
    
    public void setGiaTien(BigDecimal giaTien) {
        this.giaTien = giaTien;
    }
    
    public int getMaNhom() {
        return maNhom;
    }
    
    public void setMaNhom(int maNhom) {
        this.maNhom = maNhom;
    }
    
    public int getSoLuongConLai() {
        return soLuongConLai;
    }
    
    public void setSoLuongConLai(int soLuongConLai) {
        this.soLuongConLai = soLuongConLai;
    }
    
    @Override
    public String toString() {
        return tenMon;
    }
}