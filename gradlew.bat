@echo off
setlocal
set DIR=%~dp0
if defined JAVA_HOME (
  set JAVA_CMD=%JAVA_HOME%\bin\java
) else (
  set JAVA_CMD=java
)
"%JAVA_CMD%" --enable-native-access=ALL-UNNAMED -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar;%DIR%gradle\wrapper\gradle-wrapper-shared.jar;%DIR%gradle\wrapper\gradle-cli.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal
