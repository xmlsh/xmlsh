# Test module
echo Loading "$@"


function test()
{
   return "$*" ;
}

function set()
{ 
   _VAR=$*;
 }
 
 function get()
 { 
  return $_VAR ;
 }
 
