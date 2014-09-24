# test of get recursive

P=$PWD

#  Setup a recursive set of files to get
#

for i in <[ 1 to 10 ]> ; do
	for j in <[ 1 to 100 ]> ; do 
		ml:put -uri /test/xml/$i/${j}.xml <[ <test>{$i,$j}</test> ]> 
	done
done


[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest
mkdir $TMPDIR/_mltest
cd $TMPDIR/_mltest


ml:get -r -baseuri /test/ -maxfiles 4 -maxthreads 5  -d . xml/

ls -R .

ml:deldir /test/

cd $P
[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest


