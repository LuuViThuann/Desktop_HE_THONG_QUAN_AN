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
import config.UserSession;
import dao.NhanVienDAO;
import model.NhanVien;

public class FormDangNhap extends JFrame {
    private static final long serialVersionUID = 1L;

    // ============ COLOR PALETTE (từ PanelManHinhChinh) ============
    private static final Color PRIMARY_DARK = new Color(25, 45, 85);        
    private static final Color PRIMARY_LIGHT = new Color(70, 130, 180);     
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);     
    private static final Color SUCCESS_GREEN = new Color(46, 152, 102);     
    private static final Color DANGER_RED = new Color(192, 57, 43);         
    private static final Color WARNING_AMBER = new Color(241, 196, 15);     
    
    private static final Color BG_MAIN = new Color(241, 244, 247);          
    private static final Color BG_SECONDARY = new Color(255, 255, 255);     
    private static final Color TEXT_DARK = new Color(44, 62, 80);           
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);       
    private static final Color BORDER_COLOR = new Color(189, 195, 199);     

    // ============ UI Components ============
    private JTextField txtEmail;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;
    private JButton btnDangKy;
    private JCheckBox chkGhiNho;
    private JLabel lblErrorMessage;
    private JLabel lblQuenMatKhauLink;
    
    // ✅ THÊM MỚI - Nút toggle password
    private JButton btnTogglePassword;
    private boolean isPasswordVisible = false;
    
    // ============ Animation & Effects ============
    private boolean isLoginProcessing = false;

    public FormDangNhap() {
        setTitle("Đăng nhập - Hệ thống Quán Lẩu Thái TiTi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

                // Gradient background chuyên nghiệp
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

        // Right Panel - Login Form
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);
    }

    // ==================== LEFT PANEL - BRANDING ====================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), PRIMARY_LIGHT.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles - hiệu ứng lớp
                g2d.setColor(new Color(255, 255, 255, 8));
                g2d.fillOval(-150, -150, 400, 400);
                g2d.fillOval(200, 400, 350, 350);
                
                // Accent line
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

    // ==================== RIGHT PANEL - LOGIN FORM ====================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_SECONDARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 80, 60, 80));

        // ============ WELCOME SECTION ============
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(BG_SECONDARY);
        welcomePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel welcomeLabel = new JLabel("ĐĂNG NHẬP");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(PRIMARY_DARK);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(welcomeLabel);

        welcomePanel.add(Box.createVerticalStrut(8));

        JLabel subLabel = new JLabel("Nhập thông tin tài khoản của bạn");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(TEXT_LIGHT);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(subLabel);

        panel.add(welcomePanel);
        panel.add(Box.createVerticalStrut(30));

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
        panel.add(Box.createVerticalStrut(20));

        // ============ EMAIL FIELD ============
        JPanel emailLabelPanel = createFieldLabelPanel("Email");
        panel.add(emailLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtEmail = createModernTextField("Nhập email của bạn...");
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(20));

        // ============ PASSWORD FIELD với TOGGLE BUTTON ============
        JPanel passwordLabelPanel = createFieldLabelPanel("Mật khẩu");
        panel.add(passwordLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        // ✅ PANEL CHỨA PASSWORD FIELD VÀ NÚT TOGGLE
        JPanel passwordPanel = new JPanel(new BorderLayout(0, 0));
        passwordPanel.setBackground(BG_SECONDARY);
        passwordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        txtMatKhau = createModernPasswordField();
        
        // ✅ TẠO NÚT TOGGLE PASSWORD
        btnTogglePassword = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(220, 220, 220));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(240, 240, 240));
                } else {
                    g2d.setColor(new Color(248, 250, 252));
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2d);
            }
        };
        
        // ✅ LOAD ICON "EYE" (hiện mật khẩu)
        ImageIcon iconEye = loadIcon("src/Assets/images/eon.png", 20, 20);
        if (iconEye != null) {
            btnTogglePassword.setIcon(iconEye);
            btnTogglePassword.setText("");
        } else {
            // Fallback về emoji nếu không load được icon
            btnTogglePassword.setText("👁");
            btnTogglePassword.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        }
        
        btnTogglePassword.setForeground(TEXT_LIGHT);
        btnTogglePassword.setPreferredSize(new Dimension(45, 45));
        btnTogglePassword.setFocusPainted(false);
        btnTogglePassword.setBorderPainted(false);
        btnTogglePassword.setContentAreaFilled(false);
        btnTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePassword.setToolTipText("Hiện/Ẩn mật khẩu");
        
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());
        
        passwordPanel.add(txtMatKhau, BorderLayout.CENTER);
        passwordPanel.add(btnTogglePassword, BorderLayout.EAST);
        
        panel.add(passwordPanel);
        panel.add(Box.createVerticalStrut(16));

        // ============ REMEMBER & FORGOT PASSWORD ============
        JPanel rememberPanel = new JPanel(new BorderLayout());
        rememberPanel.setBackground(BG_SECONDARY);
        rememberPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        chkGhiNho = new JCheckBox("Ghi nhớ tài khoản");
        chkGhiNho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkGhiNho.setForeground(TEXT_DARK);
        chkGhiNho.setBackground(BG_SECONDARY);
        chkGhiNho.setFocusPainted(false);
        rememberPanel.add(chkGhiNho, BorderLayout.WEST);

        lblQuenMatKhauLink = createLinkLabel("Quên mật khẩu?");
        rememberPanel.add(lblQuenMatKhauLink, BorderLayout.EAST);

        panel.add(rememberPanel);
        panel.add(Box.createVerticalStrut(30));

        // ============ BUTTONS ============
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonsPanel.setBackground(BG_SECONDARY);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnDangNhap = createModernButton("Đăng Nhập", PRIMARY_LIGHT, true);
        btnDangKy = createModernButton("Đăng Ký", new Color(100, 150, 200), false);

        buttonsPanel.add(btnDangNhap);
        buttonsPanel.add(btnDangKy);

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

    // ✅ PHƯƠNG THỨC MỚI - TOGGLE PASSWORD VISIBILITY
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            // Hiển thị mật khẩu
            txtMatKhau.setEchoChar((char) 0);
            
            // Icon "eye-off" (ẩn mật khẩu)
            ImageIcon iconHide = loadIcon("src/Assets/images/eoff.png", 20, 20);
            if (iconHide != null) {
                btnTogglePassword.setIcon(iconHide);
                btnTogglePassword.setText("");
            } else {
                btnTogglePassword.setText("👁‍🗨");
            }
            btnTogglePassword.setToolTipText("Ẩn mật khẩu");
        } else {
            // Ẩn mật khẩu
            txtMatKhau.setEchoChar('•');
            
            // Icon "eye" (hiện mật khẩu)
            ImageIcon iconShow = loadIcon("src/Assets/images/eon.png", 20, 20);
            if (iconShow != null) {
                btnTogglePassword.setIcon(iconShow);
                btnTogglePassword.setText("");
            } else {
                btnTogglePassword.setText("👁");
            }
            btnTogglePassword.setToolTipText("Hiện mật khẩu");
        }
    }
    
    // ✅ HELPER METHOD - LOAD ICON
    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(path));
            java.awt.Image scaledImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            System.err.println("Không thể load icon: " + path);
            return null;
        }
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

                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                // Border
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

        // Placeholder effect
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

                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, height - 1, cornerRadius, cornerRadius);

                // Border
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

                // Shadow effect
                if (isPrimary) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 2, 8, 8);
                }

                // Button background
                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Highlight effect
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
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
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

    // ==================== CREATE LINK LABEL ====================
    private JLabel createLinkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(PRIMARY_LIGHT);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(ACCENT_ORANGE);
                label.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(PRIMARY_LIGHT);
                label.setText(text);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onQuenMatKhau();
            }
        });

        return label;
    }

    // ==================== SETUP EVENT LISTENERS ====================
    private void setupEventListeners() {
        btnDangNhap.addActionListener(e -> onDangNhap());
        btnDangKy.addActionListener(e -> onDangKy());

        txtMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onDangNhap();
                }
            }
        });
    }

    // ==================== HANDLE LOGIN - ✅ CẬP NHẬT PHÂN QUYỀN ====================
    private void onDangNhap() {
        if (isLoginProcessing) return;

        String email = txtEmail.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        // Validate email
        if (email.isEmpty() || email.equals("Nhập email của bạn...")) {
            showError("Vui lòng nhập email!");
            txtEmail.requestFocus();
            return;
        }

        // Validate password
        if (matKhau.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            txtMatKhau.requestFocus();
            return;
        }

        // Show loading state
        isLoginProcessing = true;
        btnDangNhap.setText("Đang xử lý...");
        btnDangNhap.setEnabled(false);

        // Perform login in background thread
        new Thread(() -> {
            try {
                NhanVien nhanVien = NhanVienDAO.login(email, matKhau);

                SwingUtilities.invokeLater(() -> {
                    if (nhanVien != null) {
                        UserSession.getInstance().setCurrentUser(nhanVien);

                        // ✅ XÁC ĐỊNH VAI TRÒ VÀ MỨC LƯƠNG
                        String vaiTro = "";
                        String mucLuong = "";
                        
                        if (nhanVien.getMaPQ() == 1) {
                            vaiTro = "Admin";
                            mucLuong = "15,000,000 VND";
                        } else if (nhanVien.getMaPQ() == 2) {
                            vaiTro = "Nhân viên";
                            mucLuong = "7,000,000 VND";
                        } else {
                            vaiTro = nhanVien.getTenQuyen() != null ? nhanVien.getTenQuyen() : "Không xác định";
                            mucLuong = "Chưa xác định";
                        }
                        
                        // ✅ HIỂN THỊ HIỆU ỨNG ĐĂNG NHẬP THÀNH CÔNG
                        showLoginSuccessAnimation(nhanVien, vaiTro, mucLuong, email);
                        
                    } else {
                        showError("❌ Email hoặc mật khẩu không chính xác!");
                        txtMatKhau.setText("");
                        txtMatKhau.requestFocus();
                        
                        isLoginProcessing = false;
                        btnDangNhap.setText("Đăng Nhập");
                        btnDangNhap.setEnabled(true);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showError("⚠️ Lỗi: " + ex.getMessage());
                    
                    isLoginProcessing = false;
                    btnDangNhap.setText("Đăng Nhập");
                    btnDangNhap.setEnabled(true);
                });
            }
        }).start();
    }
    private void showLoginSuccessAnimation(NhanVien nhanVien, String vaiTro, String mucLuong, String email) {
        // Tạo dialog loading
        JDialog loadingDialog = new JDialog(this, true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setSize(350, 300);
        loadingDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background tương tự logout
                GradientPaint gp = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), PRIMARY_LIGHT
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Viền sáng
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // ============ ICON THÀNH CÔNG ============
        JLabel lblIcon = new JLabel("");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 72));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIcon.setForeground(SUCCESS_GREEN);
        
        // ============ THÔNG BÁO ============
        JLabel lblWelcome = new JLabel("ĐĂNG NHẬP THÀNH CÔNG !");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblWelcome.setForeground(Color.WHITE);
        
        // ============ THÔNG TIN USER ============
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        String[] infoLines = {
            "" + nhanVien.getHoTen(),
            "" + vaiTro
           
        };
        
        for (String line : infoLines) {
            JLabel lblInfo = new JLabel(line);
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblInfo.setForeground(new Color(220, 230, 240));
            infoPanel.add(lblInfo);
            infoPanel.add(Box.createVerticalStrut(5));
        }
        
        // ============ PROGRESS BAR ============
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(280, 12));
        progressBar.setBackground(new Color(255, 255, 255, 30));
        progressBar.setForeground(ACCENT_ORANGE);
        progressBar.setBorderPainted(false);
        
        JLabel lblLoading = new JLabel("Đang mở hệ thống...");
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblLoading.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLoading.setForeground(new Color(200, 210, 220));
        
        // ============ THÊM VÀO PANEL ============
        contentPanel.add(lblIcon);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(lblWelcome);
        contentPanel.add(infoPanel);
        contentPanel.add(progressBar);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(lblLoading);
        
        loadingDialog.setContentPane(contentPanel);
        
        // ============ TIMER TỰ ĐỘNG ĐÓNG VÀ MỞ MAINFRAME ============
        Timer timer = new Timer(2500, e -> {
            loadingDialog.dispose();
            
            // Đóng FormDangNhap
            FormDangNhap.this.dispose();
            
            // Mở MainFrame
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
        });
        timer.setRepeats(false);
        timer.start();
        
        // Hiển thị dialog
        loadingDialog.setVisible(true);
    }
    
    
    private void openMainFrameWithFadeIn() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setOpacity(0.0f);
        mainFrame.setVisible(true);
        
        Timer fadeTimer = new Timer(30, new ActionListener() {
            float opacity = 0.0f;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                mainFrame.setOpacity(opacity);
            }
        });
        
        fadeTimer.start();
    }
    // ==================== HANDLE REGISTER ====================
    private void onDangKy() {
        FormDangKy formDangKy = new FormDangKy(this);
        formDangKy.setVisible(true);
    }

    // ==================== HANDLE FORGOT PASSWORD ====================
    private void onQuenMatKhau() {
        FormQuenMatKhau formQuenMatKhau = new FormQuenMatKhau(this);
        formQuenMatKhau.setVisible(true);
    }

    // ==================== SHOW ERROR ====================
    private void showError(String message) {
        lblErrorMessage.setText(message);
        lblErrorMessage.setVisible(true);
        
        // Auto-hide error after 5 seconds
        Timer timer = new Timer(5000, e -> {
            lblErrorMessage.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormDangNhap());
    }
}