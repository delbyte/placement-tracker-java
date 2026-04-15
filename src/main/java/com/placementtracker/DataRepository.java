package com.placementtracker;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataRepository {

    public User authenticate(String username, String password) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean registerStudent(String username, String password, String fullName, String email, String phone) {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            int userId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(username, password, role) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username.trim());
                ps.setString(2, password);
                ps.setString(3, "STUDENT");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    userId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO students(user_id, full_name, email, phone, track_type, track_stage, target_role)
                    VALUES(?,?,?,?,?,?,?)
            """)) {
                ps.setInt(1, userId);
                ps.setString(2, fullName.trim());
                ps.setString(3, email.trim());
                ps.setString(4, phone.trim());
                ps.setString(5, "Internship");
                ps.setString(6, "Looking For");
                ps.setString(7, "Software Engineer");
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public StudentProfile getProfileByUserId(int userId) {
        String sql = """
            SELECT id, user_id, full_name, email, phone, track_type, track_stage, target_role
            FROM students WHERE user_id = ?
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStudent(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public StudentProfile getProfileByStudentId(int studentId) {
        String sql = """
            SELECT id, user_id, full_name, email, phone, track_type, track_stage, target_role
            FROM students WHERE id = ?
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStudent(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void saveProfile(StudentProfile profile) {
        String sql = """
            UPDATE students
            SET full_name = ?, email = ?, phone = ?, track_type = ?, track_stage = ?, target_role = ?
            WHERE id = ?
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, profile.fullName.trim());
            ps.setString(2, profile.email.trim());
            ps.setString(3, profile.phone.trim());
            ps.setString(4, profile.trackType);
            ps.setString(5, profile.trackStage);
            ps.setString(6, profile.targetRole.trim());
            ps.setInt(7, profile.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StudentProfile> getAllStudents() {
        List<StudentProfile> list = new ArrayList<>();
        String sql = """
            SELECT id, user_id, full_name, email, phone, track_type, track_stage, target_role
            FROM students ORDER BY full_name
        """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapStudent(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Skill> getAllSkills() {
        List<Skill> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM skills ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Skill(rs.getInt("id"), rs.getString("name")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int addSkillIfMissing(String skillName) {
        String select = "SELECT id FROM skills WHERE name = ?";
        String insert = "INSERT INTO skills(name) VALUES(?)";
        try (Connection con = Database.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setString(1, skillName.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, skillName.trim());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addStudentSkill(int studentId, String skillName, int proficiency) {
        int skillId = addSkillIfMissing(skillName);
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("""
                 INSERT INTO student_skills(student_id, skill_id, proficiency)
                 VALUES(?,?,?)
                 ON DUPLICATE KEY UPDATE proficiency = VALUES(proficiency)
             """)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            ps.setInt(3, proficiency);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteStudentSkill(int studentId, int skillId) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?")) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StudentSkill> getStudentSkills(int studentId) {
        List<StudentSkill> list = new ArrayList<>();
        String sql = """
            SELECT ss.skill_id, s.name, ss.proficiency
            FROM student_skills ss
            JOIN skills s ON s.id = ss.skill_id
            WHERE ss.student_id = ?
            ORDER BY s.name
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentSkill(
                            rs.getInt("skill_id"),
                            rs.getString("name"),
                            rs.getInt("proficiency")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Company> getAllCompanies() {
        List<Company> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name, description FROM companies ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Company(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int addCompany(String name, String description) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO companies(name, description) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, description.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCompany(int companyId) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM companies WHERE id = ?")) {
            ps.setInt(1, companyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCompanySkill(int companyId, String skillName, int requiredLevel) {
        int skillId = addSkillIfMissing(skillName);
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("""
                 INSERT INTO company_skills(company_id, skill_id, required_level)
                 VALUES(?,?,?)
                 ON DUPLICATE KEY UPDATE required_level = VALUES(required_level)
             """)) {
            ps.setInt(1, companyId);
            ps.setInt(2, skillId);
            ps.setInt(3, requiredLevel);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeCompanySkill(int companyId, int skillId) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM company_skills WHERE company_id = ? AND skill_id = ?")) {
            ps.setInt(1, companyId);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CompanySkill> getCompanySkills(int companyId) {
        List<CompanySkill> list = new ArrayList<>();
        String sql = """
            SELECT cs.skill_id, s.name, cs.required_level
            FROM company_skills cs
            JOIN skills s ON s.id = cs.skill_id
            WHERE cs.company_id = ?
            ORDER BY s.name
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CompanySkill(rs.getInt("skill_id"), rs.getString("name"), rs.getInt("required_level")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<MatchResult> getMatchesForStudent(int studentId) {
        Map<Integer, Integer> studentSkills = getStudentSkills(studentId).stream()
                .collect(Collectors.toMap(ss -> ss.skillId, ss -> ss.proficiency));

        List<MatchResult> results = new ArrayList<>();
        String sql = """
            SELECT c.id AS company_id, c.name AS company_name,
                   s.id AS skill_id, s.name AS skill_name, cs.required_level
            FROM companies c
            JOIN company_skills cs ON cs.company_id = c.id
            JOIN skills s ON s.id = cs.skill_id
            ORDER BY c.name, s.name
        """;
        Map<Integer, List<CompanySkill>> companySkills = new LinkedHashMap<>();
        Map<Integer, String> companyNames = new LinkedHashMap<>();

        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int companyId = rs.getInt("company_id");
                companyNames.put(companyId, rs.getString("company_name"));
                companySkills.computeIfAbsent(companyId, k -> new ArrayList<>()).add(
                        new CompanySkill(rs.getInt("skill_id"), rs.getString("skill_name"), rs.getInt("required_level"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (var entry : companySkills.entrySet()) {
            int companyId = entry.getKey();
            List<CompanySkill> skills = entry.getValue();
            int requiredCount = skills.size();
            int matchedCount = 0;
            List<String> matched = new ArrayList<>();
            List<String> required = new ArrayList<>();

            for (CompanySkill cs : skills) {
                required.add(cs.skillName + " (" + cs.requiredLevel + ")");
                Integer studentLevel = studentSkills.get(cs.skillId);
                if (studentLevel != null && studentLevel >= cs.requiredLevel) {
                    matchedCount++;
                    matched.add(cs.skillName + " [" + studentLevel + "/" + cs.requiredLevel + "]");
                }
            }

            double score = requiredCount == 0 ? 0 : (matchedCount * 100.0 / requiredCount);
            results.add(new MatchResult(
                    companyId,
                    companyNames.get(companyId),
                    requiredCount,
                    matchedCount,
                    score,
                    matched.isEmpty() ? "None" : String.join(", ", matched),
                    String.join(", ", required)
            ));
        }

        results.sort(Comparator.comparingDouble((MatchResult m) -> m.score).reversed()
                .thenComparing(m -> m.companyName));
        return results;
    }

    public void addApplication(int studentId, int companyId, String status) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("""
                 INSERT INTO applications(student_id, company_id, status)
                 VALUES(?,?,?)
                 ON DUPLICATE KEY UPDATE status = VALUES(status), applied_at = CURRENT_TIMESTAMP
             """)) {
            ps.setInt(1, studentId);
            ps.setInt(2, companyId);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ApplicationRow> getApplicationsForStudent(int studentId) {
        List<ApplicationRow> list = new ArrayList<>();
        String sql = """
            SELECT a.id, c.name AS company_name, a.status, a.applied_at
            FROM applications a
            JOIN companies c ON c.id = a.company_id
            WHERE a.student_id = ?
            ORDER BY a.applied_at DESC
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ApplicationRow(
                            rs.getInt("id"),
                            rs.getString("company_name"),
                            rs.getString("status"),
                            rs.getTimestamp("applied_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Object[]> getAllApplications() {
        List<Object[]> rows = new ArrayList<>();
        String sql = """
            SELECT a.id, st.full_name, c.name AS company_name, a.status, a.applied_at
            FROM applications a
            JOIN students st ON st.id = a.student_id
            JOIN companies c ON c.id = a.company_id
            ORDER BY a.applied_at DESC
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("company_name"),
                        rs.getString("status"),
                        rs.getTimestamp("applied_at")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public List<Object[]> getStudentSummaryRows() {
        List<Object[]> rows = new ArrayList<>();
        String sql = """
            SELECT st.id, st.full_name, st.email, st.phone, st.track_type, st.track_stage, st.target_role,
                   COUNT(ss.skill_id) AS skill_count
            FROM students st
            LEFT JOIN student_skills ss ON ss.student_id = st.id
            GROUP BY st.id
            ORDER BY st.full_name
        """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("track_type"),
                        rs.getString("track_stage"),
                        rs.getString("target_role"),
                        rs.getInt("skill_count")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public List<Object[]> getSkillRows() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("""
            SELECT s.id, s.name, COUNT(ss.student_id) AS used_by
            FROM skills s
            LEFT JOIN student_skills ss ON ss.skill_id = s.id
            GROUP BY s.id
            ORDER BY s.name
        """); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getInt("used_by")});
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    public void deleteSkill(int skillId) {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE id = ?")) {
            ps.setInt(1, skillId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private StudentProfile mapStudent(ResultSet rs) throws SQLException {
        return new StudentProfile(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("track_type"),
                rs.getString("track_stage"),
                rs.getString("target_role")
        );
    }
}
