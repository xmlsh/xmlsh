# Test of nesting and newlines in function args

f1()
{
  case $* in 
  q) return 0 ;;
  *) echo $* ;;
  esac
}

f1 a
f1 q