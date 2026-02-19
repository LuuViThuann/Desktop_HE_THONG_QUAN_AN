package model;

import java.sql.Date;
import java.sql.Time;

public class DatBanTruoc {
    private int maDatBan;
    private String hoTenKhachHang;
    private String sdt;
    private int soLuongKhach;
    private Date ngayDat;
    private Time gioDat;
    private int maBan;
    private String trangThai;
    private String tenBan;
    private String tenKV;
    public DatBanTruoc() {}
    
    // Constructor để tạo đặt bàn mới (không cần MaDatBan vì AUTO_INCREMENT)
    public DatBanTruoc(String hoTenKhachHang, String sdt, int soLuongKhach, 
                       Date ngayDat, Time gioDat, int maBan) {
        this.hoTenKhachHang = hoTenKhachHang;
        this.sdt = sdt;
        this.soLuongKhach = soLuongKhach;
        this.ngayDat = ngayDat;
        this.gioDat = gioDat;
        this.maBan = maBan;
        this.trangThai = "Đã đặt";
    }
    
    // Getters and Setters
    public int getMaDatBan() {
        return maDatBan;
    }
    
    public void setMaDatBan(int maDatBan) {
        this.maDatBan = maDatBan;
    }
    
    public String getHoTenKhachHang() {
        return hoTenKhachHang;
    }
    
    public void setHoTenKhachHang(String hoTenKhachHang) {
        this.hoTenKhachHang = hoTenKhachHang;
    }
    
    public String getSdt() {
        return sdt;
    }
    
    public void setSdt(String sdt) {
        this.sdt = sdt;
    }
    
    public int getSoLuongKhach() {
        return soLuongKhach;
    }
    
    public void setSoLuongKhach(int soLuongKhach) {
        this.soLuongKhach = soLuongKhach;
    }
    
    public Date getNgayDat() {
        return ngayDat;
    }
    
    public void setNgayDat(Date ngayDat) {
        this.ngayDat = ngayDat;
    }
    
    public Time getGioDat() {
        return gioDat;
    }
    
    public void setGioDat(Time gioDat) {
        this.gioDat = gioDat;
    }
    
    public int getMaBan() {
        return maBan;
    }
    
    public void setMaBan(int maBan) {
        this.maBan = maBan;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    public String getTenBan() {
        return tenBan;
    }
    
    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }
    
    public String getTenKV() {
        return tenKV;
    }
    
    public void setTenKV(String tenKV) {
        this.tenKV = tenKV;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}