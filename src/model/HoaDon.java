package model;
import java.math.BigDecimal;
import java.sql.Date;


public class HoaDon {
    private int maCTHD;
    private Date ngayThanhToan;
    private int maBan;
    private int maCTBH;
    private String danhSachMon;
    private int tongSoLuongMon;
    private BigDecimal tongTienThanhToan;
    private BigDecimal phanTramGiamGia;
    private String loaiHoaDon; // "Tại quán" hoặc "Mang về"
    
    public HoaDon() {}
    
    // Getters and Setters
    public int getMaCTHD() { return maCTHD; }
    public void setMaCTHD(int maCTHD) { this.maCTHD = maCTHD; }
    
    public Date getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(Date ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }
    
    public int getMaBan() { return maBan; }
    public void setMaBan(int maBan) { this.maBan = maBan; }
    
    public int getMaCTBH() { return maCTBH; }
    public void setMaCTBH(int maCTBH) { this.maCTBH = maCTBH; }
    
    public String getDanhSachMon() { return danhSachMon; }
    public void setDanhSachMon(String danhSachMon) { this.danhSachMon = danhSachMon; }
    
    public int getTongSoLuongMon() { return tongSoLuongMon; }
    public void setTongSoLuongMon(int tongSoLuongMon) { this.tongSoLuongMon = tongSoLuongMon; }
    
    public BigDecimal getTongTienThanhToan() { return tongTienThanhToan; }
    public void setTongTienThanhToan(BigDecimal tongTienThanhToan) { this.tongTienThanhToan = tongTienThanhToan; }
    
    public BigDecimal getPhanTramGiamGia() { return phanTramGiamGia; }
    public void setPhanTramGiamGia(BigDecimal phanTramGiamGia) { this.phanTramGiamGia = phanTramGiamGia; }
    
    public String getLoaiHoaDon() { return loaiHoaDon; }
    public void setLoaiHoaDon(String loaiHoaDon) { this.loaiHoaDon = loaiHoaDon; }
}