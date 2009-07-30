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