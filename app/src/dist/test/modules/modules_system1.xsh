# system module
. ../common
import s=system
import k=types.map;

p=s:properties()
k=s:keys()
k2=k:keys($p)

[ s:is-windows() = s:isWindows() -a s:is-linux() = s:isUnix() ]  || fail "aliases should work"
[ ${#k} = ${#k2} ]

