package view;

import util.VietQRGenerator;
import com.google.zxing.WriterException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Frame để hiển thị QR Code VietQR cho thanh toán chuyển khoản
 */
public class ThanhToanQRFrame extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private int maCTHD;
    private BigDecimal soTien;
    private JLabel lblQRCode;
    private JTextArea lblHuongDan;
    private JButton btnQuayLai;
    private boolean daThanhToan = false;
    
    public ThanhToanQRFrame(JFrame parent, int maCTHD, BigDecimal soTien) {
        super(parent, "Thanh Toán QR - VietQR", true);
        this.maCTHD = maCTHD;
        this.soTien = soTien;
        
        initComponents();
        generateQRCode();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 750);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // ===== Header =====
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(52, 152, 219));
        JLabel lblTitle = new JLabel("Quét QR Code để Thanh Toán");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // ===== Center - QR Code =====
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        
        // Thông tin hóa đơn
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Thông tin thanh toán"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblMaHDLabel = new JLabel("Mã hóa đơn:");
        lblMaHDLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel lblMaHD = new JLabel(String.valueOf(maCTHD));
        lblMaHD.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMaHD.setForeground(new Color(220, 53, 69));
        
        JLabel lblSoTienLabel = new JLabel("Số tiền:");
        lblSoTienLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel lblSoTien = new JLabel(String.format("%,d VNĐ", soTien.longValue()));
        lblSoTien.setFont(new Font("Arial", Font.BOLD, 14));
        lblSoTien.setForeground(new Color(39, 174, 96));
        
        infoPanel.add(lblMaHDLabel);
        infoPanel.add(lblMaHD);
        infoPanel.add(lblSoTienLabel);
        infoPanel.add(lblSoTien);
        
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        
        // QR Code
        JPanel qrPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qrPanel.setBackground(Color.WHITE);
        lblQRCode = new JLabel();
        lblQRCode.setBackground(Color.WHITE);
        qrPanel.add(lblQRCode);
        
        centerPanel.add(qrPanel, BorderLayout.CENTER);
        
        // Hướng dẫn
        lblHuongDan = new JTextArea();
        lblHuongDan.setText(
            "HƯỚNG DẪN THANH TOÁN:\n\n" +
            "1. Mở ứng dụng Mobile Banking của ngân hàng của bạn\n" +
            "2. Chọn chức năng \"Quét mã QR\" hoặc \"Transfer QR\"\n" +
            "3. Quét mã QR phía trên\n" +
            "4. Kiểm tra số tiền: " + String.format("%,d VNĐ", soTien.longValue()) + "\n" +
            "5. Nhấn \"Xác nhận\" để chuyển khoản\n" +
            "6. Quay lại ứng dụng và nhấn \"Đã thanh toán\"\n\n" +
            "Lưu ý: Giao dịch sẽ được xử lý sau 1-2 phút."
        );
        lblHuongDan.setFont(new Font("Arial", Font.PLAIN, 12));
        lblHuongDan.setEditable(false);
        lblHuongDan.setBackground(new Color(236, 240, 241));
        lblHuongDan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        lblHuongDan.setLineWrap(true);
        lblHuongDan.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(lblHuongDan);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Hướng dẫn"));
        centerPanel.add(scrollPane, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // ===== Button Panel =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        
       
        
        btnQuayLai = new JButton("Quay Lại");
        btnQuayLai.setFont(new Font("Arial", Font.BOLD, 14));
        btnQuayLai.setBackground(new Color(231, 76, 60));
        btnQuayLai.setForeground(Color.WHITE);
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.setBorderPainted(false);
        btnQuayLai.setPreferredSize(new Dimension(150, 45));
        btnQuayLai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuayLai.addActionListener(e -> dispose());
        
        btnQuayLai.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnQuayLai.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(MouseEvent e) {
                btnQuayLai.setBackground(new Color(231, 76, 60));
            }
        });
        
      
        buttonPanel.add(btnQuayLai);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void generateQRCode() {
        try {
            BufferedImage qrImage = VietQRGenerator.createQRCode(soTien, String.valueOf(maCTHD));
            ImageIcon icon = new ImageIcon(qrImage);
            lblQRCode.setIcon(icon);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi tạo QR Code: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void confirmPayment() {
        int result = JOptionPane.showConfirmDialog(this,
            "Bạn đã hoàn tất thanh toán " + String.format("%,d VNĐ", soTien.longValue()) + "?",
            "Xác Nhận Thanh Toán",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            daThanhToan = true;
            dispose();
        }
    }
    
    public boolean isDaThanhToan() {
        return daThanhToan;
    }
}