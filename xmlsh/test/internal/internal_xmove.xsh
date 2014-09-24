# test of xsplit
import commands posix

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

xsplit -n test.xml 
rm test.xml

# Now move all files to just their numbered names
xmove -q -x /child/@id *.xml
xls | xquery '
<files>{
	for $f in //file 
	order by $f/@name 
	return <file>{$f/@name}</file>}
</files>'

cd $D
rm -rf $TMPDIR/_xmlsh