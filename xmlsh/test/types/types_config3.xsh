# Config modules
. ../common
import c=types.config
import p=types.properties 
import m=types.map
VAR=VALUE1
readconfig -format ini _c <<EOF
[default]
date=${date:}
var=${env:VAR}
[sample]
date=${date:yyyy-MM-dd}
datetime=${date:yyyy-MM-dd HH:mm:ss}
[nested]
VALUE1=value1
VALUE2=value2
[derived]
valuea=${nested.${var}}
valueb=${nested.${default.var}}
[deeply.nested]
A_VALUE1=${derived.valueb}
[deeply.derived]
vb=${deeply.nested.A_${var}}

EOF
d1=${_c[sample.date]}
d2=${_c[date]}
d3=c:get( $_c sample datetime )
echo <[ matches( $d1 , "^\d\d\d\d-\d\d-\d\d$" ) ]>
echo <[ matches( $d3 , "^\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d$" ) ]>
echo <[ let $dt := current-dateTime() , 
        $y:= fn:year-from-dateTime($dt) ,
        $m := fn:month-from-dateTime($dt),
        $d := fn:day-from-dateTime($dt)
          return (
                ($y eq xs:int( replace($d1,"^(\d\d\d\d)-(\d\d)-(\d\d)$", "$1") ) )and
                ($m eq xs:int( replace($d1,"^(\d\d\d\d)-(\d\d)-(\d\d)$", "$2") ) )and
                 ( $d eq xs:int( replace($d1,"^(\d\d\d\d)-(\d\d)-(\d\d)$", "$3") ) ))
]>
s1=c:section($_c derived)
echo p:get( $s1 valuea )
echo m:get( $_c derived.valuea )
echo c:get( $_c derived.valuea )
echo c:get( $_c derived valuea )
echo c:get( $_c derived valueb )
echo c:get( $_c deeply.derived vb )