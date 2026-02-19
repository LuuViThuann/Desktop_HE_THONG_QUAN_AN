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
import model.NhanVien;


public class FormQuenMatKhau extends JFrame {
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
    private JTextField txtEmail;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtConfirmMatKhau;
    private JButton btnXacNhan;
    private JButton btnQuayLai;
    private JLabel lblErrorMessage;
    private JLabel lblTrangThai;
    
    private JPanel rightPanel;
    private JPanel panelNhapEmail;
    private JPanel panelNhapMatKhau;
    
    private JFrame parentFrame;
    private String emailHopLe;
    private boolean daXacNhanEmail = false;
    private boolean isProcessing = false;

    public FormQuenMatKhau(JFrame parent) {
        this.parentFrame = parent;
        
        setTitle("Quên mật khẩu - Hệ thống Quán Lẩu Thái TiTi");
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

        // Right Panel - Reset Password Form
        rightPanel = createRightPanel();
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

    // ==================== RIGHT PANEL - RESET PASSWORD FORM ====================
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

        JLabel welcomeLabel = new JLabel("ĐẶT LẠI MẬT KHẨU");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(PRIMARY_DARK);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(welcomeLabel);

        welcomePanel.add(Box.createVerticalStrut(8));

        lblTrangThai = new JLabel("Bước 1: Xác thực email");
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTrangThai.setForeground(TEXT_LIGHT);
        lblTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(lblTrangThai);

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

        // ============ CARD LAYOUT FOR STEPS ============
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(BG_SECONDARY);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        // Panel nhập email
        panelNhapEmail = createPanelNhapEmail();
        cardPanel.add(panelNhapEmail, "email");

        // Panel nhập mật khẩu mới
        panelNhapMatKhau = createPanelNhapMatKhau();
        cardPanel.add(panelNhapMatKhau, "password");

        panel.add(cardPanel);
        panel.add(Box.createVerticalStrut(30));

        // ============ BUTTONS ============
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonsPanel.setBackground(BG_SECONDARY);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnXacNhan = createModernButton("Tiếp Tục", PRIMARY_LIGHT, true);
        btnQuayLai = createModernButton("Quay Lại", new Color(100, 150, 200), false);

        buttonsPanel.add(btnXacNhan);
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

    // ==================== CREATE PANEL NHẬP EMAIL ====================
    private JPanel createPanelNhapEmail() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_SECONDARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblThongBao = new JLabel("Nhập email của bạn để đặt lại mật khẩu");
        lblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThongBao.setForeground(TEXT_LIGHT);
        lblThongBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblThongBao);

        panel.add(Box.createVerticalStrut(25));

        JPanel emailLabelPanel = createFieldLabelPanel("Email");
        panel.add(emailLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtEmail = createModernTextField("Nhập email...");
        panel.add(txtEmail);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ==================== CREATE PANEL NHẬP MẬT KHẨU ====================
    private JPanel createPanelNhapMatKhau() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_SECONDARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblThongBao = new JLabel("Nhập mật khẩu mới cho tài khoản");
        lblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThongBao.setForeground(TEXT_LIGHT);
        lblThongBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblThongBao);

        panel.add(Box.createVerticalStrut(25));

        // Mật khẩu mới
        JPanel passwordLabelPanel = createFieldLabelPanel("Mật khẩu mới");
        panel.add(passwordLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtMatKhauMoi = createModernPasswordField();
        panel.add(txtMatKhauMoi);

        panel.add(Box.createVerticalStrut(20));

        // Xác nhận mật khẩu
        JPanel confirmLabelPanel = createFieldLabelPanel("Xác nhận mật khẩu");
        panel.add(confirmLabelPanel);
        panel.add(Box.createVerticalStrut(8));

        txtConfirmMatKhau = createModernPasswordField();
        panel.add(txtConfirmMatKhau);

        panel.add(Box.createVerticalGlue());

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
        btnXacNhan.addActionListener(e -> onXacNhan());
        btnQuayLai.addActionListener(e -> onQuayLai());

        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onXacNhan();
                }
            }
        });

        txtConfirmMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onXacNhan();
                }
            }
        });
    }

    // ==================== HANDLE CONFIRM ====================
    private void onXacNhan() {
        if (isProcessing) return;

        if (!daXacNhanEmail) {
            // Bước 1: Kiểm tra email
            String email = txtEmail.getText().trim();

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

            // Show loading state
            isProcessing = true;
            btnXacNhan.setText("Đang xử lý...");
            btnXacNhan.setEnabled(false);

            // Kiểm tra email trong background thread
            new Thread(() -> {
                try {
                    NhanVien nhanVien = NhanVienDAO.getByEmail(email);

                    SwingUtilities.invokeLater(() -> {
                        if (nhanVien == null) {
                            showError("Email không tồn tại trong hệ thống!");
                            txtEmail.requestFocus();
                            
                            isProcessing = false;
                            btnXacNhan.setText("Tiếp Tục");
                            btnXacNhan.setEnabled(true);
                        } else {
                            // Chuyển sang bước 2
                            emailHopLe = email;
                            daXacNhanEmail = true;

                            // Switch panels
                            CardLayout cl = (CardLayout) ((JPanel)panelNhapEmail.getParent()).getLayout();
                            cl.show((JPanel)panelNhapEmail.getParent(), "password");

                            lblTrangThai.setText("Bước 2: Nhập mật khẩu mới");
                            btnXacNhan.setText("Cập Nhật");
                            lblErrorMessage.setVisible(false);

                            txtMatKhauMoi.requestFocus();
                            
                            isProcessing = false;
                            btnXacNhan.setEnabled(true);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        showError("⚠️ Lỗi: " + ex.getMessage());
                        
                        isProcessing = false;
                        btnXacNhan.setText("Tiếp Tục");
                        btnXacNhan.setEnabled(true);
                    });
                }
            }).start();

        } else {
            // Bước 2: Cập nhật mật khẩu
            String matKhauMoi = new String(txtMatKhauMoi.getPassword());
            String confirmMatKhau = new String(txtConfirmMatKhau.getPassword());

            if (matKhauMoi.isEmpty()) {
                showError("Vui lòng nhập mật khẩu mới!");
                txtMatKhauMoi.requestFocus();
                return;
            }

            if (matKhauMoi.length() < 6) {
                showError("Mật khẩu phải tối thiểu 6 ký tự!");
                txtMatKhauMoi.requestFocus();
                return;
            }

            if (confirmMatKhau.isEmpty()) {
                showError("Vui lòng xác nhận mật khẩu!");
                txtConfirmMatKhau.requestFocus();
                return;
            }

            if (!matKhauMoi.equals(confirmMatKhau)) {
                showError("Mật khẩu không khớp!");
                txtMatKhauMoi.setText("");
                txtConfirmMatKhau.setText("");
                txtMatKhauMoi.requestFocus();
                return;
            }

            // Show loading state
            isProcessing = true;
            btnXacNhan.setText("Đang cập nhật...");
            btnXacNhan.setEnabled(false);

            // Cập nhật mật khẩu trong background thread
            new Thread(() -> {
                try {
                    boolean success = NhanVienDAO.updateMatKhau(emailHopLe, matKhauMoi);

                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(FormQuenMatKhau.this,
                                "✓ Cập nhật mật khẩu thành công!\n\n" +
                                "Email: " + emailHopLe + "\n\n" +
                                "Vui lòng đăng nhập lại với mật khẩu mới.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);

                            dispose();
                            if (parentFrame != null) {
                                parentFrame.setVisible(true);
                            }
                        } else {
                            showError("Cập nhật mật khẩu thất bại!");
                            
                            isProcessing = false;
                            btnXacNhan.setText("Cập Nhật");
                            btnXacNhan.setEnabled(true);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        showError("⚠️ Lỗi: " + ex.getMessage());
                        
                        isProcessing = false;
                        btnXacNhan.setText("Cập Nhật");
                        btnXacNhan.setEnabled(true);
                    });
                }
            }).start();
        }
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