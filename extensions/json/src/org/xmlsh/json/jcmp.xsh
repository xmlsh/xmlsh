# compare 2 JSON files
if [ $# -ne 2 ] ; then 
	echo Usage: jcmp file1.json file2.json
	exit 1;
fi

T1=$(mktemp -suffix .xml)
T2=$(mktemp -suffix .xml)

json2xml < $1 > $T1
json2xml < $2 > $T2

xcmp -x -b $T1 $T1
_RET=$?

rm -f $T1 $T2
exit $_RET