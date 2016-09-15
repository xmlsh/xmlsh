# save test results
TEST=0
[ $# -gt 0 -a "x$1" = "x-test" ] && { TEST=1 ; X=".test"; shift ; }
[ -f $1 ] || { echo usage: $0 test.xsh ; exit 1 ;  }

set -location-format true
set -encoding utf8
set -indent +v +x -location

# unset all variables if possible - but its not yet


[ -f _out.txt ] && rm _out.txt
[ -f _err.txt ] && rm _err.txt

has_diff() {
  xwhich -n diff && xwhich -n head 
}


diff_text() {
   has_diff && { diff $1 $2 | head -10 ; }
}
diff_xml() {
   has_diff && { diff $1 $2 | head -10 ; }
}




log running $1
$1  > _out.txt 2> _err.txt
RET=$?
if [ $RET -ne 0 ] ; then
   echo $1 failed: return code: $RET
   exit $RET
fi     
echo $1 return code: $RET

if [ -f out/$1.out ] ; then 
	xcmp -b _out.txt out/$1.out 
	if [ $? -ne 0 ] ; then
		echo $1 out/$1.out different output
    diff_text _out.txt out/$1.out
    echo saving to out/$1.out$X
    cp _out.txt out/$1.out$X
  else
    echo $1 out/$1.out - same
	fi
	[ -f _out.txt ] && rm _out.txt

elif [ -f out/$1.xml ] ; then 
	xcmp -x -b _out.txt out/$1.xml 
	if [ $? -ne 0 ] ; then
		echo $1 out/$1.xml different output
		diff_xml _out.txt out/$1.xml
    echo saving to out/$1.xml$X
    cp  _out.txt out/$1.xml$X
   else
    echo $1 out/$1.xml - same 
	fi
	[ -f _out.txt ] && rm _out.txt

else
	echo $1 - no output to compare 
  echo saving to out/$1.out$X
  cp _out.txt out/$1.out$X

fi

if [ -f _err.txt -a -f out/$1.err ] ; then
	xcmp -b _err.txt out/$1.err 
	if [ $? -ne 0 ] ; then
		echo $1 out/$1.err different output
		diff_text _err.txt out/$1.err
     echo saving to out/$1.err$X
     cp _err.txt out/$1.err$X
  else 
    echo $1 out/$1.err - same
	fi
elif [ -s _err.txt ] ; then 
	echo Unexpected Errors in test
     echo saving to out/$1.err$X
     cp _err.txt out/$1.err$X
		
fi
	
[ -f _err.txt ] && rm _err.txt

