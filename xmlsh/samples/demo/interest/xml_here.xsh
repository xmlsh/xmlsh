# XML Complex Here Documents
xread elem <<EOF
  <elem a="attr a1" b="attr b1">
    <sub>element</sub>
   </elem>
EOF

xread elem2 <<EOF
  <elem a="attr a2" b="attr b2">
    <sub>element2</sub>
   </elem>
EOF

doc=<[
document {
 <test>{ $elem }{ $elem2 }</test>
}
]>

echo Second Element is
echo <[$doc/test/elem[2]]>
