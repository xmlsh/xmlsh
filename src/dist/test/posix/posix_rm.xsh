# Test rm
import commands posix=posix

posix:rm -rf $TMPDIR/_xmlsh
posix:mkdir $TMPDIR/_xmlsh

# Create initial file
posix:cat < ../../samples/data/books.xml > $TMPDIR/_xmlsh/f1

cd $TMPDIR/_xmlsh

posix:cp f1 f2

[ -f f1 -a -f f2 ] || echo Fail invalid start condition
posix:rm f1
[ -f f1 ] || echo Success f1 deleted
posix:rm f2 
[ -f f2 ]|| echo Success f2 deleted

cd ..
posix:rm -rf $TMPDIR/_xmlsh
