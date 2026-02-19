package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SplashScreen extends JWindow {
    
    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK = new Color(25, 45, 85);
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);
    private static final Color BG_SECONDARY = new Color(255, 255, 255);
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);
    
    // ============ COMPONENTS ============
    private JProgressBar progressBar;
    private JLabel lblProgress;
    private JLabel lblStatus;
    private JLabel lblLogo;
    
    private int progress = 0;
    private Timer progressTimer;
    
    public SplashScreen() {
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JPanel contentPanel = createContentPanel();
        setContentPane(contentPanel);
        
        startLoadingAnimation();
    }
    
    /**
     * Tạo panel chính với gradient background
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), new Color(45, 85, 145)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative circles
                g2d.setColor(new Color(255, 255, 255, 5));
                for (int i = 0; i < 3; i++) {
                    int circleSize = 200 + (i * 100);
                    g2d.fillOval(-100 + (i * 30), -100 + (i * 50), circleSize, circleSize);
                    g2d.fillOval(getWidth() - 150, getHeight() - 150, circleSize, circleSize);
                }
            }
        };
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // ============ LOGO ============
        panel.add(Box.createVerticalStrut(40));
        lblLogo = createLogoLabel();
        panel.add(lblLogo);
        
        panel.add(Box.createVerticalStrut(30));
        
        // ============ TITLE ============
        JLabel lblTitle = new JLabel("VUA LẨU TI-TI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblTitle.setForeground(ACCENT_ORANGE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitle);
        
        panel.add(Box.createVerticalStrut(10));
        
        JLabel lblSubtitle = new JLabel("HỆ THỐNG QUÁN LẨU CHUYÊN NGHIỆP");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitle.setForeground(new Color(220, 230, 240));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblSubtitle);
        
        panel.add(Box.createVerticalStrut(50));
        
        // ============ PROGRESS BAR ============
        progressBar = createModernProgressBar();
        panel.add(progressBar);
        
        panel.add(Box.createVerticalStrut(20));
        
        // ============ STATUS LABEL ============
        lblStatus = new JLabel("Đang khởi động hệ thống...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblStatus.setForeground(new Color(200, 210, 220));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblStatus);
        
        panel.add(Box.createVerticalGlue());
        
        // ============ FOOTER ============
        JLabel lblFooter = new JLabel("© 2026 - Vua Lẩu TiTi | Cần Thơ");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(150, 160, 170));
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblFooter);
        
        return panel;
    }
    
    /**
     * Tạo logo label với icon tròn
     */
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        try {
            // Load logo từ file
            String imagePath = "src/Assets/images/logoin.jpg";
            BufferedImage image = ImageIO.read(new File(imagePath));
            int size = 120;
            Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            
            // Tạo hình tròn
            BufferedImage circleImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ viền sáng
            g2.setColor(ACCENT_ORANGE);
            g2.setStroke(new BasicStroke(4f));
            g2.drawOval(2, 2, size - 4, size - 4);
            
            // Clip hình tròn
            g2.setClip(new Ellipse2D.Float(4, 4, size - 8, size - 8));
            g2.drawImage(scaledImage, 4, 4, size - 8, size - 8, null);
            
            g2.dispose();
            logoLabel.setIcon(new ImageIcon(circleImage));
            
        } catch (Exception e) {
            // Fallback - tạo logo bằng code
            logoLabel.setIcon(new ImageIcon(createLogoIcon(120)));
        }
        
        return logoLabel;
    }
    
    /**
     * Tạo logo icon động nếu không load được file
     */
    private BufferedImage createLogoIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ gradient circle
        GradientPaint gradient = new GradientPaint(
            0, 0, ACCENT_ORANGE.brighter(),
            size, size, ACCENT_ORANGE.darker()
        );
        g2d.setPaint(gradient);
        g2d.fillOval(4, 4, size - 8, size - 8);
        
        // Vẽ viền
        g2d.setColor(ACCENT_ORANGE.darker());
        g2d.setStroke(new BasicStroke(4f));
        g2d.drawOval(4, 4, size - 8, size - 8);
        
        // Vẽ text/icon
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, size / 2));
        String text = "🍽";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2 - 5;
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Tạo progress bar hiện đại với số % hiển thị bên trong
     */
    private JProgressBar createModernProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int fillWidth = (int) ((width - 8) * (getValue() / 100.0));
                
                // Background
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Border
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, width - 2, height - 2, height - 2, height - 2);
                
                // Fill với gradient
                if (fillWidth > 0) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_ORANGE,
                        fillWidth, 0, ACCENT_ORANGE.darker()
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(4, 4, fillWidth, height - 8, height - 8, height - 8);
                    
                    // Highlight
                    g2d.setColor(new Color(255, 255, 255, 60));
                    g2d.fillRoundRect(4, 4, fillWidth, (height - 8) / 2, height - 8, height - 8);
                }
                
                // ========== HIỂN THỊ % TRÊN PROGRESS BAR ==========
                String percentText = getValue() + "%";
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(percentText);
                int textHeight = fm.getAscent();
                
                int textX = (width - textWidth) / 2;
                int textY = (height + textHeight) / 2 - 2;
                
                // Vẽ text shadow để dễ nhìn hơn
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(percentText, textX + 1, textY + 1);
                
                // Vẽ text chính
                g2d.setColor(Color.WHITE);
                g2d.drawString(percentText, textX, textY);
            }
        };
        
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setBorderPainted(false);
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(400, 30)); // Tăng height từ 20 lên 30 để text dễ nhìn
        bar.setMaximumSize(new Dimension(400, 30));
        
        return bar;
    }
    
    /**
     * Bắt đầu animation loading
     */
    private void startLoadingAnimation() {
        // Timer tăng progress từ 0 -> 100 trong 2 giây
        progressTimer = new Timer(20, e -> {
            progress += 1; // Tăng 1% mỗi 20ms = 2000ms (2s) cho 100%
            
            progressBar.setValue(progress);
            
            // Cập nhật status message theo progress
            updateStatusMessage(progress);
            
            // Khi đạt 100%
            if (progress >= 100) {
                progressTimer.stop();
                
                // Đợi 200ms rồi đóng splash và mở FormDangNhap
                Timer closeTimer = new Timer(200, evt -> {
                    dispose();
                    openLoginForm();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
        
        progressTimer.start();
    }
    
    /**
     * Cập nhật status message theo tiến trình
     */
    private void updateStatusMessage(int progress) {
        String[] messages = {
            "Đang khởi động hệ thống...",      // 0-20%
            "Đang tải cấu hình...",             // 20-40%
            "Đang kết nối cơ sở dữ liệu...",   // 40-60%
            "Đang tải giao diện...",            // 60-80%
            "Hoàn tất khởi động...",            // 80-100%
        };
        
        int index = Math.min(progress / 20, messages.length - 1);
        lblStatus.setText(messages[index]);
    }
    
    /**
     * Mở FormDangNhap sau khi loading xong
     */
    private void openLoginForm() {
        SwingUtilities.invokeLater(() -> {
            new FormDangNhap();
        });
    }
    
    /**
     * Main method để test
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);
        });
    }
}