# Simple module with 1 function

import module c=child_module.xsh
A=$1
B=foo
local C=bar

function run() {
  echo test $*
}

function run2() {
  echo A is $A
  echo B is $B 
  echo C is $C 
}

function seta() {
  echo A is $A
  A=$*
  echo A is $A
}
function setc() {
  echo C is $C
  C=$*
  echo C is $C
}

getc() { 
  return $C ;
}
function run_child() {
   c:child $*
}

function show() {

echo functions
declare -f
echo modules 
declare -m
echo 
}


function ev() {
  eval "$@" 
}

function f1() {
  return "$@" ;
}

function f2() {
   return $@ ; 
}

function f3() {
   return f1(f2($*))
}

function getlocal() {
  return c:getlocal($*) 
}

setlocal() {
  c:setlocal  $@ 
}

setlocal2() {
  : setlocal( $@ )
}

setlocal3() {
   :setlocal $@ 
}

getlocal() {
  return c:getlocal()
}

varf() {
  local f=$1
  shift 
  return $f( $* )
}

