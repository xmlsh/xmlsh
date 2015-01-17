# test of jcall command

# jcall into xpwd 
# xpwd should always return the same value as $PWD
import module java
XP=$(jcall org.xmlsh.modules.java.jcall Test)

[ "Test" = "$XP" ] && echo success jcall

# Try a jcall with an exit
jcall org.xmlsh.modules.java.jcall exit
echo exit status is $?

exit 0
