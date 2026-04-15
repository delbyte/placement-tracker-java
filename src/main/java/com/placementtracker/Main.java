package com.placementtracker;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Theme.applyLookAndFeel();
        Database.initialize();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
