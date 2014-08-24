# Test of functions using function call syntax

function f1() {
	return $@
}

xtype -s f1( a 1 <[2,3,4]> )
echo next
xtype -s {f1( {<[2,3,4]>} )}

# Assignment
a=f1()
echo empty $a
a=f1(hello world)
echo $a

# Recursion

function add()
{
  return <[ $_1 + $_2 ]>
}

function mult()
{
  return <[ $_1 * $_2 ]>
}

echo mult( add( <[1,3]> ) mult( <[ 4,5]> ) )

# Test for function containing -

function a-b()
{
	echo function a-b
}

#function -()
#{ 
#	echo function -
#}

a-b



  