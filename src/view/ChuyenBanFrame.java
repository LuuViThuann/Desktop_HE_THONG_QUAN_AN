package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import dao.BanDAO;
import model.Ban;
import javax.swing.*;
import java.awt.*;
import java.util.List;
public class ChuyenBanFrame extends JDialog {

	private static final long serialVersionUID = 1L;
    private int maBanCu;
    
    private JComboBox<Ban> cbbBanMoi;
    private JButton btnChuyen;
    private JButton btnHuy;
    
    public ChuyenBanFrame(JFrame parent, int maBanCu) {
        super(parent, "Chuyển Bàn", true);
        this.maBanCu = maBanCu;
        initComponents();
        loadBan();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 150);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel lblBanMoi = new JLabel("Chọn bàn mới:");
        cbbBanMoi = new JComboBox<>();
        
        JPanel buttonPanel = new JPanel();
        btnChuyen = new JButton("Chuyển");
        btnHuy = new JButton("Hủy");
        
        btnChuyen.addActionListener(e -> onChuyen());
        btnHuy.addActionListener(e -> dispose());
        
        buttonPanel.add(btnChuyen);
        buttonPanel.add(btnHuy);
        
        mainPanel.add(lblBanMoi);
        mainPanel.add(cbbBanMoi);
        mainPanel.add(new JLabel());
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private void loadBan() {
        List<Ban> banList = BanDAO.getAllBanWithKhuVuc();
        for (Ban ban : banList) {
            if (ban.getMaBan() != 0 && ban.getMaBan() != maBanCu) {
                cbbBanMoi.addItem(ban);
            }
        }
    }
    
    private void onChuyen() {
        Ban banMoi = (Ban) cbbBanMoi.getSelectedItem();
        if (banMoi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!");
            return;
        }
        
        // Logic chuyển bàn
        JOptionPane.showMessageDialog(this, "Chuyển bàn thành công!",
            "Thành công", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
