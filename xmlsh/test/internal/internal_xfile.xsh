# test of xfile command

#-b base
# "foo"
xfile -b foo.bar
xfile -b $PWD/foo.bar

#-n name
xfile -n foo.bar
xfile -n $PWD/foo.bar

# -d directory
D=$(xfile -d $PWD/foo.bar)
[ "$D" = "$PWD" ] && echo Success -d

# -a absolute path
P1=$(xfile -a foo.bar)
P2=$(xfile -a $PWD/foo.bar)
[ "$P1" = "P2" ] && echo Success -a

# -c cannonical path
P1=$(xfile -c ../foo.bar)
P2=$(xfile -c ../././././foo.bar)
[ "$P1" = "P2" ] && echo Success -c

# -e extension
xfile -e foo.bar
xfile -e $PWD/foo.bar
PS1=$(xfile -e foo.bar/spam)
[ -z "$PS1" ] || echo null extension failed

# get the path seperator 


# -N full name
PS1=$(xfile -N foo/bar/spam.x)
[ "$PS1" = "foo/bar/spam.x" ] || echo Full name failed


# -B basename including directory ( no extension )
PS1=$(xfile -B foo/bar/spam.xml)
[ "$PS1" = "foo/bar/spam" ] || echo Full Basename failed


