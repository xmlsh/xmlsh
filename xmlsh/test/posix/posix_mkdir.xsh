# Test mkdir command
import package posix=org.xmlsh.commands.posix

posix:rm -rf $TMPDIR/_xmlsh
posix:mkdir $TMPDIR/_xmlsh
[ -d $TMPDIR/_xmlsh ] || echo Failed to make _xmlsh

posix:mkdir $TMPDIR/_xmlsh/dir1
posix:mkdir -p $TMPDIR/_xmlsh/dir2/dir3/dir4/dir5
#xls $TMPDIR/_xmlsh
echo Success
posix:rm -rf $TMPDIR/_xmlsh
