@echo off
REM Minimal Windows Build Test
echo 🧪 Windows Build Environment Test

echo Current directory:
cd

echo Java version:
java -version

echo Maven version:
mvn -version

echo Environment:
echo JAVA_HOME=%JAVA_HOME%
echo PATH=%PATH%

echo Testing basic Maven command:
mvn help:evaluate -Dexpression=project.version -q -DforceStdout

echo Testing compilation:
mvn clean compile -q

echo Listing target after compile:
if exist target (
    dir target
) else (
    echo No target directory created
)

echo 🧪 Test completed