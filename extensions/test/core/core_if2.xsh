# If tests that previously broke parser
# This tests that the else part is not required if there is an elif

# multi line variant
if true 
then 
echo Success
elif true
then
echo Fail
fi
#

# Single line variant
if true ; then echo Success ; elif true ; then echo Fail ; fi

#
if true <<EOF
foo
EOF
then 
(
echo Success
)
elif true
then
echo Fail
fi

# Test return value of if

# should return the exit status of the list if executed
if true ; then
   true ;
fi
[ $? -eq 0 ] || { echo Fail. Expected 0 exit value ; exit 1; }


if false ; then
   true ;
fi
[ $? -eq 0 ] || { echo Fail. Expected 0 exit value ; exit 1; }

if false ; then
	false 
else 
	true 
fi
[ $? -eq 0 ] || { echo Fail. Expected 0 exit value ; exit 1 ;}



