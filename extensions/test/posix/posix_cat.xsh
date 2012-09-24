# Test cat command
import package posix=org.xmlsh.commands.posix

rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh

# Create initial file
posix:cat < ../../samples/data/books.xml > $TMPDIR/_xmlsh/f1

cd $TMPDIR/_xmlsh


# test from stdin
posix:cat < f1 > f2
xcmp f1 f2  && echo Success

# test in pipe
posix:cat < f1 | posix:cat | posix:cat > f2
xcmp f1 f2  && echo Success

# test from 1 file
posix:cat f1 > f2 
xcmp f1 f2  && echo Success


posix:cat f1 >>f2
posix:cat f1 >>f2
posix:cat f1 f1 f1 > f3


xcmp f2 f3  && echo Success
posix:cat f1 >>f2

xcmp -n f2 f3  || echo Success compare differs

posix:rm f1 f2 f3
cd ..
rm -rf $TMPDIR/_xmlsh
