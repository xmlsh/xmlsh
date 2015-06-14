# core_expandsub.xsh
# test expanding of subprocess aka $()


a=$(echo foo)
echo Should be foo: $a

# a should be "a b c" 
a=$(echo a; echo b ; echo c;)
echo Should be 1 : ${#a}
xtype -s {$a}  # "string"
echo exanding
# evaluating it should produce 3
xtype -s $a

b=$(echo a1 ; echo b1 ; echo c1 ; )
for c in $b ; do
	echo Arg $c
done

# Test return values in $()
A=$(true)  || echo FAIL true should be success
A=$(false) || echo Success expected false got false
A=$(true;false)
echo retval is $? should be 1
unset A

# Test backticks
A=`echo foo1`
[ "$A" = "foo1" ] || echo FAIL should be foo1
 
