# test of xsplit

D=$PWD
[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# Create a test file to split
xquery -n -f - > test.xml <<EOF
document {
<root>
{
for $i in 0 to 99
return
  <child id="{$i}"> 
     <text>Some text here</text>
     <node>Node: {$i}</node>
  </child>
}
</root>
}
EOF

xsplit test.xml 

files=$<(xls x*.xml)
echo Number of files: <[count($files//file)]>
# check a file to see if its right
xread a10 < x10.xml
echo <[$a10/root/child/node/text()]>

rm -f x*.xml

# Try with a different prefix and suffix and number of files

xsplit -c 10 -p foo -s bar -w root2 -e .x test.xml
files=$<(xls foo*.x)
echo Number of files: <[count($files//file)]>

xread a2 < foo2bar.x
echo <[$a2/root2/child[2]/node/text()]>

# test with wrapping with an XML expression

rm -f *.x
xsplit -c 10 -w wrap test.xml
files=$<(xls x*.xml)
echo Number of files: <[count($files//file)]>

xread a3 < x2.xml
echo <[$a3/wrap/child[2]/node/text()]>

cd $D
rm -rf $TMPDIR/_xmlsh


