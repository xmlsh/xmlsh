# Test streaming 
. ../init


P=$PWD

# Test recursive put
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest
mkdir $TMPDIR/_mltest
cd $TMPDIR/_mltest


xmkpipe -xml x

ml:put -r -baseuri /test/ -m 3 -maxthreads 2  -uri 'test{seq}.xml' -stream x &

for j in <[ 1 to 10 ]> ; do 
	    
		xecho <[ <test>{$j}</test> ]>
		
done >(x)

xmkpipe -close x;

wait ; 

ml:list /test/

ml:deldir /test/



