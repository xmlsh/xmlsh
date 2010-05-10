# core_expandsub.xsh
# test expanding of subprocess aka $()


a=$(echo foo)
echo Should be foo: $a

a=$(echo a; echo b ; echo c;)
echo Should be 3 : ${#a}

b=$(echo a1 ; echo b1 ; echo c1 ; )
for c in $b ; do
	echo Arg $c
done

# Test return values in $()
A=$(true)  || echo FAIL true should be success
A=$(false) || echo Success expected false
A=$(true;false)
echo retval is $?
unset A

# Test backticks
A=`echo foo1`
[ "$A" = "foo1" ] || echo FAIL should be foo1
 
