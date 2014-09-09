# Simple module with 1 function

import module c=child_module.xsh
A=$1
B=foo

function run() {
  echo test $*
}

function run2() {
  echo A is $A
  echo B is $B 
}

function run_child() {
   c:child $*
}

echo initializing A=$A B=$B