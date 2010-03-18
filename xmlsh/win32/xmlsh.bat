@setlocal
@set CLASSPATH=%XMLSH%\bin\xmlsh-1.0.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\log4j-1.2.7.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9he.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\xercesimpl.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\woodstox-core-asl-4.0.3.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\stax2-api-3.0.1.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\stax-utils.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\jing.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon.jar
@REM Uncomment below to use jline input editing
@REM @set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\jline-0.9.94.jar
@java -XX:+UseConcMarkSweepGC  -XX:-PrintGC -Xmx1024m -Xms256m org.xmlsh.sh.shell.Shell   %*
