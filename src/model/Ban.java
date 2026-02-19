package model;

public class Ban {
    private int maBan;
    private String tenBan;
    private String trangThai;
    private int maKV;
    private String tenKV; // Tên khu vực
    private String ghiChu;
    
    // Constructors
    public Ban() {}
    
    public Ban(int maBan, String tenBan, String trangThai, int maKV) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.trangThai = trangThai;
        this.maKV = maKV;
    }
    
    // Getters and Setters
    public int getMaBan() {
        return maBan;
    }
    
    public void setMaBan(int maBan) {
        this.maBan = maBan;
    }
    
    public String getTenBan() {
        return tenBan;
    }
    
    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public int getMaKV() {
        return maKV;
    }
    
    public void setMaKV(int maKV) {
        this.maKV = maKV;
    }
    
    public String getTenKV() {
        return tenKV;
    }
    
    public void setTenKV(String tenKV) {
        this.tenKV = tenKV;
    }
    
    public String getGhiChu() {
        return ghiChu;
    }
    
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    @Override
    public String toString() {
        return tenBan + " (" + trangThai + ")";
    }
}