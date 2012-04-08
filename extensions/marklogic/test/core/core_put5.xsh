# Create a simple xquery file in /Modules/test.xquery 
. ../init


P=$PWD

# Test recursive put
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest
mkdir $TMPDIR/_mltest
cd $TMPDIR/_mltest

	for j in <[ 1 to 10 ]> ; do 
	    f=${j}.xml
		xecho <[ <test>{$j}</test> ]> > $f 
		echo $f
done  |
ml:put -r -baseuri /test/ -m 3 -maxthreads 2  -delete -f -

ml:deldir /test/
xls .

cd $P
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest


