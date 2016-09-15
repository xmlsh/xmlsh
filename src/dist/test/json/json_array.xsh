# test json:array
import module json=json
set +indent-json
echo json:array()
echo json:array( 1  json:number(2.5)  \
    json:object( x hi y json:array( json:null() json:boolean(true)  "text" ) ) )
