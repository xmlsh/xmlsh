# test of jcall command

# jcall into xpwd 
# xpwd should always return the same value as $PWD

XP=$(jcall org.xmlsh.commands.jcall Test)

[ "Test" = "$XP" ] && echo success jcall
exit 0
