# Config modules
import c=types.config
import p=types.properties


readconfig -format ini -file ../../samples/types/config.ini _c

s=c:section($_c params)
echo ${s[param1]}
echo c:sections( $_c )
echo c:value( $_c params.param2 )
s=c:section($_c section)
echo p:get( $s name )
echo p:get( $s web )

echo ${_c[section.name]}
echo ${_c[section.var1]}