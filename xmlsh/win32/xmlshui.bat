@setlocal
@echo off
IF NOT DEFINED XMLSH GOTO NOX 
IF NOT DEFINED CLASSPATH call %~dp0\xmlshpath.bat
IF NOT DEFINED XMLSH_JVMOPTS SET XMLSH_JVMOPTS=-XX:+UseConcMarkSweepGC -Xmx1024m -Xms256m 
@start javaw -XX:+UseConcMarkSweepGC  -Xmx1024m -Xms256m org.xmlsh.sh.ui.XShell   %*
exit /b 0

:NOX
echo XMLSH not defined
exit /b 1