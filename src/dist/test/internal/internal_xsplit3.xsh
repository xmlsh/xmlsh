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
xmkpipe -xml x

while xread a ; do 
  xecho <[ $a/child/@id/string() ]>
done <(x)  &
xsplit -n -stream x test.xml


xmkpipe -close x

wait
echo Done


cd ..
rm -rf $TMPDIR/_xmlsh


