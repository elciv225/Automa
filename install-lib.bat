@echo off
setlocal enabledelayedexpansion

REM Répertoire contenant les .jar
set LIB_DIR=lib

REM Vérifie si le dossier lib existe
if not exist "%LIB_DIR%" (
    echo Le dossier %LIB_DIR% n'existe pas.
    exit /b
)

for %%f in (%LIB_DIR%\*.jar) do (
    set FILE=%%f
    set NAME=%%~nf
    echo.
    echo 📦 Installation de !FILE! ...
    mvn install:install-file ^
        -Dfile=!FILE! ^
        -DgroupId=local ^
        -DartifactId=!NAME! ^
        -Dversion=1.0 ^
        -Dpackaging=jar
)

echo.
echo ✅ Tous les JAR du dossier %LIB_DIR% ont été installés dans le dépôt Maven local.
pause
