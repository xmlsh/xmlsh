@setlocal
@echo off
call %~dp0\xmlshpath.bat
IF NOT DEFINED XMLSH_JVMOPTS SET XMLSH_JVMOPTS=-XX:+UseConcMarkSweepGC -Xmx1024m -Xms256m 
java %XMLSH_JVMOPTS% org.xmlsh.sh.shell.Shell   %*
