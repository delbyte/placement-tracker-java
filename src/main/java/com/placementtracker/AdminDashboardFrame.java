package com.placementtracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private final DataRepository repo = new DataRepository();

    private DefaultTableModel studentModel;
    private DefaultTableModel companyModel;
    private DefaultTableModel skillModel;
    private DefaultTableModel appModel;

    public AdminDashboardFrame() {
        setTitle("Placement Tracker - Admin Dashboard");
        setSize(1340, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel header = Theme.cardPanel();
        header.setLayout(new BorderLayout());
        header.add(Theme.sectionHeader("Admin Dashboard", "View students, manage companies and skills, and inspect placement activity."), BorderLayout.WEST);
        JButton refreshAll = Theme.secondaryButton("Refresh All");
        refreshAll.addActionListener(e -> refreshAll());
        header.add(refreshAll, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        setContentPane(root);

        refreshAll();
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        Theme.styleTabs(tabs);
        tabs.addTab("Students", buildStudentsTab());
        tabs.addTab("Companies", buildCompaniesTab());
        tabs.addTab("Skills", buildSkillsTab());
        tabs.addTab("Applications", buildApplicationsTab());
        return tabs;
    }

    private JComponent buildStudentsTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton view = Theme.primaryButton("View Selected Student");
        JButton refresh = Theme.secondaryButton("Refresh");
        top.add(view);
        top.add(refresh);

        studentModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone", "Track", "Stage", "Target Role", "Skill Count"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(studentModel);
        Theme.styleTable(table);

        view.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UiUtils.showError(this, "Select a student first.");
                return;
            }
            int studentId = Integer.parseInt(studentModel.getValueAt(row, 0).toString());
            StudentProfile profile = repo.getProfileByStudentId(studentId);
            new StudentDetailDialog(this, profile).setVisible(true);
        });
        refresh.addActionListener(e -> refreshStudents());

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JComponent buildCompaniesTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton addCompany = Theme.primaryButton("Add Company");
        JButton addSkill = Theme.secondaryButton("Add / Update Company Skill");
        JButton deleteCompany = Theme.dangerButton("Delete Company");
        JButton refresh = Theme.secondaryButton("Refresh");
        top.add(addCompany);
        top.add(addSkill);
        top.add(deleteCompany);
        top.add(refresh);

        companyModel = new DefaultTableModel(new Object[]{"ID", "Company", "Description", "Required Skills"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(companyModel);
        Theme.styleTable(table);

        addCompany.addActionListener(e -> {
            String name = UiUtils.prompt(this, "Add Company", "Company name:", "");
            if (name == null || name.isBlank()) return;
            String desc = UiUtils.prompt(this, "Add Company", "Description:", "");
            if (desc == null) desc = "";
            try {
                repo.addCompany(name, desc);
                refreshCompanies();
            } catch (Exception ex) {
                UiUtils.showError(this, "Company could not be added. It may already exist.");
            }
        });

        addSkill.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UiUtils.showError(this, "Select a company first.");
                return;
            }
            int companyId = Integer.parseInt(companyModel.getValueAt(row, 0).toString());
            String skill = UiUtils.prompt(this, "Company Skill", "Skill name:", "");
            if (skill == null || skill.isBlank()) return;
            Integer level = UiUtils.promptInt(this, "Company Skill", "Required level (1-5):", 3, 1, 5);
            if (level == null) return;
            repo.addCompanySkill(companyId, skill, level);
            refreshCompanies();
        });

        deleteCompany.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UiUtils.showError(this, "Select a company first.");
                return;
            }
            int id = Integer.parseInt(companyModel.getValueAt(row, 0).toString());
            repo.deleteCompany(id);
            refreshCompanies();
        });

        refresh.addActionListener(e -> refreshCompanies());

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JComponent buildSkillsTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton addSkill = Theme.primaryButton("Add Skill");
        JButton deleteSkill = Theme.dangerButton("Delete Skill");
        JButton refresh = Theme.secondaryButton("Refresh");
        top.add(addSkill);
        top.add(deleteSkill);
        top.add(refresh);

        skillModel = new DefaultTableModel(new Object[]{"ID", "Skill", "Used By Students"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(skillModel);
        Theme.styleTable(table);

        addSkill.addActionListener(e -> {
            String skill = UiUtils.prompt(this, "Add Skill", "Skill name:", "");
            if (skill == null || skill.isBlank()) return;
            repo.addSkillIfMissing(skill);
            refreshSkills();
            refreshCompanies();
        });

        deleteSkill.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UiUtils.showError(this, "Select a skill first.");
                return;
            }
            int id = Integer.parseInt(skillModel.getValueAt(row, 0).toString());
            repo.deleteSkill(id);
            refreshSkills();
            refreshCompanies();
        });

        refresh.addActionListener(e -> refreshSkills());

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JComponent buildApplicationsTab() {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton refresh = Theme.primaryButton("Refresh");
        top.add(refresh);

        appModel = new DefaultTableModel(new Object[]{"ID", "Student", "Company", "Status", "Applied At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(appModel);
        Theme.styleTable(table);

        refresh.addActionListener(e -> refreshApplications());

        p.add(top, BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshAll() {
        refreshStudents();
        refreshCompanies();
        refreshSkills();
        refreshApplications();
    }

    private void refreshStudents() {
        studentModel.setRowCount(0);
        for (Object[] row : repo.getStudentSummaryRows()) {
            studentModel.addRow(row);
        }
    }

    private void refreshCompanies() {
        companyModel.setRowCount(0);
        for (Company c : repo.getAllCompanies()) {
            List<CompanySkill> skills = repo.getCompanySkills(c.id);
            String summary = skills.isEmpty()
                    ? "No required skills yet"
                    : skills.stream().map(s -> s.skillName + "(" + s.requiredLevel + ")").reduce((a, b) -> a + ", " + b).orElse("");
            companyModel.addRow(new Object[]{c.id, c.name, c.description, summary});
        }
    }

    private void refreshSkills() {
        skillModel.setRowCount(0);
        for (Object[] row : repo.getSkillRows()) {
            skillModel.addRow(row);
        }
    }

    private void refreshApplications() {
        appModel.setRowCount(0);
        for (Object[] row : repo.getAllApplications()) {
            appModel.addRow(row);
        }
    }
}
