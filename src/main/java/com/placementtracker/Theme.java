package com.placementtracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.InputStream;

public final class Theme {
    public static final Color BG = new Color(12, 12, 14);
    public static final Color PANEL = new Color(24, 24, 28);
    public static final Color PANEL_2 = new Color(32, 32, 38);
    public static final Color TEXT = new Color(245, 245, 247);
    public static final Color MUTED = new Color(170, 170, 176);
    public static final Color BORDER = new Color(58, 58, 66);
    public static final Color ACCENT = new Color(59, 130, 246);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color WARNING = new Color(245, 158, 11);

    private static final String FALLBACK_FONT_FAMILY = "Segoe UI";
    private static final String SATOSHI_FONT_RESOURCE = "/fonts/Satoshi-Variable.ttf";
    private static final String FONT_FAMILY = loadFontFamily();

    public static final Font FONT = font(Font.PLAIN, 14f);
    public static final Font FONT_BOLD = font(Font.BOLD, 14f);
    public static final Font FONT_TITLE = font(Font.BOLD, 24f);
    public static final Font FONT_H1 = font(Font.BOLD, 28f);

    private Theme() {}

    private static String loadFontFamily() {
        try (InputStream stream = Theme.class.getResourceAsStream(SATOSHI_FONT_RESOURCE)) {
            if (stream == null) {
                return FALLBACK_FONT_FAMILY;
            }
            Font base = Font.createFont(Font.TRUETYPE_FONT, stream);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
            return base.getFamily();
        } catch (Exception ignored) {
            return FALLBACK_FONT_FAMILY;
        }
    }

    private static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, Math.round(size));
    }

    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", PANEL_2);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("PasswordField.background", PANEL_2);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("ComboBox.background", PANEL_2);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", ACCENT.darker());
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("TabbedPane.background", PANEL);
        UIManager.put("TabbedPane.foreground", Color.BLACK);
        UIManager.put("TabbedPane.selectedForeground", Color.BLACK);
        UIManager.put("TabbedPane.selected", PANEL_2);
        UIManager.put("TabbedPane.contentAreaColor", PANEL);
        UIManager.put("TabbedPane.focus", ACCENT);
        UIManager.put("TabbedPane.darkShadow", BORDER);
        UIManager.put("TabbedPane.shadow", BORDER);
        UIManager.put("TabbedPane.light", BORDER);
        UIManager.put("TabbedPane.highlight", BORDER);
        UIManager.put("Table.background", PANEL);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", PANEL_2);
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Label.font", FONT);
        UIManager.put("TextField.font", FONT);
        UIManager.put("PasswordField.font", FONT);
        UIManager.put("ComboBox.font", FONT);
    }

    public static JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_H1);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, 13f));
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel statLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(TEXT);
        return l;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PANEL_2);
        b.setForeground(TEXT);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(FONT_BOLD);
        tabs.setBackground(PANEL);
        tabs.setForeground(Color.BLACK);
        tabs.setOpaque(true);
        tabs.setFocusable(false);
    }

    public static JTextField textField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(PANEL_2);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setBackground(PANEL_2);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return f;
    }

    public static <T> void styleComboBox(JComboBox<T> combo) {
        combo.setFont(FONT);
        combo.setBackground(PANEL_2);
        combo.setForeground(TEXT);
        combo.setOpaque(false);
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new BasicArrowButton(SwingConstants.SOUTH, PANEL_2, BORDER, TEXT, BORDER);
                button.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(PANEL_2);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setFont(FONT);
                if (isSelected) {
                    label.setBackground(ACCENT.darker());
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(PANEL_2);
                    label.setForeground(TEXT);
                }
                label.setBorder(new EmptyBorder(4, 8, 4, 8));
                return label;
            }
        });
    }

    public static JScrollPane tableScroll(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(PANEL);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT.darker());
        table.setSelectionForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        styleTableHeader(header);
        return new JScrollPane(table) {{
            setBorder(BorderFactory.createLineBorder(BORDER, 1));
            setViewportBorder(BorderFactory.createEmptyBorder());
        }};
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(PANEL);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setFont(FONT);
        JTableHeader header = table.getTableHeader();
        styleTableHeader(header);
    }

    private static void styleTableHeader(JTableHeader header) {
        header.setOpaque(true);
        header.setBackground(PANEL_2);
        header.setForeground(TEXT);
        header.setFont(FONT_BOLD);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(PANEL_2);
                label.setForeground(TEXT);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));
                label.setFont(FONT_BOLD);
                return label;
            }
        });
    }

    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(font(Font.PLAIN, 13f));
        s.setForeground(MUTED);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(s);
        return p;
    }
}
