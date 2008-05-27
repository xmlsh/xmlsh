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
