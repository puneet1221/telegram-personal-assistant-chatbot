@echo off
REM Run Personal Assistant with proper JVM arguments for Java 17+ module system

set JAVA_OPTS=--add-opens java.base/java.nio.charset=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED

java %JAVA_OPTS% -jar target/personal-assistant-0.0.1-SNAPSHOT.jar %*
