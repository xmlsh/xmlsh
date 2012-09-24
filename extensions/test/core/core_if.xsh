# core_if.xsh
# Test if elif else expressions


# one-line if
if false ; then exit 1 ; fi


# single statement multi line if
if true ; then
	echo Single statement 
else
	exit 1
fi


# Multi statement with semi's
if true ; then
	echo Multi ;
	echo Statement ;
else
	exit 1
fi


# Test null statement + elif
if true ; then 
	:
elif false ; then
	exit 1 ; 
else
	echo Success 
fi

# Test redirection 
[ -d $TMPDIR/_xmlsh ] || mkdir $TMPDIR/_xmlsh
T1=$TMPDIR/_xmlsh/for1.tmp
T2=$TMPDIR/_xmlsh/for2.tmp

echo line1 > $T1

line=initial
if true ; then read line ; fi  < $T1
echo line is $line

if true ; then echo line2 is $line ; fi  > $T2
cat $T2

rm $T1 $T2



exit 0

