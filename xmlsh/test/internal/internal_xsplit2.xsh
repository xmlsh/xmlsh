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

xsplit -l test.xml 

cd ..
rm -rf $TMPDIR/_xmlsh


