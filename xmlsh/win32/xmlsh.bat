@setlocal
@echo off
call %~dp0\xmlshpath.bat
java -XX:+UseConcMarkSweepGC  -Xmx1024m -Xms256m org.xmlsh.sh.shell.Shell   %*
