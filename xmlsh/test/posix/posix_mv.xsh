# Test mv command
import package posix=org.xmlsh.commands.posix

posix:rm -rf $TMPDIR/_xmlsh
posix:mkdir $TMPDIR/_xmlsh

# Create initial file
posix:cat < ../../samples/data/books.xml > $TMPDIR/_xmlsh/f1

cd $TMPDIR/_xmlsh

[ -f f1 ] || echo Fail f1 not found
posix:mv f1 f2
[ -f f1 ] && echo Fail f1 still there
[ -f f2 ] || echo Fail f2 not found

posix:cp f2 f3 
posix:cp f2 f4 
posix:mkdir dir
posix:mv f2 f3 f4 dir
[ -f dir/f2 ] && echo Success f2
[ -f dir/f3 ] && echo Success f3 
[ -f dir/f4 ] && echo Success f4

[ -f f1 ] && echo Fail f1 still there
[ -f f2 ] && echo Fail f2 still there
[ -f f3 ] && echo Fail f3 still there
[ -f f4 ] && echo Fail f4 still there



cd ..
posix:rm -rf $TMPDIR/_xmlsh
