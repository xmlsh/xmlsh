# Test cat command
import package posix=org.xmlsh.commands.posix

posix:rm -rf $TMPDIR/_xmlsh
posix:mkdir $TMPDIR/_xmlsh

# Create initial file
posix:cat < ../../samples/data/books.xml > $TMPDIR/_xmlsh/f1

cd $TMPDIR/_xmlsh

posix:cp f1 f2
xcmp f1 f2  && echo Success

posix:mkdir dir
posix:cp f1 dir
xcmp f1 dir/f1 && echo Success 

posix:mkdir dir2
posix:cp f1 f3
posix:cp f1 f4

# test copy many to dir 
posix:cp f1 f2 f3 f4 dir2
xcmp f1 dir2/f1 && echo Success 
xcmp f1 dir2/f2 && echo Success 
xcmp f1 dir2/f3 && echo Success 
xcmp f1 dir2/f4 && echo Success 

cd ..
posix:rm -rf $TMPDIR/_xmlsh
