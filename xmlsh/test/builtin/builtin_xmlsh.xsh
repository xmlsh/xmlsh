# simple tests of xmlsh builtin command
# cannot test interactive subshells in a script

# if called with 1 arg then calling self
if [ $# -gt 0 ] ; then
	echo Called into self
	echo \$0 is $(xfile -b $0)
	echo \$* is $*
	exit 0;
fi 

xmlsh -norc $0 Arg1 Arg2


echo In parent shell
xmlsh -norc -c 'echo In sub shell ; exit 1'
echo Exit value is $?
exit 0