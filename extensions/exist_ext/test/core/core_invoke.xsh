#
. ../init

echo "1 to 5,'hi',<elem/>" | e:put -uri $_TESTDIR/run.xquery -q
e:invoke $_TESTDIR/run.xquery
e:del $_TESTDIR

