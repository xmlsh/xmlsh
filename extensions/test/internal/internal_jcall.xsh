# test of jcall command

# jcall into xpwd 
# xpwd should always return the same value as $PWD

XP=$(jcall org.xmlsh.commands.internal.jcall Test)

[ "Test" = "$XP" ] && echo success jcall

# Try a jcall with an exit
jcall org.xmlsh.commands.internal.jcall exit
echo exit status is $?

exit 0
