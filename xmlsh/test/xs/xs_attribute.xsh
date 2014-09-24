#
# Test of xs:integer
import commands xs=xs
a=xs:attribute(foo bar)
xtype $a
echo $a
echo <[ element { "elem" } { $a } ]>

b=xs:attribute( QName( foo http://uri bar ) spam )
echo <[ element { "elem" } { $b } ]>
