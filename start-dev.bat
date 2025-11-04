@echo off
echo Starting AutoTest Platform Development Environment...

echo.
echo Step 1: Starting Docker services (MySQL, Redis, MinIO)
docker-compose up -d

echo.
echo Waiting for services to start...
timeout /t 30 /nobreak

echo.
echo Step 2: Checking service status
docker-compose ps

echo.
echo Step 3: Starting Spring Boot application...
mvn spring-boot:run

pause