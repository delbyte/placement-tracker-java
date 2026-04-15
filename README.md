# Placement Tracker (Java Swing + JDBC + MySQL)

A desktop application for managing campus placement workflow:
- student registration and login
- profile and skill management
- company matching and applications
- admin dashboard for students, companies, skills, and applications

## Tech Stack
- Java 17
- Swing
- JDBC
- MySQL
- Maven

## Default Admin Login
- Username: `admin`
- Password: `admin123`

## Quick Start on a Blank Windows PC

This is the easiest path for a fresh machine.

### 1) Install prerequisites
1. Install JDK 17.
2. Install MySQL Server 8.x.
3. Install Git.

### 2) Clone the repository
```powershell
git clone https://github.com/delbyte/placement-tracker-java.git
cd placement-tracker-java
```

### 3) Run the app (recommended)
```powershell
.\run.bat
```

What `run.bat` does:
1. Detects Java.
2. Initializes a local project MySQL data folder (`mysql-data`) if needed.
3. Starts MySQL on `127.0.0.1:3306`.
4. Downloads Maven locally into `.tools` if missing.
5. Builds and launches the Swing app.

Keep the terminal open while using the app.

## Alternative Run Method (manual MySQL + Maven)

Use this if you already manage MySQL as a Windows service and have Maven installed globally.

### 1) Configure DB credentials
Edit `src/main/resources/db.properties`.

Default values:
- host URL: `jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- database URL: `jdbc:mysql://localhost:3306/placement_tracker_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- username: `root`
- password: empty

### 2) Start MySQL service
Start your local MySQL service from Services or Workbench.

### 3) Build and run
```powershell
mvn clean compile exec:java
```

## Verify Database and Tables

After app startup, MySQL should contain schema `placement_tracker_db`.

In MySQL Workbench:
1. Connect to `127.0.0.1`, port `3306`.
2. Open `SCHEMAS` and refresh.
3. Expand `placement_tracker_db` -> `Tables`.

Or run:
```sql
USE placement_tracker_db;
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM students;
```

## Common Issues

### Workbench says "No local MySQL servers running"
That status is about Windows services. The app can still connect if `run.bat` launched MySQL successfully.

### Port 3306 already in use
Stop the other MySQL instance, then run `run.bat` again.

### MySQL startup fails
Check the latest log output in `mysql-data\mysqld.err`.

### Maven command not found
Use `run.bat` (it auto-downloads Maven locally).

## Features

### Student
- register/login
- edit profile (track type/stage/target role)
- add/update skills with proficiency
- view company match scores
- apply and track status

### Admin
- view all students and profile details
- add/delete companies
- add/update company required skills
- manage skills
- view all applications

## Matching Logic

A company skill is matched when:

`student proficiency >= company required level`

Score:

`matched required skills / total required skills * 100`
