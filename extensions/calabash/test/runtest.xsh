# Extract Tests
declare namespace t="http://xproc.org/ns/testsuite" 
declare namespace p="http://www.w3.org/ns/xproc"
declare namespace c="http://www.w3.org/ns/xproc-step"
declare namespace err="http://www.w3.org/ns/xproc-error"


# Set global data directory
_BINDIR=$(xfile -d $0)
_ROOT=$(xfile -c $_BINDIR)
_OUT=$_ROOT/_out
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


###################################################################
# Parse Inputs
###################################################################

in=""
args=""
if [ <[ exists( $test/t:test/t:input ) ]> ] ; then 
	tinput=<[$test/t:test/t:input]>
	for t in $tinput ; do
		doc=$t
		if [ <[ exists( $t/t:document) ]> ] ; then 
		
		   # Only test for elements inside t:document to determine wraps
		   if [ <[ count($t/t:document/*) gt 1 ]> ] ; then 
		       # reserialize the document contents to get rid of extra namespaces
		      contents=()
		      for d in <[ $t/t:document/* ]> ; do 
		         xecho -n $d | xread c 
		         contents=<[ $contents , $c ]>
		       done
		       doc=<[ <wrap>{$contents}</wrap> ]> 
		       args="-iw"
		   else
		       # take all child nodes not just elements 
		       doc=<[ $t/t:document/node()  ]>
		   fi

		else
		   doc=<[ $t/node() ]>
		fi
		if [ <[ exists( $t/@port ) and $t/@port != "source" ]> ] ; then
			port=<[ $t/@port/string() ]>
			in="$in ($port)<$_TMP/input.${port}.xml"
			xecho -n $doc > $_TMP/input.${port}.xml
		else
			in="$in <$_TMP/input.xml"
			xecho -n $doc > $_TMP/input.xml
		fi
	done
fi
if [ -z "$in" ] ; then
   args="$args -n"
fi 


###################################################################
# Parse Outputs
###################################################################

out=""
doc=()
if [ <[ exists( $test/t:test/t:output ) ]> ] ; then 
	toutput=<[$test/t:test/t:output]>
	for t in $toutput ; do
		doc=$t
	
	    if [ <[ exists( $t/t:document) ]> ] ; then 
	     
		    if [ <[ count($t/t:document/*) gt 1 ]> ] ; then  
	    
		       # reserialize the document contents to get rid of extra namespaces
		      contents=()
		      for d in <[ $t/t:document/* ]> ; do 
		         xecho -n $d | xread c 
		         contents=<[ $contents , $c ]>
		       done
		       doc=<[ <wrap>{$contents}</wrap> ]> 
		      args="$args -ow"
	       else
 			   # take all child nodes not just elements 
		       doc=<[ $t/t:document/node()  ]>
	       fi

		else
		  doc=<[ $t/node() ]>
    	fi
    	set +indent
    	if [ <[ exists( $t/@port ) and $t/@port != "result" ]> ] ; then
			port=<[ $t/@port/string() ]>
			out="$out ($port)>$_TMP/output.${port}.xml"
			xecho -n $doc > $_TMP/expected.${port}.xml
		else
			out="$out >$_TMP/output.xml"
			xecho -n $doc > $_TMP/expected.xml
		fi
		set -indent
		
    done
fi

###################################################################
# Parse Options
###################################################################
for opt in <[ $test/t:test/t:option ]> ; do 
  name=<[$opt/@name/string()]>
  value=<[$opt/@value/string()]>
  args="$args -o ${name}=${value}"
done


###################################################################
# Run it !
###################################################################

echo running $_TEST_URI
set +indent
eval xproc -base $_TEST_URI $args $_TMP/test.xpl $in $out 2>$_TMP/error.xml
set -indent

# check for error 
if [ $? -ne 0 ] ; then 
	if [ <[ exists($test/t:test/@error ) ]> ] ; then 
		# Error expected 
		cp $TMP/error.xml $TMP/result.xml
		_EXIT=0
	else
		_EXIT=1
	fi
	
else

	_EXIT=0

fi

cd $_TMP



declare namespace "http://xproc.org/ns/testreport"

# if xproc failed
if [ $_EXIT -ne 0 ] ; then 
   expected=$(cat expected.xml)
   xecho <[
	<fail uri="{$_TEST_URI}">
	   <title>{$title}</title>
	   <expected>{$expected}</expected>
	   <actual>Failed. Exit code: {$_EXIT}</actual>
	 </fail>
	]>
	
# if failed but failure was expected
elif [ <[ exists($test/t:test/@error ) ]> ] ; then

	xecho <[
	<pass uri="{$_TEST_URI}">
	   <title>{$title}</title>
	 </pass>
	]>	

# if output and expected are 0 len
elif [ -e output.xml -a ! -s output.xml  ] && [ -e expected.xml -a ! -s expected.xml  ] ; then
	xecho <[
	<pass uri="{$_TEST_URI}">
	   <title>{$title}</title>
	 </pass>
	]>
elif xcmp $ws -x output.xml expected.xml  ; then
	xecho <[
	<pass uri="{$_TEST_URI}">
	   <title>{$title}</title>
	 </pass>
	]>	
else

    actual=$(cat output.xml)
    expected=$(cat expected.xml)
	xecho <[
	<fail uri="{$_TEST_URI}">
	   <title>{$title}</title>
	   <expected>{$expected}</expected>
	   <actual>{$actual}</actual>
	 </fail>
	]>
	
fi > $_TMP/result.xml








