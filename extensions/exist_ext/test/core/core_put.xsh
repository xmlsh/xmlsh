# basic test of get.  More exaustive tets are run in the core_put test
#
. ../init


e:del $_TESTDIR


e:put -uri $_TESTDIR/test.xml <[ <elem/> ]>

e:get $_TESTDIR/test.xml
echo 
e:get $_TESTDIR | xquery -q '//*:resource/@name/string()'
e:del $_TESTDIR 
