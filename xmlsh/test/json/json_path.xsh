import commands json=json
o=json:object( x hi y json:array( json:null() json:boolean(true)  "text" ) ) 
echo json:path( $o $.y )
