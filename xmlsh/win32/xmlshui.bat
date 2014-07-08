@setlocal
@echo off
call %~dp0\xmlshpath.bat
@start javaw -XX:+UseConcMarkSweepGC  -Xmx1024m -Xms256m org.xmlsh.sh.ui.XShell   %*
