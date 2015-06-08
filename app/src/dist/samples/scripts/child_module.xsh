# Simple module with 1 function

echo initializing $(xfile -n $0)
local _LOCAL=123

function child() {
  echo child $*
}

function getlocal() {
  return $_LOCAL ;
}

function setlocal() {
  _LOCAL=$*
}

function describe() {
  declare -p _LOCAL

}
 
function ev() {
   eval "$@" 
}
