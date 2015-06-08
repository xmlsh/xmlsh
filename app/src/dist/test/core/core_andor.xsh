# core_andor
# Tests of && and ||

true && echo true
false && echo false
true || false && echo true
false || false && echo false

if true && false ; then
	echo false
	exit 1
fi

exit 0;

