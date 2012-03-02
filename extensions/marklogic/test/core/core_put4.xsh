# Create a simple xquery file in /Modules/test.xquery 
. ../init
ml:put -uri /test1.xml <[ <foo>bar</foo> ]>
ml:get /test1.xml

P=$PWD

# Test recursive put
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest
mkdir $TMPDIR/_mltest
cd $TMPDIR/_mltest

for i in <[ 1 to 5 ]> ; do
	mkdir $i
	for j in <[ 1 to 10 ]> ; do 
	    f=$i/${j}.xml
		xecho <[ <test>{$j}</test> ]> > $f 
		echo $f
	done
done  |

ml:put -r -baseuri /test/ -m 3 -maxthreads 2  -f -
ml:list -r /test/
ml:deldir /test/


cd $P
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest


