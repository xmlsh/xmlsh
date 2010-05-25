# test of xunquote

a=<[ <foo>bar</foo> ]>
# a is a element
xtype $a

set +indent
# $b is a string
xquote {$a}>{b}
xtype $b

#convert to a document
xunquote {$b}>{c}

xtype {$c}
xecho <[ $c/foo ]>
xecho <[ <test>{$c}</test> ]>

# Test from stdin

import commands p=posix
xquote <[ <test>{$b}</test> ]>
xquote <[ <test>{$b}</test> ]> | p:cat | xunquote | xread e
xecho $e
xecho <[ $e/test ]>
echo <[ $e/test/string() ]> >{f}
xunquote "$f"


