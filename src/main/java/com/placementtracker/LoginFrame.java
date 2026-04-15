package com.placementtracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final DataRepository repo = new DataRepository();

    public LoginFrame() {
        setTitle("Placement Tracker - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(920, 600));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel hero = Theme.cardPanel();
        hero.setLayout(new BorderLayout());
        hero.setPreferredSize(new Dimension(360, 0));

        JLabel title = Theme.title("Placement Tracker");
        JLabel subtitle = Theme.subtitle("Student matching and placement status, built with Java Swing + JDBC + MySQL.");

        JPanel heroTop = new JPanel();
        heroTop.setOpaque(false);
        heroTop.setLayout(new BoxLayout(heroTop, BoxLayout.Y_AXIS));
        heroTop.add(title);
        heroTop.add(Box.createVerticalStrut(8));
        heroTop.add(subtitle);

        JTextArea notes = new JTextArea("""
                What it does
                • Student login and registration
                • Skill-based company matching
                • Internship / Job status tracking
                • Admin view for all student data
                """);
        notes.setEditable(false);
        notes.setOpaque(false);
        notes.setForeground(Theme.MUTED);
        notes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notes.setBorder(new EmptyBorder(18, 0, 0, 0));

        hero.add(heroTop, BorderLayout.NORTH);
        hero.add(notes, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        Theme.styleTabs(tabs);

        JPanel loginPanel = buildLoginPanel();
        JPanel registerPanel = buildRegisterPanel();
        tabs.addTab("Login", wrapScrollable(loginPanel));
        tabs.addTab("Student Register", wrapScrollable(registerPanel));

        JPanel right = Theme.cardPanel();
        right.setLayout(new BorderLayout(0, 16));
        right.add(tabs, BorderLayout.CENTER);

        root.add(hero, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Theme.PANEL);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setLayout(new GridBagLayout());

        JTextField username = Theme.textField(20);
        JPasswordField password = Theme.passwordField(20);
        JButton login = Theme.primaryButton("Login as Admin / Student");

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.weightx = 1.0;
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        p.add(label("Username"), g);
        g.gridy++;
        p.add(username, g);
        g.gridy++;
        p.add(label("Password"), g);
        g.gridy++;
        p.add(password, g);
        g.gridy++;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(login, g);

        login.addActionListener(e -> {
            String u = username.getText().trim();
            String pss = new String(password.getPassword());
            if (u.isEmpty() || pss.isEmpty()) {
                UiUtils.showError(this, "Enter username and password.");
                return;
            }
            User user = repo.authenticate(u, pss);
            if (user == null) {
                UiUtils.showError(this, "Invalid login.");
                return;
            }
            Session.currentUser = user;
            Session.currentProfile = "STUDENT".equalsIgnoreCase(user.role) ? repo.getProfileByUserId(user.id) : null;
            dispose();
            if ("ADMIN".equalsIgnoreCase(user.role)) {
                new AdminDashboardFrame().setVisible(true);
            } else {
                new StudentDashboardFrame().setVisible(true);
            }
        });
        return p;
    }

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Theme.PANEL);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setLayout(new GridBagLayout());

        JTextField username = Theme.textField(20);
        JPasswordField password = Theme.passwordField(20);
        JTextField fullName = Theme.textField(20);
        JTextField email = Theme.textField(20);
        JTextField phone = Theme.textField(20);
        JButton register = Theme.primaryButton("Create Student Account");

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.weightx = 1.0;
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        addFormField(p, g, "Username", username);
        addFormField(p, g, "Password", password);
        addFormField(p, g, "Full Name", fullName);
        addFormField(p, g, "Email", email);
        addFormField(p, g, "Phone", phone);

        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy++;
        p.add(register, g);

        register.addActionListener(e -> {
            String u = username.getText().trim();
            String pss = new String(password.getPassword());
            String n = fullName.getText().trim();
            String em = email.getText().trim();
            String ph = phone.getText().trim();
            if (u.isEmpty() || pss.isEmpty() || n.isEmpty()) {
                UiUtils.showError(this, "Username, password, and full name are required.");
                return;
            }
            boolean ok = repo.registerStudent(u, pss, n, em, ph);
            if (!ok) {
                UiUtils.showError(this, "Could not register. Username may already exist.");
                return;
            }
            UiUtils.showInfo(this, "Student account created. You can log in now.");
            username.setText("");
            password.setText("");
            fullName.setText("");
            email.setText("");
            phone.setText("");
        });
        return p;
    }

    private void addFormField(JPanel p, GridBagConstraints g, String label, JComponent field) {
        g.gridx = 0;
        g.fill = GridBagConstraints.NONE;
        g.gridy++;
        p.add(label(label), g);
        g.gridy++;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(field, g);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JComponent wrapScrollable(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Theme.PANEL);
        return scroll;
    }
}
