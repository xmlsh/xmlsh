# Config modules
import c=types.config
c:readconfig -format ini -file ../../samples/types/config.ini _c

s=c:section($_c params)
echo ${s[param1]}
echo c:sections( $_c )
echo c:value( $_c params.param2 )


