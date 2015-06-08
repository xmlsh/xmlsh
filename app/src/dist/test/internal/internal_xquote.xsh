# test of xquote

a=<[ <foo>bar</foo> ]>
# a is a element
xtype $a

set +indent
# $b is a string
xquote {$a}>{b}
xtype $b

# Test results within elements
xecho <[ <test>{$a}</test> ]>
xecho <[ <test>{$b}</test> ]>

# Test quote from stdin 
xecho <[ <foo>bar</foo> ]> | xquote >{c}
xtype $c

