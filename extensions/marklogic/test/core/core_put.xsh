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
		xecho <[ <test>{$j}</test> ]> > $i/${j}.xml
	done
done

ml:put -r -baseuri /test/ -m 20 -maxthreads 5 *
ml:list -r /test/
ml:deldir /test/

echo text | ml:put -t -uri /test.txt
ml:get -t /test.txt
ml:del /test.txt


cd $P
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest


