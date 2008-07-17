# core_case.xsh
# Test of case

echo test1
case foo in 
	foo) echo success ;;
	bar) echo failure ; exit 1 ;;
esac

echo test2
case foo in 
	ooo) echo failure ; exit 1;;
	f*)	 echo success ;;
esac

echo test3
case foo in 
	?oo|ooo) echo success ;;	# should hit ?oo first 
	f*)	 echo failure ; exit 1 ;;
	
esac

# Test IO to case
[ -d $TMPDIR/_xmlsh ] || mkdir $TMPDIR/_xmlsh
T1=$TMPDIR/_xmlsh/case1.tmp
T2=$TMPDIR/_xmlsh/case2.tmp

echo test4

echo data > $T1

case foo in 
	foo) read d ; echo data is $d ;;
	bar) echo failure ; exit 1 ;;
esac < $T1 > $T2

cat $T2

rm $T1 $T2
