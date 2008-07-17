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
