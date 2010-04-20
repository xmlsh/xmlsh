# Test base command
import package posix=org.xmlsh.commands.posix

rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh


# Test 1 simple base64 encode and decode of "Hello World"

echo "Hello Word" | posix:base64 | posix:base64 -d 

# Use a known binary file for testing
posix:cp ../../lib/saxon9he.jar $TMPDIR/_xmlsh/binary.dat
cd $TMPDIR/_xmlsh
xmd5sum binary.dat
posix:base64 -w binary.dat > binary.b64
posix:base64 -d binary.b64 > b2.dat
xmd5sum b2.dat

cd ..
rm -rf $TMPDIR/_xmlsh
