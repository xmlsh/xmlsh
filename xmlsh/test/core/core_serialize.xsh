# Test global and local serialization options
a=<[ <foo>bar</foo>,1,"hi" ]>

# Default seperator is newline
xecho $a

# use ","
xecho -sequence-sep , $a

xecho -sequence-sep : -sequence-term '
EOF
' $a