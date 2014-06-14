# Test of nesting and newlines in function args

function add()
{
  return <[ xs:decimal($_1) + xs:decimal($_2) ]> ;
}

function concat()
{
  [ $# -lt 2 ] && return $1 
  local _1=$1
  shift
  local _n=concat($*)
  return "$_1$_n"
}

echo concat( a b )

echo add( 1
2)

echo add( 
1
2
)

echo concat(  
  add(1 
  2)
add( 3 
 4) concat( add(
 3 1) concat( foo bar) 
 )
 )


x=concat(  
  add(1 
  2)
add( 3 
 4) concat( add(
 3 1) concat( foo bar) 
 )
 )

echo $x

function l() {
local _x=concat(  
  add(1 2)
add( 3 4) concat( add( 3 1) concat( 
foo bar) )
)
 return $_x
}

echo l()
  
