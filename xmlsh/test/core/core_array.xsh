# core_array.xsh
# test array notation

a=()
echo ${#a}
a=(foo)
echo ${#a}
a=(foo bar spam)
echo ${#a}
echo ${a[2]}
echo ${a[*]}

a=<[ ('foo' , <spam><bar a="attr">text</bar></spam>, 1) ]>
set +indent
xecho ${a[2]}

# Test array notation on positional params
set foo <[ 1,2,3 ]> $a 

echo ${1[1]}
echo ${2[2]}
xecho ${3[2]}


	 