# Placement Tracker - Java Swing + JDBC + MySQL

A desktop application for:
- student login and registration
- profile management
- skill tracking
- company matching
- internship/job status tracking
- admin visibility into all student data

## Tech stack
- Java 17
- Swing
- JDBC
- MySQL

## Default login
- Username: `admin`
- Password: `admin123`

## Setup

### 1) Install software
- JDK 17
- MySQL Server
- MySQL Workbench or another SQL client
- Maven

### 2) Configure MySQL
Open `src/main/resources/db.properties` and set:
- MySQL username
- MySQL password
- host/database URL if needed

Default assumes:
- MySQL running locally on port 3306
- username `root`
- empty password

### 3) Run the app
From the project folder:

```bash
mvn clean compile exec:java
```

The app will:
- create the database if the MySQL user has permission
- create all required tables
- seed admin and sample companies/skills

If your MySQL user cannot create databases, run `sql/schema.sql` manually first.

## What the app does
### Student side
- register as a student
- edit profile
- add/update skills with proficiency
- view best-fit companies
- apply to a company
- track application status

### Admin side
- view all students
- open any student profile
- view skill counts and matching companies
- manage companies
- map skills to companies
- manage skills
- view all applications

## Matching logic
A company skill is counted as a match when:

`student proficiency >= company required level`

The match score is:

`matched required skills / total required skills * 100`

## Notes for your viva
- JDBC is used for all database communication
- MySQL stores persistent data
- Swing provides the desktop UI
- DAO-style repository methods keep the database logic separate from the UI
