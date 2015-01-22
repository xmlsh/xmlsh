# test of jsonpath
import module json
# read from a hear document
set +indent

jsonpath -q $.B <<EOF
{ "A" : 1 ,
  "B" : [ 1 , true , null , "hi" ]
}
EOF




