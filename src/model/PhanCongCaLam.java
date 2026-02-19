package model;

import java.time.LocalDate;

public class PhanCongCaLam {
    private int maPhanCong;
    private int maNV;
    private int maCa;
    private LocalDate ngayLam;
    
    // Thông tin bổ sung để hiển thị
    private String hoTenNV;
    private String tenCa;
    private String gioBatDau;
    private String gioKetThuc;

    // Constructors
    public PhanCongCaLam() {}

    public PhanCongCaLam(int maPhanCong, int maNV, int maCa, LocalDate ngayLam) {
        this.maPhanCong = maPhanCong;
        this.maNV = maNV;
        this.maCa = maCa;
        this.ngayLam = ngayLam;
    }

    // Getters and Setters
    public int getMaPhanCong() {
        return maPhanCong;
    }

    public void setMaPhanCong(int maPhanCong) {
        this.maPhanCong = maPhanCong;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public int getMaCa() {
        return maCa;
    }

    public void setMaCa(int maCa) {
        this.maCa = maCa;
    }

    public LocalDate getNgayLam() {
        return ngayLam;
    }

    public void setNgayLam(LocalDate ngayLam) {
        this.ngayLam = ngayLam;
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

    public String getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(String gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public String getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(String gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }
}