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
@REM for http command
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\httpclient-4.2.3.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\httpcore-4.2.2.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\commons-codec-1.6.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\commons-logging-1.1.1.jar
@REM JSON support
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\json-path-0.8.2.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\json-smart-1.1.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\commons-io-2.1.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\commons-lang-2.6.jar

@REM for tagsoup
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\tagsoup-1.2.jar

@REM Uncomment below to use jline input editing
@REM @set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\jline-0.9.94.jar
@java -XX:+UseConcMarkSweepGC  -Xmx1024m -Xms256m org.xmlsh.sh.shell.Shell   %*
