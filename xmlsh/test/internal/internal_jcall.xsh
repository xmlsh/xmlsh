# test of jcall command

# jcall into xpwd 
# xpwd should always return the same value as $PWD

XP=$<(jcall org.xmlsh.commands.xpwd)
XF=$(xfile $XP)
[ "$PWD" = "$XF" ] && echo success jcall
exit 0
