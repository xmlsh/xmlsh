
# function to help test positional params and expansions
function args () 
{
	echo '$#' $#
	for a ; do
	   echo $a
	done
}

function loc() {
  local l=xlocation(-start-line -depth 0) 
  return "[$l]" 
}