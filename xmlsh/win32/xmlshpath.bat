@ECHO OFF
@REM Sets the CLASSPATH for an XMLSH program
@REM Requires XMLSH variable to be set
if NOT defined XMLSH EXIT /B 1
SET CP=%XMLSH%\bin;%XMLSH%\lib
SET CLASSPATH=
FOR /F "eol=#" %%J IN (%~dp0\classpath.txt) DO CALL :ADDPATH %%~$CP:J
SET CP=%XMLSH%\bin;%XMLSH%\lib
GOTO :EOF

:ADDPATH 
SET CLASSPATH=%CLASSPATH%;%1
GOTO :EOF
