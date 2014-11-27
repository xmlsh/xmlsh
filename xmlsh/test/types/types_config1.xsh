# Config modules
. ../common
import c=types.config
import p=types.properties

var1="VARIABLE 1"
readconfig -format ini -file ../../samples/types/config.ini _c

s=c:section($_c params)
echo loc() ${s[param1]}
echo loc() c:sections( $_c )
echo loc() c:value( $_c params.param2 ) 
echo loc() c:value( $_c params param2 ) 
echo loc() TBD dotted properties
#echo loc() c:value( $_c params.param.3 )
echo loc() c:value( $_c params param.3 )
echo loc() c:value( $_c servers.server1 )
echo loc() c:get( $_c servers.server1 )
echo loc() c:get-value( $_c servers.server1 )
echo loc() p:get(c:section( $_c servers.server1 ) , name )
s=c:section($_c section)
#echo $s
echo loc() p:get( $s name )
echo loc() p:get( $s web )

s=c:section($_c servers.server1)
echo loc() p:get( $s name )
echo loc() p:get( $s name.dot )


echo loc() ${_c[section.int]}
echo loc() ${_c[section.name]}
echo loc() ${_c[section.var1]}
echo loc() ${_c[servers.server1.name]}
echo loc() TBD dotted properties
#echo loc() ${_c[servers.server1.name.dot]}
