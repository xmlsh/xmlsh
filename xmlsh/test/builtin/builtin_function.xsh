# Test of function declarations


args ()
{
	echo args are $*
}

setvar ()
{
	_A=$1
}

subpipe() 
{
	echo <[<foo>bar</foo>]> | xcat
}


echo Test of Args
args arg1 arg2 arg3

echo Test of setvar
setvar ABC
echo _A is $_A

echo Test of subpipe

subpipe | xcat 

