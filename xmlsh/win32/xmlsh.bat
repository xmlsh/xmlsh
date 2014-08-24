@setlocal
@echo off
IF NOT DEFINED XMLSH GOTO NOX 
SET CP=%XMLSH%\bin\*;%XMLSH%\lib\*
IF NOT DEFINED XMLSH_JVMOPTS SET XMLSH_JVMOPTS=-XX:+UseConcMarkSweepGC -Xmx1024m -Xms256m 
java -cp %CP% %XMLSH_JVMOPTS% org.xmlsh.sh.shell.Shell   %*
exit /b 0

:NOX
echo XMLSH not defined
exit /b 1