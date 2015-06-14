# Test of basic map features
#a={}
#xtype $a
#a+={ name : value }
#xtype $a
#a+={ name2 : "value 2" }
#echo ${a[name2]}
#b={ $a , "c" : { "a1" : "value1" , "a2" : value2 } }
#xtype $b
#echo ${b[c]}

import m=types.map
import p=types.properties

a=m:new()
xtype $a
: m:put( $a , name , value )

xtype $a
: m:put( $a , name2 , "Value 2" )
echo ${a[name2]}
b=m:new()
c=m:new()
: m:put( $c , "a1" , "value1" )
: m:put( $c , a2 , value2 )
: m:put( $b , "c" , $c )
echo ${b[c]}