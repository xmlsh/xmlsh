# builtin xread command
# reads input into a single xml variable
# input is expected in XML format (not xquery format, no sequences)

# read from a hear document
xread a <<EOF
<test>data</test>
EOF

echo a is $a
# test type with xquery on set
echo type of a is $(set | xquery '//variable[@name="a"]/@simple-type/string()')


# read from a file 
cd ../../samples/data

xread b < books.xml
# Test type by test -X 
[ -X $b ] && echo document is XML type
exit 0

