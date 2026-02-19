package model;

import java.time.LocalDate;

public class ChamCongNhanVien {
    private int maCong;
    private LocalDate ngayCong;
    private String trangThai; 
    private int maNV;
    
    // Thông tin bổ sung để hiển thị
    private String hoTenNV;
    private String tenCa;

    // Constructors
    public ChamCongNhanVien() {}

    public ChamCongNhanVien(int maCong, LocalDate ngayCong, String trangThai, int maNV) {
        this.maCong = maCong;
        this.ngayCong = ngayCong;
        this.trangThai = trangThai;
        this.maNV = maNV;
    }

    // Getters and Setters
    public int getMaCong() {
        return maCong;
    }

    public void setMaCong(int maCong) {
        this.maCong = maCong;
    }

    public LocalDate getNgayCong() {
        return ngayCong;
    }

    public void setNgayCong(LocalDate ngayCong) {
        this.ngayCong = ngayCong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String hoTenNV) {
        this.hoTenNV = hoTenNV;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }
}