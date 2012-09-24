# Test of function declarations

# Function read in source mode

unset _A
# set global _A
foo()
{
  echo foo: args are $*
  _A=foo
}

# set local _A
function bar()
{
  echo bar: args are $*
  local _A=bar
}

# Alternate function syntax
function spam
{
   echo in spam
   
}
 

unset _A
foo arg1 arg2 arg3
echo after foo _A is $_A
unset _A
bar arg1 arg2
echo after bar _A is $_A
spam

# Test that functions don't thrash $*

set A B C
bar arg1 arg2 
[  $# -eq 3 ] || { echo FAIL : expected to preserve args ; }
foo arg1 arg2
[  $# -eq 3 ] || { echo FAIL : expected to preserve args ; }


# Test recursive with different local params


unset _A
function f1 
{
	echo enter f1 _A is $_A
	# set parent global _A
	_A=f1	
}

function f2
{
	local _A;
	_A=f2
	echo before f1 $_A
	f1
	echo after f1 $_A
}

echo _A is $_A
f2
echo _A is $_A