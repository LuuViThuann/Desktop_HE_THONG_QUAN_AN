package view;

import dao.ThongKeDAO;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class ThongKeTongQuanForm extends JPanel {

    // ============ COLOR PALETTE ============
    private static final Color PRIMARY_BG     = new Color(250, 251, 252);
    private static final Color CARD_BG        = new Color(255, 255, 255);
    private static final Color TEXT_PRIMARY   = new Color(31,  41,  55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color TEXT_LIGHT     = new Color(156, 163, 175);

    private static final Color ACCENT_BLUE    = new Color(59,  130, 246);
    private static final Color ACCENT_GREEN   = new Color(16,  185, 129);
    private static final Color ACCENT_ORANGE  = new Color(251, 146,  60);
    private static final Color ACCENT_RED     = new Color(239,  68,  68);
    private static final Color ACCENT_PURPLE  = new Color(139,  92, 246);
    private static final Color ACCENT_TEAL    = new Color(20,  184, 166);

    private static final Color BORDER_LIGHT   = new Color(229, 231, 235);
    private static final Color SHADOW_COLOR   = new Color(0, 0, 0, 8);
    private static final Color ROW_EVEN       = new Color(249, 250, 251);
    private static final Color ROW_HOVER      = new Color(239, 246, 255);

    private static final int STAGGER_DELAY_MS = 55;
    private static final int COUNTER_INTERVAL = 16;

    // ============ LABELS ============
    private JLabel lblDoanhThuHomNay, lblDoanhThuHomQua, lblTangGiam;
    private JLabel lblSoHoaDonHomNay, lblSoHoaDonDangMo, lblGiaTriTrungBinh;
    private JLabel lblTongBan, lblDangSuDung, lblTrong, lblDaDat;
    private JLabel lblTongMonHomNay, lblSoLoaiMon, lblMonBanChayNhat;

    // ============ CHART PANELS ============
    private NativeBarChartPanel  chartDoanhThu;
    private NativeBarChartPanel  chartTopMon;
    private NativeBarChartPanel  chartTinhTrangBan;
    private NativePieChartPanel  chartCongSuat;
    private NativePieChartPanel  chartMonTheoNhom;
    private NativeLineChartPanel chartMonTheoGio;

    private JPanel panelBangChiTietMon;
    private final List<SlideCard> slideCards      = new ArrayList<>();
    private final List<SlideCard> slideChartCards = new ArrayList<>();

    // Scroll viewport — dùng cho scroll-triggered animation
    private JScrollPane mainScrollPane;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // ════════════════════════════════════════════════════════════════
    //  SLIDE-UP CARD — fade-in + slide từ dưới lên
    //  Hỗ trợ scroll-triggered: tự kiểm tra xem mình có trong viewport
    // ════════════════════════════════════════════════════════════════
    static class SlideCard extends JPanel {
        private float   alpha     = 0f;
        private float   offsetY   = 30f;
        private boolean triggered = false;   // đã trigger chưa
        private boolean animating = false;
        private Timer   animTimer;
        private static final int DUR = 520;
        private static final int FPS = 60;

        SlideCard(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        /** Gọi khi scroll — kiểm tra xem card có trong viewport không */
        void checkVisibility(JViewport viewport) {
            if (triggered) return;
            if (!isShowing()) return;
            // Vị trí của card trong viewport
            Point cardInView = SwingUtilities.convertPoint(this, 0, 0, viewport);
            int vpH = viewport.getHeight();
            // Card được coi là "thấy" khi top của nó nằm trong viewport
            if (cardInView.y < vpH && cardInView.y + getHeight() > 0) {
                triggered = true;
                startAnim(0);
            }
        }

        /** Trigger ngay lập tức (không cần chờ scroll) */
        void animateIn(int delayMs) {
            triggered = true;
            alpha = 0f; offsetY = 30f; repaint();
            if (delayMs <= 0) { startAnim(0); return; }
            Timer d = new Timer(delayMs, e -> { ((Timer)e.getSource()).stop(); startAnim(0); });
            d.setRepeats(false); d.start();
        }

        void reset() {
            triggered = false; alpha = 0f; offsetY = 30f;
            if (animTimer != null) animTimer.stop();
            animating = false; repaint();
        }

        private void startAnim(int extraDelay) {
            if (animating) return;
            if (extraDelay > 0) {
                Timer d = new Timer(extraDelay, e -> { ((Timer)e.getSource()).stop(); doStartAnim(); });
                d.setRepeats(false); d.start();
            } else { doStartAnim(); }
        }

        private void doStartAnim() {
            animating = true;
            final long t0 = System.currentTimeMillis();
            animTimer = new Timer(1000 / FPS, e -> {
                float t  = Math.min(1f, (float)(System.currentTimeMillis() - t0) / DUR);
                float ep = easeOutCubic(t);
                alpha   = ep;
                offsetY = 30f * (1f - ep);
                repaint();
                if (t >= 1f) {
                    ((Timer)e.getSource()).stop();
                    animating = false;
                    alpha = 1f; offsetY = 0f;
                    repaint();
                }
            });
            animTimer.start();
        }

        private static float easeOutCubic(float t) { return 1f-(1f-t)*(1f-t)*(1f-t); }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
            g2.translate(0, (int) offsetY);
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SHIMMER PANEL
    // ════════════════════════════════════════════════════════════════
    static class ShimmerPanel extends JPanel {
        private float shimX = -0.5f;
        private Timer shimTimer;
        private final int radius;

        ShimmerPanel(int radius) {
            this.radius = radius; setOpaque(false);
            shimTimer = new Timer(16, e -> {
                shimX += 0.018f; if (shimX > 1.5f) shimX = -0.5f; repaint();
            });
            shimTimer.start();
        }
        void stopShimmer() { if (shimTimer != null) shimTimer.stop(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(233, 235, 239));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            float cx = shimX * getWidth();
            GradientPaint gp = new GradientPaint(
                cx - getWidth()*.3f, 0, new Color(233,235,239,0),
                cx,                  0, new Color(247,249,251,220));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  NATIVE BAR CHART
    // ════════════════════════════════════════════════════════════════
    static class NativeBarChartPanel extends JPanel {
        List<String>       categories  = new ArrayList<>();
        List<List<Double>> seriesData  = new ArrayList<>();
        List<String>       seriesNames = new ArrayList<>();
        List<Color>        colors      = new ArrayList<>();
        boolean            horizontal  = false;
        boolean            empty       = true;
        float              progress    = 0f;
        private Timer      animTimer;
        private static final int DUR = 700, FPS = 60;

        NativeBarChartPanel() { setOpaque(false); }

        void setData(List<String> cats, List<List<Double>> series,
                     List<String> names, List<Color> cols, boolean horiz) {
            categories  = cats   != null ? cats   : new ArrayList<>();
            seriesData  = series != null ? series : new ArrayList<>();
            seriesNames = names  != null ? names  : new ArrayList<>();
            colors      = cols   != null ? cols   : new ArrayList<>();
            horizontal  = horiz;
            empty       = categories.isEmpty();
            startAnim();
        }

        void setEmpty() {
            empty = true; categories = new ArrayList<>(); seriesData = new ArrayList<>();
            if (animTimer != null) animTimer.stop(); repaint();
        }

        void startAnim() {
            if (animTimer != null) animTimer.stop();
            progress = 0f;
            float delta = 1f / (DUR / (1000 / FPS));
            animTimer = new Timer(1000 / FPS, e -> {
                progress = Math.min(1f, progress + delta); repaint();
                if (progress >= 1f) ((Timer)e.getSource()).stop();
            });
            animTimer.start();
        }

        private static float ease(float t) { return 1f-(1f-t)*(1f-t)*(1f-t); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getWidth() < 60 || getHeight() < 60) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (empty || categories.isEmpty()) { drawEmpty(g2); g2.dispose(); return; }

            int pad  = 10, legH = 48;
            int axL  = horizontal ? 96 : 52;
            int x0 = pad + axL, y0 = pad + 8;
            int x1 = getWidth() - pad - 8;
            int y1 = getHeight() - pad - legH;

            if (x1-x0 < 30 || y1-y0 < 30) { g2.dispose(); return; }

            if (horizontal) drawHBars(g2, x0, y0, x1, y1);
            else             drawVBars(g2, x0, y0, x1, y1);
            drawLegend(g2, x0, y1);
            g2.dispose();
        }

        private void drawVBars(Graphics2D g2, int x0, int y0, int x1, int y1) {
            int n = categories.size(), ns = seriesData.size();
            if (n == 0) return;
            double maxV = 0;
            for (List<Double> s : seriesData) for (double v : s) if (v > maxV) maxV = v;
            if (maxV == 0) maxV = 1;

            g2.setStroke(new BasicStroke(.5f));
            for (int i = 0; i <= 5; i++) {
                int gy = y1 - (int)((y1-y0)*i/5.0);
                g2.setColor(new Color(229,231,235)); g2.drawLine(x0, gy, x1, gy);
                g2.setColor(TEXT_SECONDARY); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.format("%.0f", maxV*i/5.0), x0-46, gy+4);
            }
            g2.setStroke(new BasicStroke(1.5f)); g2.setColor(new Color(200,206,214));
            g2.drawLine(x0, y0, x0, y1); g2.drawLine(x0, y1, x1, y1);

            float ep = ease(progress);
            int gW = (x1-x0)/n, pad2 = Math.max(4, gW/8);
            int bW = ns > 0 ? (gW-2*pad2)/ns : gW-2*pad2;
            bW = Math.max(bW, 4);
            Font cf = new Font("Segoe UI", Font.PLAIN, 10);

            for (int i = 0; i < n; i++) {
                int gx = x0 + i*gW;
                for (int s = 0; s < ns; s++) {
                    if (s >= seriesData.size() || i >= seriesData.get(s).size()) continue;
                    double val = seriesData.get(s).get(i);
                    int bx = gx+pad2+s*bW, fH = (int)((y1-y0)*val/maxV);
                    int aH = (int)(fH*ep), by = y1-aH;
                    Color base = colors.get(s % colors.size());
                    g2.setPaint(new GradientPaint(bx, by, base.brighter(), bx, y1, base));
                    g2.fillRoundRect(bx, by, bW-2, Math.max(aH,1), 5, 5);
                    g2.setColor(base.darker()); g2.setStroke(new BasicStroke(.7f));
                    if (aH > 0) g2.drawRoundRect(bx, by, bW-2, aH, 5, 5);
                    if (progress >= 1f && aH > 16) {
                        g2.setColor(TEXT_PRIMARY); g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        String vs = String.format("%.1f", val);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(vs, bx+(bW-2-fm.stringWidth(vs))/2, by-3);
                        g2.setFont(cf);
                    }
                }
                g2.setColor(TEXT_PRIMARY); g2.setFont(cf);
                FontMetrics fm = g2.getFontMetrics();
                String cat = categories.get(i); if (cat.length()>10) cat=cat.substring(0,9)+"…";
                g2.drawString(cat, gx+gW/2-fm.stringWidth(cat)/2, y1+14);
            }
        }

        private void drawHBars(Graphics2D g2, int x0, int y0, int x1, int y1) {
            int n = categories.size();
            if (n == 0 || seriesData.isEmpty()) return;
            double maxV = 0;
            for (List<Double> s : seriesData) for (double v : s) if (v > maxV) maxV = v;
            if (maxV == 0) maxV = 1;

            float ep = ease(progress);
            g2.setStroke(new BasicStroke(.5f));
            for (int i = 0; i <= 5; i++) {
                int gx = x0+(int)((x1-x0)*i/5.0);
                g2.setColor(new Color(229,231,235)); g2.drawLine(gx, y0, gx, y1);
                g2.setColor(TEXT_SECONDARY); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String ls = String.format("%.0f", maxV*i/5.0);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ls, gx-fm.stringWidth(ls)/2, y1+14);
            }
            g2.setStroke(new BasicStroke(1.5f)); g2.setColor(new Color(200,206,214));
            g2.drawLine(x0, y0, x0, y1); g2.drawLine(x0, y1, x1, y1);

            int rH = (y1-y0)/n, pad2 = Math.max(3, rH/6);
            int bH = Math.max(rH-2*pad2, 6);
            Color bc = colors.isEmpty() ? ACCENT_BLUE : colors.get(0);
            Font cf = new Font("Segoe UI", Font.PLAIN, 10);

            for (int i = 0; i < n; i++) {
                double val = seriesData.get(0).size()>i ? seriesData.get(0).get(i) : 0;
                int by = y0+i*rH+pad2;
                int fW = (int)((x1-x0)*val/maxV), aW = (int)(fW*ep);
                g2.setPaint(new GradientPaint(x0, by, bc.brighter(), x0+Math.max(aW,1), by, bc));
                g2.fillRoundRect(x0, by, Math.max(aW,0), bH, 5, 5);
                g2.setColor(bc.darker()); g2.setStroke(new BasicStroke(.7f));
                if (aW > 0) g2.drawRoundRect(x0, by, aW, bH, 5, 5);
                g2.setColor(TEXT_PRIMARY); g2.setFont(cf);
                FontMetrics fm = g2.getFontMetrics();
                String cat = categories.get(i); if (cat.length()>13) cat=cat.substring(0,12)+"…";
                g2.drawString(cat, x0-fm.stringWidth(cat)-6, by+bH/2+4);
                if (progress >= 1f && aW > 24) {
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String vs = String.format("%.1f", val);
                    fm = g2.getFontMetrics();
                    g2.drawString(vs, x0+aW-fm.stringWidth(vs)-5, by+bH/2+4);
                }
            }
        }

        private void drawLegend(Graphics2D g2, int x0, int y1) {
            if (seriesNames == null || seriesNames.isEmpty()) return;
            int lx = x0, ly = y1+30;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < seriesNames.size(); i++) {
                Color c = colors.get(i % colors.size());
                g2.setColor(c); g2.fillRoundRect(lx, ly-9, 12, 12, 3, 3);
                g2.setColor(TEXT_PRIMARY); g2.drawString(seriesNames.get(i), lx+16, ly);
                lx += g2.getFontMetrics().stringWidth(seriesNames.get(i)) + 32;
                if (lx > getWidth()-50) { lx = x0; ly += 16; }
            }
        }

        private void drawEmpty(Graphics2D g2) {
            g2.setColor(TEXT_LIGHT); g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            String msg = "Chưa có dữ liệu"; FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth()-fm.stringWidth(msg))/2, getHeight()/2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  NATIVE PIE CHART — FIX label vị trí chính xác
    //
    //  Arc2D coordinate system:
    //    • 0°   = 3 giờ (phải)
    //    • 90°  = 12 giờ (trên) — ta dùng startAngle = 90
    //    • Sweep dương = ngược chiều kim đồng hồ (toán học)
    //
    //  Để tính tọa độ pixel từ góc Arc2D (arcAngle degrees):
    //    px = cx + r * cos(toRadians(arcAngle))
    //    py = cy - r * sin(toRadians(arcAngle))   ← trừ vì trục Y màn hình ngược
    // ════════════════════════════════════════════════════════════════
    static class NativePieChartPanel extends JPanel {
        List<String> labels       = new ArrayList<>();
        List<Double> values       = new ArrayList<>();
        List<Color>  colors       = new ArrayList<>();
        boolean      empty        = true;
        float        progress     = 0f;
        int          hoveredSlice = -1;
        private Timer animTimer;
        private static final int DUR = 800, FPS = 60;

        NativePieChartPanel() {
            setOpaque(false);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) { updateHover(e.getX(), e.getY()); }
            });
        }

        void setData(List<String> lbs, List<Double> vals, List<Color> cols) {
            labels = lbs  != null ? lbs  : new ArrayList<>();
            values = vals != null ? vals : new ArrayList<>();
            colors = cols != null ? cols : new ArrayList<>();
            empty  = labels.isEmpty();
            startAnim();
        }

        void setEmpty() {
            empty = true; labels = new ArrayList<>(); values = new ArrayList<>();
            if (animTimer != null) animTimer.stop(); repaint();
        }

        void startAnim() {
            if (animTimer != null) animTimer.stop();
            progress = 0f;
            float delta = 1f / (DUR / (1000 / FPS));
            animTimer = new Timer(1000 / FPS, e -> {
                progress = Math.min(1f, progress + delta); repaint();
                if (progress >= 1f) ((Timer)e.getSource()).stop();
            });
            animTimer.start();
        }

        // ease-out back
        private static float ease(float t) {
            float c1 = 1.70158f, c3 = c1 + 1f;
            return 1f + c3*(float)Math.pow(t-1,3) + c1*(float)Math.pow(t-1,2);
        }

        // Chuyển góc Arc2D (degrees) → tọa độ pixel trên màn hình
        // px = cx + r * cos(rad)
        // py = cy - r * sin(rad)   ← Y ngược
        private static int arcToScreenX(double cx, double r, double arcDeg) {
            return (int)(cx + r * Math.cos(Math.toRadians(arcDeg)));
        }
        private static int arcToScreenY(double cy, double r, double arcDeg) {
            return (int)(cy - r * Math.sin(Math.toRadians(arcDeg)));
        }

        private void updateHover(int mx, int my) {
            if (empty) return;
            int legH = 50, cx = getWidth()/2;
            int pieH = getHeight()-legH, cy = pieH/2+8;
            int r = Math.min(getWidth(), pieH)/2 - 14;
            if (Math.hypot(mx-cx, my-cy) > r) { hoveredSlice = -1; repaint(); return; }

            // Chuyển mouse → góc Arc2D
            // Trong màn hình: dy = my - cy (dương = xuống)
            // Arc2D: sin dương = lên → phải đảo dấu Y
            double arcAng = Math.toDegrees(Math.atan2(-(my-cy), mx-cx));
            if (arcAng < 0) arcAng += 360;

            double total = values.stream().mapToDouble(Double::doubleValue).sum();
            double cur = 90.0; // startAngle
            for (int i = 0; i < values.size(); i++) {
                double sw = values.get(i)/total*360;
                double end = cur + sw;
                // Chuẩn hoá về [0,360)
                double s = ((cur % 360) + 360) % 360;
                double e2 = ((end % 360) + 360) % 360;
                boolean inSlice = (s < e2) ? (arcAng >= s && arcAng < e2)
                                           : (arcAng >= s || arcAng < e2);
                if (inSlice) { hoveredSlice = i; repaint(); return; }
                cur += sw;
            }
            hoveredSlice = -1; repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getWidth() < 60 || getHeight() < 60) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (empty || values.isEmpty()) { drawEmpty(g2); g2.dispose(); return; }

            int legH = 50, cx = getWidth()/2;
            int pieH = getHeight()-legH, cy = pieH/2+8;
            int r = Math.min(getWidth(), pieH)/2 - 14;
            if (r < 10) { g2.dispose(); return; }

            double total = values.stream().mapToDouble(Double::doubleValue).sum();
            if (total == 0) { drawEmpty(g2); g2.dispose(); return; }

            float  ep         = Math.min(1f, ease(progress));
            double sweepTotal = 360.0 * ep;
            double startAng   = 90.0;  // 12 giờ

            for (int i = 0; i < values.size(); i++) {
                double frac  = values.get(i)/total;
                double sweep = frac * sweepTotal;
                if (sweep <= 0) continue;

                Color base = colors.get(i % colors.size());
                boolean hov = (i == hoveredSlice && progress >= 1f);

                // Góc giữa slice (Arc2D degrees)
                double midArc = startAng + sweep / 2.0;

                // Offset hover: đẩy ra ngoài theo hướng giữa slice
                int dx = 0, dy = 0;
                if (hov) {
                    dx = arcToScreenX(0, 9, midArc);
                    dy = arcToScreenY(0, 9, midArc);
                }

                // Vẽ slice
                g2.setPaint(new GradientPaint(cx+dx, cy+dy-r, base.brighter(), cx+dx, cy+dy+r, base.darker()));
                Arc2D arc = new Arc2D.Double(cx-r+dx, cy-r+dy, r*2, r*2, startAng, sweep, Arc2D.PIE);
                g2.fill(arc);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(arc);

                // ── Label % — vị trí CHÍNH XÁC tại trung tâm slice ──
                if (frac > 0.05 && progress >= 1f) {
                    // Tâm slice ở 62% bán kính
                    int lx = arcToScreenX(cx+dx, r*0.62, midArc);
                    int ly = arcToScreenY(cy+dy, r*0.62, midArc);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String pct = String.format("%.1f%%", frac*100);
                    FontMetrics fm = g2.getFontMetrics();
                    // Căn giữa text tại (lx, ly)
                    g2.drawString(pct, lx - fm.stringWidth(pct)/2, ly + fm.getAscent()/2 - 1);
                }
                startAng += sweep;
            }

            // Legend
            int lx = 10, ly = pieH + 18;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < labels.size(); i++) {
                Color c = colors.get(i % colors.size());
                g2.setColor(c); g2.fillRoundRect(lx, ly-9, 13, 13, 4, 4);
                g2.setColor(c.darker()); g2.drawRoundRect(lx, ly-9, 13, 13, 4, 4);
                g2.setColor(TEXT_PRIMARY); g2.drawString(labels.get(i), lx+18, ly);
                lx += g2.getFontMetrics().stringWidth(labels.get(i)) + 38;
                if (lx > getWidth()-60) { lx = 10; ly += 20; }
            }
            g2.dispose();
        }

        private void drawEmpty(Graphics2D g2) {
            g2.setColor(TEXT_LIGHT); g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            String msg = "Chưa có dữ liệu"; FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth()-fm.stringWidth(msg))/2, getHeight()/2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  NATIVE LINE CHART
    // ════════════════════════════════════════════════════════════════
    static class NativeLineChartPanel extends JPanel {
        List<Integer> xData     = new ArrayList<>();
        List<Double>  yData     = new ArrayList<>();
        Color         lineColor = ACCENT_TEAL;
        boolean       empty     = true;
        float         progress  = 0f;
        private Timer animTimer;
        private static final int DUR = 800, FPS = 60;

        NativeLineChartPanel() { setOpaque(false); }

        void setData(List<Integer> xs, List<Double> ys, Color col) {
            xData = xs != null ? xs : new ArrayList<>();
            yData = ys != null ? ys : new ArrayList<>();
            lineColor = col != null ? col : ACCENT_TEAL;
            empty = xData.isEmpty();
            startAnim();
        }

        void setEmpty() {
            empty = true; xData = new ArrayList<>(); yData = new ArrayList<>();
            if (animTimer != null) animTimer.stop(); repaint();
        }

        void startAnim() {
            if (animTimer != null) animTimer.stop();
            progress = 0f;
            float delta = 1f / (DUR / (1000 / FPS));
            animTimer = new Timer(1000 / FPS, e -> {
                progress = Math.min(1f, progress + delta); repaint();
                if (progress >= 1f) ((Timer)e.getSource()).stop();
            });
            animTimer.start();
        }

        private static float ease(float t) { return 1f-(1f-t)*(1f-t)*(1f-t); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getWidth() < 60 || getHeight() < 60) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (empty || xData.isEmpty()) { drawEmpty(g2); g2.dispose(); return; }

            int axL=46, padR=10, padT=14, padB=36;
            int x0=axL, y0=padT, x1=getWidth()-padR, y1=getHeight()-padB;
            if (x1-x0<30 || y1-y0<30) { g2.dispose(); return; }

            double maxY = yData.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            double minX = xData.stream().mapToInt(Integer::intValue).min().orElse(0);
            double maxX = xData.stream().mapToInt(Integer::intValue).max().orElse(23);
            if (maxY == 0) maxY = 1;
            if (maxX <= minX) maxX = minX+1;

            g2.setStroke(new BasicStroke(.5f));
            for (int i = 0; i <= 4; i++) {
                int gy = y1-(int)((y1-y0)*i/4.0);
                g2.setColor(new Color(229,231,235)); g2.drawLine(x0, gy, x1, gy);
                g2.setColor(TEXT_SECONDARY); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.format("%.0f", maxY*i/4.0), 2, gy+4);
            }
            g2.setStroke(new BasicStroke(1.5f)); g2.setColor(new Color(200,206,214));
            g2.drawLine(x0, y0, x0, y1); g2.drawLine(x0, y1, x1, y1);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g2.setColor(TEXT_SECONDARY);
            for (int xv : xData) {
                int px = x0+(int)((xv-minX)/(maxX-minX)*(x1-x0));
                if (xv % 3 == 0) g2.drawString(xv+"h", px-7, y1+12);
            }

            float ep = ease(progress);
            int vis = Math.min(xData.size(), Math.max(1, (int)(xData.size()*ep)));

            int[] px = new int[vis], py = new int[vis];
            for (int i = 0; i < vis; i++) {
                px[i] = x0+(int)((xData.get(i)-minX)/(maxX-minX)*(x1-x0));
                py[i] = y1-(int)(yData.get(i)/maxY*(y1-y0));
            }

            if (vis > 1) {
                GeneralPath area = new GeneralPath();
                area.moveTo(px[0], y1);
                for (int i = 0; i < vis; i++) area.lineTo(px[i], py[i]);
                area.lineTo(px[vis-1], y1); area.closePath();
                g2.setPaint(new GradientPaint(0, y0,
                    new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 55),
                    0, y1,
                    new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 5)));
                g2.fill(area);
                g2.setColor(lineColor);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < vis-1; i++) g2.drawLine(px[i], py[i], px[i+1], py[i+1]);
            }
            for (int i = 0; i < vis; i++) {
                g2.setColor(CARD_BG); g2.fillOval(px[i]-4, py[i]-4, 8, 8);
                g2.setColor(lineColor); g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px[i]-4, py[i]-4, 8, 8);
            }
            g2.dispose();
        }

        private void drawEmpty(Graphics2D g2) {
            g2.setColor(TEXT_LIGHT); g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            String msg = "Chưa có dữ liệu"; FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth()-fm.stringWidth(msg))/2, getHeight()/2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════
    public ThongKeTongQuanForm() {
        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(PRIMARY_BG);
        add(buildHeader(), BorderLayout.NORTH);

        mainScrollPane = new JScrollPane(buildMainContent());
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setBorder(null);
        mainScrollPane.setBackground(PRIMARY_BG);
        mainScrollPane.getViewport().setBackground(PRIMARY_BG);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // ── SCROLL-TRIGGERED ANIMATION ──
        // Mỗi khi viewport thay đổi vị trí (scroll), kiểm tra card nào đã vào tầm nhìn
        mainScrollPane.getViewport().addChangeListener(e -> checkAllCardsVisibility());

        add(mainScrollPane, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // Lần đầu load: check sau khi layout hoàn tất
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.invokeLater(this::checkAllCardsVisibility);
        });
    }

    /** Kiểm tra tất cả cards — trigger animation nếu đã vào viewport */
    private void checkAllCardsVisibility() {
        JViewport vp = mainScrollPane.getViewport();
        for (SlideCard c : slideCards)      c.checkVisibility(vp);
        for (SlideCard c : slideChartCards) c.checkVisibility(vp);
    }

    // ─── HEADER ───
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(15,0));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,BORDER_LIGHT),
            BorderFactory.createEmptyBorder(20,30,20,30)));

        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setOpaque(false);
        JLabel title = new JLabel("Thống kê tổng quan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(TEXT_PRIMARY); title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Báo cáo kinh doanh hôm nay  •  " +
            LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12)); sub.setForeground(TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(4,0,0,0)); sub.setAlignmentX(LEFT_ALIGNMENT);
        left.add(title); left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0)); right.setOpaque(false);
        JButton btn = makeBtn("Làm mới", ACCENT_BLUE);
        btn.addActionListener(e -> {
            // Reset tất cả cards rồi re-trigger khi scroll đến
            for (SlideCard c : slideCards)      c.reset();
            for (SlideCard c : slideChartCards) c.reset();
            loadDashboardData();
            SwingUtilities.invokeLater(this::checkAllCardsVisibility);
        });
        right.add(btn);
        p.add(left, BorderLayout.WEST); p.add(right, BorderLayout.EAST);
        return p;
    }

    // ─── FOOTER ───
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,BORDER_LIGHT));
        p.setPreferredSize(new Dimension(0,44));
        JLabel l = new JLabel("© 2026 Hệ thống quán lẩu - TiTi");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l.setForeground(TEXT_LIGHT);
        p.add(l); return p;
    }

    // ─── MAIN CONTENT ───
    private JPanel buildMainContent() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PRIMARY_BG); p.setBorder(BorderFactory.createEmptyBorder(22,22,22,22));

        p.add(section("Hiệu suất hôm nay",              buildRow1()));          p.add(vgap(18));
        p.add(section("Chi tiết hoạt động",              buildRow2()));          p.add(vgap(18));
        p.add(section("Quản lý bàn",                     buildRow3()));          p.add(vgap(22));
        p.add(section("Thống kê món bán hôm nay",        buildMonBanCards()));   p.add(vgap(18));
        p.add(section("Phân tích món bán hôm nay",       buildMonBanCharts()));  p.add(vgap(18));
        p.add(section("Chi tiết từng món hôm nay",       buildTablePanel()));    p.add(vgap(22));
        p.add(section("Phân tích doanh thu & hoạt động", buildAnalysisCharts()));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel section(String title, JPanel content) {
        JPanel p = new JPanel(new BorderLayout(0,12)); p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); lbl.setForeground(TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH); p.add(content, BorderLayout.CENTER); return p;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    // ─── METRIC ROWS ───
    private JPanel buildRow1() {
        JPanel p = gridRow();
        SlideCard c1 = metricCard("Doanh thu hôm nay","0đ",  ACCENT_BLUE);    lblDoanhThuHomNay  = getValueLbl(c1);
        SlideCard c2 = metricCard("Doanh thu hôm qua","0đ",  TEXT_SECONDARY); lblDoanhThuHomQua  = getValueLbl(c2);
        SlideCard c3 = metricCard("Tăng / Giảm","0%",         ACCENT_GREEN);  lblTangGiam        = getValueLbl(c3);
        SlideCard c4 = metricCard("Hóa đơn hôm nay","0",     ACCENT_ORANGE); lblSoHoaDonHomNay  = getValueLbl(c4);
        slideCards.add(c1); slideCards.add(c2); slideCards.add(c3); slideCards.add(c4);
        p.add(c1); p.add(c2); p.add(c3); p.add(c4); return p;
    }

    private JPanel buildRow2() {
        JPanel p = gridRow();
        SlideCard c1 = metricCard("Hóa đơn đang mở","0",    ACCENT_ORANGE); lblSoHoaDonDangMo  = getValueLbl(c1);
        SlideCard c2 = metricCard("Giá trị trung bình","0đ", ACCENT_PURPLE); lblGiaTriTrungBinh = getValueLbl(c2);
        SlideCard c3 = metricCard("Bàn trống","0",            ACCENT_GREEN);  lblTrong           = getValueLbl(c3);
        SlideCard c4 = metricCard("Bàn đang dùng","0",        ACCENT_RED);   lblDangSuDung      = getValueLbl(c4);
        slideCards.add(c1); slideCards.add(c2); slideCards.add(c3); slideCards.add(c4);
        p.add(c1); p.add(c2); p.add(c3); p.add(c4); return p;
    }

    private JPanel buildRow3() {
        JPanel p = gridRow();
        SlideCard c1 = metricCard("Tổng số bàn","0",  ACCENT_BLUE);   lblTongBan = getValueLbl(c1);
        SlideCard c2 = metricCard("Bàn đã đặt","0",   ACCENT_ORANGE); lblDaDat   = getValueLbl(c2);
        slideCards.add(c1); slideCards.add(c2);
        p.add(c1); p.add(c2); p.add(emptySlot()); p.add(emptySlot()); return p;
    }

    private JPanel buildMonBanCards() {
        JPanel p = gridRow();
        SlideCard c1 = metricCard("Tổng món bán ra","0",    ACCENT_TEAL);   lblTongMonHomNay  = getValueLbl(c1);
        SlideCard c2 = metricCard("Loại món bán được","0",  ACCENT_PURPLE); lblSoLoaiMon       = getValueLbl(c2);
        SlideCard c3 = metricCard("Món bán chạy nhất","---",ACCENT_ORANGE); lblMonBanChayNhat = getValueLbl(c3);
        lblMonBanChayNhat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        slideCards.add(c1); slideCards.add(c2); slideCards.add(c3);
        p.add(c1); p.add(c2); p.add(c3); p.add(emptySlot()); return p;
    }

    // ─── CHART SECTIONS ───
    private JPanel buildMonBanCharts() {
        JPanel p = new JPanel(new GridLayout(1,2,14,0)); p.setOpaque(false);
        p.setPreferredSize(new Dimension(0,290)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,290));
        chartMonTheoNhom = new NativePieChartPanel();
        chartMonTheoGio  = new NativeLineChartPanel();
        SlideCard cc1 = chartCard("Số lượng bán theo nhóm món",    chartMonTheoNhom);
        SlideCard cc2 = chartCard("Xu hướng bán theo giờ hôm nay", chartMonTheoGio);
        slideChartCards.add(cc1); slideChartCards.add(cc2);
        p.add(cc1); p.add(cc2); return p;
    }

    private JPanel buildAnalysisCharts() {
        JPanel p = new JPanel(new GridLayout(2,2,14,14)); p.setOpaque(false);
        p.setPreferredSize(new Dimension(0,600)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,600));
        chartDoanhThu     = new NativeBarChartPanel();
        chartTopMon       = new NativeBarChartPanel();
        chartTinhTrangBan = new NativeBarChartPanel();
        chartCongSuat     = new NativePieChartPanel();
        SlideCard cc1 = chartCard("Doanh thu tháng này (triệu đồng)", chartDoanhThu);
        SlideCard cc2 = chartCard("Top món bán chạy hôm nay",         chartTopMon);
        SlideCard cc3 = chartCard("Tình trạng bàn theo khu vực",      chartTinhTrangBan);
        SlideCard cc4 = chartCard("Công suất khu vực (%)",             chartCongSuat);
        slideChartCards.add(cc1); slideChartCards.add(cc2); slideChartCards.add(cc3); slideChartCards.add(cc4);
        p.add(cc1); p.add(cc2); p.add(cc3); p.add(cc4); return p;
    }

    private JPanel buildTablePanel() {
        panelBangChiTietMon = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SHADOW_COLOR); g2.fillRoundRect(0,2,getWidth(),getHeight()-2,12,12);
                g2.setColor(CARD_BG);      g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(BORDER_LIGHT); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            }
        };
        panelBangChiTietMon.setOpaque(false);
        panelBangChiTietMon.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        panelBangChiTietMon.setPreferredSize(new Dimension(0,340));
        panelBangChiTietMon.setMaximumSize(new Dimension(Integer.MAX_VALUE,340));
        panelBangChiTietMon.setAlignmentX(LEFT_ALIGNMENT);
        showTableShimmer();
        return panelBangChiTietMon;
    }

    // ─── CARD BUILDERS ───
    private SlideCard metricCard(String title, String value, Color accent) {
        SlideCard card = new SlideCard(new BorderLayout(0,10)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SHADOW_COLOR); g2.fillRoundRect(0,2,getWidth(),getHeight()-2,12,12);
                g2.setColor(CARD_BG);      g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(BORDER_LIGHT); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setColor(accent);       g2.setStroke(new BasicStroke(3f));
                g2.drawLine(12, 0, getWidth()-12, 0);
            }
        };
        card.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lTitle.setForeground(TEXT_SECONDARY);
        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Segoe UI", Font.BOLD, 26)); lValue.setForeground(TEXT_PRIMARY);
        card.add(lTitle, BorderLayout.NORTH); card.add(lValue, BorderLayout.CENTER);
        return card;
    }

    private SlideCard chartCard(String title, JPanel content) {
        SlideCard card = new SlideCard(new BorderLayout(0,8)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SHADOW_COLOR); g2.fillRoundRect(0,2,getWidth(),getHeight()-2,12,12);
                g2.setColor(CARD_BG);      g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(BORDER_LIGHT); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            }
        };
        card.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 12)); lTitle.setForeground(TEXT_PRIMARY);
        card.add(lTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JLabel getValueLbl(SlideCard card) {
        for (Component c : card.getComponents())
            if (c instanceof JLabel && ((JLabel)c).getFont().getSize() >= 14) return (JLabel)c;
        return new JLabel();
    }

    private JPanel gridRow() { JPanel p = new JPanel(new GridLayout(1,4,14,0)); p.setOpaque(false); return p; }
    private JPanel emptySlot() { JPanel p = new JPanel(); p.setOpaque(false); return p; }

    private void showTableShimmer() {
        panelBangChiTietMon.removeAll();
        panelBangChiTietMon.setLayout(new BoxLayout(panelBangChiTietMon, BoxLayout.Y_AXIS));
        panelBangChiTietMon.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        ShimmerPanel hdr = new ShimmerPanel(6);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); hdr.setPreferredSize(new Dimension(0,36));
        panelBangChiTietMon.add(hdr); panelBangChiTietMon.add(Box.createVerticalStrut(7));
        for (int i = 0; i < 5; i++) {
            ShimmerPanel row = new ShimmerPanel(6);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE,32)); row.setPreferredSize(new Dimension(0,32));
            panelBangChiTietMon.add(row);
            if (i < 4) panelBangChiTietMon.add(Box.createVerticalStrut(5));
        }
        panelBangChiTietMon.revalidate(); panelBangChiTietMon.repaint();
    }

    // ════════════════════════════════════════════════════════════════
    //  LOAD DATA
    // ════════════════════════════════════════════════════════════════
    private void loadDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Object> data = ThongKeDAO.getDashboardData();
                BigDecimal dtHomNay = (BigDecimal) data.get("doanhThuHomNay");
                BigDecimal dtHomQua = (BigDecimal) data.get("doanhThuHomQua");
                BigDecimal tg       = (BigDecimal) data.get("tangGiamDoanhThu");

                animateLbl(lblDoanhThuHomNay, dtHomNay.doubleValue(), true);
                animateLbl(lblDoanhThuHomQua, dtHomQua.doubleValue(), true);

                double pct = tg.doubleValue();
                if      (pct > 0) { lblTangGiam.setText("↑ "+String.format("%.1f%%",pct));           lblTangGiam.setForeground(ACCENT_GREEN); }
                else if (pct < 0) { lblTangGiam.setText("↓ "+String.format("%.1f%%",Math.abs(pct))); lblTangGiam.setForeground(ACCENT_RED); }
                else              { lblTangGiam.setText("→ 0%");                                      lblTangGiam.setForeground(TEXT_SECONDARY); }

                animateLbl(lblSoHoaDonHomNay,  ((Number)data.get("soHoaDonHomNay")).doubleValue(),      false);
                animateLbl(lblSoHoaDonDangMo,  ((Number)data.get("soHoaDonDangMo")).doubleValue(),      false);
                animateLbl(lblGiaTriTrungBinh, ((BigDecimal)data.get("giaTriTrungBinh")).doubleValue(), true);

                @SuppressWarnings("unchecked")
                Map<String,Integer> tb = (Map<String,Integer>) data.get("thongKeBan");
                animateLbl(lblTongBan,    tb.get("TongBan"),    false);
                animateLbl(lblDangSuDung, tb.get("DangSuDung"), false);
                animateLbl(lblTrong,      tb.get("Trong"),      false);
                animateLbl(lblDaDat,      tb.get("DaDat"),      false);

                loadMonBanHomNay();
                loadDoanhThuChart();
                loadTopMonChart();
                loadBanKhuVucChart();
                loadCongSuatChart();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: "+ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }

    private void loadMonBanHomNay() {
        List<Object[]> chiTiet = ThongKeDAO.getChiTietMonBanHomNay();
        int tongMon=0, soLoai=0, maxSL=0; String monChay="---";
        for (Object[] row : chiTiet) {
            int sl=(int)row[2]; tongMon+=sl;
            if (sl > 0) soLoai++;
            if (sl > maxSL) { maxSL=sl; monChay=(String)row[0]; }
        }
        animateLbl(lblTongMonHomNay, tongMon, false);
        animateLbl(lblSoLoaiMon,     soLoai,  false);
        if (maxSL > 0) {
            String t = monChay.length()>14 ? monChay.substring(0,13)+"…" : monChay;
            lblMonBanChayNhat.setText(t+" ("+maxSL+")");
        } else lblMonBanChayNhat.setText("Chưa có");

        loadMonTheoNhomChart();
        loadMonTheoGioChart();
        loadBangChiTiet(chiTiet);
    }

    private void loadMonTheoNhomChart() {
        try {
            List<Object[]> data = ThongKeDAO.getMonBanTheoNhomHomNay();
            if (data==null||data.isEmpty()||data.stream().allMatch(r->(int)r[1]==0)) { chartMonTheoNhom.setEmpty(); return; }
            List<String> lbs=new ArrayList<>(); List<Double> vals=new ArrayList<>(); List<Color> cols=new ArrayList<>();
            Color[] pal={ACCENT_BLUE,ACCENT_ORANGE,ACCENT_GREEN,ACCENT_PURPLE,ACCENT_TEAL}; int ci=0;
            for (Object[] row : data) { int sl=(int)row[1]; if (sl>0) { lbs.add((String)row[0]); vals.add((double)sl); cols.add(pal[ci++%pal.length]); } }
            if (lbs.isEmpty()) { chartMonTheoNhom.setEmpty(); return; }
            chartMonTheoNhom.setData(lbs, vals, cols);
        } catch (Exception e) { chartMonTheoNhom.setEmpty(); e.printStackTrace(); }
    }

    private void loadMonTheoGioChart() {
        try {
            List<Object[]> data = ThongKeDAO.getMonBanTheoGioHomNay();
            if (data==null||data.isEmpty()) { chartMonTheoGio.setEmpty(); return; }
            List<Integer> xs=new ArrayList<>(); List<Double> ys=new ArrayList<>();
            for (Object[] row : data) { xs.add((int)row[0]); ys.add(((Number)row[1]).doubleValue()); }
            chartMonTheoGio.setData(xs, ys, ACCENT_TEAL);
        } catch (Exception e) { chartMonTheoGio.setEmpty(); e.printStackTrace(); }
    }

    private void loadDoanhThuChart() {
        try {
            LocalDate today = LocalDate.now();
            List<Object[]> data = ThongKeDAO.getDoanhThuTheoNgayTrongThang(today.getMonthValue(), today.getYear());
            if (data==null||data.isEmpty()) { chartDoanhThu.setEmpty(); return; }
            List<String> cats=new ArrayList<>(); List<Double> vals=new ArrayList<>();
            for (Object[] row : data) { cats.add(String.valueOf(row[0])); vals.add(((BigDecimal)row[1]).doubleValue()/1_000_000.0); }
            chartDoanhThu.setData(cats, Arrays.asList(vals), Arrays.asList("Doanh thu (tr.đ)"), Arrays.asList(ACCENT_BLUE), false);
        } catch (Exception e) { chartDoanhThu.setEmpty(); e.printStackTrace(); }
    }

    private void loadTopMonChart() {
        try {
            LocalDate today = LocalDate.now();
            List<Object[]> data = ThongKeDAO.getTopMonBanChay(8, today, today);
            if (data==null||data.isEmpty()) { chartTopMon.setEmpty(); return; }
            List<String> cats=new ArrayList<>(); List<Double> vals=new ArrayList<>();
            for (Object[] row : data) { cats.add((String)row[0]); vals.add(((Number)row[1]).doubleValue()); }
            Collections.reverse(cats); Collections.reverse(vals);
            chartTopMon.setData(cats, Arrays.asList(vals), Arrays.asList("Số lượng"), Arrays.asList(ACCENT_ORANGE), true);
        } catch (Exception e) { chartTopMon.setEmpty(); e.printStackTrace(); }
    }

    private void loadBanKhuVucChart() {
        try {
            List<Object[]> data = ThongKeDAO.getThongKeBanTheoKhuVuc();
            if (data==null||data.isEmpty()) { chartTinhTrangBan.setEmpty(); return; }
            List<String> cats=new ArrayList<>(); List<Double> dangDung=new ArrayList<>(), trong=new ArrayList<>();
            for (Object[] row : data) { cats.add((String)row[0]); dangDung.add((double)(int)row[2]); trong.add((double)(int)row[3]); }
            chartTinhTrangBan.setData(cats, Arrays.asList(dangDung,trong), Arrays.asList("Đang dùng","Trống"), Arrays.asList(ACCENT_RED,ACCENT_GREEN), false);
        } catch (Exception e) { chartTinhTrangBan.setEmpty(); e.printStackTrace(); }
    }

    private void loadCongSuatChart() {
        try {
            List<Object[]> data = ThongKeDAO.getThongKeBanTheoKhuVuc();
            if (data==null||data.isEmpty()) { chartCongSuat.setEmpty(); return; }
            List<String> lbs=new ArrayList<>(); List<Double> vals=new ArrayList<>(); List<Color> cols=new ArrayList<>();
            Color[] pal={ACCENT_BLUE,ACCENT_ORANGE,ACCENT_GREEN,ACCENT_PURPLE,ACCENT_TEAL,ACCENT_RED}; int ci=0;
            for (Object[] row : data) { lbs.add((String)row[0]); vals.add((double)row[4]); cols.add(pal[ci++%pal.length]); }
            chartCongSuat.setData(lbs, vals, cols);
        } catch (Exception e) { chartCongSuat.setEmpty(); e.printStackTrace(); }
    }

    private void loadBangChiTiet(List<Object[]> chiTiet) {
        stopShimmers(panelBangChiTietMon);
        String[] cols={"Tên món","Nhóm","Đã bán","Doanh thu","Tỷ lệ %","Còn lại","Đơn giá"};
        DefaultTableModel model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        for (Object[] row : chiTiet)
            model.addRow(new Object[]{ row[0], row[1], row[2],
                currencyFormat.format(row[3]), String.format("%.1f%%",row[4]), row[5], currencyFormat.format(row[6]) });

        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r,int row,int col){
                Component c=super.prepareRenderer(r,row,col);
                if (isRowSelected(row)) { c.setBackground(ROW_HOVER); c.setForeground(TEXT_PRIMARY); }
                else {
                    c.setBackground(row%2==0?CARD_BG:ROW_EVEN); c.setForeground(TEXT_PRIMARY);
                    if (col==5){ int cl=(int)getValueAt(row,col); c.setForeground(cl<=10?ACCENT_RED:cl<=30?ACCENT_ORANGE:ACCENT_GREEN); }
                    if (col==2){ int sl=(int)getValueAt(row,col); c.setForeground(sl>0?ACCENT_BLUE:TEXT_LIGHT); }
                }
                return c;
            }
        };
        table.setFont(new Font("Segoe UI",Font.PLAIN,12)); table.setRowHeight(33);
        table.setShowGrid(false); table.setBackground(CARD_BG);
        table.setIntercellSpacing(new Dimension(0,0)); table.setFocusable(false);
        table.setSelectionBackground(ROW_HOVER);
        DefaultTableCellRenderer cr=new DefaultTableCellRenderer(); cr.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rr=new DefaultTableCellRenderer(); rr.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(cr);
        table.getColumnModel().getColumn(3).setCellRenderer(rr);
        table.getColumnModel().getColumn(4).setCellRenderer(cr);
        table.getColumnModel().getColumn(5).setCellRenderer(cr);
        table.getColumnModel().getColumn(6).setCellRenderer(rr);
        int[] ws={155,108,90,130,72,68,118};
        for (int i=0;i<ws.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        JTableHeader hdr=table.getTableHeader();
        hdr.setFont(new Font("Segoe UI",Font.BOLD,12));
        hdr.setBackground(new Color(243,244,246)); hdr.setForeground(TEXT_SECONDARY);
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_LIGHT));
        hdr.setPreferredSize(new Dimension(0,35)); hdr.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)hdr.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(1,1,1,1,BORDER_LIGHT));
        sp.getViewport().setBackground(CARD_BG);

        JPanel fadeWrap = new JPanel(new BorderLayout()) {
            float a=0f; Timer ft;
            { ft=new Timer(16,e->{ a=Math.min(1f,a+0.045f); repaint(); if(a>=1f)((Timer)e.getSource()).stop();}); ft.start(); }
            @Override protected void paintChildren(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
                super.paintChildren(g2); g2.dispose();
            }
        };
        fadeWrap.setOpaque(false); fadeWrap.add(sp, BorderLayout.CENTER);

        JPanel leg=new JPanel(new FlowLayout(FlowLayout.LEFT,14,0)); leg.setOpaque(false);
        leg.add(legLbl("● Còn nhiều (>30)",ACCENT_GREEN)); leg.add(legLbl("● Sắp hết (≤30)",ACCENT_ORANGE));
        leg.add(legLbl("● Rất ít (≤10)",ACCENT_RED));      leg.add(legLbl("● Đã bán hôm nay",ACCENT_BLUE));

        panelBangChiTietMon.removeAll();
        panelBangChiTietMon.setLayout(new BorderLayout(0,8));
        panelBangChiTietMon.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        panelBangChiTietMon.add(leg, BorderLayout.NORTH);
        panelBangChiTietMon.add(fadeWrap, BorderLayout.CENTER);
        panelBangChiTietMon.revalidate(); panelBangChiTietMon.repaint();
    }

    private JLabel legLbl(String t, Color c) {
        JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,11)); l.setForeground(c); return l;
    }

    private void stopShimmers(JPanel panel) {
        for (Component c : panel.getComponents()) if (c instanceof ShimmerPanel) ((ShimmerPanel)c).stopShimmer();
    }

    private void animateLbl(JLabel label, double target, boolean currency) {
        final long t0=System.currentTimeMillis(); final int dur=700;
        Timer t=new Timer(COUNTER_INTERVAL, null);
        t.addActionListener(e -> {
            float p=Math.min(1f,(float)(System.currentTimeMillis()-t0)/dur);
            float ep=1f-(float)Math.pow(2,-10*p);
            double cur=target*ep;
            label.setText(currency?currencyFormat.format(cur):String.format("%,.0f",cur));
            if (p>=1f) { ((Timer)e.getSource()).stop(); label.setText(currency?currencyFormat.format(target):String.format("%,.0f",target)); }
        });
        t.start();
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b=new JButton(text) {
            private boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited (MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?bg.darker():bg);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        b.setFocusPainted(false); b.setContentAreaFilled(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130,36)); return b;
    }
}