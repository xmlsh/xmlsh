# Config modules
. ../common
import c=types.config

readconfig -format ini _c <<EOF
[default]
date=${date:}
[sample]
date=${date:yyyy-MM-dd}
datetime=${date:yyyy-MM-dd HH:mm:ss}
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
