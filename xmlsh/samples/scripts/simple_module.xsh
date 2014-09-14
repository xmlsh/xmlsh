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

function show() {

echo functions
declare -f
echo modules 
import module
echo 
}

show