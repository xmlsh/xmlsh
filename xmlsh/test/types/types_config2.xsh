# Config modules
. ../common
import c=types.config
import p=types.properties

var1="VARIABLE 1"
readconfig -format ini -file ../../samples/types/sample.config _c

echo ${_c[server.name]}
echo ${_c[servers.name]}
echo ${_c[test.name]}
