@setlocal
@echo off
IF NOT DEFINED XMLSH GOTO NOX 
SET CP=%XMLSH%\bin\*;%XMLSH%\lib\*
SET _JVMDEFS=-Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl
IF NOT DEFINED XMLSH_JVMOPTS SET XMLSH_JVMOPTS=-XX:+UseConcMarkSweepGC -Xmx1024m -Xms256m 
@start javaw %_JVMDEFS% -cp %CP% %XMLSH_JVMOPTS% org.xmlsh.sh.ui.XShell   %*
exit /b 0

:NOX
echo XMLSH not defined
exit /b 1