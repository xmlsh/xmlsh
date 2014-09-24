# Extract Tests
import module org.xmlsh.xproc
declare namespace t="http://xproc.org/ns/testsuite" 
declare namespace p="http://www.w3.org/ns/xproc"
declare namespace c="http://www.w3.org/ns/xproc-step"
declare namespace err="http://www.w3.org/ns/xproc-error"


# Set global data directory
_BINDIR=$(xfile -d $0)
_ROOT=$(xfile -c $_BINDIR/..)
_OUT=$_ROOT/_test
[ -d $_OUT ] || mkdir -p $_OUT
_TEST=$1
_TEST_URI=http://tests.xproc.org/tests/$1


_TMP=$(xfile -B $_OUT/$_TEST)
[ -d $_TMP ] && rm -rf $_TMP
mkdir -p $_TMP
unset _BINDIR _ROOT _OUT

#echo extracting $1
cat < $_TEST_URI > $_TMP/test.xml
xread test < $_TEST_URI
tpipe=<[$test/t:test/t:pipeline]>

ws=<[ if( $test/t:test/@ignore-whitespace-differences eq 'false' ) then "" else "-b" ]>



title=<[$test/t:test/t:title/string()]>
if [ <[ exists( $tpipe/@href ) ]> ] ; then
    href=<[ $tpipe/@href/string() ]>
	cat < $(xuri $_TEST_URI $href)
else
    xpath -i $tpipe './node()' 
fi  > $_TMP/test.xpl

in=""
if [ <[ exists( $test/t:test/t:input ) ]> ] ; then 
	tinput=<[$test/t:test/t:input]>
	for t in $tinput ; do
		doc=$t
		if [ <[ exists( $t/t:document) ]> ] ; then 
		   doc=<[ $t/t:document ]> 
		fi
		if [ <[ exists( $t/@port ) and $t/@port != "source" ]> ] ; then
			port=<[ $t/@port/string() ]>
			in="$in ($port)<$_TMP/input.${port}.xml"
			xecho <[ $doc/node() ]> > $_TMP/input.${port}.xml
		else
			in="$in <$_TMP/input.xml"
			xecho <[ $doc/node() ]> > $_TMP/input.xml
		fi
	done
fi
if [ -z "$in" ] ; then
   in="< /dev/null";
fi 


if [ <[ exists( $test/t:test/t:output ) ]> ] ; then 
	toutput=<[$test/t:test/t:output]>
	set +indent 
	xecho <[ $toutput/node() ]> > $_TMP/expected.xml
	
	set -indent
fi

echo running $_TEST_URI
xproc2xmlsh -base $_TEST_URI < $_TMP/test.xpl > $_TMP/test.xsh
_TEST=$(xfile $_TMP test.xsh)


eval $_TEST  $in > $_TMP/output.xml 
_EXIT=$?


cd $_TMP
expected=$(cat expected.xml)


declare namespace "http://xproc.org/ns/testreport"
if [ $_EXIT -ne 0 ] ; then 
   xecho <[
	<fail uri="{$_TEST_URI}">
	   <title>{$title}</title>
	   <expected>{$expected}</expected>
	   <actual>Failed. Exit code: {$_EXIT}</actual>
	 </fail>
	]>
elif  xcmp $ws -x output.xml expected.xml  ; then
	xecho <[
	<pass uri="{$_TEST_URI}">
	   <title>{$title}</title>
	 </pass>
	]>	
else

    actual=$(cat output.xml)
    
	xecho <[
	<fail uri="{$_TEST_URI}">
	   <title>{$title}</title>
	   <expected>{$expected}</expected>
	   <actual>{$actual}</actual>
	 </fail>
	]>
	
fi > $_TMP/result.xml








