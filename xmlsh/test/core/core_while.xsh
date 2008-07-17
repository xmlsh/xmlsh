# core_while.xsh

while false ; do
  echo SHN 
done
# core_while.xsh
# tests of while

while false ; do echo SNH ; done

RET=1
echo before while
while [ -z "$X" ] ; do
   echo while RET = $RET
   X="A"
   RET=0
   echo while2 RET = $RET   
done

# Until
until true ; do
	echo Failed
	exit 1
done

X=0

until [ $X -eq 1 ] ; do
	echo Until $X
	X=1
done

echo X = $X - should be 1
echo RET = $RET - should be 0




# Test redirection 
[ -d $TMPDIR/_xmlsh ] || mkdir $TMPDIR/_xmlsh
T1=$TMPDIR/_xmlsh/while1.tmp
T2=$TMPDIR/_xmlsh/while2.tmp

echo line1 > $T1
echo line2 >> $T1
echo line3 >> $T1

while read line ; do
	echo line is $line
done < $T1 > $T2
echo data is
cat $T2
rm $T1 $T2




exit 0