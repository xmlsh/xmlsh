# test json:null
import module json=json
set +indent

echo json:null()
echo json:object( a json:null() b json:array( 1 json:null() hi) )
