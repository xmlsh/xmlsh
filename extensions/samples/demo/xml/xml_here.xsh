# XML Here document

xread data <<EOF
<name>
  <first>Jack</first>
  <last>Straw</last>
</name>
EOF

echo First name is <[$data/name/first/string()]>

