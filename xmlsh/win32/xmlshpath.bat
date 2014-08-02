@ECHO OFF
@REM Sets the CLASSPATH for an XMLSH program
@REM Requires XMLSH variable to be set
if NOT defined XMLSH EXIT /B 1

SET CP=%XMLSH%\bin;%XMLSH%\lib
SET CLASSPATH=%XMLSH%\bin\*;%XMLSH%\lib\*
