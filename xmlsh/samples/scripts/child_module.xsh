# Simple module with 1 function

echo initializing $0

function child() {
  echo child $*
}


function ev() {
   eval "$@" 
}
