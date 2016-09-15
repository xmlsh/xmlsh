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
echo '<foo2/>' | ( xcat | xcat )
# last cmd in pipeline runs in current shell
unset A
echo '<pipe/>' | xcat | xread A
echo $A
unset A

# Test exit status of last cmd
false | true && echo Success of last command
true | false && Failure of last command

 

# ( env does change
A=1
( A=2  )

if [ $A = 2 ] ; then 
	echo  Subshell  environment wrong A=$A
fi

# Make sure $* are propigated to sub shell
set A B C
( echo $* )

# Make sure $() propogates args
X=$(echo $*)
echo $X




exit 0

