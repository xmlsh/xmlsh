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

# Functions returning values

ret0()
{
  echo in ret 0 ;
  return 0;
  echo SNH
}

ret0 && echo Success of ret0
ret0 || echo Fail of ret0 

