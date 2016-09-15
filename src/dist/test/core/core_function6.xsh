# Test of nesting and newlines in function args

f1()
{
  while true ; 
  do 
#	  local x=y
#	  local foo;
	  foo=<[ 1 ]>
	  case $* in 
	  q) return 0 ;;
	  h) echo help ;;
	  *) echo $* ;;
	  
  esac
  break;
  done
}

f1 a
f1 q && f1 a