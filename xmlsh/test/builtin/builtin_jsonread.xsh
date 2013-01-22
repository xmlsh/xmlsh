# builtin jsonread command
# reads input into a single json variable

# read from a hear document
jsonread a <<EOF
{ "A" : 1 ,
  "B" : [ 1 , true , null , "hi" ]
}
EOF

echo $a
exit 0
