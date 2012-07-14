#
XMODPATH=/usr/local/xmlsh
. ../init


e:del $_TESTDIR


e:put -uri $_TESTDIR/test.xml <[ <elem/> ]>
e:get $_TESTDIR/test.xml
echo
echo "Hello World" | e:put -uri $_TESTDIR/test.txt
e:get  $_TESTDIR/test.txt

e:del $_TESTDIR


[ -d $TMPDIR/_mltest ] && rm -rf $TMPDIR/_mltest
mkdir $TMPDIR/_mltest
cd $TMPDIR/_mltest

for j in <[ 1 to 10 ]> ; do 
	xecho <[ <test>{$j}</test> ]> > ${j}.xml
done


e:put -baseuri $_TESTDIR *.xml
e:get $_TESTDIR | xquery -q 'for $n in //*:resource/@name/string() order by $n return $n'
e:del $_TESTDIR
