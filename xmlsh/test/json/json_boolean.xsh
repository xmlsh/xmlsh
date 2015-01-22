# test json:boolean
import module json=json
set +indent
echo json:boolean( 1 )
echo json:boolean( true )
echo json:boolean( 0 )
echo json:boolean( false )
echo json:boolean( <[ fn:true() ]> )
echo json:boolean( <[ fn:false() ]> )
echo json:object( a json:boolean( false ) )

