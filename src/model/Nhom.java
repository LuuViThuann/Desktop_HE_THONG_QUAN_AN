package model;

public class Nhom {
    private int maNhom;
    private String tenNhom;
    
    // Constructors
    public Nhom() {}
    
    public Nhom(int maNhom, String tenNhom) {
        this.maNhom = maNhom;
        this.tenNhom = tenNhom;
    }
    
    // Getters and Setters
    public int getMaNhom() {
        return maNhom;
    }
    
    public void setMaNhom(int maNhom) {
        this.maNhom = maNhom;
    }
    
    public String getTenNhom() {
        return tenNhom;
    }
    
    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }
    
    @Override
    public String toString() {
        return tenNhom;
    }
}