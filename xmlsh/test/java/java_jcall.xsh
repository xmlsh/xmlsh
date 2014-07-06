# test of jcall command

# jcall into xpwd 
# xpwd should always return the same value as $PWD
import commands java
XP=$(jcall org.xmlsh.commands.java.jcall Test)

[ "Test" = "$XP" ] && echo success jcall

# Try a jcall with an exit
jcall org.xmlsh.commands.java.jcall exit
echo exit status is $?

exit 0
