package com.placementtracker;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public final class Database {
    private static final String DEFAULT_HOST_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/placement_tracker_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DB_NAME = "placement_tracker_db";

    private static String hostUrl = DEFAULT_HOST_URL;
    private static String dbUrl = DEFAULT_DB_URL;
    private static String username = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    private Database() {}

    public static void loadConfig() {
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            if (in != null) {
                props.load(in);
                hostUrl = props.getProperty("db.hostUrl", DEFAULT_HOST_URL);
                dbUrl = props.getProperty("db.dbUrl", DEFAULT_DB_URL);
                username = props.getProperty("db.username", DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASSWORD);
            }
        } catch (Exception ignored) {}
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }

    public static void initialize() {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(hostUrl, username, password);
                 Statement st = con.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            try (Connection con = getConnection(); Statement st = con.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(100) NOT NULL,
                        role VARCHAR(20) NOT NULL
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS students (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,
                        full_name VARCHAR(100) NOT NULL,
                        email VARCHAR(120),
                        phone VARCHAR(20),
                        track_type VARCHAR(20) NOT NULL DEFAULT 'Internship',
                        track_stage VARCHAR(20) NOT NULL DEFAULT 'Looking For',
                        target_role VARCHAR(120),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS skills (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(80) NOT NULL UNIQUE
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS student_skills (
                        student_id INT NOT NULL,
                        skill_id INT NOT NULL,
                        proficiency INT NOT NULL,
                        PRIMARY KEY (student_id, skill_id),
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS companies (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(120) NOT NULL UNIQUE,
                        description VARCHAR(255)
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS company_skills (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        company_id INT NOT NULL,
                        skill_id INT NOT NULL,
                        required_level INT NOT NULL,
                        UNIQUE KEY uq_company_skill (company_id, skill_id),
                        FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS applications (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        student_id INT NOT NULL,
                        company_id INT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        notes VARCHAR(255),
                        UNIQUE KEY uq_app (student_id, company_id),
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                        FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
                    )
                """);
            }

            seedData();
        } catch (Exception e) {
            throw new RuntimeException("""
                Database initialization failed.

                Check these things:
                1) MySQL Server is running
                2) Username/password in src/main/resources/db.properties are correct
                3) The user has permission to CREATE DATABASE

                Error: """ + e.getMessage(), e);
        }
    }

    private static void seedData() throws SQLException {
        try (Connection con = getConnection()) {
            insertIfMissing(con, "INSERT IGNORE INTO users(username, password, role) VALUES(?,?,?)", "admin", "admin123", "ADMIN");

            String[] skills = {
                "Java", "SQL", "OOP", "DSA", "Git", "Problem Solving", "Communication",
                "HTML", "CSS", "JavaScript", "Spring Boot", "Testing", "Python", "React"
            };
            for (String skill : skills) {
                insertSkill(con, skill);
            }

            insertCompany(con, "Google", "Product engineering and software development");
            insertCompany(con, "Microsoft", "Enterprise software and cloud engineering");
            insertCompany(con, "Amazon", "Cloud, backend, and large-scale systems");
            insertCompany(con, "Infosys", "IT services and consulting");
            insertCompany(con, "TCS", "Enterprise software delivery");
            insertCompany(con, "Deloitte", "Consulting and technology solutions");

            seedCompanySkill(con, "Google", "Java", 4);
            seedCompanySkill(con, "Google", "DSA", 4);
            seedCompanySkill(con, "Google", "OOP", 4);
            seedCompanySkill(con, "Google", "Git", 2);
            seedCompanySkill(con, "Google", "Problem Solving", 4);

            seedCompanySkill(con, "Microsoft", "Java", 4);
            seedCompanySkill(con, "Microsoft", "SQL", 3);
            seedCompanySkill(con, "Microsoft", "OOP", 4);
            seedCompanySkill(con, "Microsoft", "Git", 2);
            seedCompanySkill(con, "Microsoft", "Communication", 3);

            seedCompanySkill(con, "Amazon", "Java", 4);
            seedCompanySkill(con, "Amazon", "DSA", 4);
            seedCompanySkill(con, "Amazon", "SQL", 3);
            seedCompanySkill(con, "Amazon", "Problem Solving", 5);

            seedCompanySkill(con, "Infosys", "Java", 3);
            seedCompanySkill(con, "Infosys", "SQL", 2);
            seedCompanySkill(con, "Infosys", "Communication", 3);
            seedCompanySkill(con, "Infosys", "Git", 1);

            seedCompanySkill(con, "TCS", "Java", 3);
            seedCompanySkill(con, "TCS", "OOP", 3);
            seedCompanySkill(con, "TCS", "Communication", 3);
            seedCompanySkill(con, "TCS", "SQL", 2);

            seedCompanySkill(con, "Deloitte", "SQL", 3);
            seedCompanySkill(con, "Deloitte", "Communication", 4);
            seedCompanySkill(con, "Deloitte", "Problem Solving", 4);
            seedCompanySkill(con, "Deloitte", "Testing", 3);
        }
    }

    private static void insertIfMissing(Connection con, String sql, String username, String password, String role) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    private static int skillId(Connection con, String skillName) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM skills WHERE name = ?")) {
            ps.setString(1, skillName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return insertSkill(con, skillName);
    }

    private static int insertSkill(Connection con, String skillName) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO skills(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skillName);
            ps.executeUpdate();
            try (PreparedStatement qs = con.prepareStatement("SELECT id FROM skills WHERE name = ?")) {
                qs.setString(1, skillName);
                try (ResultSet rs = qs.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
    }

    private static int companyId(Connection con, String companyName, String desc) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM companies WHERE name = ?")) {
            ps.setString(1, companyName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return insertCompany(con, companyName, desc);
    }

    private static int insertCompany(Connection con, String companyName, String desc) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO companies(name, description) VALUES(?,?)")) {
            ps.setString(1, companyName);
            ps.setString(2, desc);
            ps.executeUpdate();
        }
        try (PreparedStatement qs = con.prepareStatement("SELECT id FROM companies WHERE name = ?")) {
            qs.setString(1, companyName);
            try (ResultSet rs = qs.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void seedCompanySkill(Connection con, String companyName, String skillName, int level) throws SQLException {
        int cId = companyId(con, companyName, "");
        int sId = skillId(con, skillName);
        try (PreparedStatement ps = con.prepareStatement("""
                INSERT IGNORE INTO company_skills(company_id, skill_id, required_level)
                VALUES(?,?,?)
        """)) {
            ps.setInt(1, cId);
            ps.setInt(2, sId);
            ps.setInt(3, level);
            ps.executeUpdate();
        }
    }
}
