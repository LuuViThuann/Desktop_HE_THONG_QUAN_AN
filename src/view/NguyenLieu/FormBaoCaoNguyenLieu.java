package view.NguyenLieu;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.*;
import java.util.List;


import config.DatabaseConfig;

public class FormBaoCaoNguyenLieu extends JPanel {

    // ─────────────── Màu sắc (đồng bộ hệ thống) ───────────────
    private static final Color NAV_BG       = new Color(25,  45,  85);
    private static final Color CONTENT_BG   = new Color(243, 246, 250);
    private static final Color CARD_BG      = new Color(255, 255, 255);
    private static final Color PRIMARY      = new Color(52, 130, 200);
    private static final Color SUCCESS      = new Color(39, 174,  96);
    private static final Color DANGER       = new Color(192,  57,  43);
    private static final Color WARNING      = new Color(230, 170,  20);
    private static final Color PURPLE       = new Color(142,  68, 173);
    private static final Color TEXT_DARK    = new Color(44,  62,  80);
    private static final Color TEXT_MID     = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(214, 220, 229);
    private static final Color ROW_ALT      = new Color(248, 250, 253);
    private static final Color AUTO_BLUE    = new Color(219, 234, 254);

    // ─────────────── Tab controls ───────────────
    private JTextField       txtNgay;
    private JTable           tblNgay;
    private DefaultTableModel mdlNgay;
    private JLabel           lblSumNgay;
    private AnimatedBarChartPanel chartPanelNgay;

    private JTextField       txtTu, txtDen;
    private JTable           tblKhoang;
    private DefaultTableModel mdlKhoang;
    private JLabel           lblSumKhoang;
    private AnimatedBarChartPanel chartPanelKhoang;

    private JTable           tblCanhBao;
    private DefaultTableModel mdlCanhBao;
    private JLabel           lblCanhBaoCount;
    private AnimatedBarChartPanel chartPanelCanhBao;

    private JTextField       txtNgayLoc;
    private JTable           tblChiTiet;
    private DefaultTableModel mdlChiTiet;
    private JLabel           lblSumChiTiet;
    private AnimatedPieChartPanel chartPanelChiTiet;

    private JTable           tblCanNhap;
    private DefaultTableModel mdlCanNhap;
    private AnimatedBarChartPanel chartPanelCanNhap;

    private JTabbedPane tabs;

    // ════════════════════════════════════════════════════════════════
    //  ANIMATED BAR CHART PANEL
    // ════════════════════════════════════════════════════════════════
    static class AnimatedBarChartPanel extends JPanel {
        private String title = "";
        private List<String>       categories  = new ArrayList<>();
        private List<List<Double>> seriesData  = new ArrayList<>();
        private List<String>       seriesNames = new ArrayList<>();
        private List<Color>        colors      = new ArrayList<>();
        private boolean            horizontal  = false;
        private boolean            empty       = true;

        // Animation
        private float  progress  = 0f; // 0 → 1
        private Timer  animTimer;
        private static final int ANIM_DURATION_MS = 700;
        private static final int ANIM_FPS         = 60;

        AnimatedBarChartPanel(String borderTitle) {
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 229), 1),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), borderTitle,
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13), new Color(25, 45, 85)),
                    new EmptyBorder(4, 8, 8, 8))));
        }

        void setData(List<String> cats, List<List<Double>> series,
                     List<String> names, List<Color> cols, boolean horiz) {
            this.categories  = cats;
            this.seriesData  = series;
            this.seriesNames = names;
            this.colors      = cols;
            this.horizontal  = horiz;
            this.empty       = (cats == null || cats.isEmpty());
            startAnimation();
        }

        void setEmpty() {
            this.empty = true;
            this.categories = new ArrayList<>();
            this.seriesData = new ArrayList<>();
            stopAnimation();
            repaint();
        }

        private void startAnimation() {
            stopAnimation();
            progress = 0f;
            int steps = ANIM_DURATION_MS / (1000 / ANIM_FPS);
            float delta = 1f / steps;
            animTimer = new Timer(1000 / ANIM_FPS, e -> {
                progress = Math.min(1f, progress + delta);
                repaint();
                if (progress >= 1f) stopAnimation();
            });
            animTimer.start();
        }

        private void stopAnimation() {
            if (animTimer != null && animTimer.isRunning()) {
                animTimer.stop();
            }
        }

        // Easing: ease-out cubic
        private float ease(float t) {
            return 1f - (1f - t) * (1f - t) * (1f - t);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (empty || categories.isEmpty()) {
                drawEmpty(g2);
                g2.dispose();
                return;
            }

            Insets ins = getInsets();
            int x0 = ins.left + 50;
            int y0 = ins.top + 20;
            int x1 = getWidth()  - ins.right  - 10;
            int y1 = getHeight() - ins.bottom  - 60; // legend space

            if (x1 - x0 < 50 || y1 - y0 < 50) { g2.dispose(); return; }

            if (horizontal) drawHBars(g2, x0, y0, x1, y1);
            else             drawVBars(g2, x0, y0, x1, y1);

            drawLegend(g2, ins, y1);
            g2.dispose();
        }

        /* ── Vertical bar chart (from-bottom animation) ── */
        private void drawVBars(Graphics2D g2, int x0, int y0, int x1, int y1) {
            int n = categories.size();
            int nseries = seriesData.size();
            if (n == 0) return;

            // Find max value
            double maxVal = 0;
            for (List<Double> s : seriesData)
                for (double v : s) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1;

            // Draw grid lines
            g2.setStroke(new BasicStroke(0.5f));
            g2.setColor(new Color(229, 231, 235));
            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                int gy = y1 - (int)((y1 - y0) * i / gridLines);
                g2.drawLine(x0, gy, x1, gy);
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                double label = maxVal * i / gridLines;
                g2.drawString(String.format("%.0f", label), x0 - 48, gy + 4);
                g2.setColor(new Color(229, 231, 235));
            }

            // Draw axes
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(180, 190, 200));
            g2.drawLine(x0, y0, x0, y1);
            g2.drawLine(x0, y1, x1, y1);

            float easedP = ease(progress);

            int groupW  = (x1 - x0) / n;
            int padding = Math.max(4, groupW / 8);
            int barW    = nseries > 0 ? (groupW - 2 * padding) / nseries : groupW - 2 * padding;
            barW = Math.max(barW, 4);

            Font catFont = new Font("Segoe UI", Font.PLAIN, 10);
            g2.setFont(catFont);

            for (int i = 0; i < n; i++) {
                int gx = x0 + i * groupW;
                for (int s = 0; s < nseries; s++) {
                    if (s >= seriesData.size() || i >= seriesData.get(s).size()) continue;
                    double val = seriesData.get(s).get(i);
                    int bx = gx + padding + s * barW;
                    int fullH = (int)((y1 - y0) * val / maxVal);
                    int animH = (int)(fullH * easedP);
                    int by = y1 - animH;

                    Color base = colors.get(s % colors.size());
                    // Gradient fill
                    GradientPaint gp = new GradientPaint(bx, by, base.brighter(), bx, y1, base.darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(bx, by, barW - 2, animH, 4, 4);

                    // Subtle border
                    g2.setColor(base.darker());
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawRoundRect(bx, by, barW - 2, animH, 4, 4);

                    // Value label on top (only when animation done)
                    if (progress >= 1f && animH > 14) {
                        g2.setColor(TEXT_DARK);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        String vStr = String.format("%.1f", val);
                        FontMetrics fm = g2.getFontMetrics();
                        int vx = bx + (barW - 2 - fm.stringWidth(vStr)) / 2;
                        g2.drawString(vStr, vx, by - 2);
                        g2.setFont(catFont);
                    }
                }
                // Category label
                g2.setColor(TEXT_DARK);
                g2.setFont(catFont);
                FontMetrics fm = g2.getFontMetrics();
                String cat = categories.get(i);
                if (cat.length() > 10) cat = cat.substring(0, 9) + "…";
                int lx = gx + groupW / 2 - fm.stringWidth(cat) / 2;
                g2.drawString(cat, lx, y1 + 14);
            }
        }

        /* ── Horizontal bar chart (from-left animation) ── */
        private void drawHBars(Graphics2D g2, int x0, int y0, int x1, int y1) {
            int n = categories.size();
            if (n == 0) return;

            double maxVal = 0;
            for (List<Double> s : seriesData)
                for (double v : s) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1;

            float easedP = ease(progress);

            // Category label area
            int labelW = 90;
            int chartX0 = x0 + labelW;

            // Grid lines (vertical)
            g2.setStroke(new BasicStroke(0.5f));
            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                int gx = chartX0 + (int)((x1 - chartX0) * i / gridLines);
                g2.setColor(new Color(229, 231, 235));
                g2.drawLine(gx, y0, gx, y1);
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                double label = maxVal * i / gridLines;
                String ls = String.format("%.0f", label);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ls, gx - fm.stringWidth(ls) / 2, y1 + 14);
            }

            // Axes
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(180, 190, 200));
            g2.drawLine(chartX0, y0, chartX0, y1);
            g2.drawLine(chartX0, y1, x1, y1);

            int rowH   = (y1 - y0) / n;
            int padding = Math.max(3, rowH / 6);
            int barH    = rowH - 2 * padding;
            barH = Math.max(barH, 6);
            Color barColor = colors.isEmpty() ? PRIMARY : colors.get(0);

            Font catFont = new Font("Segoe UI", Font.PLAIN, 10);

            for (int i = 0; i < n; i++) {
                double val = seriesData.isEmpty() ? 0 : (i < seriesData.get(0).size() ? seriesData.get(0).get(i) : 0);
                int by   = y0 + i * rowH + padding;
                int fullW = (int)((x1 - chartX0) * val / maxVal);
                int animW = (int)(fullW * easedP);

                // Gradient fill
                GradientPaint gp = new GradientPaint(chartX0, by, barColor.brighter(), chartX0 + animW, by, barColor);
                g2.setPaint(gp);
                g2.fillRoundRect(chartX0, by, animW, barH, 4, 4);

                g2.setColor(barColor.darker());
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(chartX0, by, animW, barH, 4, 4);

                // Category label (left)
                g2.setColor(TEXT_DARK);
                g2.setFont(catFont);
                FontMetrics fm = g2.getFontMetrics();
                String cat = categories.get(i);
                if (cat.length() > 12) cat = cat.substring(0, 11) + "…";
                g2.drawString(cat, chartX0 - fm.stringWidth(cat) - 4, by + barH / 2 + 4);

                // Value label at end of bar
                if (progress >= 1f && animW > 20) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String vStr = String.format("%.1f", val);
                    fm = g2.getFontMetrics();
                    g2.drawString(vStr, chartX0 + animW - fm.stringWidth(vStr) - 4, by + barH / 2 + 4);
                }
            }
        }

        private void drawLegend(Graphics2D g2, Insets ins, int y1) {
            if (seriesNames == null || seriesNames.isEmpty()) return;
            int lx = ins.left + 50;
            int ly = y1 + 28;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < seriesNames.size(); i++) {
                Color c = colors.get(i % colors.size());
                g2.setColor(c);
                g2.fillRoundRect(lx, ly - 9, 12, 12, 3, 3);
                g2.setColor(TEXT_DARK);
                g2.drawString(seriesNames.get(i), lx + 16, ly);
                lx += g2.getFontMetrics().stringWidth(seriesNames.get(i)) + 36;
            }
        }

        private void drawEmpty(Graphics2D g2) {
            g2.setColor(TEXT_MID);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            String msg = "Không có dữ liệu để hiển thị";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  ANIMATED PIE CHART PANEL
    // ════════════════════════════════════════════════════════════════
    static class AnimatedPieChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();
        private List<Color>  colors = new ArrayList<>();
        private boolean      empty  = true;

        // Animation
        private float  progress  = 0f;
        private Timer  animTimer;
        private int    hoveredSlice = -1;
        private static final int ANIM_DURATION_MS = 800;
        private static final int ANIM_FPS         = 60;

        AnimatedPieChartPanel(String borderTitle) {
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 229), 1),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), borderTitle,
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13), new Color(25, 45, 85)),
                    new EmptyBorder(4, 8, 8, 8))));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateHover(e.getX(), e.getY());
                }
            });
        }

        void setData(List<String> lbls, List<Double> vals, List<Color> cols) {
            this.labels = lbls;
            this.values = vals;
            this.colors = cols;
            this.empty  = (lbls == null || lbls.isEmpty());
            startAnimation();
        }

        void setEmpty() {
            this.empty = true;
            this.labels = new ArrayList<>();
            this.values = new ArrayList<>();
            stopAnimation();
            repaint();
        }

        private void startAnimation() {
            stopAnimation();
            progress = 0f;
            int steps = ANIM_DURATION_MS / (1000 / ANIM_FPS);
            float delta = 1f / steps;
            animTimer = new Timer(1000 / ANIM_FPS, e -> {
                progress = Math.min(1f, progress + delta);
                repaint();
                if (progress >= 1f) stopAnimation();
            });
            animTimer.start();
        }

        private void stopAnimation() {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        }

        private float ease(float t) {
            // ease-out back (slight overshoot for playfulness)
            float c1 = 1.70158f;
            float c3 = c1 + 1f;
            return 1f + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
        }

        private void updateHover(int mx, int my) {
            if (empty) return;
            Insets ins = getInsets();
            int cx = ins.left + (getWidth() - ins.left - ins.right) / 2;
            int cy = ins.top  + (getHeight() - ins.top - ins.bottom) * 2 / 5;
            int r  = Math.min(getWidth() - ins.left - ins.right,
                              getHeight() - ins.top - ins.bottom) / 3;
            double dist = Math.hypot(mx - cx, my - cy);
            if (dist > r) { hoveredSlice = -1; repaint(); return; }
            double angle = Math.toDegrees(Math.atan2(my - cy, mx - cx));
            if (angle < 0) angle += 360;

            double total  = values.stream().mapToDouble(Double::doubleValue).sum();
            double cursor = 0;
            for (int i = 0; i < values.size(); i++) {
                double sweep = values.get(i) / total * 360;
                if (angle >= cursor && angle < cursor + sweep) {
                    hoveredSlice = i;
                    repaint();
                    return;
                }
                cursor += sweep;
            }
            hoveredSlice = -1;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (empty || values.isEmpty()) {
                drawEmpty(g2);
                g2.dispose();
                return;
            }

            Insets ins = getInsets();
            int w  = getWidth()  - ins.left - ins.right;
            int h  = getHeight() - ins.top  - ins.bottom;
            int cx = ins.left + w / 2;
            int pieH = h * 3 / 5;
            int cy = ins.top + pieH / 2 + 10;
            int r  = Math.min(w, pieH) / 2 - 10;
            if (r < 10) { g2.dispose(); return; }

            double total = values.stream().mapToDouble(Double::doubleValue).sum();
            if (total == 0) { drawEmpty(g2); g2.dispose(); return; }

            // Animated sweep: expand from 0 to full
            float easedP = Math.min(1f, ease(progress));
            double sweepTotal = 360 * easedP;

            double startAngle = -90; // start from top
            for (int i = 0; i < values.size(); i++) {
                double fraction = values.get(i) / total;
                double sweep    = fraction * sweepTotal;
                if (sweep <= 0) continue;

                Color base = colors.get(i % colors.size());
                boolean hov = (i == hoveredSlice && progress >= 1f);

                int dx = 0, dy = 0;
                if (hov) {
                    // Push slice outward on hover
                    double mid = Math.toRadians(startAngle + sweep / 2);
                    dx = (int)(Math.cos(mid) * 8);
                    dy = (int)(Math.sin(mid) * 8);
                }

                // Gradient
                GradientPaint gp = new GradientPaint(
                    cx + dx, cy + dy - r, base.brighter(),
                    cx + dx, cy + dy + r, base.darker());
                g2.setPaint(gp);

                Arc2D arc = new Arc2D.Double(
                    cx - r + dx, cy - r + dy, r * 2, r * 2,
                    startAngle, sweep, Arc2D.PIE);
                g2.fill(arc);

                // Border
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(arc);

                // Label if slice big enough
                if (fraction > 0.06 && progress >= 1f) {
                    double mid = Math.toRadians(startAngle + sweep / 2);
                    int lx = (int)(cx + dx + Math.cos(mid) * r * 0.65);
                    int ly = (int)(cy + dy + Math.sin(mid) * r * 0.65);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String pct = String.format("%.1f%%", fraction * 100);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(pct, lx - fm.stringWidth(pct) / 2, ly + 4);
                }

                startAngle += sweep;
            }

            // Legend
            int legendY = ins.top + pieH + 20;
            int legendX = ins.left + 10;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < labels.size(); i++) {
                Color c = colors.get(i % colors.size());
                g2.setColor(c);
                g2.fillRoundRect(legendX, legendY - 9, 14, 14, 4, 4);
                g2.setColor(new Color(25, 45, 85));
                g2.drawRoundRect(legendX, legendY - 9, 14, 14, 4, 4);
                g2.setColor(TEXT_DARK);
                String lbl = labels.get(i);
                g2.drawString(lbl, legendX + 20, legendY);
                legendX += g2.getFontMetrics().stringWidth(lbl) + 42;
                if (legendX > getWidth() - 60) {
                    legendX = ins.left + 10;
                    legendY += 22;
                }
            }

            g2.dispose();
        }

        private void drawEmpty(Graphics2D g2) {
            g2.setColor(TEXT_MID);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            String msg = "Không có dữ liệu để hiển thị";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════
    public FormBaoCaoNguyenLieu() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);
        buildUI();
        refreshAll();
    }

    // ─────────────────────── BUILD UI ───────────────────────
    private void buildUI() {
        add(buildTopBar(), BorderLayout.NORTH);

        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(CONTENT_BG);
        tabs.setBorder(new EmptyBorder(0, 10, 10, 10));

        // Re-trigger animation when tab is selected
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            switch (idx) {
                case 0 -> { if (chartPanelNgay   != null) chartPanelNgay.startAnimation();   }
                case 1 -> { if (chartPanelKhoang  != null) chartPanelKhoang.startAnimation(); }
                case 2 -> { if (chartPanelCanhBao != null) chartPanelCanhBao.startAnimation(); }
                case 3 -> { if (chartPanelChiTiet != null) chartPanelChiTiet.startAnimation(); }
                case 4 -> { if (chartPanelCanNhap != null) chartPanelCanNhap.startAnimation(); }
            }
        });

        tabs.addTab("Báo Cáo Ngày",    buildTabNgay());
        tabs.addTab("Khoảng Ngày",      buildTabKhoang());
        tabs.addTab("Cảnh Báo Tồn Kho", buildTabCanhBao());
        tabs.addTab("Lịch Sử Xuất Kho", buildTabChiTiet());
        tabs.addTab("Cần Nhập Thêm",    buildTabCanNhap());

        add(tabs, BorderLayout.CENTER);
    }

    // ─────────────────────── TOP BAR ───────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(NAV_BG);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(new EmptyBorder(0, 18, 0, 18));

        JLabel title = new JLabel("BÁO CÁO & THỐNG KÊ NGUYÊN LIỆU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JButton btnRefresh = new JButton("Tải Lại Dữ Liệu") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color col = getModel().isPressed() ? SUCCESS.darker() : getModel().isRollover() ? SUCCESS.brighter() : SUCCESS;
                g2.setColor(col);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g);
            }
        };
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setOpaque(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshAll());

        bar.add(title, BorderLayout.WEST);
        bar.add(btnRefresh, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════
    //  TAB 1 — BÁO CÁO NGÀY
    // ══════════════════════════════════════════════════════
    private JPanel buildTabNgay() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CONTENT_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = buildToolbar();
        JLabel lblDate = bold("Ngày (dd/MM/yyyy):");
        txtNgay = miniField(getTodayStr(), 120);
        JButton btnXem = actionBtn("Xem Báo Cáo", PRIMARY);
        btnXem.addActionListener(e -> loadBaoCaoNgay());
        lblSumNgay = summaryLabel();
        toolbar.add(lblDate); toolbar.add(txtNgay); toolbar.add(btnXem); toolbar.add(lblSumNgay);

        String[] cols = {"Nguyên Liệu", "ĐVT", "Tổng Xuất", "Tồn Hiện Tại", "Tồn Tối Thiểu", "Trạng Thái", "Món Sử Dụng"};
        mdlNgay = mkModel(cols);
        tblNgay  = mkTable(mdlNgay);
        tblNgay.setDefaultRenderer(Object.class, statusRenderer(mdlNgay, 5));

        chartPanelNgay = new AnimatedBarChartPanel("Biểu Đồ Xuất Kho Ngày");
        chartPanelNgay.setPreferredSize(new Dimension(420, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            wrapTable(tblNgay, "Danh Sách Nguyên Liệu"), chartPanelNgay);
        split.setResizeWeight(0.60);
        split.setDividerSize(5);
        split.setBorder(null);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        p.add(legendRow(
            new String[]{"Hết hàng", "Sắp hết", "Tồn quá nhiều", "Bình thường"},
            new Color[]{new Color(255, 200, 200), new Color(255, 243, 205), new Color(209, 231, 221), Color.WHITE}),
            BorderLayout.SOUTH);
        return p;
    }

    private void loadBaoCaoNgay() {
        String ngay = toSqlDate(txtNgay.getText().trim());
        String sql =
            "SELECT nl.TenNguyenLieu, nl.DonViTinh, " +
            "COALESCE(SUM(x.SoLuong),0) AS TongXuat, " +
            "nl.SoLuong AS TonHienTai, nl.MucTonToiThieu, " +
            "CASE WHEN nl.SoLuong<=0 THEN 'Hết hàng' " +
            "     WHEN nl.SoLuong<=nl.MucTonToiThieu THEN 'Sắp hết' " +
            "     WHEN nl.MucTonToiDa>0 AND nl.SoLuong>nl.MucTonToiDa THEN 'Tồn quá nhiều' " +
            "     ELSE 'Bình thường' END AS TrangThai, " +
            "GROUP_CONCAT(DISTINCT m.TenMon ORDER BY m.TenMon SEPARATOR ', ') AS MonDung " +
            "FROM nguyenlieu nl " +
            "LEFT JOIN lichsuxuatkho x ON x.MaNL=nl.MaNL AND DATE(x.NgayXuat)=? " +
            "LEFT JOIN monan m ON m.MaMon=x.MaMon " +
            "WHERE nl.TrangThai=1 " +
            "GROUP BY nl.MaNL,nl.TenNguyenLieu,nl.DonViTinh,nl.SoLuong,nl.MucTonToiThieu,nl.MucTonToiDa " +
            "ORDER BY TongXuat DESC";

        List<String> names = new ArrayList<>();
        List<Double>  xuat  = new ArrayList<>();
        List<Double>  ton   = new ArrayList<>();
        double sumXuat = 0;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ngay);
            ResultSet rs = ps.executeQuery();
            mdlNgay.setRowCount(0);
            while (rs.next()) {
                double tx = rs.getDouble("TongXuat");
                sumXuat += tx;
                String ten = rs.getString("TenNguyenLieu");
                mdlNgay.addRow(new Object[]{
                    ten, rs.getString("DonViTinh"),
                    fmt2(tx), fmt2(rs.getDouble("TonHienTai")),
                    fmt2(rs.getDouble("MucTonToiThieu")),
                    rs.getString("TrangThai"),
                    rs.getString("MonDung") != null ? rs.getString("MonDung") : "—"
                });
                if (tx > 0 && names.size() < 10) {
                    names.add(shorten(ten));
                    xuat.add(tx);
                    ton.add(rs.getDouble("TonHienTai"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        lblSumNgay.setText("  Tổng xuất: " + fmt2(sumXuat));

        if (!names.isEmpty()) {
            chartPanelNgay.setData(names,
                Arrays.asList(xuat, ton),
                Arrays.asList("Xuất kho", "Tồn kho"),
                Arrays.asList(DANGER, SUCCESS),
                false);
        } else {
            chartPanelNgay.setEmpty();
        }
    }

    // ══════════════════════════════════════════════════════
    //  TAB 2 — KHOẢNG NGÀY
    // ══════════════════════════════════════════════════════
    private JPanel buildTabKhoang() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CONTENT_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = buildToolbar();
        toolbar.add(bold("Từ ngày:")); txtTu  = miniField(getTodayStr(), 110); toolbar.add(txtTu);
        toolbar.add(bold("Đến ngày:")); txtDen = miniField(getTodayStr(), 110); toolbar.add(txtDen);
        JButton btn = actionBtn("Xem Báo Cáo", PRIMARY);
        btn.addActionListener(e -> loadBaoCaoKhoang());
        lblSumKhoang = summaryLabel();
        toolbar.add(btn); toolbar.add(lblSumKhoang);

        String[] cols = {"Nguyên Liệu", "ĐVT", "Tổng Xuất", "Tồn Hiện Tại", "Trạng Thái", "Số Ngày Dùng", "Món Sử Dụng"};
        mdlKhoang = mkModel(cols);
        tblKhoang  = mkTable(mdlKhoang);
        tblKhoang.setDefaultRenderer(Object.class, statusRenderer(mdlKhoang, 4));

        chartPanelKhoang = new AnimatedBarChartPanel("Biểu Đồ Xuất Theo Khoảng Thời Gian");
        chartPanelKhoang.setPreferredSize(new Dimension(420, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            wrapTable(tblKhoang, "Thống Kê Khoảng Ngày"), chartPanelKhoang);
        split.setResizeWeight(0.60);
        split.setDividerSize(5);
        split.setBorder(null);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        p.add(legendRow(
            new String[]{"Hết hàng", "Sắp hết", "Bình thường"},
            new Color[]{new Color(255, 200, 200), new Color(255, 243, 205), Color.WHITE}),
            BorderLayout.SOUTH);
        return p;
    }

    private void loadBaoCaoKhoang() {
        String tu  = toSqlDate(txtTu.getText().trim());
        String den = toSqlDate(txtDen.getText().trim());
        String sql =
            "SELECT nl.TenNguyenLieu, nl.DonViTinh, " +
            "COALESCE(SUM(x.SoLuong),0) AS TongXuat, nl.SoLuong AS TonHienTai, " +
            "CASE WHEN nl.SoLuong<=0 THEN 'Hết hàng' " +
            "     WHEN nl.SoLuong<=nl.MucTonToiThieu THEN 'Sắp hết' " +
            "     ELSE 'Bình thường' END AS TrangThai, " +
            "COUNT(DISTINCT DATE(x.NgayXuat)) AS SoNgayDung, " +
            "GROUP_CONCAT(DISTINCT m.TenMon ORDER BY m.TenMon SEPARATOR ', ') AS MonDung " +
            "FROM nguyenlieu nl " +
            "LEFT JOIN lichsuxuatkho x ON x.MaNL=nl.MaNL AND DATE(x.NgayXuat) BETWEEN ? AND ? " +
            "LEFT JOIN monan m ON m.MaMon=x.MaMon " +
            "WHERE nl.TrangThai=1 " +
            "GROUP BY nl.MaNL,nl.TenNguyenLieu,nl.DonViTinh,nl.SoLuong,nl.MucTonToiThieu " +
            "ORDER BY TongXuat DESC";

        List<String> names = new ArrayList<>();
        List<Double>  xuat  = new ArrayList<>();
        double sum = 0;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tu); ps.setString(2, den);
            ResultSet rs = ps.executeQuery();
            mdlKhoang.setRowCount(0);
            while (rs.next()) {
                double tx = rs.getDouble("TongXuat"); sum += tx;
                String ten = rs.getString("TenNguyenLieu");
                mdlKhoang.addRow(new Object[]{
                    ten, rs.getString("DonViTinh"),
                    fmt2(tx), fmt2(rs.getDouble("TonHienTai")),
                    rs.getString("TrangThai"),
                    rs.getInt("SoNgayDung") + " ngày",
                    rs.getString("MonDung") != null ? rs.getString("MonDung") : "—"
                });
                if (tx > 0 && names.size() < 10) { names.add(shorten(ten)); xuat.add(tx); }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        lblSumKhoang.setText("  Tổng xuất: " + fmt2(sum));

        if (!names.isEmpty()) {
            // Reverse for horizontal (top = biggest)
            List<String> rev = new ArrayList<>(names); Collections.reverse(rev);
            List<Double>  rval = new ArrayList<>(xuat);  Collections.reverse(rval);
            chartPanelKhoang.setData(rev,
                Arrays.asList(rval),
                Arrays.asList("Xuất kho"),
                Arrays.asList(PRIMARY),
                true); // horizontal
        } else {
            chartPanelKhoang.setEmpty();
        }
    }

    // ══════════════════════════════════════════════════════
    //  TAB 3 — CẢNH BÁO TỒN KHO
    // ══════════════════════════════════════════════════════
    private JPanel buildTabCanhBao() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CONTENT_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(255, 243, 205));
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNING, 1), new EmptyBorder(10, 14, 10, 14)));
        lblCanhBaoCount = new JLabel("Đang tải...");
        lblCanhBaoCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCanhBaoCount.setForeground(new Color(160, 90, 0));
        banner.add(lblCanhBaoCount, BorderLayout.CENTER);

        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "ĐVT", "Tồn Hiện Tại", "Tồn Tối Thiểu", "Trạng Thái"};
        mdlCanhBao = mkModel(cols);
        tblCanhBao  = mkTable(mdlCanhBao);
        tblCanhBao.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int mr = t.convertRowIndexToModel(r);
                    String tt = String.valueOf(mdlCanhBao.getValueAt(mr, 5));
                    comp.setBackground("Hết hàng".equals(tt) ? new Color(255, 200, 200) : new Color(255, 243, 205));
                    comp.setForeground(TEXT_DARK);
                } else { comp.setBackground(new Color(213, 232, 255)); comp.setForeground(NAV_BG); }
                return comp;
            }
        });

        chartPanelCanhBao = new AnimatedBarChartPanel("Biểu Đồ Tồn Kho Sắp Hết");
        chartPanelCanhBao.setPreferredSize(new Dimension(420, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            wrapTable(tblCanhBao, "Danh Sách Cần Bổ Sung"), chartPanelCanhBao);
        split.setResizeWeight(0.55);
        split.setDividerSize(5);
        split.setBorder(null);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(CONTENT_BG);
        top.add(banner, BorderLayout.CENTER);

        p.add(top, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private void loadCanhBao() {
        String sql = "SELECT MaNL, TenNguyenLieu, DonViTinh, SoLuong, MucTonToiThieu, " +
                     "CASE WHEN SoLuong<=0 THEN 'Hết hàng' ELSE 'Sắp hết' END AS TrangThai " +
                     "FROM nguyenlieu WHERE TrangThai=1 AND SoLuong<=MucTonToiThieu ORDER BY SoLuong ASC";

        List<String> names   = new ArrayList<>();
        List<Double>  tonList = new ArrayList<>();
        List<Double>  minList = new ArrayList<>();

        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            mdlCanhBao.setRowCount(0);
            while (rs.next()) {
                String ten = rs.getString("TenNguyenLieu");
                double ton = rs.getDouble("SoLuong");
                double min = rs.getDouble("MucTonToiThieu");
                mdlCanhBao.addRow(new Object[]{
                    rs.getInt("MaNL"), ten, rs.getString("DonViTinh"),
                    fmt2(ton), fmt2(min), rs.getString("TrangThai")
                });
                if (names.size() < 12) { names.add(shorten(ten)); tonList.add(ton); minList.add(min); }
            }
            int cnt = mdlCanhBao.getRowCount();
            if (cnt == 0) { loadCanhBaoMoRong(); return; }
            lblCanhBaoCount.setText("CÓ " + cnt + " NGUYÊN LIỆU SẮP HẾT / HẾT KHO — Cần nhập thêm ngay!");
            tabs.setTitleAt(2, "Cảnh Báo (" + cnt + ")");
        } catch (SQLException e) { e.printStackTrace(); }

        if (!names.isEmpty()) {
            chartPanelCanhBao.setData(names,
                Arrays.asList(tonList, minList),
                Arrays.asList("Tồn Hiện Tại", "Tồn Tối Thiểu"),
                Arrays.asList(DANGER, WARNING),
                false);
        } else {
            chartPanelCanhBao.setEmpty();
        }
    }

    private void loadCanhBaoMoRong() {
        String sql = "SELECT MaNL, TenNguyenLieu, DonViTinh, SoLuong, MucTonToiThieu, " +
                     "CASE WHEN SoLuong<=0 THEN 'Hết hàng' " +
                     "     WHEN SoLuong<=MucTonToiThieu THEN 'Sắp hết' " +
                     "     WHEN SoLuong <= MucTonToiThieu * 2 THEN 'Tồn thấp' " +
                     "     ELSE 'Bình thường' END AS TrangThai " +
                     "FROM nguyenlieu WHERE TrangThai=1 " +
                     "ORDER BY (SoLuong / NULLIF(MucTonToiThieu,0)) ASC LIMIT 10";

        List<String> names   = new ArrayList<>();
        List<Double>  tonList = new ArrayList<>();
        List<Double>  minList = new ArrayList<>();

        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            mdlCanhBao.setRowCount(0);
            while (rs.next()) {
                String ten = rs.getString("TenNguyenLieu");
                double ton = rs.getDouble("SoLuong");
                double min = rs.getDouble("MucTonToiThieu");
                mdlCanhBao.addRow(new Object[]{
                    rs.getInt("MaNL"), ten, rs.getString("DonViTinh"),
                    fmt2(ton), fmt2(min), rs.getString("TrangThai")
                });
                if (names.size() < 10) { names.add(shorten(ten)); tonList.add(ton); minList.add(min); }
            }
            lblCanhBaoCount.setText("Tất cả nguyên liệu đang đủ tồn — Hiển thị top 10 NL có tồn kho thấp nhất.");
            tabs.setTitleAt(2, "Cảnh Báo Tồn Kho");
        } catch (SQLException e) { e.printStackTrace(); }

        if (!names.isEmpty()) {
            chartPanelCanhBao.setData(names,
                Arrays.asList(tonList, minList),
                Arrays.asList("Tồn Hiện Tại", "Tồn Tối Thiểu"),
                Arrays.asList(PRIMARY, WARNING),
                false);
        } else {
            chartPanelCanhBao.setEmpty();
        }
    }

    // ══════════════════════════════════════════════════════
    //  TAB 4 — LỊCH SỬ XUẤT CHI TIẾT
    // ══════════════════════════════════════════════════════
    private JPanel buildTabChiTiet() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CONTENT_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = buildToolbar();
        toolbar.add(bold("Lọc ngày (để trống = tất cả):"));
        txtNgayLoc = miniField("", 120);
        JButton btn = actionBtn("Lọc Dữ Liệu", PRIMARY);
        btn.addActionListener(e -> loadChiTietXuat());
        lblSumChiTiet = summaryLabel();
        toolbar.add(txtNgayLoc); toolbar.add(btn); toolbar.add(lblSumChiTiet);

        String[] cols = {"Thời Gian", "Nguyên Liệu", "ĐVT", "Số Lượng", "Lý Do", "Loại Xuất", "Món Ăn", "Mã HĐ"};
        mdlChiTiet = mkModel(cols);
        tblChiTiet  = mkTable(mdlChiTiet);
        tblChiTiet.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int mr = t.convertRowIndexToModel(r);
                    String loai = String.valueOf(mdlChiTiet.getValueAt(mr, 5));
                    comp.setBackground(loai.contains("Tự động") ? AUTO_BLUE : r % 2 == 0 ? Color.WHITE : ROW_ALT);
                    comp.setForeground(TEXT_DARK);
                } else { comp.setBackground(new Color(213, 232, 255)); comp.setForeground(NAV_BG); }
                return comp;
            }
        });

        chartPanelChiTiet = new AnimatedPieChartPanel("Phân Bố Loại Xuất Kho");
        chartPanelChiTiet.setPreferredSize(new Dimension(380, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            wrapTable(tblChiTiet, "Chi Tiết Xuất Kho"), chartPanelChiTiet);
        split.setResizeWeight(0.62);
        split.setDividerSize(5);
        split.setBorder(null);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        p.add(legendRow(
            new String[]{"Xuất tự động (bán hàng)", "Xuất thủ công"},
            new Color[]{AUTO_BLUE, Color.WHITE}),
            BorderLayout.SOUTH);
        return p;
    }

    private void loadChiTietXuat() {
        String ngayFilter = txtNgayLoc.getText().trim();
        StringBuilder sql = new StringBuilder(
            "SELECT DATE_FORMAT(x.NgayXuat,'%d/%m/%Y %H:%i') AS ThoiGian, " +
            "nl.TenNguyenLieu, nl.DonViTinh, x.SoLuong, x.LyDo, " +
            "CASE WHEN LOWER(x.LyDo) = 'ban hang' OR x.LyDo = 'Bán hàng' " +
            "     THEN 'Tự động (bán hàng)' ELSE 'Thủ công' END AS LoaiXuat, " +
            "COALESCE(m.TenMon,'—') AS TenMon, " +
            "CASE WHEN x.MaHoaDon IS NOT NULL THEN CONCAT('#',x.MaHoaDon) ELSE '—' END AS MaHD " +
            "FROM lichsuxuatkho x JOIN nguyenlieu nl ON nl.MaNL=x.MaNL " +
            "LEFT JOIN monan m ON m.MaMon=x.MaMon ");
        if (!ngayFilter.isEmpty()) sql.append("WHERE DATE(x.NgayXuat)=? ");
        sql.append("ORDER BY x.NgayXuat DESC LIMIT 2000");

        int cntTuDong = 0, cntThuCong = 0;
        double sumSL = 0;

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            if (!ngayFilter.isEmpty()) ps.setString(1, toSqlDate(ngayFilter));
            ResultSet rs = ps.executeQuery();
            mdlChiTiet.setRowCount(0);
            while (rs.next()) {
                double sl = rs.getDouble("SoLuong");
                sumSL += sl;
                String loai = rs.getString("LoaiXuat");
                if ("Tự động (bán hàng)".equals(loai)) cntTuDong++; else cntThuCong++;
                mdlChiTiet.addRow(new Object[]{
                    rs.getString("ThoiGian"), rs.getString("TenNguyenLieu"),
                    rs.getString("DonViTinh"), fmt2(sl),
                    rs.getString("LyDo"), loai, rs.getString("TenMon"), rs.getString("MaHD")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        int total = cntTuDong + cntThuCong;
        lblSumChiTiet.setText(String.format("  %d dòng | Tổng SL: %s | Tự động: %d | Thủ công: %d",
            total, fmt2(sumSL), cntTuDong, cntThuCong));

        if (total > 0) {
            List<String> labels = new ArrayList<>();
            List<Double>  vals   = new ArrayList<>();
            List<Color>   cols   = new ArrayList<>();
            if (cntTuDong > 0) { labels.add("Tự động (" + cntTuDong + ")"); vals.add((double) cntTuDong); cols.add(PRIMARY); }
            if (cntThuCong > 0) { labels.add("Thủ công (" + cntThuCong + ")"); vals.add((double) cntThuCong); cols.add(PURPLE); }
            chartPanelChiTiet.setData(labels, vals, cols);
        } else {
            chartPanelChiTiet.setEmpty();
        }
    }

    // ══════════════════════════════════════════════════════
    //  TAB 5 — CẦN NHẬP THÊM
    // ══════════════════════════════════════════════════════
    private JPanel buildTabCanNhap() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CONTENT_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel infoBox = new JPanel(new BorderLayout());
        infoBox.setBackground(new Color(232, 244, 248));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1), new EmptyBorder(10, 14, 10, 14)));
        JLabel hint = new JLabel("Danh sách nguyên liệu có tồn kho ≤ mức tối thiểu và lượng cần nhập thêm để đạt tồn tối đa.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(NAV_BG);
        infoBox.add(hint, BorderLayout.CENTER);

        String[] cols = {"Nguyên Liệu", "ĐVT", "Tồn Hiện Tại", "Tồn Tối Thiểu", "Tồn Tối Đa", "Cần Nhập Thêm", "Đơn Giá (VNĐ)", "Tổng Chi Phí"};
        mdlCanNhap = mkModel(cols);
        tblCanNhap  = mkTable(mdlCanNhap);
        tblCanNhap.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int mr = t.convertRowIndexToModel(r);
                    try {
                        double ton = Double.parseDouble(String.valueOf(mdlCanNhap.getValueAt(mr, 2)));
                        comp.setBackground(ton <= 0 ? new Color(255, 200, 200) : new Color(255, 243, 205));
                    } catch (Exception ex) { comp.setBackground(ROW_ALT); }
                    comp.setForeground(TEXT_DARK);
                } else { comp.setBackground(new Color(213, 232, 255)); comp.setForeground(NAV_BG); }
                return comp;
            }
        });

        chartPanelCanNhap = new AnimatedBarChartPanel("Lượng Cần Nhập Thêm (Top 10)");
        chartPanelCanNhap.setPreferredSize(new Dimension(420, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            wrapTable(tblCanNhap, "Danh Sách Cần Nhập Bổ Sung"), chartPanelCanNhap);
        split.setResizeWeight(0.58);
        split.setDividerSize(5);
        split.setBorder(null);

        p.add(infoBox, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private void loadCanNhap() {
        String sql =
            "SELECT TenNguyenLieu, DonViTinh, SoLuong, MucTonToiThieu, MucTonToiDa, DonGia, " +
            "GREATEST(0, COALESCE(NULLIF(MucTonToiDa,0), MucTonToiThieu*3) - SoLuong) AS CanNhap " +
            "FROM nguyenlieu WHERE TrangThai=1 AND SoLuong<=MucTonToiThieu ORDER BY SoLuong ASC";

        List<String> names   = new ArrayList<>();
        List<Double>  canNhap = new ArrayList<>();

        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            mdlCanNhap.setRowCount(0);
            while (rs.next()) {
                String ten = rs.getString("TenNguyenLieu");
                double cn = rs.getDouble("CanNhap");
                double donGia = rs.getDouble("DonGia");
                double chiPhi = cn * donGia;
                mdlCanNhap.addRow(new Object[]{
                    ten, rs.getString("DonViTinh"),
                    fmt2(rs.getDouble("SoLuong")),
                    fmt2(rs.getDouble("MucTonToiThieu")),
                    rs.getDouble("MucTonToiDa") > 0 ? fmt2(rs.getDouble("MucTonToiDa")) : "Chưa đặt",
                    fmt2(cn), fmtMoney(donGia), fmtMoney(chiPhi)
                });
                if (names.size() < 10) { names.add(shorten(ten)); canNhap.add(cn); }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (mdlCanNhap.getRowCount() == 0) {
            loadCanNhapMoRong(names, canNhap);
        } else {
            if (!names.isEmpty()) {
                List<String> rev  = new ArrayList<>(names);   Collections.reverse(rev);
                List<Double>  rval = new ArrayList<>(canNhap); Collections.reverse(rval);
                chartPanelCanNhap.setData(rev, Arrays.asList(rval),
                    Arrays.asList("Cần nhập"), Arrays.asList(DANGER), true);
            } else {
                chartPanelCanNhap.setEmpty();
            }
        }
    }

    private void loadCanNhapMoRong(List<String> names, List<Double> canNhap) {
        String sql =
            "SELECT TenNguyenLieu, DonViTinh, SoLuong, MucTonToiThieu, MucTonToiDa, DonGia, " +
            "GREATEST(0, COALESCE(NULLIF(MucTonToiDa,0), MucTonToiThieu*3) - SoLuong) AS CanNhap " +
            "FROM nguyenlieu WHERE TrangThai=1 " +
            "ORDER BY (SoLuong / NULLIF(MucTonToiThieu,0)) ASC LIMIT 8";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            mdlCanNhap.setRowCount(0);
            while (rs.next()) {
                String ten = rs.getString("TenNguyenLieu");
                double cn = rs.getDouble("CanNhap");
                double donGia = rs.getDouble("DonGia");
                double chiPhi = cn * donGia;
                mdlCanNhap.addRow(new Object[]{
                    ten, rs.getString("DonViTinh"),
                    fmt2(rs.getDouble("SoLuong")),
                    fmt2(rs.getDouble("MucTonToiThieu")),
                    rs.getDouble("MucTonToiDa") > 0 ? fmt2(rs.getDouble("MucTonToiDa")) : "Chưa đặt",
                    fmt2(cn), fmtMoney(donGia), fmtMoney(chiPhi)
                });
                if (names.size() < 8) {
                    names.add(shorten(ten));
                    canNhap.add(Math.max(cn, rs.getDouble("MucTonToiThieu") * 0.5));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (!names.isEmpty()) {
            List<String> rev  = new ArrayList<>(names);   Collections.reverse(rev);
            List<Double>  rval = new ArrayList<>(canNhap); Collections.reverse(rval);
            chartPanelCanNhap.setData(rev, Arrays.asList(rval),
                Arrays.asList("Đề xuất nhập"), Arrays.asList(new Color(230, 120, 20)), true);
        } else {
            chartPanelCanNhap.setEmpty();
        }
    }

    // ─────────────────── REFRESH ALL ───────────────────
    private void refreshAll() {
        loadBaoCaoNgay();
        loadBaoCaoKhoang();
        loadCanhBao();
        loadChiTietXuat();
        loadCanNhap();
    }

    // ══════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════
    private DefaultTableModel mkModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable mkTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setGridColor(new Color(235, 238, 242));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                l.setBackground(NAV_BG);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                return l;
            }
        });
        return t;
    }

    private JPanel wrapTable(JTable t, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(cardBorder(title));
        JScrollPane sc = new JScrollPane(t);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sc.getViewport().setBackground(CARD_BG);
        card.add(sc, BorderLayout.CENTER);
        return card;
    }

    private Border cardBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEmptyBorder(), title,
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 13), NAV_BG),
                new EmptyBorder(4, 8, 8, 8)));
    }

    private JPanel buildToolbar() {
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tb.setBackground(CARD_BG);
        tb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(4, 8, 4, 8)));
        return tb;
    }

    private JButton actionBtn(String txt, Color bg) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color col = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(col);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField miniField(String val, int w) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(w, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JLabel bold(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JLabel summaryLabel() {
        JLabel l = new JLabel();
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(PRIMARY);
        l.setBorder(new EmptyBorder(0, 8, 0, 0));
        return l;
    }

    private DefaultTableCellRenderer statusRenderer(DefaultTableModel mdl, int statusCol) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    int mr = t.convertRowIndexToModel(r);
                    String tt = String.valueOf(mdl.getValueAt(mr, statusCol));
                    if      ("Hết hàng".equals(tt))       comp.setBackground(new Color(255, 200, 200));
                    else if ("Sắp hết".equals(tt))        comp.setBackground(new Color(255, 243, 205));
                    else if ("Tồn quá nhiều".equals(tt))  comp.setBackground(new Color(209, 231, 221));
                    else comp.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
                    comp.setForeground(TEXT_DARK);
                } else { comp.setBackground(new Color(213, 232, 255)); comp.setForeground(NAV_BG); }
                return comp;
            }
        };
    }

    private JPanel legendRow(String[] labels, Color[] colors) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(4, 8, 4, 8)));
        JLabel lead = new JLabel("Chú thích:");
        lead.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lead.setForeground(TEXT_MID);
        p.add(lead);
        for (int i = 0; i < labels.length; i++) {
            JLabel chip = new JLabel("  " + labels[i] + "  ");
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chip.setOpaque(true);
            chip.setBackground(colors[i]);
            chip.setBorder(BorderFactory.createLineBorder(colors[i].darker(), 1));
            p.add(chip);
        }
        return p;
    }

    private String shorten(String s) {
        if (s == null) return "";
        return s.length() > 14 ? s.substring(0, 13) + "." : s;
    }

    private String fmt2(double v)     { return String.format("%.2f", v); }
    private String fmtMoney(double v) { return String.format("%,.0f", v); }

    private String getTodayStr() {
        java.time.LocalDate d = java.time.LocalDate.now();
        return String.format("%02d/%02d/%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    private String toSqlDate(String dmy) {
        if (dmy == null || dmy.trim().isEmpty()) return "";
        String[] p = dmy.split("/");
        return p.length == 3 ? p[2] + "-" + p[1] + "-" + p[0] : dmy;
    }
}