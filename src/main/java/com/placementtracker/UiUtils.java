package com.placementtracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public final class UiUtils {
    private UiUtils() {}

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String prompt(Component parent, String title, String message, String initial) {
        JTextField input = Theme.textField(24);
        input.setText(initial == null ? "" : initial);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PANEL);
        panel.setBorder(new EmptyBorder(10, 10, 4, 10));

        JLabel label = new JLabel(message);
        label.setForeground(Theme.TEXT);
        label.setFont(Theme.FONT_BOLD);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(parent, panel, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return null;
        return input.getText();
        }

        public static String promptChoice(Component parent, String title, String message, String[] options, String initial) {
        JComboBox<String> combo = new JComboBox<>(options);
        Theme.styleComboBox(combo);
        if (initial != null) combo.setSelectedItem(initial);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PANEL);
        panel.setBorder(new EmptyBorder(10, 10, 4, 10));

        JLabel label = new JLabel(message);
        label.setForeground(Theme.TEXT);
        label.setFont(Theme.FONT_BOLD);
        panel.add(label, BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(parent, panel, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return null;
        return Objects.toString(combo.getSelectedItem(), null);
    }

    public static Integer promptInt(Component parent, String title, String message, int initial, int min, int max) {
        String value = prompt(parent, title, message, String.valueOf(initial));
        if (value == null) return null;
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min || parsed > max) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException e) {
            showError(parent, "Please enter a number between " + min + " and " + max + ".");
            return null;
        }
    }
}
