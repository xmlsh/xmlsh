# simple tests of xmlsh builtin command
# cannot test interactive subshells in a script

echo In parent shell
xmlsh -c 'echo In sub shell ; exit 1'
echo Exit value is $?
exit 0