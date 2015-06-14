# test importing a package
import package ex=org.xmlsh.experimental.commands
ex:xsysinfo | xpath 'count(/systeminfo)'
