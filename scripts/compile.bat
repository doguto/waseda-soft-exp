@echo off
REM scripts/compile.bat
REM Wrapper for compile.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0compile.ps1"
pause
