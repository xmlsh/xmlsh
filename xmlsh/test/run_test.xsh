# Run a single test and return 0 for success or 1 for failure

set -encoding utf8

[ $# -ne 1 ] && exit 1

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


if [ -f out/${1}.out ] ; then 
	xcmp -b _out.txt out/${1}.out 
	if [ $? -ne 0 ] ; then
		echo $1 out/${1}.out different output
		diff_text _out.txt out/$1.out
		exit 1
	fi
	[ -f _out.txt ] && rm _out.txt

elif [ -f out/${1}.xml ] ; then 
	xcmp -x -b _out.txt out/${1}.xml 
	if [ $? -ne 0 ] ; then
		echo $1 out/${1}.xml different output
		diff_xml _out.txt out/$1.xml
		exit 1
	fi
	[ -f _out.txt ] && rm _out.txt

else
	echo $1 - no output to compare 
 	exit 2
fi

if [ -f _err.txt -a -f out/${1}.err ] ; then
	xcmp -b _err.txt out/${1}.err 
	if [ $? -ne 0 ] ; then
		echo $1 out/${1}.err different output
		exit 1
	fi
elif [ -s _err.txt ] ; then 
	echo Unexpected Errors in test
	exit 1  
		
fi
	
[ -f _err.txt ] && rm _err.txt


exit 0
