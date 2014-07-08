@ECHO OFF
@REM Sets the CLASSPATH for an XMLSH program
@REM Requires XMLSH variable to be set
if NOT defined XMLSH EXIT /B 1

@REM Find appropreate SAXON JAR
@REM Find the appropreate saxon to put in the path ..
@REM use the first of saxon9ee.jar saxon9pe.jar saxon8he.jar

SET _SAXON=
FOR %%S IN (%XMLSH%\lib\saxon9ee.jar %XMLSH%\lib\saxon9pe.jar %XMLSH%\lib\saxon9he.jar) DO if NOT DEFINED _SAXON IF EXIST %%S SET _SAXON=%%S
IF DEFINED _SAXON CALL :ADDPATH %_SAXON%
SET _SAXON=
FOR %%J IN (%XMLSH%\lib\*.jar) DO CALL :ADDPATH %%J
call :ADDPATH %XMLSH%\bin\xmlsh-1.2.jar


GOTO :EOF

:ADDPATH
SET CLASSPATH=%1;%CLASSPATH%
GOTO :EOF
