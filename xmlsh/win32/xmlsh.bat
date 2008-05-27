@set CLASSPATH=%XMLSH%\bin\xmlsh.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\log4j-1.2.7.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9-dom.jar
@set CLASSPATH=%CLASSPATH%;%XMLSH%\lib\saxon9-s9api.jar
@java -Xmx1024m -Xms256m org.xmlsh.sh.shell.Shell   %*
