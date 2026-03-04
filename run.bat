@echo off
echo ========================================
echo   Admin Panel - Gestion Platform
echo ========================================
echo.

echo Compilation du projet avec Maven...
call mvn clean compile

echo.
echo Lancement de l'application...
call mvn javafx:run

pause
