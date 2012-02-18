set SCRIPT_DIR=%~dp0
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=250m -Xmx1024M -Xms128M -Xss2M -jar "%SCRIPT_DIR%\sbt-launch.jar" %*
