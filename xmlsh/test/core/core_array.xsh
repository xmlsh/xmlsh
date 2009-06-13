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

	 