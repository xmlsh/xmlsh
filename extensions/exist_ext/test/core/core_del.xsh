#
. ../init

e:del $_TESTDIR

e:put -uri $_TESTDIR/test.xml <[ <elem/> ]>
e:get $_TESTDIR | xquery -q '//*:resource/@name/string()'
echo

if e:get $_TESTDIR/test.xml ; then  
	echo Successfully deleted test file $_TESTDIR/test.xml
else
	echo Failed to delete $_TESTDIR/test.xml
fi

e:del $_TESTDIR

e:get $_TESTDIR || 
{
	echo Successfully deleted test directory $_TESTDIR
}  
 