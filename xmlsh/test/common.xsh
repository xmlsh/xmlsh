
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

function message() {
  echo loc() "$@"
}

function error() {
  echo loc() "$@" >(error)
  log "error in " loc() "$@"
}

function die() {
   error "$@" 
   exit 1
}