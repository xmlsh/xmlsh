# test of jsonpath

# read from a hear document
jsonpath -q B <<EOF
{ "A" : 1 ,
  "B" : [ 1 , true , null , "hi" ]
}
EOF




