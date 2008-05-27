# core_subshell.xsh

# Non-outputing test
( false )
( false  ) && ( echo invalid return ; exit 1  )
( false ; true )

# simple output
( echo true )
( echo line1 ; echo line2 ; )

# xml piping
( echo '<foo/>' ) | xcat
echo
echo '<foo2/>' | ( xcat | xcat )
 

# ( env does change
A=1
( A=2  )

if [ $A = 2 ] ; then 
	echo  Subshell  environment wrong A=$A
fi



exit 0

