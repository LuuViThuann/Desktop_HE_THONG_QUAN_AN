package view;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import dao.NhanVienDAO;


public class FormDangKy extends JFrame {
    private static final long serialVersionUID = 1L;

    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_DARK = new Color(25, 45, 85);        
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);     
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);     
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);     
    private static final Color DANGER_RED = new Color(192, 57, 43);         
    
    private static final Color BG_MAIN = new Color(241, 244, 247);          
    private static final Color BG_SECONDARY = new Color(255, 255, 255);     
    private static final Color TEXT_DARK = new Color(44, 62, 80);           
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);       
    private static final Color BORDER_COLOR = new Color(189, 195, 199);     

    // ============ UI Components ============
    private JTextField txtHoTen;
    private JTextField txtEmail;
    private JTextField txtSdt;
    private JPasswordField txtMatKhau;
    private JPasswordField txtConfirmMatKhau;
    private JButton btnDangKy;
    private JButton btnQuayLai;
    private JLabel lblErrorMessage;
    
    private JFrame parentFrame;
    private boolean isProcessing = false;

    public FormDangKy(JFrame parent) {
        this.parentFrame = parent;
        
        setTitle("Đăng ký - Hệ thống Quán Lẩu Thái TiTi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        // Main Panel với gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), new Color(45, 85, 145)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative elements
                g2d.setColor(new Color(255, 255, 255, 5));
                for (int i = 0; i < 5; i++) {
                    int circleSize = 200 + (i * 150);
                    g2d.fillOval(-100 + (i * 50), -100 + (i * 100), circleSize, circleSize);
                    g2d.fillOval(getWidth() - 150 + (i * 50), getHeight() - 200 + (i * 100), circleSize, circleSize);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Left Panel - Logo & Branding
        JPanel leftPanel = createLeftPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Right Panel - Register Form
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        
        // Window listener để hiện lại form đăng nhập khi đóng
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });
    }

    // ==================== LEFT PANEL - BRANDING ====================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), PRIMARY_LIGHT.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(255, 255, 255, 8));
                g2d.fillOval(-150, -150, 400, 400);
                g2d.fillOval(200, 400, 350, 350);
                
                g2d.setColor(ACCENT_ORANGE);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawLine(0, 250, getWidth(), 250);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(480, 0));

        panel.add(Box.createVerticalStrut(100));

        // ============ LOGO ============
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        try {
            String imagePath = "src/Assets/images/logoin.jpg";
            BufferedImage image = ImageIO.read(new File(imagePath));
            int size = 140;
            Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            
            BufferedImage circleImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(0, 0, size, size);
            g2.setComposite(AlphaComposite.SrcIn);
            g2.drawImage(scaledImage, 0, 0, size, size, null);
            g2.dispose();

            logoLabel.setIcon(new ImageIcon(circleImage));

        } catch (IOException e) {
            logoLabel.setText("🍽️");
            logoLabel.setFont(new Font("Arial", Font.PLAIN, 100));
            logoLabel.setForeground(ACCENT_ORANGE);
        }

        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(50));

        // ============ TITLE & SUBTITLE ============
        JLabel titleLabel = new JLabel("VUA LẨU");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(ACCENT_ORANGE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("TiTi - Chi nhánh Cần Thơ");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(255, 255, 255));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitleLabel);

        panel.add(Box.createVerticalStrut(50));
        panel.add(Box.createVerticalGlue());

        // ============ FOOTER ============
        JPanel footerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(255, 255, 255, 15));
                g2d.drawLine(30, 0, getWidth() - 30, 0);
            }
        };
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel footerTitle = new JLabel("© 2026 - Hệ thống Quán Lẩu Thái");
        footerTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        footerTitle.setForeground(new Color(255, 255, 255));
        footerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel footerInfo = new JLabel("TP. Cần Thơ | Phục vụ chuyên nghiệp");
        footerInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerInfo.setForeground(new Color(200, 220, 240));
        footerInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        footerPanel.add(footerTitle);
        footerPanel.add(footerInfo);
        
        panel.add(footerPanel);

        return panel;
    }

    // ==================== RIGHT PANEL - REGISTER FORM ====================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_SECONDARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(40, 80, 40, 80));

        // ============ WELCOME SECTION ============
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(BG_SECONDARY);
        welcomePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel welcomeLabel = new JLabel("ĐĂNG KÝ");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(PRIMARY_DARK);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(welcomeLabel);

        welcomePanel.add(Box.createVerticalStrut(8));

        JLabel subLabel = new JLabel("Tạo tài khoản mới để bắt đầu");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(TEXT_LIGHT);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(subLabel);

        panel.add(welcomePanel);
        panel.add(Box.createVerticalStrut(25));

        // ============ ERROR MESSAGE ============
        lblErrorMessage = new JLabel();
        lblErrorMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblErrorMessage.setForeground(DANGER_RED);
        lblErrorMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblErrorMessage.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DANGER_RED, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        lblErrorMessage.setBackground(new Color(255, 240, 240));
        lblErrorMessage.setOpaque(true);
        lblErrorMessage.setVisible(false);
        panel.add(lblErrorMessage);
        panel.add(Box.createVerticalStrut(15));

        // ============ HỌ TÊN FIELD ============
        JPanel hoTenLabelPanel = createFieldLabelPanel("Họ và tên");
        panel.add(hoTenLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtHoTen = createModernTextField("Nhập họ và tên...");
        panel.add(txtHoTen);
        panel.add(Box.createVerticalStrut(15));

        // ============ EMAIL FIELD ============
        JPanel emailLabelPanel = createFieldLabelPanel("Email");
        panel.add(emailLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtEmail = createModernTextField("Nhập email...");
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(15));

        // ============ SỐ ĐIỆN THOẠI FIELD ============
        JPanel sdtLabelPanel = createFieldLabelPanel("Số điện thoại");
        panel.add(sdtLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtSdt = createModernTextField("Nhập số điện thoại...");
        panel.add(txtSdt);
        panel.add(Box.createVerticalStrut(15));

        // ============ PASSWORD FIELD ============
        JPanel passwordLabelPanel = createFieldLabelPanel("Mật khẩu");
        panel.add(passwordLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtMatKhau = createModernPasswordField();
        panel.add(txtMatKhau);
        panel.add(Box.createVerticalStrut(15));

        // ============ CONFIRM PASSWORD FIELD ============
        JPanel confirmPasswordLabelPanel = createFieldLabelPanel("Xác nhận mật khẩu");
        panel.add(confirmPasswordLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtConfirmMatKhau = createModernPasswordField();
        panel.add(txtConfirmMatKhau);
        panel.add(Box.createVerticalStrut(25));

        // ============ BUTTONS ============
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonsPanel.setBackground(BG_SECONDARY);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnDangKy = createModernButton("Đăng Ký", PRIMARY_LIGHT, true);
        btnQuayLai = createModernButton("Quay Lại", new Color(100, 150, 200), false);

        buttonsPanel.add(btnDangKy);
        buttonsPanel.add(btnQuayLai);

        panel.add(buttonsPanel);
        panel.add(Box.createVerticalGlue());

        // ============ FOOTER ============
        JPanel footerSignup = new JPanel();
        footerSignup.setBackground(BG_SECONDARY);
        footerSignup.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JLabel footerText = new JLabel("© 2026 - Hệ thống quản lý quán lẩu thái");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerText.setForeground(TEXT_LIGHT);
        footerSignup.add(footerText);
        
        panel.add(footerSignup);

        // ============ EVENT LISTENERS ============
        setupEventListeners();

        return panel;
    }

    // ==================== CREATE MODERN TEXTFIELD ====================
    private JTextField createModernTextField(String placeholder) {
        JTextField textField = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int height = getHeight();
                int cornerRadius = 8;

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                if (hasFocus()) {
                    g2d.setColor(PRIMARY_LIGHT);
                    g2d.setStroke(new BasicStroke(2.5f));
                } else {
                    g2d.setColor(BORDER_COLOR);
                    g2d.setStroke(new BasicStroke(1.5f));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                super.paintComponent(g);
            }
        };

        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setForeground(TEXT_DARK);
        textField.setBackground(new Color(248, 250, 252));
        textField.setCaretColor(PRIMARY_LIGHT);
        textField.setBorder(new EmptyBorder(12, 16, 12, 16));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        textField.setOpaque(true);

        textField.addFocusListener(new FocusAdapter() {
            private String placeholder = textField.getText();

            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(new Color(180, 180, 180));
                }
            }
        });

        return textField;
    }

    // ==================== CREATE MODERN PASSWORD FIELD ====================
    private JPasswordField createModernPasswordField() {
        JPasswordField passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int height = getHeight();
                int cornerRadius = 8;

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                if (hasFocus()) {
                    g2d.setColor(PRIMARY_LIGHT);
                    g2d.setStroke(new BasicStroke(2.5f));
                } else {
                    g2d.setColor(BORDER_COLOR);
                    g2d.setStroke(new BasicStroke(1.5f));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                super.paintComponent(g);
            }
        };

        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setForeground(TEXT_DARK);
        passwordField.setBackground(new Color(248, 250, 252));
        passwordField.setCaretColor(PRIMARY_LIGHT);
        passwordField.setBorder(new EmptyBorder(12, 16, 12, 16));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        passwordField.setOpaque(true);

        return passwordField;
    }

    // ==================== CREATE MODERN BUTTON ====================
    private JButton createModernButton(String text, Color bgColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentColor = bgColor;
                
                if (getModel().isPressed()) {
                    currentColor = bgColor.darker().darker();
                } else if (getModel().isRollover()) {
                    currentColor = bgColor.brighter();
                }

                if (isPrimary) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 2, 8, 8);
                }

                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                if (isPrimary) {
                    g2d.setColor(new Color(255, 255, 255, 40));
                    g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() / 2, 8, 8);
                }

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setPreferredSize(new Dimension(0, 45));
        
        return btn;
    }

    // ==================== CREATE FIELD LABEL ====================
    private JPanel createFieldLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(BG_SECONDARY);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(PRIMARY_DARK);

        panel.add(label);
        return panel;
    }

    // ==================== SETUP EVENT LISTENERS ====================
    private void setupEventListeners() {
        btnDangKy.addActionListener(e -> onDangKy());
        btnQuayLai.addActionListener(e -> onQuayLai());

        txtConfirmMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onDangKy();
                }
            }
        });
    }

    // ==================== HANDLE REGISTER ====================
    private void onDangKy() {
        if (isProcessing) return;

        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSdt.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());
        String confirmMatKhau = new String(txtConfirmMatKhau.getPassword());

        // Validate
        if (hoTen.isEmpty() || hoTen.equals("Nhập họ và tên...")) {
            showError("Vui lòng nhập họ tên!");
            txtHoTen.requestFocus();
            return;
        }

        if (email.isEmpty() || email.equals("Nhập email...")) {
            showError("Vui lòng nhập email!");
            txtEmail.requestFocus();
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Email không hợp lệ!");
            txtEmail.requestFocus();
            return;
        }

        if (sdt.isEmpty() || sdt.equals("Nhập số điện thoại...")) {
            showError("Vui lòng nhập số điện thoại!");
            txtSdt.requestFocus();
            return;
        }

        if (!sdt.matches("\\d{10}")) {
            showError("Số điện thoại phải có 10 chữ số!");
            txtSdt.requestFocus();
            return;
        }

        if (matKhau.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            txtMatKhau.requestFocus();
            return;
        }

        if (matKhau.length() < 6) {
            showError("Mật khẩu phải tối thiểu 6 ký tự!");
            txtMatKhau.requestFocus();
            return;
        }

        if (!matKhau.equals(confirmMatKhau)) {
            showError("Mật khẩu không khớp!");
            txtMatKhau.setText("");
            txtConfirmMatKhau.setText("");
            txtMatKhau.requestFocus();
            return;
        }

        // Show loading state
        isProcessing = true;
        btnDangKy.setText("Đang xử lý...");
        btnDangKy.setEnabled(false);

        // Perform register in background thread
        new Thread(() -> {
            try {
                if (NhanVienDAO.isEmailExists(email)) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Email đã được đăng ký!");
                        txtEmail.requestFocus();
                        
                        isProcessing = false;
                        btnDangKy.setText("Đăng Ký");
                        btnDangKy.setEnabled(true);
                    });
                    return;
                }

                boolean success = NhanVienDAO.register(hoTen, email, sdt, matKhau);

                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(FormDangKy.this,
                            "✓ Đăng ký thành công!\n\n" +
                            "Họ tên: " + hoTen + "\n" +
                            "Email: " + email + "\n\n" +
                            "Bạn có thể đăng nhập ngay.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                        dispose();
                        if (parentFrame != null) {
                            parentFrame.setVisible(true);
                        }
                    } else {
                        showError("Đăng ký thất bại. Vui lòng thử lại!");
                        
                        isProcessing = false;
                        btnDangKy.setText("Đăng Ký");
                        btnDangKy.setEnabled(true);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showError("⚠️ Lỗi: " + ex.getMessage());
                    
                    isProcessing = false;
                    btnDangKy.setText("Đăng Ký");
                    btnDangKy.setEnabled(true);
                });
            }
        }).start();
    }

    // ==================== HANDLE BACK ====================
    private void onQuayLai() {
        dispose();
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        }
    }

    // ==================== SHOW ERROR ====================
    private void showError(String message) {
        lblErrorMessage.setText(message);
        lblErrorMessage.setVisible(true);
        
        Timer timer = new Timer(5000, e -> {
            lblErrorMessage.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }
}