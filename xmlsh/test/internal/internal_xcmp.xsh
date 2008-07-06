# test xcmp

rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# Simple binary compare

cat > f1 <<EOF
This is some text
to test with a binary compare
another line to test
EOF

cp f1 f2 
xcmp f1 f1 && echo Success compared Success

echo another line >> f2
xcmp f1 f1 && echo Success compared Failed

rm f1 f2

# Test XML compare
cat > f1 <<EOF
<foo attr1="a1" attr2="a2">
    <bar> text </bar>
</foo>
EOF

cp f1 f2
# identity xml compare
xcmp -x f1 f2 && Echo xml compare Success

# Create equivilent but different file
cat > f3 <<EOF
<foo attr2="a2" attr1="a1" >
    <bar> text </bar>
</foo>
EOF

xcmp -x f1 f3 && Echo xml compare Success
#rm f1 f2 f3
exit 0
   

