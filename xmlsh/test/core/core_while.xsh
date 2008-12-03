# core_while.xsh

while false ; do
  echo SHN 
done
# core_while.xsh
# tests of while

while false ; do echo SNH ; done


# test setting of vars in loop
unset X
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


echo 2 level while
X=<[0]>
while [ $X -lt 3 ] ;  do
   Y=<[0]>
   while [ $Y -lt 3 ] ; do 
      echo -n "$X$Y "
      Y=<[$Y + 1]>
   done
   echo
   X=<[$X + 1 ]>
done


echo 2 level while with break 
X=<[0]>
while [ $X -lt 3 ] ;  do
   Y=<[0]>
   while [ $Y -lt 3 ] ; do 
      echo -n "$X$Y "
      [ $Y -eq 1 ] && break ;
      Y=<[$Y + 1]>
   done
   echo
   X=<[$X + 1 ]>
done



echo 2 level while with break 2
X=<[0]>
while [ $X -lt 3 ] ;  do
   Y=<[0]>
   while [ $Y -lt 3 ] ; do 
      echo -n "$X$Y "
      [ $Y -eq 1 ] && break 2;
      Y=<[$Y + 1]>
   done
   echo
   X=<[$X + 1 ]>
done
echo



echo 2 level while with continue 
X=<[0]>
while [ $X -lt 3 ] ;  do
   Y=<[0]>
   while [ $Y -lt 3 ] ; do 
      echo -n "$X$Y "
      Y=<[$Y + 1]>
      [ $Y -eq 1 ] && continue ;
      [ $Y -eq 1 ] && echo Failed continue ;
   done
   echo
   X=<[$X + 1 ]>
done
echo

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