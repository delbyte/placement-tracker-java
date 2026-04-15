package com.placementtracker;

import java.sql.Timestamp;

class User {
    int id;
    String username;
    String password;
    String role;

    User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override public String toString() { return username + " (" + role + ")"; }
}

class StudentProfile {
    int id;
    int userId;
    String fullName;
    String email;
    String phone;
    String trackType;
    String trackStage;
    String targetRole;

    StudentProfile(int id, int userId, String fullName, String email, String phone,
                   String trackType, String trackStage, String targetRole) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.trackType = trackType;
        this.trackStage = trackStage;
        this.targetRole = targetRole;
    }

    @Override public String toString() { return fullName; }
}

class Skill {
    int id;
    String name;

    Skill(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() { return name; }
}

class StudentSkill {
    int skillId;
    String skillName;
    int proficiency;

    StudentSkill(int skillId, String skillName, int proficiency) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiency = proficiency;
    }
}

class Company {
    int id;
    String name;
    String description;

    Company(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override public String toString() { return name; }
}

class CompanySkill {
    int skillId;
    String skillName;
    int requiredLevel;

    CompanySkill(int skillId, String skillName, int requiredLevel) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.requiredLevel = requiredLevel;
    }
}

class MatchResult {
    int companyId;
    String companyName;
    int requiredCount;
    int matchedCount;
    double score;
    String matchedSkills;
    String requiredSkills;

    MatchResult(int companyId, String companyName, int requiredCount, int matchedCount,
                double score, String matchedSkills, String requiredSkills) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.requiredCount = requiredCount;
        this.matchedCount = matchedCount;
        this.score = score;
        this.matchedSkills = matchedSkills;
        this.requiredSkills = requiredSkills;
    }
}

class ApplicationRow {
    int id;
    String companyName;
    String status;
    Timestamp appliedAt;

    ApplicationRow(int id, String companyName, String status, Timestamp appliedAt) {
        this.id = id;
        this.companyName = companyName;
        this.status = status;
        this.appliedAt = appliedAt;
    }
}
