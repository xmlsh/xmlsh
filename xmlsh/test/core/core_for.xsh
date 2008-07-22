# core_for.xsh
# tests of for

for a in a b c ; do
	echo $a ;
done

for a in a b c ; do echo $a ; done

# test leftover variable
echo After for a is $a

set xa xb xc
for a ; do echo $a ; done



# 2 level for
for a in a b c ; do
   for  b in c d e ; do
      echo -n "$a$b "
   done
   echo
done

echo 1 level break
for a in a b c ; do
 [ $a = "b" ] && break ;
 echo a is $a
 [ $a != "a" ] && echo Failed  break
done

echo 1 level continue
for a in a b c ; do
 [ $a = "b" ] && continue ;
 [ $a = "b" ] && echo Failed continue 
 echo a is $a
done


echo 2 level for with break 1
for a in a b c ; do
   for  b in c d e ; do
       echo -n "$a$b "
       [ $b = "d" ] && break 1;
       [ $b = "d" ] && echo Failed level 1 break
   done
   echo
done



echo 2 level for with break 2
for a in a b c ; do
   for  b in c d e ; do
       echo -n "$a$b "
       [ $b = "d" ] && break 2;
       [ $b = "d" ] && echo Failed level 1 break
   done
   echo Failed level 2 break 
done
echo



echo 2 level for with continue 1
for a in a b c ; do
   for  b in c d e ; do
       echo -n "$a$b "
       [ $b = "d" ] && continue 1;
       [ $b = "d" ] && echo Failed level 1 continue
   done
   echo
done



echo 2 level for with continue 2
for a in a b c ; do
   for  b in c d e ; do
       echo -n "$a$b "
       [ $b = "d" ] && continue 2;
       [ $b = "d" ] && echo Failed level 1 continue
   done
   echo Failed continue
done
echo
# test IO redirection

[ -d $TMPDIR/_xmlsh ] || mkdir $TMPDIR/_xmlsh
T1=$TMPDIR/_xmlsh/for1.tmp
T2=$TMPDIR/_xmlsh/for2.tmp

echo line1 > $T1
echo line2 >> $T1
echo line3 >> $T1

for a in a b c  ; do
   read line 
   echo $line
done < $T1 > $T2

echo Output is 
cat $T2

rm $T1 $T2 
