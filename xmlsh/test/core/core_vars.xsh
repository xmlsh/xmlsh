# core_vars.xsh
# test variables

if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	echo Temp directory required TMPDIR: $TMPDIR
	exit 1;
fi





# turn off indentation for this test
set +indent


# clear just in case they were exported
unset A AA

echo NO $A variable
echo NO $AA variable
echo NO ${A} variable
echo NO ${AA} variable

A=foo
AA=bar

echo $A variable
echo $AA variable
echo ${AA} variable

set A $A "$A"

if [ $3 != foo -o $3 != "foo" ] ; then
	echo $3 should be foo
fi


# Test $()
A=$(echo '<foo/>' | xcat)
echo $A 


# Test $(<)
F=$TMPDIR/test_core_vars.txt
echo foo bar > $F
echo $(<$F)
echo spam bletch >> $F
A=$(<$F)
echo $A

rm $F
F=$TMPDIR/test_core_vars.xml
# Test $<(<)
echo <[<foo attr="an attr"><bar>text</bar></foo>]> > $F
A=$<(<$F)
echo $A
rm $F




# Test local variable setting for simple commands
unset A
A=B eval 'echo A is $A'
[ -z "$A" ] || echo A should not be set

# Test that $< passes through 
echo "$<"

# Test that "$(cmd)" passes trough unchanged
# NOTE: differes from bsh/ksh
echo "$(cmd)"

# Test that @ can be part of a name
A=xcc://foo:bar@home:8003/5MCC
echo $A

# Test that set -option doesnt clear args
set ARG
[ "$1" = "ARG" ] || echo Arg1 should be set
set +indent
set -indent
[ "$1" = "ARG" ] || echo Arg1 should still be set

# Test echo $
a=$(echo $)
[ "$a" = "$" ] || echo Failed echo of $

# Test that variable assignment concatenates property 
a=pre
b=post

[ $a/$b = "pre/post" ] || echo Failed variable concat


exit 0