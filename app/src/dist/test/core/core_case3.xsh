# core_case3.xsh
# multiline cases
. ../common

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

# simple variable setting cases

case A in 
  X) echo Failed should be A ;;
  A) _A=B ;;
esac

[ "$_A" = "B" ] || echo Failed variable not set

# [] globs
case 1b in 
  abc)  die "FAIL 'abc'" ;;
  1c) die "FAIL '1c'" ;;
  [123][ab]) echo Success ;;
  1b) die "FAIL should not hit" ;;
esac
 