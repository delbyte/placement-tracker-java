@echo off
setlocal

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"

if not defined JAVA_HOME (
	for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
		if exist "%%~fD\bin\java.exe" set "JAVA_HOME=%%~fD"
	)
)

if not defined JAVA_HOME (
	echo [ERROR] JAVA_HOME is not set.
	echo Install JDK 17 and reopen this terminal.
	pause
	exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
	echo [ERROR] JAVA_HOME is invalid: %JAVA_HOME%
	pause
	exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

set "MYSQL_BASE="
set "MYSQLD_EXE="
set "MYSQL_EXE="
for /d %%D in ("C:\Program Files\MySQL\MySQL Server *") do (
	if exist "%%~fD\bin\mysqld.exe" (
		set "MYSQL_BASE=%%~fD"
		set "MYSQLD_EXE=%%~fD\bin\mysqld.exe"
		set "MYSQL_EXE=%%~fD\bin\mysql.exe"
	)
)

if not defined MYSQLD_EXE (
	echo [ERROR] MySQL Server not found.
	echo Install MySQL Server and try again.
	pause
	exit /b 1
)

set "MYSQL_DATA=%ROOT_DIR%mysql-data"
if not exist "%MYSQL_DATA%" mkdir "%MYSQL_DATA%"
set "MYSQL_ERROR_LOG=%MYSQL_DATA%\mysqld.err"

if not exist "%MYSQL_DATA%\mysql" (
	echo [INFO] Initializing local MySQL data directory...
	call "%MYSQLD_EXE%" --initialize-insecure --basedir="%MYSQL_BASE%" --datadir="%MYSQL_DATA%" --console
	if errorlevel 1 (
		echo [ERROR] MySQL initialization failed.
		pause
		exit /b 1
	)
)

"%MYSQL_EXE%" -h 127.0.0.1 -P 3306 -u root -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
	set "PORT_3306_PID="
	for /f %%P in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "$c=Get-NetTCPConnection -State Listen -LocalPort 3306 -ErrorAction SilentlyContinue | Select-Object -First 1; if($c){$c.OwningProcess}"') do set "PORT_3306_PID=%%P"
	if defined PORT_3306_PID (
		echo [ERROR] Port 3306 is already in use by PID %PORT_3306_PID%.
		echo Stop the other MySQL instance or change db.port settings before running this script.
		pause
		exit /b 1
	)

	echo [INFO] Starting local MySQL on port 3306...
	if exist "%MYSQL_ERROR_LOG%" del /f /q "%MYSQL_ERROR_LOG%" >nul 2>&1
	start "" /B "%MYSQLD_EXE%" --no-defaults --basedir="%MYSQL_BASE%" --datadir="%MYSQL_DATA%" --port=3306 --bind-address=127.0.0.1 --mysqlx=0 --console > "%MYSQL_ERROR_LOG%" 2>&1
	echo [INFO] Waiting for MySQL to become ready...
	powershell -NoProfile -ExecutionPolicy Bypass -Command "$mysql='%MYSQL_EXE%'; $ready=$false; for($i=0; $i -lt 40; $i++){ & $mysql -h 127.0.0.1 -P 3306 -u root -e \"SELECT 1\" > $null 2>&1; if($LASTEXITCODE -eq 0){ $ready=$true; break }; Start-Sleep -Milliseconds 500 }; if($ready){ exit 0 }; exit 1"
	if errorlevel 1 (
		echo [ERROR] MySQL did not become ready within 20 seconds.
		if exist "%MYSQL_ERROR_LOG%" (
			echo [INFO] Showing latest MySQL error log lines:
			powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Content '%MYSQL_ERROR_LOG%' -Tail 40"
		)
		echo Try closing old MySQL terminals and run this script again.
		pause
		exit /b 1
	)
)

set "MAVEN_VERSION=3.9.6"
set "MAVEN_DIR=%ROOT_DIR%.tools\apache-maven-%MAVEN_VERSION%"
set "MAVEN_CMD=%MAVEN_DIR%\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
	echo [INFO] Downloading local Maven %MAVEN_VERSION%...
	if not exist "%ROOT_DIR%.tools" mkdir "%ROOT_DIR%.tools"

	powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%ROOT_DIR%.tools\apache-maven-%MAVEN_VERSION%-bin.zip'"
	if errorlevel 1 (
		echo [ERROR] Maven download failed.
		pause
		exit /b 1
	)

	powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ROOT_DIR%.tools\apache-maven-%MAVEN_VERSION%-bin.zip' -DestinationPath '%ROOT_DIR%.tools' -Force"
	if errorlevel 1 (
		echo [ERROR] Maven extraction failed.
		pause
		exit /b 1
	)
)

call "%MAVEN_CMD%" clean compile exec:java
pause
