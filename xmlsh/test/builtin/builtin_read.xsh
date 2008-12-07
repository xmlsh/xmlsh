# builtin read command
# reads input into string variables

read a <<EOF
foo
EOF

echo a is $a

read a b c <<EOF
foo bar spam
EOF

echo a b c is $a $b $c

# Test reading of less then enough vars
read a b c <<EOF
foo bar
EOF

echo echo a b c is $a $b $c

[ -z "$c" ] || echo Failed clearing variable

# testing reading nothing
read a <<EOF

EOF

[ -z "$a" ] || echo Failed clearing variable

# Test reading partial data
read a <<EOF
foo bar spam
EOF

[ "$a" = "foo bar spam" ] || echo Failed reading extra data

read a b <<EOF
foo bar spam
EOF

[ "$a" = "foo" ] || echo Failed reading extra data
[ "$b" = "bar spam" ] || echo Failed reading extra data
