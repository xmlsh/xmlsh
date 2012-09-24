#
# Test of xs:element
import commands xs=xs
# Simple element
a=xs:element( foo )
xtype $a
xecho $a

# Element with text values
b=xs:element( foo bar )
xecho $b

# Element with namespaces 
c=xs:element( QName(foo http://uri bar) xs:attribute( QName( foo http://uri a1 ) value ) Text )
xecho $c

# Nested element with top level namespace 
xecho xs:element( QName(foo http://uri root) $c $c $c )
   