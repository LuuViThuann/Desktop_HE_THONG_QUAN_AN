package model;

public class KhuVucQuan {
    private int maKV;
    private String tenKV;

    public KhuVucQuan() {}

    public KhuVucQuan(int maKV, String tenKV) {
        this.maKV = maKV;
        this.tenKV = tenKV;
    }

    public int getMaKV() { return maKV; }
    public void setMaKV(int maKV) { this.maKV = maKV; }

    public String getTenKV() { return tenKV; }
    public void setTenKV(String tenKV) { this.tenKV = tenKV; }

 
    @Override
    public String toString() {
        return tenKV != null ? tenKV : "Chưa có tên";
       
    }
    // ──────────────────────────────────────────────────
}