package com.placementtracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class StudentDashboardFrame extends JFrame {
    private final DataRepository repo = new DataRepository();

    private JLabel nameLabel;
    private JLabel statusLabel;
    private JLabel targetLabel;

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> trackTypeCombo;
    private JComboBox<String> trackStageCombo;
    private JTextField targetRoleField;

    private DefaultTableModel skillModel;
    private DefaultTableModel matchModel;
    private DefaultTableModel appModel;

    public StudentDashboardFrame() {
        setTitle("Placement Tracker - Student Dashboard");
        setSize(1300, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Session.currentProfile = repo.getProfileByUserId(Session.currentUser.id);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        setContentPane(root);
        refreshAll();
    }

    private JComponent buildHeader() {
        JPanel header = Theme.cardPanel();
        header.setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        nameLabel = new JLabel();
        nameLabel.setFont(Theme.FONT_H1);
        nameLabel.setForeground(Theme.TEXT);

        statusLabel = new JLabel();
        statusLabel.setForeground(Theme.MUTED);

        targetLabel = new JLabel();
        targetLabel.setForeground(Theme.MUTED);

        left.add(nameLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(statusLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(targetLabel);

        JButton refresh = Theme.secondaryButton("Refresh All");
        refresh.addActionListener(e -> refreshAll());

        header.add(left, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        return header;
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        Theme.styleTabs(tabs);
        tabs.addTab("Profile", buildProfileTab());
        tabs.addTab("Skills", buildSkillsTab());
        tabs.addTab("Matches", buildMatchesTab());
        tabs.addTab("Applications", buildApplicationsTab());
        return tabs;
    }

    private JComponent buildProfileTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new GridBagLayout());

        fullNameField = Theme.textField(24);
        emailField = Theme.textField(24);
        phoneField = Theme.textField(24);
        trackTypeCombo = new JComboBox<>(new String[]{"Internship", "Job"});
        trackStageCombo = new JComboBox<>(new String[]{"Looking For", "Upcoming", "Ongoing"});
        Theme.styleComboBox(trackTypeCombo);
        Theme.styleComboBox(trackStageCombo);
        targetRoleField = Theme.textField(24);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(p, g, y++, "Full Name", fullNameField);
        addRow(p, g, y++, "Email", emailField);
        addRow(p, g, y++, "Phone", phoneField);
        addRow(p, g, y++, "Track Type", trackTypeCombo);
        addRow(p, g, y++, "Track Stage", trackStageCombo);
        addRow(p, g, y++, "Target Role", targetRoleField);

        JButton save = Theme.primaryButton("Save Profile");
        g.gridy = y++;
        g.gridx = 0;
        g.gridwidth = 2;
        p.add(save, g);

        save.addActionListener(e -> saveProfile());
        return p;
    }

    private JComponent buildSkillsTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton add = Theme.primaryButton("Add / Update Skill");
        JButton delete = Theme.dangerButton("Delete Selected");
        JButton refresh = Theme.secondaryButton("Refresh");
        top.add(add);
        top.add(delete);
        top.add(refresh);

        skillModel = new DefaultTableModel(new Object[]{"Skill", "Proficiency"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(skillModel);
        Theme.styleTable(table);

        add.addActionListener(e -> addSkill());
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UiUtils.showError(this, "Select a skill first.");
                return;
            }
            String skill = skillModel.getValueAt(row, 0).toString();
            int skillId = findSkillIdByName(skill);
            repo.deleteStudentSkill(Session.currentProfile.id, skillId);
            refreshAll();
        });
        refresh.addActionListener(e -> refreshSkills());

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JComponent buildMatchesTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton refresh = Theme.primaryButton("Refresh Matches");
        JButton apply = Theme.secondaryButton("Apply to Selected Company");
        top.add(refresh);
        top.add(apply);

        matchModel = new DefaultTableModel(new Object[]{"Company", "Match", "Matched Skills", "Required Skills"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(matchModel);
        Theme.styleTable(table);

        refresh.addActionListener(e -> refreshMatches());
        apply.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UiUtils.showError(this, "Select a company first.");
                return;
            }
            String company = matchModel.getValueAt(row, 0).toString();
            Company selected = repo.getAllCompanies().stream().filter(c -> c.name.equals(company)).findFirst().orElse(null);
            if (selected == null) return;
            String[] options = {"Applied", "Interview", "Offered", "Rejected"};
                String status = UiUtils.promptChoice(this, "Apply", "Application status:", options, options[0]);
            if (status == null) return;
            repo.addApplication(Session.currentProfile.id, selected.id, status);
            refreshApplications();
            UiUtils.showInfo(this, "Saved application for " + company + ".");
        });

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JComponent buildApplicationsTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton refresh = Theme.primaryButton("Refresh Applications");
        top.add(refresh);

        appModel = new DefaultTableModel(new Object[]{"Company", "Status", "Applied At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(appModel);
        Theme.styleTable(table);

        refresh.addActionListener(e -> refreshApplications());

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private void saveProfile() {
        if (Session.currentProfile == null) {
            UiUtils.showError(this, "Profile not found.");
            return;
        }
        Session.currentProfile.fullName = fullNameField.getText();
        Session.currentProfile.email = emailField.getText();
        Session.currentProfile.phone = phoneField.getText();
        Session.currentProfile.trackType = Objects.toString(trackTypeCombo.getSelectedItem(), "Internship");
        Session.currentProfile.trackStage = Objects.toString(trackStageCombo.getSelectedItem(), "Looking For");
        Session.currentProfile.targetRole = targetRoleField.getText();

        if (Session.currentProfile.fullName.isBlank()) {
            UiUtils.showError(this, "Full name cannot be empty.");
            return;
        }
        repo.saveProfile(Session.currentProfile);
        refreshAll();
        UiUtils.showInfo(this, "Profile saved.");
    }

    private void addSkill() {
        String name = UiUtils.prompt(this, "Add Skill", "Skill name:", "");
        if (name == null || name.isBlank()) return;
        Integer proficiency = UiUtils.promptInt(this, "Proficiency", "Proficiency (1-5):", 3, 1, 5);
        if (proficiency == null) return;
        repo.addStudentSkill(Session.currentProfile.id, name, proficiency);
        refreshSkills();
        refreshMatches();
    }

    private void refreshAll() {
        if (Session.currentProfile != null) {
            Session.currentProfile = repo.getProfileByStudentId(Session.currentProfile.id);
            nameLabel.setText(Session.currentProfile.fullName);
            statusLabel.setText("Track: " + Session.currentProfile.trackType + " | Stage: " + Session.currentProfile.trackStage);
            targetLabel.setText("Target role: " + Session.currentProfile.targetRole);

            fullNameField.setText(Session.currentProfile.fullName);
            emailField.setText(Session.currentProfile.email);
            phoneField.setText(Session.currentProfile.phone);
            trackTypeCombo.setSelectedItem(Session.currentProfile.trackType);
            trackStageCombo.setSelectedItem(Session.currentProfile.trackStage);
            targetRoleField.setText(Session.currentProfile.targetRole);
        }
        refreshSkills();
        refreshMatches();
        refreshApplications();
    }

    private void refreshSkills() {
        skillModel.setRowCount(0);
        for (StudentSkill s : repo.getStudentSkills(Session.currentProfile.id)) {
            skillModel.addRow(new Object[]{s.skillName, s.proficiency});
        }
    }

    private void refreshMatches() {
        matchModel.setRowCount(0);
        for (MatchResult m : repo.getMatchesForStudent(Session.currentProfile.id)) {
            matchModel.addRow(new Object[]{
                    m.companyName,
                    String.format("%.0f%% (%d/%d)", m.score, m.matchedCount, m.requiredCount),
                    m.matchedSkills,
                    m.requiredSkills
            });
        }
    }

    private void refreshApplications() {
        appModel.setRowCount(0);
        for (ApplicationRow a : repo.getApplicationsForStudent(Session.currentProfile.id)) {
            appModel.addRow(new Object[]{a.companyName, a.status, a.appliedAt});
        }
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        p.add(l(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    private JLabel l(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        return l;
    }

    private int findSkillIdByName(String skillName) {
        for (Skill s : repo.getAllSkills()) {
            if (s.name.equalsIgnoreCase(skillName.trim())) return s.id;
        }
        return repo.addSkillIfMissing(skillName.trim());
    }
}
