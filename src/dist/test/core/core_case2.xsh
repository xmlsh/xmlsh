# core_case2.xsh
# Test of case

for i in update insert delete ; do 
	echo $i 
	case $i in
	   update|insert) echo update/insert ;;
	   delete) echo delete ;;
	esac
done

# Test quoted strings

case "foo bar" in 
     "foo" | bar ) echo FAIL SNH ;;
     "foo bar spam"  ) echo FAIL SNH ;;
     "foo bar"  ) echo Success ;;
     *) echo FAIL SNH ;;
esac

# Test expanded vars
a="foo"
b="bar"

case "foo bar" in 
     "foo" | bar ) echo FAIL SNH ;;
     "foo bar spam"  ) echo FAIL SNH ;;
  	 "$a $b" | "spam" ) echo Success ;;
  	 *) echo FAIL SNH ;;
esac



