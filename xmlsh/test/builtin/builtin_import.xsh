# test importing a package
#import java ../../bin/xmlsh-test.jar
import package ex=org.xmlsh.experimental.commands
ex:xsysinfo | xpath 'count(/systeminfo)'
