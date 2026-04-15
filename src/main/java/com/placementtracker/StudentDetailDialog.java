package com.placementtracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentDetailDialog extends JDialog {
    private final DataRepository repo = new DataRepository();

    public StudentDetailDialog(Window owner, StudentProfile profile) {
        super(owner, "Student Profile - " + profile.fullName, ModalityType.APPLICATION_MODAL);
        setSize(900, 620);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel top = Theme.cardPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(head(profile.fullName));
        top.add(Box.createVerticalStrut(8));
        top.add(line("Email: " + profile.email));
        top.add(line("Phone: " + profile.phone));
        top.add(line("Track: " + profile.trackType + " | Stage: " + profile.trackStage));
        top.add(line("Target Role: " + profile.targetRole));

        DefaultTableModel skillModel = new DefaultTableModel(new Object[]{"Skill", "Proficiency"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable skillTable = new JTable(skillModel);
        Theme.styleTable(skillTable);

        for (StudentSkill s : repo.getStudentSkills(profile.id)) {
            skillModel.addRow(new Object[]{s.skillName, s.proficiency});
        }

        DefaultTableModel matchModel = new DefaultTableModel(new Object[]{"Company", "Match %", "Matched Skills"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable matchTable = new JTable(matchModel);
        Theme.styleTable(matchTable);
        for (MatchResult m : repo.getMatchesForStudent(profile.id)) {
            matchModel.addRow(new Object[]{
                    m.companyName,
                    String.format("%.0f%% (%d/%d)", m.score, m.matchedCount, m.requiredCount),
                    m.matchedSkills
            });
        }

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrapWithTitle("Skills", skillTable),
                wrapWithTitle("Best Matches", matchTable));
        split.setResizeWeight(0.42);
        split.setBorder(null);

        root.add(top, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel wrapWithTitle(String title, JTable table) {
        JPanel p = Theme.cardPanel();
        p.setLayout(new BorderLayout(0, 12));
        p.add(Theme.sectionHeader(title, ""), BorderLayout.NORTH);
        p.add(Theme.tableScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JLabel head(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_H1);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JLabel line(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.MUTED);
        return l;
    }
}
