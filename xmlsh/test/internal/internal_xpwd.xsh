# test of xpwd command

# xpwd should always return the same value as $PWD

XP=$<(xpwd)
XF=$(xfile $XP)

[ "$PWD" = "$XF" ] && echo success xpwd
exit 0