# try/catch/finally basics

#
# several lexical forms of try with no failures
#
try { true ; } catch X { echo Failed Catch ; } finally { echo Success ; }

try 
{ 
	true ; 
} catch X 
{ 
	echo Failed Catch  
} finally 
{ 
echo Success ; 
}

#
# try with no finally, several forms 
#
try { true ; } 
catch X { echo Failed catch ; }
echo Success No Finally 

try { true ; } catch X { echo Failed catch ; }
echo Success No Finally 

try 
{ true ; } 
catch X 
{ 
	echo Failed catch ; 
}
echo Success No Finally 


# Throw a string

try {
	echo Before throw
	throw "A string"
	echo Fail SNH after thorw
} catch X
{
	echo Catch success: $X
}



