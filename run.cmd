@echo off
REM Run Personal Assistant with proper JVM arguments for Java 17+ module system

set JAVA_OPTS=--add-opens java.base/java.nio.charset=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED

where ngrok >nul 2>&1
if %ERRORLEVEL%==0 (
  echo Starting ngrok tunnel to port 8080...
  start "" ngrok http 8080
) else (
  echo ngrok not found in PATH; skipping tunnel startup.
)

timeout /t 2 /nobreak >nul
java %JAVA_OPTS% -jar target/personal-assistant-0.0.1-SNAPSHOT.jar %*
