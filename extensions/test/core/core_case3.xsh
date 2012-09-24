# core_case3.xsh
# multiline cases

case foo in 
bar)	
	echo SNH	
	;;
foo)
	case "a()" in
	"b()" ) 
		echo FAIL
		;;
	"a()" )
		echo SUCCESS
		;;
	*) 
		echo FAIL 
		;;
	esac
	;;
esac

