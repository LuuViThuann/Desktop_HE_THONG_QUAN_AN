package model;
import java.math.BigDecimal;
public class NhanVien {
    private int maNV;
    private String hoTen;
    private String gioiTinh;
    private String sdt;
    private String diaChi;
    private String email;
    private String matKhau;
    private int tongNgayCong;
    private BigDecimal tongLuong;
    private int maPQ;
    private String tenQuyen;
    
    // Constructors
    public NhanVien() {}
    
    // Getters and Setters
    public int getMaNV() {
        return maNV;
    }
    
    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }
    
    public String getHoTen() {
        return hoTen;
    }
    
    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }
    
    public String getGioiTinh() {
        return gioiTinh;
    }
    
    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }
    
    public String getSdt() {
        return sdt;
    }
    
    public void setSdt(String sdt) {
        this.sdt = sdt;
    }
    
    public String getDiaChi() {
        return diaChi;
    }
    
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMatKhau() {
        return matKhau;
    }
    
    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }
    
    public int getTongNgayCong() {
        return tongNgayCong;
    }
    
    public void setTongNgayCong(int tongNgayCong) {
        this.tongNgayCong = tongNgayCong;
    }
    
    public BigDecimal getTongLuong() {
        return tongLuong;
    }
    
    public void setTongLuong(BigDecimal tongLuong) {
        this.tongLuong = tongLuong;
    }
    
    public int getMaPQ() {
        return maPQ;
    }
    
    public void setMaPQ(int maPQ) {
        this.maPQ = maPQ;
    }
    
    public String getTenQuyen() {
        return tenQuyen;
    }
    
    public void setTenQuyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }
}