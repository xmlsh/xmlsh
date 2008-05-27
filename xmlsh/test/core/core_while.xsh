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


echo RET = $RET
exit 0