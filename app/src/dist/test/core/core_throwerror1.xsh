# throw on error basicsd


set -e

# simple one command 
try 
{ 
	true ;
	echo Success - executed true
	false ; 
	echo SNH Executed false
} catch X 
{ 
	echo Success caught error
} 

# dont throw when protected by conditions
try 
{ 
	! true
	! false 
	true && false || true
	if false ; then echo SNH ; fi
	X=<[ 0 ]>
	while [ $X -lt 1 ] ; do
		X=<[ $X + 1 ]>
	done
	
	echo Success 

} catch X 
{ 
	echo Failed should not have any errors 
} 

# brace commands with failures
try 
{ 
	{ true ; true ; false ;  } 
	echo SNH Executed false
} catch X 
{ 
	echo Success caught error
} 


# subshell commands with failures
try 
{ 
	( true ; true ; false ; echo Fail  )
	echo SNH Executed false
} catch X 
{ 
	echo Success caught error
} 

try 
{ 
	set +e 
	false 
	echo Success ignored error
} catch X 
{ 
	echo FAIL should not throw error
} 

