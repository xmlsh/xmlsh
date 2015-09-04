# Test base command
import commands posix=posix
# requires rm in path
rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh


# Test 1 simple base64 encode and decode of "Hello World"

echo "Hello Word" | posix:base64 | posix:base64 -d 

# Use a known binary file for testing
posix:cp ../../lib/[Ss]axon*[hH][eE]*9*.jar $TMPDIR/_xmlsh/binary.dat
cd $TMPDIR/_xmlsh
xmd5sum -r binary.dat
posix:base64 -w binary.dat > binary.b64
posix:base64 -d binary.b64 > b2.dat
xmd5sum -r b2.dat

cd ..
rm -rf $TMPDIR/_xmlsh
