# builtin xread command
# reads input into a single xml variable
# input is expected in XML format (not xquery format, no sequences)

xread a <<EOF
<test>data</test>
EOF

echo a is $a
echo type of a is 
set | xquery '//variable[@name="a"]/@type/string()'


