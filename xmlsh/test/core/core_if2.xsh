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
