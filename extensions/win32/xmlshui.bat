@setlocal
@echo off
@set CLASSPATH=%XMLSH%\bin\xmlsh-1.2.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\log4j-1.2.7.jar

@REM Choose which saxon you wish to use
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9he.jar

@REM @set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9ee.jar
@REM @set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9pe.jar

@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\xercesimpl.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\woodstox-core-asl-4.0.3.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\stax2-api-3.0.1.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\stax-utils.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\jing.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\trang.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\xom-1.2.6.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\tagsoup-1.2.jar
@REM Uncomment below to use jline input editing
@REM @set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\jline-0.9.94.jar
@start javaw -XX:+UseConcMarkSweepGC  -Xmx1024m -Xms256m org.xmlsh.sh.ui.XShell   %*