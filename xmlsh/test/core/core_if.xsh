# core_if.xsh
# Test if elif else expressions


# one-line if
if false ; then exit 1 ; fi


# single statement multi line if
if true ; then
	echo Single statement 
else
	exit 1
fi


# Multi statement with semi's
if true ; then
	echo Multi ;
	echo Statement ;
else
	exit 1
fi


# Test null statement + elif
if true ; then 
	:
elif false ; then
	exit 1 ; 
else
	echo Success 
fi


exit 0

