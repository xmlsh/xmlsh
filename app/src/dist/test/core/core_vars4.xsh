# core_vars3.xsh
# Test expansion of sequences 
function loc() {
  local l=xlocation(-start-line -depth 0) 
  return "[$l]" 
}
echo TODO: THIS TEST IS NOT RIGHT ..
echo positional
set a b c
echo loc() $*
echo loc() $@
echo loc() ${*}
echo loc() ${@}
echo loc() "x$*"
echo loc()  x$*
echo loc() x${*}y
echo loc() "x$*y"
echo loc() "x${*}y"
echo sequence
A=(A B C)
echo loc() $A
echo loc() ${A}
echo loc() ${A[@]}
echo loc() ${A[*]}
echo loc() "X$A"
echo loc() X$A
echo loc() X${A[*]}X
echo loc() "X${A[*]}X"

# Test "$@" vanishes but "$*" doesnt

f() { 
	echo loc() $#
	for i ; do
  	echo loc() arg $1
   	shift
	done  
}

shift 100 
f $*
f $@
f "$*"
f "$@"
f ${@}
f ${*}
f "${@}"
f "${*}"
f "${@}${*}"
f "${@}${@}"

f 1 2 3 4 5 6 




