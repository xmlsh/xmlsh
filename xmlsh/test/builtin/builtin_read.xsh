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
