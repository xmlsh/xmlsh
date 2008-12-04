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

# Make sure $* are propigated to sub shell
echo
set A B C
( echo $* )

# Make sure $() propogates args
X=$(echo $*)
echo $X




exit 0

