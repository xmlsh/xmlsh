# test of xml2json
import module json=json
import commands posix
. ../common

[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh 
mkdir $TMPDIR/_xmlsh || die "cannot copy create tempdir" 
cp ../../samples/data/testjx.json  $TMPDIR/_xmlsh/j.json || die "cannot copy sample file" 
cd $TMPDIR/_xmlsh


json:json2xml -format jsonx  < j.json  > j0.xml   || die "error converting to xml" 
json:xml2json  -format jsonx < j0.xml > j0.json    || die "error converting to json" 
json:json2xml  -format jsonx < j0.json > j0x.xml  || die "error converting to xml" 
jdiff j.json j0.json  || message diff failed
xcmp j0.xml j0x.xml  || message diff failed

json:json2xml -format jxon  < j.json  > j1.xml   || die "error converting to xml" 
json:xml2json  -format jxon < j1.xml > j1.json    || die "error converting to json" 
json:json2xml -format jxon  < j1.json  > j1x.xml  || die "error converting to xml" 
jdiff j.json j1.json  || message diff failed
xcmp j1.xml j1x.xml   || message diff failed
diff j0.json j1.json  || message diff failed
echo Complete
cd ..
rm -rf $TMPDIR/_xmlsh
exit 0
