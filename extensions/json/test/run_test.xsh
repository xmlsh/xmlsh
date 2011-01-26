# Run a single test and return 0 for success or 1 for failure

[ $# -ne 1 ] && exit 1

[ -f _out.txt ] && rm _out.txt
[ -f _err.txt ] && rm _err.txt


$1  > _out.txt 2> _err.txt
RET=$?
if [ $RET -ne 0 ] ; then
   echo $1 failed: return code: $RET
   exit $RET
fi     

base=$(xfile -b $1)

if [ -f out/${base}.out ] ; then 
	xcmp -b _out.txt out/${base}.out 
	if [ $? -ne 0 ] ; then
		echo $1 out/${base}.out different output
		exit 1
	fi
	[ -f _out.txt ] && rm _out.txt

elif [ -f out/${base}.xml ] ; then 
	xcmp -x -b _out.txt out/${base}.xml 
	if [ $? -ne 0 ] ; then
		echo $1 out/${base}.xml different output
		exit 1
	fi
	[ -f _out.txt ] && rm _out.txt

elif [ -f out/${base}.json ] ; then 
	j:jcmp  _out.txt out/${base}.json 
	if [ $? -ne 0 ] ; then
		echo $1 out/${base}.json different output
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
fi
	
[ -f _err.txt ] && rm _err.txt


exit 0
