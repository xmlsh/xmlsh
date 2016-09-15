# Test the null command ":"
:
: and some args

# Colon in if
if true ; then : ; else echo failed ; fi

# colon in brace
{ : ; } 

# colon in subshell
( : do nothing  ) 
echo success
exit 0
