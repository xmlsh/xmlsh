# Test cp -r 
import commands posix=posix

posix:rm -rf $TMPDIR/_xmlsh
posix:mkdir $TMPDIR/_xmlsh

# Create initial file
posix:cat < ../../samples/data/books.xml > $TMPDIR/_xmlsh/f1

cd $TMPDIR/_xmlsh

posix:cp f1 f2
posix:cp f1 f3

posix:mkdir dir1
posix:cp f[123] dir1
posix:mkdir dir1/dir2
posix:cp f1 dir1/dir2/f4
posix:cp f1 dir1/dir2/f5
posix:cp f1 dir1/dir2/f6

posix:mkdir dir3
posix:cp -r dir1 dir3


xcmp f1 dir3/dir1/f1 && echo Success 
xcmp f1 dir3/dir1/f2 && echo Success 
xcmp f1 dir3/dir1/f3 && echo Success 
xcmp f1 dir3/dir1/dir2/f4 && echo Success 
xcmp f1 dir3/dir1/dir2/f5 && echo Success 
xcmp f1 dir3/dir1/dir2/f6 && echo Success 

posix:cp -r dir1 dir4

xcmp f1 dir4/f1 && echo Success 
xcmp f1 dir4/f2 && echo Success 
xcmp f1 dir4/f3 && echo Success 
xcmp f1 dir4/dir2/f4 && echo Success 
xcmp f1 dir4/dir2/f5 && echo Success 
xcmp f1 dir4/dir2/f6 && echo Success 

#posix:ls -R
cd ..
posix:rm -rf $TMPDIR/_xmlsh
