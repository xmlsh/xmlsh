# core_array.xsh
# test array notation
. ../common
a=()
echo loc() ${#a}
a=(foo)
echo loc() ${#a}
a=(foo bar spam)
echo loc() ${#a}
echo loc()  ${a[2]}
echo loc()  ${a[*]}

a=<[ ('foo' , <spam><bar a="attr">text</bar></spam>, 1) ]>
set +indent
xecho ${a[2]}

# Test array notation on positional params
set foo {<[ 1,2,3 ]>} {$a} 

echo loc() ${1}
echo loc() ${2}
xecho ${3}
echo loc() "${3}"


	 